package com.dducwsjvbe.backendjava.model;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_group", indexes = {
        @Index(name = "idx_group_id", columnList = "id"),
        @Index(name = "idx_group_role_id", columnList = "role_id")
})
public class Group extends AbstracEntity<Integer> {
    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @OneToOne
    @JoinColumn(name = "role_id")
    private Role role;

}
