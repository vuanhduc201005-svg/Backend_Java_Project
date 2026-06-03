package com.dducwsjvbe.backendjava.model;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_group_has_user", indexes = {
        @Index(name = "idx_group_has_user_group_id", columnList = "group_id"),
        @Index(name = "idx_group_has_user_user_id", columnList = "user_id")
})
public class GroupHasUser extends AbstracEntity<Integer> {
    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
