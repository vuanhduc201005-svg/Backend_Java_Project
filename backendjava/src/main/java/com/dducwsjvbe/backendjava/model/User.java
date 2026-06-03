package com.dducwsjvbe.backendjava.model;

import com.dducwsjvbe.backendjava.enums.Gender;
import com.dducwsjvbe.backendjava.enums.UserStatus;
import com.dducwsjvbe.backendjava.enums.UserType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;


@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tbl_user", indexes = {
        @Index(name = "idx_user_username", columnList = "username"),
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_username_email", columnList = "username, email"),
        @Index(name = "idx_user_first_name", columnList = "first_name"),
        @Index(name = "idx_user_last_name", columnList = "last_name")
})
public class User extends AbstracEntity<Long> {

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "date_of_birth")
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;

    @Enumerated(EnumType.STRING)
//    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
//    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type")
    private UserType type;

    @Enumerated(EnumType.STRING)
//    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status")
    private UserStatus status;

    @Column(name = "token_refresh", unique = true)
    private String tokenRefresh;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_token")
    private UserStatus tokenRefreshStatus;

    @OneToMany(mappedBy = "user")
    private Set<UserHasRole> roles = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<GroupHasUser> groups = new HashSet<>();

    @OneToOne(mappedBy = "user")
    private BlackListTokenRefresh blackListTokenRefresh;

    @OneToMany(mappedBy = "user")
    private List<Product> products;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private Set<Address> addresses = new HashSet<>();

    public void saveAddress(Address address) {
        if (address != null) {
            if (addresses == null) {
                addresses = new HashSet<>();
            }
            addresses.add(address);
            address.setUser(this); // save user_id
        }
    }

}
/*
@JsonIgnore:ko đưa field vào response json
vì address bị lặp vô tận
 */
