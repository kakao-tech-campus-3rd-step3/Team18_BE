package com.kakaotech.team18.backend_server.global.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class NoSpecialCharValidator implements ConstraintValidator<NoSpecialChar, String> {

    // 한글, 영어, 숫자, 공백만 허용
    private static final Pattern pattern = Pattern.compile("^[가-힣a-zA-Z0-9\\s]+$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return pattern.matcher(value).matches();
    }
}