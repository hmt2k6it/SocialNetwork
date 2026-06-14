package com.example.socialnetwork.module.relationship.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.socialnetwork.common.dto.response.ApiResponse;
import com.example.socialnetwork.module.relationship.dto.response.FriendshipResponse;
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

}
