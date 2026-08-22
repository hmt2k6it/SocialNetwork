package com.example.socialnetwork.module.relationship.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.socialnetwork.module.identity.entity.User;
import com.example.socialnetwork.module.relationship.entity.UserBlock;

public interface UserBlockRepository extends JpaRepository<UserBlock, String> {

    // Kiểm tra xem A có chặn B không
    boolean existsByBlockerAndBlocked(User blocker, User blocked);

    // Kiểm tra xem A có chặn B, hoặc B có chặn A không (2 chiều độc lập)
    boolean existsByBlockerAndBlockedOrBlockerAndBlocked(User blocker1, User blocked1, User blocker2, User blocked2);

    // Tìm record block cụ thể
    Optional<UserBlock> findByBlockerAndBlocked(User blocker, User blocked);

    // Lấy danh sách những người mình đã chặn
    Page<UserBlock> findByBlocker(User blocker, Pageable pageable);
}
