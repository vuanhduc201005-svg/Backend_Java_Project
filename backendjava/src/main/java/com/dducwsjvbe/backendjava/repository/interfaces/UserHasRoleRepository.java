package com.dducwsjvbe.backendjava.repository.interfaces;

import com.dducwsjvbe.backendjava.model.UserHasRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserHasRoleRepository extends JpaRepository<UserHasRole,Integer> {
}
