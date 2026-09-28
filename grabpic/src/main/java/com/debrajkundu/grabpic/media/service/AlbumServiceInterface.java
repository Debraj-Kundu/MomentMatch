package com.debrajkundu.grabpic.media.service;

import java.util.Optional;
import java.util.UUID;

import com.debrajkundu.grabpic.common.dto.PageRequest;
import com.debrajkundu.grabpic.common.dto.PageResponse;
import com.debrajkundu.grabpic.media.dto.album.AlbumRequestDto;
import com.debrajkundu.grabpic.media.dto.album.AlbumResponseDto;
import com.debrajkundu.grabpic.media.entity.AlbumStatus;
import com.debrajkundu.grabpic.media.entity.Media;
import com.debrajkundu.grabpic.user.entity.User;

public interface AlbumServiceInterface {
    Optional<AlbumResponseDto> getAlbumById(UUID albumId);

    PageResponse<AlbumResponseDto> getAlbumByOwnerId(UUID ownerId, PageRequest pageRequest);

    int updateMediaCount(UUID albumId, Integer count);

    int updateCoverMedia(UUID albumId, Media coverMedia);



    int updateAlbumStatus(UUID albumId, AlbumStatus albumStatus);

    int updateProcessedMedia(UUID albumId, Integer count);

    AlbumResponseDto createAlbum(User owner, AlbumRequestDto requestDto);
}
