package com.jeimandei.imanuelbytes.cms.service;

import com.jeimandei.imanuelbytes.cms.dto.CmsContentBlockDto;
import com.jeimandei.imanuelbytes.cms.dto.CreateContentBlockRequest;

import java.util.List;

public interface ContentBlockService {
    List<CmsContentBlockDto> getBlocksByPage(Long pageId);
    CmsContentBlockDto createBlock(CreateContentBlockRequest request);
    CmsContentBlockDto updateBlock(Long id, CreateContentBlockRequest request);
    void deleteBlock(Long id);
    CmsContentBlockDto toggleActive(Long id);
}
