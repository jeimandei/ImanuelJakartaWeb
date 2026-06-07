package com.jeimandei.imanuelbytes.interaction.service;

import com.jeimandei.imanuelbytes.interaction.dto.CreateVolunteerApplicationRequest;
import com.jeimandei.imanuelbytes.interaction.dto.VolunteerApplicationDto;
import com.jeimandei.imanuelbytes.interaction.entity.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VolunteerService {

    VolunteerApplicationDto submitApplication(CreateVolunteerApplicationRequest request);

    Page<VolunteerApplicationDto> getAllApplications(Pageable pageable);

    Page<VolunteerApplicationDto> getByMinistry(String ministry, Pageable pageable);

    VolunteerApplicationDto updateStatus(Long id, RequestStatus status);
}
