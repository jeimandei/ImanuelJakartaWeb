package com.jeimandei.imanuelbytes.interaction.service;

import com.jeimandei.imanuelbytes.interaction.dto.CreatePrayerRequestRequest;
import com.jeimandei.imanuelbytes.interaction.dto.PrayerRequestDto;
import com.jeimandei.imanuelbytes.interaction.dto.UpdatePrayerRequestStatusRequest;
import com.jeimandei.imanuelbytes.interaction.entity.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface PrayerRequestService {

    PrayerRequestDto submitPrayerRequest(CreatePrayerRequestRequest request);

    Page<PrayerRequestDto> getAllPrayerRequests(Pageable pageable);

    PrayerRequestDto getPrayerRequestById(Long id);

    PrayerRequestDto updateStatus(Long id, UpdatePrayerRequestStatusRequest request);

    long getCountByStatus(RequestStatus status);

    Map<String, Long> getStats();
}
