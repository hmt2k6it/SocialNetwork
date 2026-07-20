package com.example.socialnetwork.module.relationship.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.example.socialnetwork.common.exception.AppException;
import com.example.socialnetwork.common.exception.ErrorCode;
import com.example.socialnetwork.common.utils.SecurityUtil;
import com.example.socialnetwork.module.identity.entity.User;
import com.example.socialnetwork.module.identity.service.UserService;
import com.example.socialnetwork.module.relationship.dto.response.FriendshipResponse;
import com.example.socialnetwork.module.relationship.entity.Friendship;
import com.example.socialnetwork.module.relationship.entity.FriendshipStatus;
import com.example.socialnetwork.module.relationship.mapper.RelationshipMapper;
import com.example.socialnetwork.module.relationship.repository.FriendshipRepository;
import com.example.socialnetwork.module.relationship.service.impl.RelationshhipSerivceImpl;

@ExtendWith(MockitoExtension.class)
public class RelationshipServiceTest {
    @Mock
    FriendshipRepository friendshipRepository;

    @Mock
    UserService userService;

    @Mock
    RelationshipMapper relationshipMapper;

    @InjectMocks
    RelationshhipSerivceImpl relationshipService;

    MockedStatic<SecurityUtil> mockedSecurityUtil;

    String currentUserId = "user-A";
    String targetUserId = "user-B";
    User currentUser;
    User targetUser;

    @BeforeEach
    void setUp() {
        mockedSecurityUtil = mockStatic(SecurityUtil.class);
        mockedSecurityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(currentUserId);

        currentUser = User.builder().userId(currentUserId).build();
        targetUser = User.builder().userId(targetUserId).build();
    }

    @AfterEach
    void tearDown() {
        mockedSecurityUtil.close();
    }

    @Test
    void sendRequest_UserNotFound_ThrowsAppException() {
        when(userService.existsByUserId(targetUserId)).thenReturn(false);

        AppException exception = assertThrows(AppException.class, () -> relationshipService.sendRequest(targetUserId));
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void sendRequest_SelfAdd_ThrowsAppException() {
        when(userService.existsByUserId(currentUserId)).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> relationshipService.sendRequest(currentUserId));
        assertEquals(ErrorCode.CANNOT_ADD_SELF, exception.getErrorCode());
    }

    @Test
    void sendRequest_AlreadyFriends_ThrowsAppException() {
        when(userService.existsByUserId(targetUserId)).thenReturn(true);
        when(userService.getUserReference(currentUserId)).thenReturn(currentUser);
        when(userService.getUserReference(targetUserId)).thenReturn(targetUser);

        Friendship existingFriendship = Friendship.builder()
                .status(FriendshipStatus.ACCEPTED)
                .build();

        when(friendshipRepository.findByUser1AndUser2(currentUser, targetUser))
                .thenReturn(Optional.of(existingFriendship));

        AppException exception = assertThrows(AppException.class, () -> relationshipService.sendRequest(targetUserId));
        assertEquals(ErrorCode.ALREADY_FRIENDS, exception.getErrorCode());
    }

    @Test
    void sendRequest_RequestAlreadySent_ThrowsAppException() {
        when(userService.existsByUserId(targetUserId)).thenReturn(true);
        when(userService.getUserReference(currentUserId)).thenReturn(currentUser);
        when(userService.getUserReference(targetUserId)).thenReturn(targetUser);

        Friendship existingFriendship = Friendship.builder()
                .status(FriendshipStatus.PENDING)
                .actionUserId(currentUserId)
                .build();

        when(friendshipRepository.findByUser1AndUser2(currentUser, targetUser))
                .thenReturn(Optional.of(existingFriendship));

        AppException exception = assertThrows(AppException.class, () -> relationshipService.sendRequest(targetUserId));
        assertEquals(ErrorCode.FRIEND_REQUEST_ALREADY_SENT, exception.getErrorCode());
    }

    @Test
    void sendRequest_RequestAlreadyReceived_ThrowsAppException() {
        when(userService.existsByUserId(targetUserId)).thenReturn(true);
        when(userService.getUserReference(currentUserId)).thenReturn(currentUser);
        when(userService.getUserReference(targetUserId)).thenReturn(targetUser);

        Friendship existingFriendship = Friendship.builder()
                .status(FriendshipStatus.PENDING)
                .actionUserId(targetUserId)
                .build();

        when(friendshipRepository.findByUser1AndUser2(currentUser, targetUser))
                .thenReturn(Optional.of(existingFriendship));

        AppException exception = assertThrows(AppException.class, () -> relationshipService.sendRequest(targetUserId));
        assertEquals(ErrorCode.FRIEND_REQUEST_ALREADY_RECEIVED, exception.getErrorCode());
    }

