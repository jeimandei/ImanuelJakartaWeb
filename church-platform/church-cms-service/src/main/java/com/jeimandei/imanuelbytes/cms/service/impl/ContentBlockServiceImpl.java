package com.jeimandei.imanuelbytes.cms.service.impl;

import com.jeimandei.imanuelbytes.cms.dto.CmsContentBlockDto;
import com.jeimandei.imanuelbytes.cms.dto.CreateContentBlockRequest;
import com.jeimandei.imanuelbytes.cms.entity.CmsContentBlock;
import com.jeimandei.imanuelbytes.cms.repository.CmsContentBlockRepository;
import com.jeimandei.imanuelbytes.cms.service.ContentBlockService;
import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ContentBlockServiceImpl implements ContentBlockService {

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
        CmsContentBlock block = new CmsContentBlock();
        applyRequest(block, request);
        block.setActive(true);
        return toDto(blockRepository.save(block));
    }

    @Override
    public CmsContentBlockDto updateBlock(Long id, CreateContentBlockRequest request) {
        CmsContentBlock block = blockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ContentBlock", "id", id));
        applyRequest(block, request);
        return toDto(blockRepository.save(block));
    }

    @Override
    public void deleteBlock(Long id) {
        if (!blockRepository.existsById(id)) {
            throw new ResourceNotFoundException("ContentBlock", "id", id);
        }
        blockRepository.deleteById(id);
    }

    @Override
    public CmsContentBlockDto toggleActive(Long id) {
        CmsContentBlock block = blockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ContentBlock", "id", id));
        block.setActive(!block.isActive());
        return toDto(blockRepository.save(block));
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
