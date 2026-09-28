package com.debrajkundu.grabpic.session.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.debrajkundu.grabpic.common.dto.PageRequest;
import com.debrajkundu.grabpic.common.dto.PageResponse;
import com.debrajkundu.grabpic.session.dto.CreateSessionMessageRequestDto;
import com.debrajkundu.grabpic.session.dto.CreateSessionRequestDto;
import com.debrajkundu.grabpic.session.dto.MatchFaceResponseDto;
import com.debrajkundu.grabpic.session.dto.SessionMessageResponseDto;
import com.debrajkundu.grabpic.session.dto.SessionResponseDto;

public interface SessionServiceInterface {
    SessionResponseDto createSession(CreateSessionRequestDto reqBody);

    PageResponse<SessionResponseDto> getAllSession(UUID albumId, PageRequest pageRequest);

    Optional<SessionResponseDto> getSessionById(UUID sessionId);

    int updatePrimaryMedia(UUID sessionId, UUID primaryMediaId);

    SessionMessageResponseDto createMessage(UUID sessionId, CreateSessionMessageRequestDto requestDto);

    PageResponse<SessionMessageResponseDto> getSessionMessages(UUID sessionId, PageRequest pageRequest);

    List<SessionMessageResponseDto> getSessionMessageHistory(UUID sessionId);

    Optional<SessionMessageResponseDto> getMessageById(UUID messageId);

    void deleteMessage(UUID messageId);

    void sendAlbumForProcessing(UUID albumId);

    MatchFaceResponseDto matchPrimaryFace(UUID sessionId, UUID primaryMediaId);

    MatchFaceResponseDto matchPrimaryFace(UUID sessionId);

    MatchFaceResponseDto findSimilarImages(UUID sessionId, String message);
}
