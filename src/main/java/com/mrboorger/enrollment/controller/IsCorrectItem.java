package com.mrboorger.enrollment.controller;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = { IsCorrectItemValidator.class })
public @interface IsCorrectItem {
    String message() default "item is incorrect";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
