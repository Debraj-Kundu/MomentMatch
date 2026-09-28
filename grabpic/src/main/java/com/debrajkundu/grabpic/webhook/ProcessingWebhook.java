package com.debrajkundu.grabpic.webhook;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.debrajkundu.grabpic.session.dto.CreateSessionMessageRequestDto;
import com.debrajkundu.grabpic.session.entity.Session;
import com.debrajkundu.grabpic.session.entity.SessionMessageRole;
import com.debrajkundu.grabpic.session.dto.SessionMessageResponseDto;
import com.debrajkundu.grabpic.session.repository.SessionRepository;
import com.debrajkundu.grabpic.session.service.SessionServiceInterface;
import com.debrajkundu.grabpic.webhook.dto.ImageSearchResultDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/webhooks/process")
@RequiredArgsConstructor
@Slf4j
public class ProcessingWebhook {

    private final SessionServiceInterface sessionService;
    private final SessionRepository sessionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping()
    public ResponseEntity<Void> imageSearchProcessing(@RequestBody ImageSearchResultDto payload) {
        log.info("Received image search processing webhook for album: {}, session: {}, media count: {}",
                payload.albumId(), payload.sessionId(), payload.mediaIds() != null ? payload.mediaIds().size() : 0);

        UUID targetSessionId = payload.sessionId();

        if (targetSessionId == null && payload.albumId() != null) {
            targetSessionId = sessionRepository
                    .findFirstByAlbumIdAndIsDeletedFalseOrderByCreatedAtDesc(payload.albumId())
                    .map(Session::getId)
                    .orElse(null);
        }

        if (targetSessionId == null) {
            log.warn("No active session found for image search result payload: {}", payload);
            return ResponseEntity.ok().build();
        }

        String content = (payload.mediaIds() == null || payload.mediaIds().isEmpty())
                ? "No matching photos found."
                : "Found " + payload.mediaIds().size() + " matching photo(s).";

        CreateSessionMessageRequestDto messageDto = new CreateSessionMessageRequestDto(
                SessionMessageRole.ASSISTANT,
                content,
                payload.mediaIds(),
                null
        );

        SessionMessageResponseDto savedMessage = sessionService.createMessage(targetSessionId, messageDto);
        messagingTemplate.convertAndSend(
            "/topic/sessions/" + targetSessionId + "/messages",
            savedMessage);
        log.info("Successfully recorded ASSISTANT response in session {}", targetSessionId);

        return ResponseEntity.ok().build();
    }
}
