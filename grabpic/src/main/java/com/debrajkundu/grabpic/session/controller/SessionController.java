package com.debrajkundu.grabpic.session.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.debrajkundu.grabpic.common.dto.PageRequest;
import com.debrajkundu.grabpic.common.dto.PageResponse;
import com.debrajkundu.grabpic.session.dto.CreateSessionMessageRequestDto;
import com.debrajkundu.grabpic.session.dto.CreateSessionRequestDto;
import com.debrajkundu.grabpic.session.dto.MatchFaceRequestDto;
import com.debrajkundu.grabpic.session.dto.MatchFaceResponseDto;
import com.debrajkundu.grabpic.session.dto.SessionMessageResponseDto;
import com.debrajkundu.grabpic.session.dto.SessionResponseDto;
import com.debrajkundu.grabpic.session.service.SessionServiceInterface;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/session")
public class SessionController {
    private final SessionServiceInterface sessionService;

    @PostMapping()
    public ResponseEntity<?> createAlbumSession(@Valid @RequestBody CreateSessionRequestDto reqBody) {
        SessionResponseDto session = sessionService.createSession(reqBody);
        return ResponseEntity.created(URI.create("/api/v1/session/" + (session.getId() != null ? session.getId() : "")))
                .body(session);
    }

    @PatchMapping("/{sessionId}/primary-media")
    public ResponseEntity<?> updatePrimaryMedia(@PathVariable UUID sessionId, @RequestBody UUID mediaId) {
        return ResponseEntity.ok(sessionService.updatePrimaryMedia(sessionId, mediaId));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<?> getSessionById(@PathVariable UUID sessionId) {
        return sessionService.getSessionById(sessionId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{sessionId}/messages")
    public ResponseEntity<SessionMessageResponseDto> addMessage(
            @PathVariable UUID sessionId,
            @Valid @RequestBody CreateSessionMessageRequestDto reqBody) {
        SessionMessageResponseDto message = sessionService.createMessage(sessionId, reqBody);

        if (reqBody.mediaIds() != null && !reqBody.mediaIds().isEmpty()) {
            sessionService.matchPrimaryFace(sessionId, reqBody.mediaIds().get(0));
        } else {
            sessionService.findSimilarImages(sessionId, reqBody.content());
        }

        return ResponseEntity.created(URI.create("/api/v1/session/" + sessionId + "/messages/" + message.id()))
                .body(message);
    }

    @GetMapping("/{sessionId}/messages")
    public ResponseEntity<PageResponse<SessionMessageResponseDto>> getSessionMessages(
            @PathVariable UUID sessionId,
            @ModelAttribute PageRequest pageRequest) {
        return ResponseEntity.ok(sessionService.getSessionMessages(sessionId, pageRequest));
    }

    @GetMapping("/{sessionId}/messages/history")
    public ResponseEntity<List<SessionMessageResponseDto>> getSessionMessageHistory(
            @PathVariable UUID sessionId) {
        return ResponseEntity.ok(sessionService.getSessionMessageHistory(sessionId));
    }

    @GetMapping("/{sessionId}/messages/{messageId}")
    public ResponseEntity<SessionMessageResponseDto> getMessageById(
            @PathVariable UUID sessionId,
            @PathVariable UUID messageId) {
        return sessionService.getMessageById(messageId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{sessionId}/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable UUID sessionId,
            @PathVariable UUID messageId) {
        sessionService.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{sessionId}/match-face")
    public ResponseEntity<MatchFaceResponseDto> matchPrimaryFace(
            @PathVariable UUID sessionId,
            @RequestBody(required = false) MatchFaceRequestDto requestDto) {
        UUID primaryMediaId = requestDto != null ? requestDto.primaryMediaId() : null;
        MatchFaceResponseDto response = sessionService.matchPrimaryFace(sessionId, primaryMediaId);
        return ResponseEntity.accepted().body(response);
    }
}
