package com.example.socialnetwork.module.relationship.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import com.example.socialnetwork.common.dto.response.ApiResponse;
import com.example.socialnetwork.module.identity.dto.response.UserPublicResponse;
import com.example.socialnetwork.module.relationship.dto.response.FriendshipResponse;
import com.example.socialnetwork.module.relationship.enums.RelationshipState;
import com.example.socialnetwork.module.relationship.service.RelationshipService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1/friendships")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RelationshipController {

    RelationshipService relationshipService;

    @PostMapping("/requests/{targetUserId}")
    public ApiResponse<FriendshipResponse> sendRequest(@PathVariable String targetUserId) {
        return ApiResponse.<FriendshipResponse>builder()
                .result(relationshipService.sendRequest(targetUserId))
                .build();
    }

    @DeleteMapping("/requests/{targetUserId}")
    public ApiResponse<String> unsendRequest(@PathVariable String targetUserId) {
        return ApiResponse.<String>builder()
                .result(relationshipService.unsendRequest(targetUserId))
                .build();
    }

    @PutMapping("/requests/{friendshipId}/accept")
    public ApiResponse<FriendshipResponse> acceptRequest(@PathVariable String friendshipId) {
        return ApiResponse.<FriendshipResponse>builder()
                .result(relationshipService.acceptRequest(friendshipId))
                .build();
    }

    @PutMapping("/{targetUserId}/unfriend")
    public ApiResponse<FriendshipResponse> unfriend(@PathVariable String targetUserId) {
        return ApiResponse.<FriendshipResponse>builder()
                .result(relationshipService.unfriend(targetUserId))
                .build();
    }

    @PutMapping("/requests/{friendshipId}/decline")
    public ApiResponse<FriendshipResponse> declineRequest(@PathVariable String friendshipId) {
        return ApiResponse.<FriendshipResponse>builder()
                .result(relationshipService.declineRequest(friendshipId))
                .build();
    }

    @GetMapping("/status/{targetUserId}")
    public ApiResponse<RelationshipState> getRelationshipStatus(@PathVariable String targetUserId) {
        return ApiResponse.<RelationshipState>builder()
                .result(relationshipService.getRelationshipStatus(targetUserId))
                .build();
    }

    @GetMapping("/friends")
    public ApiResponse<Page<UserPublicResponse>> getFriends(@PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.<Page<UserPublicResponse>>builder()
                .result(relationshipService.getFriends(pageable))
                .build();
    }

}
