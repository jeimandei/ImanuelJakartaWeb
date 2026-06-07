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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LivestreamServiceImpl implements LivestreamService {

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
        return livestreamRepository.findFirstByActiveTrueOrderByScheduledStartDesc()
                .map(livestreamMapper::toDto);
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
        validateEmbedUrl(request.getYoutubeEmbedUrl());
        Livestream livestream = livestreamMapper.toEntity(request);
        Livestream saved = livestreamRepository.save(livestream);
        return livestreamMapper.toDto(saved);
    }

    @Override
    public LivestreamDto updateLivestream(Long id, UpdateLivestreamRequest request) {
        Livestream livestream = livestreamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livestream", id));
        if (request.getYoutubeEmbedUrl() != null) {
            validateEmbedUrl(request.getYoutubeEmbedUrl());
        }
        livestreamMapper.updateEntity(livestream, request);
        Livestream saved = livestreamRepository.save(livestream);
        return livestreamMapper.toDto(saved);
    }

    @Override
    public void deleteLivestream(Long id) {
        if (!livestreamRepository.existsById(id)) {
            throw new ResourceNotFoundException("Livestream", id);
        }
        livestreamRepository.deleteById(id);
    }

    @Override
    public LivestreamDto activateLivestream(Long id) {
        Livestream target = livestreamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livestream", id));
        List<Livestream> activeOthers = livestreamRepository.findByActiveTrueOrderByScheduledStartDesc();
        for (Livestream active : activeOthers) {
            if (!active.getId().equals(id)) {
                active.setActive(false);
                livestreamRepository.save(active);
            }
        }
        target.setActive(true);
        Livestream saved = livestreamRepository.save(target);
        return livestreamMapper.toDto(saved);
    }

    @Override
    public LivestreamDto deactivateLivestream(Long id) {
        Livestream livestream = livestreamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livestream", id));
        livestream.setActive(false);
        Livestream saved = livestreamRepository.save(livestream);
        return livestreamMapper.toDto(saved);
    }

    private void validateEmbedUrl(String url) {
        if (url != null && !url.matches("https://www\\.youtube\\.com/embed/.*")) {
            throw new ValidationException("youtubeEmbedUrl", "Must be a YouTube embed URL", true);
        }
    }
}
