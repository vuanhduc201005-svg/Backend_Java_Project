package com.dducwsjvbe.backendjava.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "tbl_user_comfirm", indexes = {
        @Index(name = "idx_user_uncomfirm_username", columnList = "username")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUncomfirm extends AbstracEntity<Long>{
    @Column(name = "username",nullable = false,unique = true)
    private String userName;
    @Column(name = "password",nullable = false)
    private String password;
    @Column(name = "email",nullable = false)
    private String email;
    @Column(name = "token_register")
    private String tokenRegister;
}
