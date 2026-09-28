package com.debrajkundu.grabpic.session.mapper;

import com.debrajkundu.grabpic.media.dto.media.MediaIdAndObjectKey;
import com.debrajkundu.grabpic.media.mapper.AlbumMapper;
import com.debrajkundu.grabpic.session.dto.SessionResponseDto;
import com.debrajkundu.grabpic.session.entity.Session;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SessionMapper {

    private final AlbumMapper albumMapper;

    public SessionResponseDto toDto(Session session) {
        if (session == null) {
            return null;
        }

        return SessionResponseDto.builder()
                .id(session.getId())
                .title(session.getTitle())
                .primaryMedia(session.getPrimaryMedia() != null
                        ? new MediaIdAndObjectKey(session.getPrimaryMedia().getId(), session.getPrimaryMedia().getObjectKey())
                        : null)
                .album(albumMapper.toDto(session.getAlbum()))
                .build();
    }
}
