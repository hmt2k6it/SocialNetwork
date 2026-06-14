package com.example.socialnetwork.module.relationship.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.socialnetwork.module.identity.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.persistence.Index;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(
    name = "friendships", 
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user1_id", "user2_id"}, name = "uq_friendships_pair")
    },
    indexes = {
        @Index(name = "idx_friendships_users", columnList = "user1_id, user2_id"),
        @Index(name = "idx_friendships_user2", columnList = "user2_id")
    }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    // user1 luôn có UUID nhỏ hơn user2 theo thứ tự từ điển (Canonical Ordering)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false)
    User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false)
    User user2;

    // Người thực hiện hành động cuối cùng (VD: Gửi request, Accept, Unfriend)
    @Column(name = "action_user_id", nullable = false)
    String actionUserId;

    @Enumerated(EnumType.STRING)
    FriendshipStatus status;

    @Version
    Long version; // Chống Race Condition (Optimistic Locking)

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;
}
