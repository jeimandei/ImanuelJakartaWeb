package com.jeimandei.imanuelbytes.cms.repository;

import com.jeimandei.imanuelbytes.cms.entity.ServiceTime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceTimeRepository extends JpaRepository<ServiceTime, Long> {

    List<ServiceTime> findByActiveTrueOrderBySortOrderAscStartTimeAsc();

    List<ServiceTime> findAllByOrderBySortOrderAscDayOfWeekAscStartTimeAsc();
}
