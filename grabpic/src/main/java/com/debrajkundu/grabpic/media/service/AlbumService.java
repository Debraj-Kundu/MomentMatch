package com.debrajkundu.grabpic.media.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debrajkundu.grabpic.common.dto.PageRequest;
import com.debrajkundu.grabpic.common.dto.PageResponse;
import com.debrajkundu.grabpic.media.dto.album.AlbumRequestDto;
import com.debrajkundu.grabpic.media.dto.album.AlbumResponseDto;
import com.debrajkundu.grabpic.media.entity.Album;
import com.debrajkundu.grabpic.media.entity.AlbumStatus;
import com.debrajkundu.grabpic.media.entity.Media;
import com.debrajkundu.grabpic.media.repository.AlbumRepository;
import com.debrajkundu.grabpic.user.entity.User;
import com.debrajkundu.grabpic.media.mapper.AlbumMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlbumService implements AlbumServiceInterface {

    private final AlbumRepository albumRepository;
    private final AlbumMapper albumMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<AlbumResponseDto> getAlbumById(UUID albumId) {
        return albumRepository.findById(albumId).map(albumMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AlbumResponseDto> getAlbumByOwnerId(UUID ownerId, PageRequest pageRequest) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.getPage(),
                pageRequest.getSize(),
                Sort.by(
                        Sort.Direction.fromString(pageRequest.getOrder()),
                        pageRequest.getSortBy()));

        Page<AlbumResponseDto> albums = albumRepository.findAllAlbumByOwnerId(ownerId, pageable)
                .map(albumMapper::toDto);

        return PageResponse.<AlbumResponseDto>builder()
                .content(albums.getContent())
                .page(albums.getNumber())
                .size(albums.getSize())
                .totalElements(albums.getTotalElements())
                .totalPages(albums.getTotalPages())
                .first(albums.isFirst())
                .last(albums.isLast())
                .build();
    }

    @Override
    @Transactional
    public int updateMediaCount(UUID albumId, Integer count) {
        return albumRepository.updateMediaCount(albumId, count);
    }

    @Override
    @Transactional
    public int updateCoverMedia(UUID albumId, Media coverMedia) {
        return albumRepository.updateCoverMedia(albumId, coverMedia);
    }




    @Override
    @Transactional
    public int updateAlbumStatus(UUID albumId, AlbumStatus albumStatus) {
        return albumRepository.updateAlbumStatus(albumId, albumStatus);
    }

    @Override
    @Transactional
    public int updateProcessedMedia(UUID albumId, Integer count) {
        return albumRepository.updateProcessedMedia(albumId, count);
    }

    @Override
    @Transactional
    public AlbumResponseDto createAlbum(User owner, AlbumRequestDto requestDto) {
        Album album = Album.builder()
                .owner(owner)
                .name(requestDto.getName())
                .coverMedia(requestDto.getCoverMedia())
                .albumStatus(AlbumStatus.PENDING)
                .build();
        return albumMapper.toDto(albumRepository.save(album));
    }

}
