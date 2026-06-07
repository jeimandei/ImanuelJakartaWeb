package com.jeimandei.imanuelbytes.media.service;

import com.jeimandei.imanuelbytes.media.dto.CreateSermonRequest;
import com.jeimandei.imanuelbytes.media.dto.SermonDto;
import com.jeimandei.imanuelbytes.media.dto.UpdateSermonRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SermonService {

    Page<SermonDto> getAllSermons(Pageable pageable);

    Page<SermonDto> searchSermons(String q, Pageable pageable);

    Page<SermonDto> getSermonsBySpeaker(String speaker, Pageable pageable);

    Page<SermonDto> getSermonsBySeries(String series, Pageable pageable);

    List<SermonDto> getLatestSermons(int limit);

    SermonDto getSermonById(Long id);

    SermonDto createSermon(CreateSermonRequest request);

    SermonDto updateSermon(Long id, UpdateSermonRequest request);

    void deleteSermon(Long id);
}
