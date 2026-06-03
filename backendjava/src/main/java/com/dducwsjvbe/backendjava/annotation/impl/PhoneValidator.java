package com.dducwsjvbe.backendjava.annotation.impl;

import com.dducwsjvbe.backendjava.annotation.interfaces.PhoneNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.antlr.v4.runtime.atn.PredictionMode;

import java.lang.annotation.Annotation;

public class PhoneValidator implements ConstraintValidator<PhoneNumber,String> {
    @Override
    public void initialize(PhoneNumber phoneNumberNo) {
    }
    @Override
    public boolean isValid(String phoneNo, ConstraintValidatorContext constraintValidatorContext) {
        if(phoneNo == null) return false;
        //0816699232
        if (phoneNo.matches("\\d{10}")) return true;
            //081-669-9232
        else if(phoneNo.matches("\\d{3}[-\\.\\s]\\d{3}[-\\.\\s]\\d{4}")) return true;
            //081-669-9232
        else if(phoneNo.matches("\\d{3}-\\d{3}-\\d{4}\\s(x|(ext))\\d{3,5}")) return true;
        else return phoneNo.matches("\\(\\d{3}\\)-\\d{3}-\\d{4}");
    }
}
