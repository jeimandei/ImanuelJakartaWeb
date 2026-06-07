package com.jeimandei.imanuelbytes.interaction.service;

import com.jeimandei.imanuelbytes.interaction.dto.SubmitTestimonyRequest;
import com.jeimandei.imanuelbytes.interaction.dto.TestimonyDto;
import com.jeimandei.imanuelbytes.interaction.entity.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TestimonyService {

    TestimonyDto submitTestimony(SubmitTestimonyRequest request);

    Page<TestimonyDto> getApprovedTestimonies(Pageable pageable);

    Page<TestimonyDto> getAllTestimonies(Pageable pageable);

    TestimonyDto approveTestimony(Long id);

    TestimonyDto updateStatus(Long id, RequestStatus status);
}
