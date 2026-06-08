package com.jeimandei.imanuelbytes.media.service.impl;

import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import com.jeimandei.imanuelbytes.common.exception.ValidationException;
import com.jeimandei.imanuelbytes.media.audit.AuditClientService;
import com.jeimandei.imanuelbytes.media.dto.CreateLivestreamRequest;
import com.jeimandei.imanuelbytes.media.dto.LivestreamDto;
import com.jeimandei.imanuelbytes.media.dto.UpdateLivestreamRequest;
import com.jeimandei.imanuelbytes.media.entity.Livestream;
import com.jeimandei.imanuelbytes.media.mapper.LivestreamMapper;
import com.jeimandei.imanuelbytes.media.repository.LivestreamRepository;
import com.jeimandei.imanuelbytes.media.service.LivestreamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LivestreamServiceImpl implements LivestreamService {

    private static final Logger log = LoggerFactory.getLogger(LivestreamServiceImpl.class);

    private static final String EMBED_URL_PATTERN = "https://www.youtube.com/embed/";

    private final LivestreamRepository livestreamRepository;
    private final LivestreamMapper livestreamMapper;
    private final AuditClientService auditClient;

    public LivestreamServiceImpl(LivestreamRepository livestreamRepository,
                                 LivestreamMapper livestreamMapper,
                                 AuditClientService auditClient) {
        this.livestreamRepository = livestreamRepository;
        this.livestreamMapper = livestreamMapper;
        this.auditClient = auditClient;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LivestreamDto> getActiveLivestream() {
        log.debug("Fetching active livestream");
        Optional<LivestreamDto> active = livestreamRepository.findFirstByActiveTrueOrderByScheduledStartDesc()
                .map(livestreamMapper::toDto);
        if (active.isEmpty()) {
            log.debug("No active livestream found");
        }
        return active;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LivestreamDto> getAllLivestreams(Pageable pageable) {
        return livestreamRepository.findAllByOrderByScheduledStartDesc(pageable)
                .map(livestreamMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public LivestreamDto getLivestreamById(Long id) {
        Livestream livestream = livestreamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livestream", id));
        return livestreamMapper.toDto(livestream);
    }

    @Override
    public LivestreamDto createLivestream(CreateLivestreamRequest request) {
        log.debug("Creating livestream with title='{}'", request.getTitle());
        validateEmbedUrl(request.getYoutubeEmbedUrl());
        Livestream livestream = livestreamMapper.toEntity(request);
        Livestream saved = livestreamRepository.save(livestream);
        log.info("Livestream created successfully: id={}, title='{}'", saved.getId(), saved.getTitle());
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "CREATE_LIVESTREAM", "Livestream",
                    String.valueOf(saved.getId()), saved.getTitle());
        } catch (Exception e) {
            log.warn("Audit log failed for CREATE_LIVESTREAM {}: {}", saved.getId(), e.getMessage());
        }
        return livestreamMapper.toDto(saved);
    }

    @Override
    public LivestreamDto updateLivestream(Long id, UpdateLivestreamRequest request) {
        log.debug("Updating livestream id={}", id);
        Livestream livestream = livestreamRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Livestream not found for update: id={}", id);
                    return new ResourceNotFoundException("Livestream", id);
                });
        if (request.getYoutubeEmbedUrl() != null) {
            validateEmbedUrl(request.getYoutubeEmbedUrl());
        }
        livestreamMapper.updateEntity(livestream, request);
        Livestream saved = livestreamRepository.save(livestream);
        log.info("Livestream updated successfully: id={}", saved.getId());
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "UPDATE_LIVESTREAM", "Livestream",
                    String.valueOf(saved.getId()), saved.getTitle());
        } catch (Exception e) {
            log.warn("Audit log failed for UPDATE_LIVESTREAM {}: {}", id, e.getMessage());
        }
        return livestreamMapper.toDto(saved);
    }

    @Override
    public void deleteLivestream(Long id) {
        log.debug("Deleting livestream id={}", id);
        Livestream livestream = livestreamRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Livestream not found for deletion: id={}", id);
                    return new ResourceNotFoundException("Livestream", id);
                });
        livestreamRepository.deleteById(id);
        log.info("Livestream deleted successfully: id={}", id);
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "DELETE_LIVESTREAM", "Livestream",
                    String.valueOf(id), livestream.getTitle());
        } catch (Exception e) {
            log.warn("Audit log failed for DELETE_LIVESTREAM {}: {}", id, e.getMessage());
        }
    }

    @Override
    public LivestreamDto activateLivestream(Long id) {
        log.debug("Activating livestream id={}", id);
        Livestream target = livestreamRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Livestream not found for activation: id={}", id);
                    return new ResourceNotFoundException("Livestream", id);
                });
        List<Livestream> activeOthers = livestreamRepository.findByActiveTrueOrderByScheduledStartDesc();
        for (Livestream active : activeOthers) {
            if (!active.getId().equals(id)) {
                log.debug("Deactivating previously active livestream id={}", active.getId());
                active.setActive(false);
                livestreamRepository.save(active);
            }
        }
        target.setActive(true);
        Livestream saved = livestreamRepository.save(target);
        log.info("Livestream activated: id={}", saved.getId());
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "ACTIVATE_LIVESTREAM", "Livestream",
                    String.valueOf(saved.getId()), saved.getTitle());
        } catch (Exception e) {
            log.warn("Audit log failed for ACTIVATE_LIVESTREAM {}: {}", id, e.getMessage());
        }
        return livestreamMapper.toDto(saved);
    }

    @Override
    public LivestreamDto deactivateLivestream(Long id) {
        log.debug("Deactivating livestream id={}", id);
        Livestream livestream = livestreamRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Livestream not found for deactivation: id={}", id);
                    return new ResourceNotFoundException("Livestream", id);
                });
        livestream.setActive(false);
        Livestream saved = livestreamRepository.save(livestream);
        log.info("Livestream deactivated: id={}", saved.getId());
        return livestreamMapper.toDto(saved);
    }

    private String getCurrentActor() {
        try {
            org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName();
            }
        } catch (Exception ignored) {}
        return "system";
    }

    private String getCurrentActorRole() {
        try {
            org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                return auth.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority())
                    .orElse("UNKNOWN");
            }
        } catch (Exception ignored) {}
        return "UNKNOWN";
    }

    private void validateEmbedUrl(String url) {
        if (url != null && !url.matches("https://www\\.youtube\\.com/embed/.*")) {
            throw new ValidationException("youtubeEmbedUrl", "Must be a YouTube embed URL", true);
        }
    }
}
