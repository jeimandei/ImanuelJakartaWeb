package com.jeimandei.imanuelbytes.media.repository;

import com.jeimandei.imanuelbytes.media.entity.GalleryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GalleryItemRepository extends JpaRepository<GalleryItem, Long> {

    Page<GalleryItem> findByAlbumNameAndActiveTrueOrderByDisplayOrderAsc(String albumName, Pageable pageable);

    Page<GalleryItem> findByActiveTrueOrderByAlbumNameAscDisplayOrderAsc(Pageable pageable);

    @Query("SELECT DISTINCT g.albumName FROM GalleryItem g WHERE g.active = true ORDER BY g.albumName")
    List<String> findDistinctAlbumNames();
}
