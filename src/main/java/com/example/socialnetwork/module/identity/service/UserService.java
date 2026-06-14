package com.example.socialnetwork.module.identity.service;

import com.example.socialnetwork.module.identity.dto.request.UserUpdateRequest;
import com.example.socialnetwork.module.identity.dto.response.UserPrivateResponse;
import com.example.socialnetwork.module.identity.dto.response.UserPublicResponse;
import com.example.socialnetwork.module.identity.entity.User;

public interface UserService {
    UserPrivateResponse getMyProfile();

    UserPrivateResponse updateMyProfile(UserUpdateRequest userUpdateRequest);

    UserPublicResponse getUserProfile(String userId);

    boolean existsByUserId(String userId);

    User getUserReference(String userId);
}
