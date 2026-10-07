package br.edu.umc.loginseguro.user.validation;

import java.util.Objects;

import org.springframework.beans.BeanWrapperImpl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FieldsMatchValidator implements ConstraintValidator<FieldsMatch, Object> {

    private String field;
    private String confirmField;
    private String message;

    @Override
    public void initialize(FieldsMatch annotation) {
        this.field = annotation.field();
        this.confirmField = annotation.confirmField();
        this.message = annotation.message();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        BeanWrapperImpl wrapper = new BeanWrapperImpl(value);
        Object first = wrapper.getPropertyValue(field);
        Object second = wrapper.getPropertyValue(confirmField);
        boolean valid = Objects.equals(first, second);
        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode(confirmField)
                    .addConstraintViolation();
        }
        return valid;
    }
}
