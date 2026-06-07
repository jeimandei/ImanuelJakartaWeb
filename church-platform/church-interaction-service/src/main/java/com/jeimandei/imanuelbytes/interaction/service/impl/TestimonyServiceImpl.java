package com.jeimandei.imanuelbytes.interaction.service.impl;

import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import com.jeimandei.imanuelbytes.interaction.dto.SubmitTestimonyRequest;
import com.jeimandei.imanuelbytes.interaction.dto.TestimonyDto;
import com.jeimandei.imanuelbytes.interaction.entity.RequestStatus;
import com.jeimandei.imanuelbytes.interaction.entity.TestimonySubmission;
import com.jeimandei.imanuelbytes.interaction.repository.TestimonyRepository;
import com.jeimandei.imanuelbytes.interaction.service.TestimonyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TestimonyServiceImpl implements TestimonyService {

    private final TestimonyRepository testimonyRepository;

    public TestimonyServiceImpl(TestimonyRepository testimonyRepository) {
        this.testimonyRepository = testimonyRepository;
    }

    @Override
    public TestimonyDto submitTestimony(SubmitTestimonyRequest request) {
        TestimonySubmission submission = new TestimonySubmission();
        submission.setName(request.getName());
        submission.setEmail(request.getEmail());
        submission.setTestimony(request.getTestimony());
        submission.setStatus(RequestStatus.NEW);
        submission.setApproved(false);
        TestimonySubmission saved = testimonyRepository.save(submission);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TestimonyDto> getApprovedTestimonies(Pageable pageable) {
        return testimonyRepository.findByApprovedTrueOrderByCreatedAtDesc(pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TestimonyDto> getAllTestimonies(Pageable pageable) {
        return testimonyRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toDto);
    }

    @Override
    public TestimonyDto approveTestimony(Long id) {
        TestimonySubmission submission = testimonyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestimonySubmission", id));
        submission.setApproved(true);
        submission.setStatus(RequestStatus.REVIEWED);
        TestimonySubmission saved = testimonyRepository.save(submission);
        return toDto(saved);
    }

    @Override
    public TestimonyDto updateStatus(Long id, RequestStatus status) {
        TestimonySubmission submission = testimonyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestimonySubmission", id));
        submission.setStatus(status);
        TestimonySubmission saved = testimonyRepository.save(submission);
        return toDto(saved);
    }

    // -------------------------------------------------------------------------
    // Mapping
    // -------------------------------------------------------------------------

    private TestimonyDto toDto(TestimonySubmission entity) {
        TestimonyDto dto = new TestimonyDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        dto.setTestimony(entity.getTestimony());
        dto.setStatus(entity.getStatus());
        dto.setApproved(entity.isApproved());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
