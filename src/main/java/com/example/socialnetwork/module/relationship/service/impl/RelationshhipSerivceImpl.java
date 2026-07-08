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
import com.example.socialnetwork.module.relationship.entity.FriendshipStatus;
import com.example.socialnetwork.module.relationship.mapper.RelationshipMapper;
import com.example.socialnetwork.common.exception.AppException;
import com.example.socialnetwork.module.identity.entity.User;
import com.example.socialnetwork.common.exception.ErrorCode;
import com.example.socialnetwork.common.utils.SecurityUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RelationshhipSerivceImpl implements RelationshipService {
    FriendshipRepository friendshipRepository;
    UserService userService;
    RelationshipMapper relationshipMapper;
    @Override
    @org.springframework.transaction.annotation.Transactional
    public FriendshipResponse sendRequest(String targetUserId) {
        String currentUserId = SecurityUtil.getCurrentUserId();
        if (!userService.existsByUserId(targetUserId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        if (currentUserId.equals(targetUserId)) {
            throw new AppException(ErrorCode.CANNOT_ADD_SELF);
        }
        User currentUser = userService.getUserReference(currentUserId);
        User targetUser = userService.getUserReference(targetUserId);

        // TODO: Check block 2 chiều

        boolean isCurrentFirst = currentUserId.compareTo(targetUserId) < 0;
        User user1 = isCurrentFirst ? currentUser : targetUser;
        User user2 = isCurrentFirst ? targetUser : currentUser;

        Optional<Friendship> friendship = friendshipRepository.findByUser1AndUser2(user1, user2);

        if (friendship.isPresent()) {
            Friendship existingFriendship = friendship.get();
            FriendshipStatus status = existingFriendship.getStatus();
            if (status.equals(FriendshipStatus.PENDING)) {
                if (existingFriendship.getActionUserId().equals(currentUserId)) {
                    throw new AppException(ErrorCode.FRIEND_REQUEST_ALREADY_SENT);
                } else {
                    throw new AppException(ErrorCode.FRIEND_REQUEST_ALREADY_RECEIVED);
                }
            }
            if (status.equals(FriendshipStatus.ACCEPTED)) {
                throw new AppException(ErrorCode.ALREADY_FRIENDS);
            }
            
            existingFriendship.setStatus(FriendshipStatus.PENDING);
            existingFriendship.setActionUserId(currentUserId);
            return relationshipMapper.toFriendshipResponse(friendshipRepository.save(existingFriendship));
        } else {
            Friendship newFriendship = Friendship.builder()
                    .user1(user1)
                    .user2(user2)
                    .actionUserId(currentUserId)
                    .status(FriendshipStatus.PENDING)
                    .build();
            return relationshipMapper.toFriendshipResponse(friendshipRepository.save(newFriendship));
        }
    }
    @Override
    public FriendshipResponse acceptRequest(String relationshipId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'acceptRequest'");
    }

    @Override
    public FriendshipResponse rejectRequest(String relationshipId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rejectRequest'");
    }

    @Override
    public String unsendRequest(String targetUserId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'unsendRequest'");
    }

    @Override
    public String unfriend(String targetUserId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'unfriend'");
    }

    @Override
    public String getRelationshipStatus(String targetUserId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRelationshipStatus'");
    }

    @Override
    public Page<UserPublicResponse> getFriends(Pageable pageable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFriends'");
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

}
