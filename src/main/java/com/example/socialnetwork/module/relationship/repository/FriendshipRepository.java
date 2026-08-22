package com.example.socialnetwork.module.relationship.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.socialnetwork.module.identity.entity.User;
import com.example.socialnetwork.module.relationship.entity.Friendship;

public interface FriendshipRepository extends JpaRepository<Friendship, String> {

    // Tìm mối quan hệ dựa trên Canonical Ordering (Dữ liệu đầu vào user1_id phải <
    // user2_id)
    Optional<Friendship> findByUser1AndUser2(User user1, User user2);

    // Lấy danh sách bạn bè (status = ACCEPTED)
    @EntityGraph(attributePaths = { "user1", "user2" })
    @Query("SELECT f FROM Friendship f WHERE (f.user1 = :user OR f.user2 = :user) AND f.status = 'ACCEPTED'")
    Page<Friendship> findFriends(@Param("user") User user, Pageable pageable);

    // Lấy danh sách yêu cầu chờ duyệt ĐẾN mình (status = PENDING và actionUserId
    // KHÁC
    // mình)
    @Query("SELECT f FROM Friendship f WHERE (f.user1 = :user OR f.user2 = :user) AND f.status = 'PENDING' AND f.actionUserId != :#{#user.userId}")
    @EntityGraph(attributePaths = { "user1", "user2" })
    Page<Friendship> findIncomingRequests(@Param("user") User user, Pageable pageable);

    // Lấy danh sách yêu cầu mình ĐÃ GỬI đi (status = PENDING và actionUserId LÀ
    // mình)
    @Query("SELECT f FROM Friendship f WHERE (f.user1 = :user OR f.user2 = :user) AND f.status = 'PENDING' AND f.actionUserId = :#{#user.userId}")
    @EntityGraph(attributePaths = { "user1", "user2" })
    Page<Friendship> findOutgoingRequests(@Param("user") User user, Pageable pageable);
}
