package com.dducwsjvbe.backendjava.repository.interfaces;

import com.dducwsjvbe.backendjava.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role,Integer> {
}
