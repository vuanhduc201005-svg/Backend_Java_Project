package com.dducwsjvbe.backendjava.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl-black-list-token-refresh", indexes = {
        @Index(name = "idx_black_list_token_user_id", columnList = "user_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlackListTokenRefresh extends AbstracEntity<Long> {
    @Column(name = "name")
    private String tokenBlack;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
