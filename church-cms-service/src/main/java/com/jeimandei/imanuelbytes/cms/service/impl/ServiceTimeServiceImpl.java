package com.jeimandei.imanuelbytes.cms.service.impl;

import com.jeimandei.imanuelbytes.cms.audit.AuditClientService;
import com.jeimandei.imanuelbytes.cms.dto.CreateServiceTimeRequest;
import com.jeimandei.imanuelbytes.cms.dto.ServiceTimeDto;
import com.jeimandei.imanuelbytes.cms.entity.ServiceTime;
import com.jeimandei.imanuelbytes.cms.mapper.ServiceTimeMapper;
import com.jeimandei.imanuelbytes.cms.repository.ServiceTimeRepository;
import com.jeimandei.imanuelbytes.cms.service.ServiceTimeService;
import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ServiceTimeServiceImpl implements ServiceTimeService {

    private static final Logger log = LoggerFactory.getLogger(ServiceTimeServiceImpl.class);

    private final ServiceTimeRepository serviceTimeRepository;
    private final ServiceTimeMapper serviceTimeMapper;
    private final AuditClientService auditClient;

    public ServiceTimeServiceImpl(ServiceTimeRepository serviceTimeRepository,
                                   ServiceTimeMapper serviceTimeMapper,
                                   AuditClientService auditClient) {
        this.serviceTimeRepository = serviceTimeRepository;
        this.serviceTimeMapper = serviceTimeMapper;
        this.auditClient = auditClient;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceTimeDto> getAllServiceTimes() {
        return serviceTimeRepository.findAllByOrderBySortOrderAscDayOfWeekAscStartTimeAsc()
                .stream().map(serviceTimeMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceTimeDto> getActiveServiceTimes() {
        return serviceTimeRepository.findByActiveTrueOrderBySortOrderAscStartTimeAsc()
                .stream().map(serviceTimeMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceTimeDto getServiceTimeById(Long id) {
        return serviceTimeRepository.findById(id)
                .map(serviceTimeMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceTime", "id", id));
    }

    @Override
    public ServiceTimeDto createServiceTime(CreateServiceTimeRequest request) {
        log.debug("Creating service time '{}'", request.getName());
        ServiceTime saved = serviceTimeRepository.save(serviceTimeMapper.toEntity(request));
        log.info("Created service time id={}, name='{}'", saved.getId(), saved.getName());
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "CREATE_SERVICE_TIME", "ServiceTime",
                    String.valueOf(saved.getId()), saved.getName());
        } catch (Exception e) {
            log.warn("Audit log failed for CREATE_SERVICE_TIME {}: {}", saved.getId(), e.getMessage());
        }
        return serviceTimeMapper.toDto(saved);
    }

    @Override
    public ServiceTimeDto updateServiceTime(Long id, CreateServiceTimeRequest request) {
        log.debug("Updating service time id={}", id);
        ServiceTime serviceTime = serviceTimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceTime", "id", id));
        serviceTimeMapper.updateEntity(serviceTime, request);
        ServiceTimeDto updated = serviceTimeMapper.toDto(serviceTimeRepository.save(serviceTime));
        log.info("Updated service time id={}", updated.getId());
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "UPDATE_SERVICE_TIME", "ServiceTime",
                    String.valueOf(updated.getId()), updated.getName());
        } catch (Exception e) {
            log.warn("Audit log failed for UPDATE_SERVICE_TIME {}: {}", id, e.getMessage());
        }
        return updated;
    }

    @Override
    public void deleteServiceTime(Long id) {
        log.debug("Deleting service time id={}", id);
        if (!serviceTimeRepository.existsById(id)) {
            throw new ResourceNotFoundException("ServiceTime", "id", id);
        }
        serviceTimeRepository.deleteById(id);
        log.info("Deleted service time id={}", id);
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "DELETE_SERVICE_TIME", "ServiceTime",
                    String.valueOf(id), String.valueOf(id));
        } catch (Exception e) {
            log.warn("Audit log failed for DELETE_SERVICE_TIME {}: {}", id, e.getMessage());
        }
    }

    private String getCurrentActor() {
        try {
            org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName();
            }
        } catch (Exception ignored) {}
        return "system";
    }

    private String getCurrentActorRole() {
        try {
            org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                return auth.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority())
                    .orElse("UNKNOWN");
            }
        } catch (Exception ignored) {}
        return "UNKNOWN";
    }
}
