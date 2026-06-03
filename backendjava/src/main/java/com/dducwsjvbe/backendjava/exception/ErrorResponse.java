package com.dducwsjvbe.backendjava.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
@Getter
@Setter
public class ErrorResponse {
    private Date timestamp;
    private String message;
    private String error;
    private String path;
    private int status;
}
