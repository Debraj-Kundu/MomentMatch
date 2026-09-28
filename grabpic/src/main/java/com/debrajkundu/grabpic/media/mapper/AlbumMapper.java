package com.debrajkundu.grabpic.media.mapper;

import com.debrajkundu.grabpic.media.dto.album.AlbumResponseDto;
import com.debrajkundu.grabpic.media.entity.Album;
import com.debrajkundu.grabpic.storage.service.StorageService;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AlbumMapper {

    private final MediaMapper mediaMapper;
    private final StorageService storageService;

    public AlbumResponseDto toDto(Album album) {
    if (album == null) {
    return null;
    }

    return new AlbumResponseDto(
    album.getId(),
    album.getName(),
    album.getOwner() != null ? album.getOwner().getId() : null,
        album.getCoverMedia() != null ? mediaMapper.toDto(
            album.getCoverMedia(),
            storageService.generateDownloadUrl(album.getCoverMedia().getObjectKey())) :
    null,
    album.getAlbumStatus(),
    album.getTotalMedia(),
    album.getProcessedMedia(),
    album.getCreatedAt() != null ? album.getCreatedAt().toString() : null
    );
    }
}
