package com.dducwsjvbe.backendjava.repository.interfaces;

import com.dducwsjvbe.backendjava.model.Permission;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

public interface PermissionRepository extends JpaRepositoryImplementation<Permission,Integer> {
}
