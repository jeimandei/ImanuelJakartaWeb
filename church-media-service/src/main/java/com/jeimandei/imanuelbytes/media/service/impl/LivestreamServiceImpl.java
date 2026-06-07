package com.jeimandei.imanuelbytes.media.service.impl;

import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import com.jeimandei.imanuelbytes.common.exception.ValidationException;
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

    public LivestreamServiceImpl(LivestreamRepository livestreamRepository,
                                 LivestreamMapper livestreamMapper) {
        this.livestreamRepository = livestreamRepository;
        this.livestreamMapper = livestreamMapper;
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
        return livestreamMapper.toDto(saved);
    }

    @Override
    public void deleteLivestream(Long id) {
        log.debug("Deleting livestream id={}", id);
        if (!livestreamRepository.existsById(id)) {
            log.warn("Livestream not found for deletion: id={}", id);
            throw new ResourceNotFoundException("Livestream", id);
        }
        livestreamRepository.deleteById(id);
        log.info("Livestream deleted successfully: id={}", id);
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

    private void validateEmbedUrl(String url) {
        if (url != null && !url.matches("https://www\\.youtube\\.com/embed/.*")) {
            throw new ValidationException("youtubeEmbedUrl", "Must be a YouTube embed URL", true);
        }
    }
}
