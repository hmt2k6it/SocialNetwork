package com.example.socialnetwork.module.relationship.dto.response;

import java.time.LocalDateTime;

import com.example.socialnetwork.module.identity.dto.response.UserPublicResponse;
import com.example.socialnetwork.module.relationship.enums.FriendshipStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FriendshipResponse {

    String friendshipId;

    UserPublicResponse user1;

    UserPublicResponse user2;

    String actionUserId;

    FriendshipStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
