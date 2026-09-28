package com.debrajkundu.grabpic.webhook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.debrajkundu.grabpic.session.dto.CreateSessionMessageRequestDto;
import com.debrajkundu.grabpic.session.dto.SessionMessageResponseDto;
import com.debrajkundu.grabpic.session.entity.Session;
import com.debrajkundu.grabpic.session.entity.SessionMessageRole;
import com.debrajkundu.grabpic.session.repository.SessionRepository;
import com.debrajkundu.grabpic.session.service.SessionServiceInterface;
import com.debrajkundu.grabpic.webhook.dto.ImageSearchResultDto;

@ExtendWith(MockitoExtension.class)
class ProcessingWebhookTest {

    @Mock
    private SessionServiceInterface sessionService;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ProcessingWebhook processingWebhook;

    @Test
    void shouldCreateAssistantMessageInSelectedSessionWhenWebhookPayloadIncludesSessionId() {
        UUID sessionId = UUID.randomUUID();
        UUID mediaId = UUID.randomUUID();
        SessionMessageResponseDto savedMessage = new SessionMessageResponseDto(
            UUID.randomUUID(), sessionId, SessionMessageRole.ASSISTANT, "Found 1 matching photo(s).",
            List.of(), null, null);
        ImageSearchResultDto payload = new ImageSearchResultDto(null, sessionId, List.of(mediaId));
        when(sessionService.createMessage(eq(sessionId), any(CreateSessionMessageRequestDto.class)))
            .thenReturn(savedMessage);

        ResponseEntity<Void> response = processingWebhook.imageSearchProcessing(payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(sessionService).createMessage(eq(sessionId), any(CreateSessionMessageRequestDto.class));
        verify(messagingTemplate).convertAndSend("/topic/sessions/" + sessionId + "/messages", savedMessage);
    }

    @Test
    void shouldFallbackToLatestSessionForAlbumWhenSessionIdMissing() {
        UUID albumId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID mediaId = UUID.randomUUID();
        Session session = Session.builder().id(sessionId).build();
        ImageSearchResultDto payload = new ImageSearchResultDto(albumId, null, List.of(mediaId));

        when(sessionRepository.findFirstByAlbumIdAndIsDeletedFalseOrderByCreatedAtDesc(albumId))
                .thenReturn(Optional.of(session));

        ResponseEntity<Void> response = processingWebhook.imageSearchProcessing(payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(sessionService).createMessage(eq(sessionId), any(CreateSessionMessageRequestDto.class));
        verify(sessionService).createMessage(eq(sessionId), any());
    }
}
