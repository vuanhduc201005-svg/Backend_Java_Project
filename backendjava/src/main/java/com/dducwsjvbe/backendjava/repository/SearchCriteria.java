package com.dducwsjvbe.backendjava.repository;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SearchCriteria {
    private String key;//a:b,a1:b1,...
    private String operation;//toán tử query
    private Object value;
}
