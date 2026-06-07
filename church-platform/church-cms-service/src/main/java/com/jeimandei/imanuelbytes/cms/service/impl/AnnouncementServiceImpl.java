package com.jeimandei.imanuelbytes.cms.service.impl;

import com.jeimandei.imanuelbytes.cms.dto.AnnouncementDto;
import com.jeimandei.imanuelbytes.cms.dto.CreateAnnouncementRequest;
import com.jeimandei.imanuelbytes.cms.entity.Announcement;
import com.jeimandei.imanuelbytes.cms.mapper.AnnouncementMapper;
import com.jeimandei.imanuelbytes.cms.repository.AnnouncementRepository;
import com.jeimandei.imanuelbytes.cms.service.AnnouncementService;
import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementMapper announcementMapper;

    public AnnouncementServiceImpl(AnnouncementRepository announcementRepository,
                                    AnnouncementMapper announcementMapper) {
        this.announcementRepository = announcementRepository;
        this.announcementMapper = announcementMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AnnouncementDto> getAllAnnouncements(Pageable pageable) {
        return announcementRepository.findAll(pageable).map(announcementMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnnouncementDto> getActiveAnnouncements() {
        return announcementRepository.findActiveAnnouncements(LocalDate.now())
                .stream().map(announcementMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AnnouncementDto getAnnouncementById(Long id) {
        return announcementRepository.findById(id)
                .map(announcementMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement", "id", id));
    }

    @Override
    public AnnouncementDto createAnnouncement(CreateAnnouncementRequest request) {
        Announcement saved = announcementRepository.save(announcementMapper.toEntity(request));
        return announcementMapper.toDto(saved);
    }

    @Override
    public AnnouncementDto updateAnnouncement(Long id, CreateAnnouncementRequest request) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement", "id", id));
        announcementMapper.updateEntity(announcement, request);
        return announcementMapper.toDto(announcementRepository.save(announcement));
    }

    @Override
    public void deleteAnnouncement(Long id) {
        if (!announcementRepository.existsById(id)) {
            throw new ResourceNotFoundException("Announcement", "id", id);
        }
        announcementRepository.deleteById(id);
    }
}
