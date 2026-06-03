package com.dducwsjvbe.backendjava.model;

import com.dducwsjvbe.backendjava.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_file")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class File extends AbstracEntity<Long> {
    @Column(unique = true, nullable = false)
    private String fileId;

    @Column(unique = true, nullable = false)
    private String fileName;

    @Column(unique = true, nullable = false)
    private String filePath;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @OneToOne(mappedBy = "file")
    private Product product;

}
