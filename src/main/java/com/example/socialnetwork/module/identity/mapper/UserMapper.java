package com.example.socialnetwork.module.identity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import com.example.socialnetwork.module.identity.dto.request.UserCreationRequest;
import com.example.socialnetwork.module.identity.dto.request.UserUpdateRequest;
import com.example.socialnetwork.module.identity.dto.response.UserPrivateResponse;
import com.example.socialnetwork.module.identity.dto.response.UserPublicResponse;
import com.example.socialnetwork.module.identity.entity.Role;
import com.example.socialnetwork.module.identity.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    @Mapping(target = "bio", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "banAt", ignore = true)
    @Mapping(target = "banReason", ignore = true)
    @Mapping(target = "banned", ignore = true)
    @Mapping(target = "deleteAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)

    User toUser(UserCreationRequest request);

    UserPrivateResponse toUserPrivateResponse(User user);

    default String map(Role role) {
        return role.getName();
    }

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "banned", ignore = true)
    @Mapping(target = "banAt", ignore = true)
    @Mapping(target = "banReason", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deleteAt", ignore = true)
    void updateUserFromRequest(UserUpdateRequest userUpdateRequest, @MappingTarget User user);

    @Named("toUserPublicResponse")
    UserPublicResponse toUserPublicResponse(User user);

}
