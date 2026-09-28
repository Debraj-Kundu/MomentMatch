package com.debrajkundu.grabpic.session.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.debrajkundu.grabpic.session.entity.SessionMessageMedia;

public interface SessionMessageMediaRepository extends JpaRepository<SessionMessageMedia, UUID> {

    List<SessionMessageMedia> findBySessionMessageIdOrderByRankAsc(UUID sessionMessageId);

    List<SessionMessageMedia> findBySessionMessageIdInOrderByRankAsc(List<UUID> sessionMessageIds);
}

