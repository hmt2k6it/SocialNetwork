package com.example.socialnetwork.module.relationship.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.socialnetwork.module.identity.dto.response.UserPublicResponse;
import com.example.socialnetwork.module.relationship.dto.response.FriendshipResponse;
import com.example.socialnetwork.module.relationship.enums.RelationshipState;

public interface RelationshipService {
    FriendshipResponse sendRequest(String targetUserId);

    FriendshipResponse acceptRequest(String relationshipId);

    FriendshipResponse declineRequest(String relationshipId);

    String unsendRequest(String targetUserId);

    FriendshipResponse unfriend(String targetUserId);

    RelationshipState getRelationshipStatus(String targetUserId); // Thêm hàm lấy trạng thái quan hệ

    Page<UserPublicResponse> getFriends(Pageable pageable);

    // Tùy chọn: có thể thêm tham số type = INCOMING / OUTGOING
    Page<FriendshipResponse> getPendingRequests(String type, Pageable pageable, boolean isInComing);

    Page<UserPublicResponse> getFriendsByUserId(String targetUserId, Pageable pageable);

}
