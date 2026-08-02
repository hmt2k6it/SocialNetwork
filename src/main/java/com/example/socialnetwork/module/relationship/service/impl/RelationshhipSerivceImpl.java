package com.example.socialnetwork.module.relationship.service.impl;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.socialnetwork.module.identity.dto.response.UserPublicResponse;
import com.example.socialnetwork.module.identity.service.UserService;
import com.example.socialnetwork.module.relationship.dto.response.FriendshipResponse;
import com.example.socialnetwork.module.relationship.entity.Friendship;
import com.example.socialnetwork.module.relationship.service.RelationshipService;
import com.example.socialnetwork.module.relationship.repository.FriendshipRepository;
import com.example.socialnetwork.module.relationship.enums.RelationshipState;
import com.example.socialnetwork.module.relationship.mapper.RelationshipMapper;
import com.example.socialnetwork.common.exception.AppException;
import com.example.socialnetwork.module.identity.entity.User;
import com.example.socialnetwork.common.exception.ErrorCode;
import com.example.socialnetwork.common.utils.SecurityUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.socialnetwork.module.identity.mapper.UserMapper;

@Transactional
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RelationshhipSerivceImpl implements RelationshipService {
    FriendshipRepository friendshipRepository;
    UserService userService;
    RelationshipMapper relationshipMapper;
    UserMapper userMapper;

    @Override
    public FriendshipResponse sendRequest(String targetUserId) {
        String currentUserId = SecurityUtil.getCurrentUserId();
        if (!userService.existsByUserId(targetUserId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        if (currentUserId.equals(targetUserId)) {
            throw new AppException(ErrorCode.CANNOT_ADD_SELF);
        }

        // TODO: Check block 2 chiều

        SortedUserPair sortedUsers = getSortedUserPair(currentUserId, targetUserId);

        Optional<Friendship> friendship = friendshipRepository.findByUser1AndUser2(sortedUsers.user1(),
                sortedUsers.user2());

        if (friendship.isPresent()) {
            Friendship existingFriendship = friendship.get();
            existingFriendship.renew(currentUserId);
            return relationshipMapper.toFriendshipResponse(friendshipRepository.save(existingFriendship));
        } else {
            Friendship newFriendship = Friendship.create(sortedUsers.user1(), sortedUsers.user2(), currentUserId);
            return relationshipMapper.toFriendshipResponse(friendshipRepository.save(newFriendship));
        }
    }

    @Override
    public FriendshipResponse acceptRequest(String relationshipId) {
        String currentUserId = SecurityUtil.getCurrentUserId();
        Friendship friendship = friendshipRepository.findById(relationshipId).orElseThrow(
                () -> new AppException(ErrorCode.RELATIONSHIP_NOT_FOUND));
        friendship.accept(currentUserId);
        return relationshipMapper.toFriendshipResponse(friendshipRepository.save(friendship));
    }

    @Override
    public FriendshipResponse rejectRequest(String relationshipId) {
        String currentUserId = SecurityUtil.getCurrentUserId();
        Friendship friendship = friendshipRepository.findById(relationshipId).orElseThrow(
                () -> new AppException(ErrorCode.RELATIONSHIP_NOT_FOUND));
        friendship.reject(currentUserId);
        return relationshipMapper.toFriendshipResponse(friendshipRepository.save(friendship));
    }

    @Override
    public String unsendRequest(String targetUserId) {
        String currentUserId = SecurityUtil.getCurrentUserId();
        if (!userService.existsByUserId(targetUserId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        SortedUserPair sortedUsers = getSortedUserPair(currentUserId, targetUserId);

        Friendship friendship = friendshipRepository.findByUser1AndUser2(sortedUsers.user1(), sortedUsers.user2())
                .orElseThrow(() -> new AppException(ErrorCode.RELATIONSHIP_NOT_FOUND));
        friendship.validateCanUnsend(currentUserId);
        friendshipRepository.delete(friendship);
        return "Friend request unsent successfully";
    }

    @Override
    public FriendshipResponse unfriend(String targetUserId) {
        String currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId.equals(targetUserId)) {
            throw new AppException(ErrorCode.CANNOT_ADD_SELF);
        }
        if (!userService.existsByUserId(targetUserId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        SortedUserPair sortedUsers = getSortedUserPair(currentUserId, targetUserId);
        Friendship friendship = friendshipRepository.findByUser1AndUser2(sortedUsers.user1(), sortedUsers.user2())
                .orElseThrow(() -> new AppException(ErrorCode.RELATIONSHIP_NOT_FOUND));
        friendship.unfriend(currentUserId);
        return relationshipMapper.toFriendshipResponse(friendshipRepository.save(friendship));
    }

    @Override
    @Transactional(readOnly = true)
    public RelationshipState getRelationshipStatus(String targetUserId) {
        String currentUserId = SecurityUtil.getCurrentUserId();
        if (!userService.existsByUserId(targetUserId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        if (currentUserId.equals(targetUserId)) {
            return RelationshipState.SELF;
        }
        // Check block 2 chiều
        SortedUserPair sortedUsers = getSortedUserPair(currentUserId, targetUserId);

        Optional<Friendship> friendship = friendshipRepository.findByUser1AndUser2(sortedUsers.user1(),
                sortedUsers.user2());

        return friendship
                .map(f -> f.getStateForUser(currentUserId))
                .orElse(RelationshipState.NONE);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserPublicResponse> getFriends(Pageable pageable) {
        String currentUserId = SecurityUtil.getCurrentUserId();
        User currentUser = userService.getUserReference(currentUserId);

        Page<Friendship> friendships = friendshipRepository.findFriends(currentUser, pageable);

        return friendships.map(friendship -> {
            User friend = friendship.getUser1().getUserId().equals(currentUserId)
                    ? friendship.getUser2()
                    : friendship.getUser1();
            return userMapper.toUserPublicResponse(friend);
        });
    }

    @Override
    public Page<FriendshipResponse> getPendingRequests(String type, Pageable pageable, boolean isInComing) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPendingRequests'");
    }

    @Override
    public Page<UserPublicResponse> getFriendsByUserId(String targetUserId, Pageable pageable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFriendsByUserId'");
    }

    private SortedUserPair getSortedUserPair(String currentUserId, String targetUserId) {
        boolean isCurrentFirst = currentUserId.compareTo(targetUserId) < 0;
        User user1;
        User user2;
        if (isCurrentFirst) {
            user1 = userService.getUserReference(currentUserId);
            user2 = userService.getUserReference(targetUserId);
        } else {
            user1 = userService.getUserReference(targetUserId);
            user2 = userService.getUserReference(currentUserId);
        }
        return new SortedUserPair(user1, user2);
    }

    private record SortedUserPair(User user1, User user2) {
    }
}
