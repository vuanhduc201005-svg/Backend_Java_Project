package com.dducwsjvbe.backendjava.repository.interfaces;

import com.dducwsjvbe.backendjava.model.UserUncomfirm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserUncomfirmRepository extends JpaRepository<UserUncomfirm, Long> {

    UserUncomfirm findByUserName(@Param("username")String userName);
}
