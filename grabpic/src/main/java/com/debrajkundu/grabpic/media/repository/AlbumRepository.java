package com.debrajkundu.grabpic.media.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.debrajkundu.grabpic.media.entity.Album;
import com.debrajkundu.grabpic.media.entity.AlbumStatus;
import com.debrajkundu.grabpic.media.entity.Media;

public interface AlbumRepository extends JpaRepository<Album, UUID> {

    Optional<Album> findById(UUID albumId);

    Page<Album> findAllAlbumByOwnerId(UUID ownerId, Pageable pageable);

    @Modifying
    @Query("UPDATE Album a SET a.totalMedia = :count WHERE a.id = :albumId")
    int updateMediaCount(@Param("albumId") UUID albumId, @Param("count") Integer count);

    @Modifying
    @Query("UPDATE Album a SET a.coverMedia = :coverMedia WHERE a.id = :albumId")
    int updateCoverMedia(@Param("albumId") UUID albumId, @Param("coverMedia") Media coverMedia);




    @Modifying
    @Query("UPDATE Album a SET a.albumStatus = :albumStatus WHERE a.id = :albumId")
    int updateAlbumStatus(@Param("albumId") UUID albumId, @Param("albumStatus") AlbumStatus albumStatus);

    @Modifying
    @Query("UPDATE Album a SET a.processedMedia = :count WHERE a.id = :albumId")
    int updateProcessedMedia(@Param("albumId") UUID albumId, @Param("count") Integer count);
}
