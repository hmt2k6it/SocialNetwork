package com.example.socialnetwork.module.relationship.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.socialnetwork.common.exception.AppException;
import com.example.socialnetwork.common.exception.ErrorCode;
import com.example.socialnetwork.module.identity.entity.User;
import com.example.socialnetwork.module.relationship.enums.FriendshipStatus;
import com.example.socialnetwork.module.relationship.enums.RelationshipState;

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
@Table(name = "friendships", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "user1_id", "user2_id" }, name = "uq_friendships_pair")
}, indexes = {
		@Index(name = "idx_friendships_users", columnList = "user1_id, user2_id"),
		@Index(name = "idx_friendships_user2", columnList = "user2_id")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Friendship {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "friendship_id")
	String friendshipId;

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

	public void accept(String currentUserId) {
		validateReceiver(currentUserId);
		this.status = FriendshipStatus.ACCEPTED;
		this.actionUserId = currentUserId;
	}

	public void renew(String currentUserId) {
		if (status.equals(FriendshipStatus.PENDING)) {
			if (this.getActionUserId().equals(currentUserId)) {
				throw new AppException(ErrorCode.FRIEND_REQUEST_ALREADY_SENT);
			} else {
				throw new AppException(ErrorCode.FRIEND_REQUEST_ALREADY_RECEIVED);
			}
		}
		if (status.equals(FriendshipStatus.ACCEPTED)) {
			throw new AppException(ErrorCode.ALREADY_FRIENDS);
		}

		this.setStatus(FriendshipStatus.PENDING);
		this.setActionUserId(currentUserId);
	}

	public void decline(String currentUserId) {
		validateReceiver(currentUserId);
		this.setStatus(FriendshipStatus.DECLINED);
		this.setActionUserId(currentUserId);
	}

	public static Friendship create(User user1, User user2, String actionUserId) {
		return Friendship.builder()
				.user1(user1)
				.user2(user2)
				.actionUserId(actionUserId)
				.status(FriendshipStatus.PENDING)
				.build();
	}

	public void unfriend(String currentUserId) {
		if (!this.status.equals(FriendshipStatus.ACCEPTED)) {
			throw new AppException(ErrorCode.NOT_FRIENDS);
		}
		this.setActionUserId(currentUserId);
		this.setStatus(FriendshipStatus.UNFRIENDED);
	}

	public void validateCanUnsend(String currentUserId) {
		if (this.status != FriendshipStatus.PENDING) {
			throw new AppException(ErrorCode.REQUEST_ALREADY_HANDLED);
		}
		if (!this.actionUserId.equals(currentUserId)) {
			throw new AppException(ErrorCode.NOT_REQUEST_OWNER);
		}
	}

	private void validateReceiver(String currentUserId) {
		boolean isReceiver = this.getUser1().getUserId().equals(currentUserId)
				|| this.getUser2().getUserId().equals(currentUserId);
		// Check xem có phải là là đúng người nhận và không phải là người gửi
		if (!isReceiver || currentUserId.equals(this.getActionUserId())) {
			throw new AppException(ErrorCode.NOT_REQUEST_OWNER);
		}

		if (this.getStatus() != FriendshipStatus.PENDING) {
			throw new AppException(ErrorCode.REQUEST_ALREADY_HANDLED);
		}
	}

	public RelationshipState getStateForUser(String currentUserId) {
		if (this.status == FriendshipStatus.DECLINED || this.status == FriendshipStatus.UNFRIENDED) {
			return RelationshipState.NONE;
		}
		if (this.status == FriendshipStatus.PENDING) {
			return this.actionUserId.equals(currentUserId)
					? RelationshipState.PENDING_OUTGOING
					: RelationshipState.PENDING_INCOMING;
		}
		return RelationshipState.ACCEPTED;
	}
}
