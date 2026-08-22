package com.example.socialnetwork.module.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.socialnetwork.module.identity.entity.Permission;

public interface PermissionRepository extends JpaRepository<Permission, String> {
}
