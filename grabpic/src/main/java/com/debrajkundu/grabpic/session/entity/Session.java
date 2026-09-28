package com.debrajkundu.grabpic.session.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.debrajkundu.grabpic.common.entity.BaseEntity;
import com.debrajkundu.grabpic.media.entity.Album;
import com.debrajkundu.grabpic.media.entity.Media;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "album_sessions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Session extends BaseEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    private Album album;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_media_id", unique = true)
    private Media primaryMedia;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<SessionMessage> messages = new ArrayList<>();
}
