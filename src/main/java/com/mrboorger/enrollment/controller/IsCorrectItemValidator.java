package com.mrboorger.enrollment.controller;

import com.mrboorger.enrollment.model.Item;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IsCorrectItemValidator implements ConstraintValidator<IsCorrectItem, Item> {
    @Override
    public void initialize(IsCorrectItem constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Item item, ConstraintValidatorContext context) {
        if (item.getId() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("id is required")
                    .addPropertyNode("id").addConstraintViolation();
            return false;
        }
        if (item.getType() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("type is required")
                    .addPropertyNode("type").addConstraintViolation();
            return false;
        }
        if (item.getName() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("name is required")
                    .addPropertyNode("name").addConstraintViolation();
            return false;
        }
        if (!item.getType().equals("OFFER") && !item.getType().equals("CATEGORY")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("type must be OFFER or CATEGORY")
                    .addPropertyNode("type").addConstraintViolation();
            return false;
        }
        if (item.getType().equals("OFFER") && (item.getPrice() == null || item.getPrice() < 0)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("price must be non negative integer for OFFER type")
                    .addPropertyNode("price").addConstraintViolation();
            return false;
        }
        if (item.getType().equals("CATEGORY") && item.getPrice() != null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("price must be null for CATEGORY type")
                    .addPropertyNode("price").addConstraintViolation();
            return false;
        }

        return true;
    }
}

