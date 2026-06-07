package com.jeimandei.imanuelbytes.media.service.impl;

import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import com.jeimandei.imanuelbytes.media.dto.CreateSermonRequest;
import com.jeimandei.imanuelbytes.media.dto.SermonDto;
import com.jeimandei.imanuelbytes.media.dto.UpdateSermonRequest;
import com.jeimandei.imanuelbytes.media.entity.Sermon;
import com.jeimandei.imanuelbytes.media.mapper.SermonMapper;
import com.jeimandei.imanuelbytes.media.repository.SermonRepository;
import com.jeimandei.imanuelbytes.media.service.SermonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SermonServiceImpl implements SermonService {

    private static final Logger log = LoggerFactory.getLogger(SermonServiceImpl.class);

    private final SermonRepository sermonRepository;
    private final SermonMapper sermonMapper;

    public SermonServiceImpl(SermonRepository sermonRepository, SermonMapper sermonMapper) {
        this.sermonRepository = sermonRepository;
        this.sermonMapper = sermonMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SermonDto> getAllSermons(Pageable pageable) {
        return sermonRepository.findAllByOrderBySermonDateDesc(pageable)
                .map(sermonMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SermonDto> searchSermons(String q, Pageable pageable) {
        return sermonRepository.searchSermons(q, pageable)
                .map(sermonMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SermonDto> getSermonsBySpeaker(String speaker, Pageable pageable) {
        return sermonRepository.findBySpeakerContainingIgnoreCase(speaker, pageable)
                .map(sermonMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SermonDto> getSermonsBySeries(String series, Pageable pageable) {
        return sermonRepository.findBySeriesName(series, pageable)
                .map(sermonMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SermonDto> getLatestSermons(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return sermonRepository.findAllByOrderBySermonDateDesc(pageable)
                .getContent()
                .stream()
                .map(sermonMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SermonDto getSermonById(Long id) {
        log.debug("Fetching sermon by id={}", id);
        Sermon sermon = sermonRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Sermon not found: id={}", id);
                    return new ResourceNotFoundException("Sermon", id);
                });
        return sermonMapper.toDto(sermon);
    }

    @Override
    public SermonDto createSermon(CreateSermonRequest request) {
        log.debug("Creating sermon with title='{}', speaker='{}'", request.getTitle(), request.getSpeaker());
        Sermon sermon = sermonMapper.toEntity(request);
        String normalizedUrl = normalizeYoutubeUrl(request.getYoutubeUrl());
        sermon.setYoutubeUrl(normalizedUrl);
        Sermon saved = sermonRepository.save(sermon);
        log.info("Sermon created successfully: id={}, title='{}'", saved.getId(), saved.getTitle());
        return sermonMapper.toDto(saved);
    }

    @Override
    public SermonDto updateSermon(Long id, UpdateSermonRequest request) {
        log.debug("Updating sermon id={}", id);
        Sermon sermon = sermonRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Sermon not found for update: id={}", id);
                    return new ResourceNotFoundException("Sermon", id);
                });
        sermonMapper.updateEntity(sermon, request);
        if (request.getYoutubeUrl() != null) {
            sermon.setYoutubeUrl(normalizeYoutubeUrl(request.getYoutubeUrl()));
        }
        Sermon saved = sermonRepository.save(sermon);
        log.info("Sermon updated successfully: id={}", saved.getId());
        return sermonMapper.toDto(saved);
    }

    @Override
    public void deleteSermon(Long id) {
        log.debug("Deleting sermon id={}", id);
        if (!sermonRepository.existsById(id)) {
            log.warn("Sermon not found for deletion: id={}", id);
            throw new ResourceNotFoundException("Sermon", id);
        }
        sermonRepository.deleteById(id);
        log.info("Sermon deleted successfully: id={}", id);
    }

    private String normalizeYoutubeUrl(String url) {
        if (url == null) {
            return null;
        }
        if (url.contains("watch?v=")) {
            String videoId = url.substring(url.indexOf("watch?v=") + 8);
            int ampersandIndex = videoId.indexOf('&');
            if (ampersandIndex != -1) {
                videoId = videoId.substring(0, ampersandIndex);
            }
            String embedUrl = "https://www.youtube.com/embed/" + videoId;
            log.debug("Normalized YouTube URL: '{}' -> '{}'", url, embedUrl);
            return embedUrl;
        }
        return url;
    }
}
