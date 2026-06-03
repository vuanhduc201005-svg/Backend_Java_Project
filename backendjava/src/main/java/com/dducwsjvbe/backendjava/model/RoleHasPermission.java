package com.dducwsjvbe.backendjava.model;
import jakarta.persistence.*;
import lombok.*;
@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_role_has_permission", indexes = {
        @Index(name = "idx_role_has_permission_role_id", columnList = "role_id"),
        @Index(name = "idx_role_has_permission_permission_id", columnList = "permission_id"),
        @Index(name = "idx_role_has_permission_role_perm", columnList = "role_id, permission_id")
})
public class RoleHasPermission extends AbstracEntity<Integer> {
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne
    @JoinColumn(name = "permission_id")
    private Permission permission;
}
