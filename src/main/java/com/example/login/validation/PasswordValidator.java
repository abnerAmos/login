package com.example.login.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final String PASSWORD_PATTERN =
            "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@$!%*?&.])[0-9a-zA-Z@$!%*?&.]{8,}$";

    private static final String SEQUENCES_PATTERN =
            ".*(012|123|234|345|456|567|678|789|890|[a-z]{3}|[A-Z]{3}|([a-zA-Z])\1\1).*";

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Senha não pode ser nula")
                    .addConstraintViolation();

            return false;
        }

        if (password.matches(SEQUENCES_PATTERN)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("A senha não deve conter sequenciais")
                    .addConstraintViolation();

            return false;
        }

        if (!password.matches(PASSWORD_PATTERN)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("A senha deve conter ao menos uma letra maiúscula, uma letra minúscula, um número, um caractere especial, e no mínimo 8 caracteres")
                    .addConstraintViolation();

            return false;
        }

        return true;
    }

}