    @Test
    void sendRequest_NewFriendshipCurrentIdLessThanTargetId_Success() {
        mockedSecurityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(currentUserId);

        when(userService.existsByUserId(targetUserId)).thenReturn(true);
        when(userService.getUserReference(currentUserId)).thenReturn(currentUser);
        when(userService.getUserReference(targetUserId)).thenReturn(targetUser);

        when(friendshipRepository.findByUser1AndUser2(currentUser, targetUser)).thenReturn(Optional.empty());

        Friendship savedFriendship = Friendship.builder()
                .user1(currentUser).user2(targetUser)
                .status(FriendshipStatus.PENDING).actionUserId(currentUserId).build();

        when(friendshipRepository.save(any(Friendship.class))).thenReturn(savedFriendship);
        when(relationshipMapper.toFriendshipResponse(savedFriendship)).thenReturn(mock(FriendshipResponse.class));

        FriendshipResponse response = relationshipService.sendRequest(targetUserId);

        assertNotNull(response);
        verify(friendshipRepository).save(argThat(f -> f.getUser1().getUserId().equals(currentUserId) &&
                f.getUser2().getUserId().equals(targetUserId) &&
                f.getStatus() == FriendshipStatus.PENDING &&
                f.getActionUserId().equals(currentUserId)));
    }

    @Test
    void sendRequest_NewFriendshipCurrentIdGreaterThanTargetId_Success() {
        currentUserId = "B-user"; // Greater lexicographically
        targetUserId = "A-user";
        mockedSecurityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(currentUserId);

        when(userService.existsByUserId(targetUserId)).thenReturn(true);
        currentUser = User.builder().userId(currentUserId).build();
        targetUser = User.builder().userId(targetUserId).build();
        when(userService.getUserReference(currentUserId)).thenReturn(currentUser);
        when(userService.getUserReference(targetUserId)).thenReturn(targetUser);

        when(friendshipRepository.findByUser1AndUser2(targetUser, currentUser)).thenReturn(Optional.empty());

        Friendship savedFriendship = Friendship.builder()
                .user1(targetUser).user2(currentUser)
                .status(FriendshipStatus.PENDING).actionUserId(currentUserId).build();

        when(friendshipRepository.save(any(Friendship.class))).thenReturn(savedFriendship);
        when(relationshipMapper.toFriendshipResponse(savedFriendship)).thenReturn(mock(FriendshipResponse.class));

        FriendshipResponse response = relationshipService.sendRequest(targetUserId);

        assertNotNull(response);
        verify(friendshipRepository).save(argThat(f -> f.getUser1().getUserId().equals(targetUserId) &&
                f.getUser2().getUserId().equals(currentUserId) &&
                f.getStatus() == FriendshipStatus.PENDING &&
                f.getActionUserId().equals(currentUserId)));
    }

    @Test
    void sendRequest_ExistingFriendshipDeclinedOrUnfriended_Success() {
        when(userService.existsByUserId(targetUserId)).thenReturn(true);
        when(userService.getUserReference(currentUserId)).thenReturn(currentUser);
        when(userService.getUserReference(targetUserId)).thenReturn(targetUser);

        Friendship existingFriendship = Friendship.builder()
                .status(FriendshipStatus.UNFRIENDED)
                .actionUserId(targetUserId)
                .build();

        when(friendshipRepository.findByUser1AndUser2(currentUser, targetUser))
                .thenReturn(Optional.of(existingFriendship));
        when(friendshipRepository.save(any(Friendship.class))).thenReturn(existingFriendship);
        when(relationshipMapper.toFriendshipResponse(existingFriendship)).thenReturn(mock(FriendshipResponse.class));

        FriendshipResponse response = relationshipService.sendRequest(targetUserId);

        assertNotNull(response);
        verify(friendshipRepository).save(argThat(f -> f.getStatus() == FriendshipStatus.PENDING &&
                f.getActionUserId().equals(currentUserId)));
    }
}
