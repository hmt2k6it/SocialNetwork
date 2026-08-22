package com.example.socialnetwork.module.identity.repository;

import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.socialnetwork.module.identity.entity.User;

public interface UserRepository extends JpaRepository<User, String> {
    @Override
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @NonNull
    Optional<User> findById(String id);

    @Override
    @EntityGraph(attributePaths = {"roles"})
    @NonNull
    List<User> findAll();


    boolean existsByUsername(String username);
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

}
