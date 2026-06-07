package com.jeimandei.imanuelbytes.cms.service;

import com.jeimandei.imanuelbytes.cms.dto.AnnouncementDto;
import com.jeimandei.imanuelbytes.cms.dto.CreateAnnouncementRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AnnouncementService {
    Page<AnnouncementDto> getAllAnnouncements(Pageable pageable);
    List<AnnouncementDto> getActiveAnnouncements();
    AnnouncementDto getAnnouncementById(Long id);
    AnnouncementDto createAnnouncement(CreateAnnouncementRequest request);
    AnnouncementDto updateAnnouncement(Long id, CreateAnnouncementRequest request);
    void deleteAnnouncement(Long id);
}
