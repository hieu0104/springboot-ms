package com.hieu.ms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.hieu.ms.entity.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {}
