package com.dducwsjvbe.backendjava.model;

import jakarta.persistence.*;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_user_has_role", indexes = {
        @Index(name = "idx_user_has_role_user_id", columnList = "user_id"),
        @Index(name = "idx_user_has_role_role_id", columnList = "role_id"),
        @Index(name = "idx_user_has_role_user_role", columnList = "user_id, role_id")
})
public class UserHasRole extends AbstracEntity<Integer> {
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;
}
