package com.jeimandei.imanuelbytes.interaction.service.impl;

import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import com.jeimandei.imanuelbytes.interaction.dto.CreateVolunteerApplicationRequest;
import com.jeimandei.imanuelbytes.interaction.dto.VolunteerApplicationDto;
import com.jeimandei.imanuelbytes.interaction.entity.RequestStatus;
import com.jeimandei.imanuelbytes.interaction.entity.VolunteerApplication;
import com.jeimandei.imanuelbytes.interaction.repository.VolunteerApplicationRepository;
import com.jeimandei.imanuelbytes.interaction.service.VolunteerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VolunteerServiceImpl implements VolunteerService {

    private final VolunteerApplicationRepository volunteerApplicationRepository;

    public VolunteerServiceImpl(VolunteerApplicationRepository volunteerApplicationRepository) {
        this.volunteerApplicationRepository = volunteerApplicationRepository;
    }

    @Override
    public VolunteerApplicationDto submitApplication(CreateVolunteerApplicationRequest request) {
        VolunteerApplication application = new VolunteerApplication();
        application.setFullName(request.getFullName());
        application.setEmail(request.getEmail());
        application.setPhone(request.getPhone());
        application.setMinistry(request.getMinistry());
        application.setMessage(request.getMessage());
        application.setStatus(RequestStatus.NEW);
        VolunteerApplication saved = volunteerApplicationRepository.save(application);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VolunteerApplicationDto> getAllApplications(Pageable pageable) {
        return volunteerApplicationRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VolunteerApplicationDto> getByMinistry(String ministry, Pageable pageable) {
        return volunteerApplicationRepository.findByMinistryOrderByCreatedAtDesc(ministry, pageable)
                .map(this::toDto);
    }

    @Override
    public VolunteerApplicationDto updateStatus(Long id, RequestStatus status) {
        VolunteerApplication application = volunteerApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VolunteerApplication", id));
        application.setStatus(status);
        VolunteerApplication saved = volunteerApplicationRepository.save(application);
        return toDto(saved);
    }

    // -------------------------------------------------------------------------
    // Mapping
    // -------------------------------------------------------------------------

    private VolunteerApplicationDto toDto(VolunteerApplication entity) {
        VolunteerApplicationDto dto = new VolunteerApplicationDto();
        dto.setId(entity.getId());
        dto.setFullName(entity.getFullName());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        dto.setMinistry(entity.getMinistry());
        dto.setMessage(entity.getMessage());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
