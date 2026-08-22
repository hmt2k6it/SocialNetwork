package com.example.socialnetwork.module.identity.repository;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.socialnetwork.module.identity.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    @Override
    @EntityGraph(attributePaths = {"user", "user.roles", "user.roles.permissions"})
    @NonNull
    Optional<RefreshToken> findById(String id);
}
