package com.jeimandei.imanuelbytes.cms.service.impl;

import com.jeimandei.imanuelbytes.cms.dto.CmsContentBlockDto;
import com.jeimandei.imanuelbytes.cms.dto.CreateContentBlockRequest;
import com.jeimandei.imanuelbytes.cms.entity.CmsContentBlock;
import com.jeimandei.imanuelbytes.cms.repository.CmsContentBlockRepository;
import com.jeimandei.imanuelbytes.cms.service.ContentBlockService;
import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ContentBlockServiceImpl implements ContentBlockService {

    private static final Logger log = LoggerFactory.getLogger(ContentBlockServiceImpl.class);

    private final CmsContentBlockRepository blockRepository;

    public ContentBlockServiceImpl(CmsContentBlockRepository blockRepository) {
        this.blockRepository = blockRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CmsContentBlockDto> getBlocksByPage(Long pageId) {
        return blockRepository.findByPageIdOrderByDisplayOrder(pageId)
                .stream().map(this::toDto).toList();
    }

    @Override
    public CmsContentBlockDto createBlock(CreateContentBlockRequest request) {
        log.debug("Creating content block for pageId={}, blockType='{}'", request.getPageId(), request.getBlockType());
        CmsContentBlock block = new CmsContentBlock();
        applyRequest(block, request);
        block.setActive(true);
        CmsContentBlockDto created = toDto(blockRepository.save(block));
        log.info("Created content block id={} for pageId={}", created.getId(), created.getPageId());
        return created;
    }

    @Override
    public CmsContentBlockDto updateBlock(Long id, CreateContentBlockRequest request) {
        log.debug("Updating content block id={}", id);
        CmsContentBlock block = blockRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Content block not found for id={}", id);
                    return new ResourceNotFoundException("ContentBlock", "id", id);
                });
        applyRequest(block, request);
        CmsContentBlockDto updated = toDto(blockRepository.save(block));
        log.info("Updated content block id={}", updated.getId());
        return updated;
    }

    @Override
    public void deleteBlock(Long id) {
        log.debug("Deleting content block id={}", id);
        if (!blockRepository.existsById(id)) {
            log.warn("Content block not found for id={}", id);
            throw new ResourceNotFoundException("ContentBlock", "id", id);
        }
        blockRepository.deleteById(id);
        log.info("Deleted content block id={}", id);
    }

    @Override
    public CmsContentBlockDto toggleActive(Long id) {
        log.debug("Toggling active state for content block id={}", id);
        CmsContentBlock block = blockRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Content block not found for id={}", id);
                    return new ResourceNotFoundException("ContentBlock", "id", id);
                });
        block.setActive(!block.isActive());
        CmsContentBlockDto toggled = toDto(blockRepository.save(block));
        log.info("Content block id={} active set to {}", toggled.getId(), toggled.isActive());
        return toggled;
    }

    private void applyRequest(CmsContentBlock block, CreateContentBlockRequest r) {
        block.setPageId(r.getPageId());
        block.setBlockType(r.getBlockType());
        block.setTitle(r.getTitle());
        block.setBody(r.getBody());
        block.setImageUrl(r.getImageUrl());
        block.setDisplayOrder(r.getDisplayOrder());
    }

    private CmsContentBlockDto toDto(CmsContentBlock b) {
        CmsContentBlockDto dto = new CmsContentBlockDto();
        dto.setId(b.getId());
        dto.setPageId(b.getPageId());
        dto.setBlockType(b.getBlockType());
        dto.setTitle(b.getTitle());
        dto.setBody(b.getBody());
        dto.setImageUrl(b.getImageUrl());
        dto.setDisplayOrder(b.getDisplayOrder());
        dto.setActive(b.isActive());
        return dto;
    }
}
