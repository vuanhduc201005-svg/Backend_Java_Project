package com.dducwsjvbe.backendjava.model;

import com.dducwsjvbe.backendjava.enums.UserStatus;
import com.dducwsjvbe.backendjava.enums.UserType;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tbl_product", indexes = {
        @Index(name = "idx_product_type", columnList = "status"),
        @Index(name = "idx_product_type_view", columnList = "status, view"),
        @Index(name = "idx_product_user_id", columnList = "user_id"),
        @Index(name = "idx_product_name", columnList = "name"),
        @Index(name = "idx_product_topic", columnList = "topic"),
        @Index(
                name = "idx_product_status_updatedat",
                columnList = "status, updated_at"
        )
})
public class Product extends AbstracEntity<Long> {
    @Column(name = "name")
    private String name;

    @Column(name = "topic")
    private String topic;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserStatus type;

    @Column(name = "view")
    private Long view;

    @OneToOne
    @JoinColumn(name = "file_id")
    private File file;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
