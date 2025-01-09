package com.example.login.validation.validator;

import com.example.login.validation.ValidPassword;
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
            return validator(context, "Senha não pode ser nula");
        }

        if (password.matches(SEQUENCES_PATTERN)) {
            return validator(context, "A senha não deve conter sequenciais");
        }

        if (!password.matches(PASSWORD_PATTERN)) {
            return validator(context, "A senha deve conter ao menos uma letra maiúscula, uma letra minúscula, um número, um caractere especial, e no mínimo 8 caracteres");
        }

        return true;
    }

    private boolean validator(ConstraintValidatorContext context, String messageError) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(messageError)
                .addConstraintViolation();

        return false;
    }

}
