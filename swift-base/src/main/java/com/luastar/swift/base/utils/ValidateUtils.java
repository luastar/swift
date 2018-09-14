package com.luastar.swift.base.utils;

import com.luastar.swift.base.exception.ValidateException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

/**
 * 校验工具类
 */
public class ValidateUtils {

    private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public static <T> T validate(T obj) {
        if (obj == null) {
            throw new ValidateException("参数对象不能为空");
        }
        Set<ConstraintViolation<T>> violationSet = validator.validate(obj);
        for (ConstraintViolation<T> violation : violationSet) {
            throw new ValidateException(String.format("参数[%s]%s", violation.getPropertyPath(), violation.getMessage()));
        }
        return obj;
    }

}
