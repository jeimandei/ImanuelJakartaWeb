package com.jeimandei.imanuelbytes.cms.mapper;

import com.jeimandei.imanuelbytes.cms.dto.CreateServiceTimeRequest;
import com.jeimandei.imanuelbytes.cms.dto.ServiceTimeDto;
import com.jeimandei.imanuelbytes.cms.entity.ServiceTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ServiceTimeMapper {

    ServiceTimeDto toDto(ServiceTime serviceTime);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ServiceTime toEntity(CreateServiceTimeRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget ServiceTime serviceTime, CreateServiceTimeRequest request);
}
