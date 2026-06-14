package com.example.socialnetwork.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
        // Common/System errors: 1xxx
        UNCATEGORIZED(1000, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
        UNAUTHORIZED(1001, "Unauthorized", HttpStatus.UNAUTHORIZED),
        INVALID_KEY(1002, "Invalid key", HttpStatus.BAD_REQUEST),
        // Identity Module: 2xxx
        USERNAME_EXIST(2001, "Username already exists", HttpStatus.BAD_REQUEST),
        ROLE_NOT_FOUND(2002, "Role not found", HttpStatus.BAD_REQUEST),
        USER_NOT_FOUND(2003, "User not found", HttpStatus.BAD_REQUEST),
        PASSWORD_INCORRECT(2004, "Password incorrect", HttpStatus.BAD_REQUEST),
        UNAUTHENTICATED(2005, "Unauthenticated", HttpStatus.UNAUTHORIZED),
        OTP_EXPIRED(2006, "OTP expired", HttpStatus.BAD_REQUEST),
        INVALID_OTP(2007, "Invalid OTP", HttpStatus.BAD_REQUEST),
        PASSWORD_NOT_MATCH(2008, "Password not match", HttpStatus.BAD_REQUEST),
        INVALID_USERNAME(2009, "Invalid username, username must be between %d and %d characters",
                        HttpStatus.BAD_REQUEST),
        INVALID_PASSWORD(2010,
                        "Invalid password, password must be between %d and %d characters, contain at least one uppercase letter, one lowercase letter, one number, and one special character",
                        HttpStatus.BAD_REQUEST),
        INVALID_EMAIL(2011, "Invalid email", HttpStatus.BAD_REQUEST),
        INVALID_DOB(2012, "Invalid date of birth, you must be at least 18 years old and at most 100 years old",
                        HttpStatus.BAD_REQUEST),
        TOO_MANY_REQUESTS(2013, "Too many requests, please try again later", HttpStatus.TOO_MANY_REQUESTS),
        ROLE_EXIST(2014, "Role already exists", HttpStatus.BAD_REQUEST),
        PERMISSION_EXIST(2015, "Permission already exists", HttpStatus.BAD_REQUEST),
        PERMISSION_NOT_FOUND(2016, "Permission not found", HttpStatus.BAD_REQUEST),
        USER_BANNED(2017, "User is banned", HttpStatus.BAD_REQUEST),
        //Relationship Module: 3xxx
        RELATIONSHIP_NOT_FOUND(3001, "Relationship or friend request not found", HttpStatus.NOT_FOUND),
        CANNOT_ADD_SELF(3002, "Cannot perform action on yourself", HttpStatus.BAD_REQUEST),
        BLOCKED_BY_USER(3003, "Cannot interact due to block settings", HttpStatus.FORBIDDEN),
        ALREADY_FRIENDS(3004, "You are already friends", HttpStatus.CONFLICT),
        FRIEND_REQUEST_ALREADY_SENT(3005, "Friend request already sent", HttpStatus.CONFLICT),
        FRIEND_REQUEST_ALREADY_RECEIVED(3006, "Friend request already received", HttpStatus.CONFLICT),
        NOT_REQUEST_OWNER(3007, "Not authorized to handle this request", HttpStatus.FORBIDDEN),
        REQUEST_ALREADY_HANDLED(3008, "Friend request already handled", HttpStatus.CONFLICT),
        NOT_FRIENDS(3009, "You are not friends", HttpStatus.CONFLICT),
        ALREADY_BLOCKED(3010, "User already blocked", HttpStatus.CONFLICT),
        FRIEND_LIMIT_EXCEEDED(3011, "You have reached the maximum number of friends", HttpStatus.BAD_REQUEST),
        TARGET_FRIEND_LIMIT_EXCEEDED(3012, "Target user has reached the maximum number of friends", HttpStatus.BAD_REQUEST),
        ;

        int code;
        String message;
        HttpStatus status;
}
