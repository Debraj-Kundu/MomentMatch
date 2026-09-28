package com.debrajkundu.grabpic.session.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.debrajkundu.grabpic.session.entity.SessionMessage;

public interface SessionMessageRepository extends JpaRepository<SessionMessage, UUID> {

    @EntityGraph(attributePaths = {"mediaAttachments", "mediaAttachments.media"})
    Optional<SessionMessage> findByIdAndIsDeletedFalse(UUID id);

    @EntityGraph(attributePaths = {"mediaAttachments", "mediaAttachments.media"})
    List<SessionMessage> findBySessionIdAndIsDeletedFalseOrderByCreatedAtAsc(UUID sessionId);

    @Query("SELECT sm FROM SessionMessage sm WHERE sm.session.id = :sessionId AND sm.isDeleted = false")
    Page<SessionMessage> findBySessionIdAndIsDeletedFalse(@Param("sessionId") UUID sessionId, Pageable pageable);

    @Modifying
    @Query("UPDATE SessionMessage sm SET sm.isDeleted = true WHERE sm.id = :id")
    int softDeleteById(@Param("id") UUID id);
}

