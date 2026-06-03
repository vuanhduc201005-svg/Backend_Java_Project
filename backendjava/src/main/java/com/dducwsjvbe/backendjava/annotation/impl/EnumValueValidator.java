package com.dducwsjvbe.backendjava.annotation.impl;

import com.dducwsjvbe.backendjava.annotation.interfaces.EnumValue;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.stream.Stream;

public class EnumValueValidator implements ConstraintValidator<EnumValue, CharSequence> {
    private List<String> acceptedValues;

    @Override
    public void initialize(EnumValue enumValue) {
        //lấy biến enumClass của enumValue,lấy giá trị trong class đó,chuyển enum thành string
        acceptedValues = Stream.of(enumValue.enumClass().getEnumConstants())
                .map(Enum::name)
                .toList();
    }
//check input có hợp lệ ko,chỉ trả ra nếu null(validation null bắt) or nằm trong enum
    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        return value == null || acceptedValues.contains(value.toString().toUpperCase());
    }
}
