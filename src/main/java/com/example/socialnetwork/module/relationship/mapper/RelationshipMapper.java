package com.example.socialnetwork.module.relationship.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.example.socialnetwork.module.relationship.dto.response.FriendshipResponse;
import com.example.socialnetwork.module.relationship.entity.Friendship;

@Mapper(componentModel = "spring")
public interface RelationshipMapper {

    @Mapping(target = "user1Id", source = "user1.userId")
    @Mapping(target = "user2Id", source = "user2.userId")
    FriendshipResponse toFriendshipResponse(Friendship friendship);
}
