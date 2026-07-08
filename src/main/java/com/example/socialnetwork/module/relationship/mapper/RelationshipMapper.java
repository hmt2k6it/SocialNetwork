package com.example.socialnetwork.module.relationship.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.socialnetwork.module.identity.mapper.UserMapper;
import com.example.socialnetwork.module.relationship.dto.response.FriendshipResponse;
import com.example.socialnetwork.module.relationship.entity.Friendship;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface RelationshipMapper {

    @Mapping(target = "user1", source = "user1", qualifiedByName = "toUserPublicResponse")
    @Mapping(target = "user2", source = "user2", qualifiedByName = "toUserPublicResponse")
    FriendshipResponse toFriendshipResponse(Friendship friendship);
}
