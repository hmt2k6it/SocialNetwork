package com.example.socialnetwork.module.identity.service.impl;

import org.springframework.stereotype.Service;

import com.example.socialnetwork.common.exception.AppException;
import com.example.socialnetwork.common.exception.ErrorCode;
import com.example.socialnetwork.common.utils.SecurityUtil;
import com.example.socialnetwork.module.identity.dto.request.UserUpdateRequest;
import com.example.socialnetwork.module.identity.dto.response.UserPrivateResponse;
import com.example.socialnetwork.module.identity.dto.response.UserPublicResponse;
import com.example.socialnetwork.module.identity.entity.User;
import com.example.socialnetwork.module.identity.mapper.UserMapper;
import com.example.socialnetwork.module.identity.repository.UserRepository;
import com.example.socialnetwork.module.identity.service.UserService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    UserMapper userMapper;

    public UserPrivateResponse getMyProfile() {
        User user = getUser();
        return userMapper.toUserPrivateResponse(user);
    }

    public UserPrivateResponse updateMyProfile(UserUpdateRequest userUpdateRequest) {
        User user = getUser();
        userMapper.updateUserFromRequest(userUpdateRequest, user);
        return userMapper.toUserPrivateResponse(userRepository.save(user));
    }

    private User getUser() {
        String userId = SecurityUtil.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public UserPublicResponse getUserProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (user.isBanned() || user.isDeleted()) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        return userMapper.toUserPublicResponse(user);
    }

}