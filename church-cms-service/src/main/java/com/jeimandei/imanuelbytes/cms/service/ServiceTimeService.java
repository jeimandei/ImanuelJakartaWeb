package com.jeimandei.imanuelbytes.cms.service;

import com.jeimandei.imanuelbytes.cms.dto.CreateServiceTimeRequest;
import com.jeimandei.imanuelbytes.cms.dto.ServiceTimeDto;

import java.util.List;

public interface ServiceTimeService {

    List<ServiceTimeDto> getAllServiceTimes();

    List<ServiceTimeDto> getActiveServiceTimes();

    ServiceTimeDto getServiceTimeById(Long id);

    ServiceTimeDto createServiceTime(CreateServiceTimeRequest request);

    ServiceTimeDto updateServiceTime(Long id, CreateServiceTimeRequest request);

    void deleteServiceTime(Long id);
}
