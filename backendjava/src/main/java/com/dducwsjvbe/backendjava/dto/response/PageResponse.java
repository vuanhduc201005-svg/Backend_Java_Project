package com.dducwsjvbe.backendjava.dto.response;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> implements Serializable {
    private HttpStatus status;
    private String message;
    private int pageNo;
    private int pageSize;
    private int totalPages; //tổng số trang
    private T items;
}
