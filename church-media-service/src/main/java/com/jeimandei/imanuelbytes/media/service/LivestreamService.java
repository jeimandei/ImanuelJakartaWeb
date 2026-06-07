package com.jeimandei.imanuelbytes.media.service;

import com.jeimandei.imanuelbytes.media.dto.CreateLivestreamRequest;
import com.jeimandei.imanuelbytes.media.dto.LivestreamDto;
import com.jeimandei.imanuelbytes.media.dto.UpdateLivestreamRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface LivestreamService {

    Optional<LivestreamDto> getActiveLivestream();

    Page<LivestreamDto> getAllLivestreams(Pageable pageable);

    LivestreamDto getLivestreamById(Long id);

    LivestreamDto createLivestream(CreateLivestreamRequest request);

    LivestreamDto updateLivestream(Long id, UpdateLivestreamRequest request);

    void deleteLivestream(Long id);

    LivestreamDto activateLivestream(Long id);

    LivestreamDto deactivateLivestream(Long id);
}
