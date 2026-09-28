package com.debrajkundu.grabpic.session.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.debrajkundu.grabpic.media.entity.Media;
import com.debrajkundu.grabpic.session.entity.Session;

public interface SessionRepository extends JpaRepository<Session, UUID> {

    Optional<Session> findByIdAndIsDeletedFalse(UUID id);

    boolean existsByIdAndIsDeletedFalse(UUID id);

    Page<Session> findAllSessionByAlbumId(UUID albumId, Pageable pageable);

    Page<Session> findAllByAlbumIdAndIsDeletedFalse(UUID albumId, Pageable pageable);

    Optional<Session> findFirstByAlbumIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID albumId);

    @Modifying
    @Query("UPDATE Session s SET s.primaryMedia = :primaryMedia WHERE s.id = :sessionId")
    int updatePrimaryMedia(@Param("sessionId") UUID sessionId, @Param("primaryMedia") Media primaryMedia);

}
