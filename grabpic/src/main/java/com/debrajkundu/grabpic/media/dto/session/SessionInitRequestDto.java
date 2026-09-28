package com.debrajkundu.grabpic.media.dto.session;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SessionInitRequestDto {
    UUID albumId;
    String title;
}
