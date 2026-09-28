package com.debrajkundu.grabpic.media.controller;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.debrajkundu.grabpic.common.dto.PageRequest;
import com.debrajkundu.grabpic.media.dto.album.AlbumInitRequestDto;
import com.debrajkundu.grabpic.media.dto.album.AlbumInitResponseDto;
import com.debrajkundu.grabpic.media.dto.album.AlbumRequestDto;
import com.debrajkundu.grabpic.media.dto.album.AlbumResponseDto;
import com.debrajkundu.grabpic.media.entity.AlbumStatus;
import com.debrajkundu.grabpic.media.entity.Media;
import com.debrajkundu.grabpic.media.service.AlbumServiceInterface;
import com.debrajkundu.grabpic.session.service.SessionService;
import com.debrajkundu.grabpic.media.service.AlbumHandlerService;
import com.debrajkundu.grabpic.media.service.MediaServiceInterface;
import com.debrajkundu.grabpic.user.entity.User;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/album")
public class AlbumController {

    private final AlbumServiceInterface albumService;
    private final AlbumHandlerService albumHandlerService;
    private final MediaServiceInterface mediaService;
    private final SessionService sessionService;

    @PostMapping
    public ResponseEntity<?> createAlbum(@RequestBody AlbumRequestDto requestDto) {
        User user = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return ResponseEntity.ok(albumService.createAlbum(user, requestDto));
    }

    @GetMapping
    public ResponseEntity<?> getMyAlbums(@ModelAttribute PageRequest pageRequest) {
        User user = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return ResponseEntity.ok(albumService.getAlbumByOwnerId(user.getId(), pageRequest));
    }

    @PostMapping("/init")
    public ResponseEntity<AlbumInitResponseDto> createAlbumWithMedia(@RequestBody AlbumInitRequestDto requestDto) {
        User user = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return ResponseEntity.ok(albumHandlerService.createAlbumWithMedia(user, requestDto));
    }

    @GetMapping("/{albumId}")
    public ResponseEntity<?> getAlbumById(@PathVariable UUID albumId) {
        Optional<AlbumResponseDto> album = albumService.getAlbumById(albumId);
        if (album.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(album.get());
    }

    @GetMapping("/{albumId}/gallery")
    public ResponseEntity<?> getGalleryDownloadUrls(@PathVariable UUID albumId, @ModelAttribute PageRequest pageRequest) {
        return ResponseEntity.ok(mediaService.getGalleryDownloadUrls(albumId, pageRequest));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<?> getAlbumByOwnerId(@PathVariable UUID ownerId, @ModelAttribute PageRequest pageRequest) {
        return ResponseEntity.ok(albumService.getAlbumByOwnerId(ownerId, pageRequest));
    }

    @PutMapping("/media/count/{albumId}")
    public ResponseEntity<?> updateMediaCount(@PathVariable UUID albumId, @RequestBody Integer count) {
        return ResponseEntity.ok(albumService.updateMediaCount(albumId, count));
    }

    @PutMapping("/cover/{albumId}")
    public ResponseEntity<?> updateCoverMedia(@PathVariable UUID albumId, @RequestBody Media coverMedia) {
        return ResponseEntity.ok(albumService.updateCoverMedia(albumId, coverMedia));
    }

    @GetMapping("/{albumId}/sessions")
    public ResponseEntity<?> getSessionsByAlbumId(@PathVariable UUID albumId, @ModelAttribute PageRequest pageRequest) {
        return ResponseEntity.ok(sessionService.getAllSession(albumId, pageRequest));
    }


    @PutMapping("/status/{albumId}")
    public ResponseEntity<?> updateAlbumStatus(@PathVariable UUID albumId, @RequestBody AlbumStatus albumStatus) {
        return ResponseEntity.ok(albumService.updateAlbumStatus(albumId, albumStatus));
    }

    @PutMapping("/processed/{albumId}")
    public ResponseEntity<?> updateProcessedMedia(@PathVariable UUID albumId, @RequestBody Integer count) {
        return ResponseEntity.ok(albumService.updateProcessedMedia(albumId, count));
    }

    @PostMapping("process/{albumId}")
    public ResponseEntity<?> processAlbum(@PathVariable UUID albumId) {
        albumHandlerService.sendAlbumForProcessing(albumId);
        return ResponseEntity.accepted().build();
    }
}
