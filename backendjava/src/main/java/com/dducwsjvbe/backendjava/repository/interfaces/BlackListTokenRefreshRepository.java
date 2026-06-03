package com.dducwsjvbe.backendjava.repository.interfaces;

import com.dducwsjvbe.backendjava.model.BlackListTokenRefresh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlackListTokenRefreshRepository extends JpaRepository<BlackListTokenRefresh, Long> {
    BlackListTokenRefresh findByUserId(Long userId);
}
