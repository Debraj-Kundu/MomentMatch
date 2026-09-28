package com.debrajkundu.grabpic.session.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.debrajkundu.grabpic.common.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "session_messages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionMessage extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "album_session_id", nullable = false)
    private Session session;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "role", nullable = false)
    private SessionMessageRole role;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @OneToMany(mappedBy = "sessionMessage", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("rank ASC")
    @BatchSize(size = 25)
    @Builder.Default
    private List<SessionMessageMedia> mediaAttachments = new ArrayList<>();

    public void addMediaAttachment(SessionMessageMedia mediaAttachment) {
        mediaAttachments.add(mediaAttachment);
        mediaAttachment.setSessionMessage(this);
    }
}

