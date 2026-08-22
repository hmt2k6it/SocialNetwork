package com.example.socialnetwork.module.identity.repository;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.socialnetwork.module.identity.entity.Role;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, String> {
    @Override
    @EntityGraph(attributePaths = {"permissions"})
    @NonNull
    List<Role> findAll();
}
