package com.jeimandei.imanuelbytes.cms.repository;

import com.jeimandei.imanuelbytes.cms.entity.CmsContentBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CmsContentBlockRepository extends JpaRepository<CmsContentBlock, Long> {

    List<CmsContentBlock> findByPageIdOrderByDisplayOrder(Long pageId);

    List<CmsContentBlock> findByPageIdAndActive(Long pageId, boolean active);
}
