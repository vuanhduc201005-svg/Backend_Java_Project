package com.dducwsjvbe.backendjava.repository.interfaces;

import com.dducwsjvbe.backendjava.model.RoleHasPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleHasPermissionRepository extends JpaRepository<RoleHasPermission,Integer> {
}
