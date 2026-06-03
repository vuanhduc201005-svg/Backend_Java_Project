package com.dducwsjvbe.backendjava.dto.Dto;

import lombok.*;

import java.io.Serializable;
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto implements Serializable {
    private String name;
    private String topic;
    private Long productId;
    private Long fileId;
}
