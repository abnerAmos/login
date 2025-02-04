package com.example.login.validation.validator;

import com.example.login.validation.ValidPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    /**
     * Padrão de expressão regular para validação da senha.
     * <p>
     * Este padrão exige que a senha contenha:
     * - Pelo menos uma letra minúscula.
     * - Pelo menos uma letra maiúscula.
     * - Pelo menos um número.
     * - Pelo menos um caractere especial de uma lista definida (ex: @$!%*?&.).
     * - No mínimo 8 caracteres no total.
     */
    private static final String PASSWORD_PATTERN =
            "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@$!%*?&.])[0-9a-zA-Z@$!%*?&.]{8,}$";

    /**
     * Valida a senha de acordo com os padrões definidos.
     * <p>
     * Este método verifica se a senha é nula, se contém sequências simples ou caracteres repetidos, e se atende aos critérios de complexidade definidos no padrão de senha.
     * Caso a senha seja inválida, uma mensagem de erro apropriada será adicionada ao contexto de validação.
     *
     * @param password A senha a ser validada.
     * @param context O contexto da validação, usado para adicionar mensagens de erro.
     * @return `true` se a senha for válida, `false` caso contrário.
     */
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return validator(context, "Senha não pode ser nula");
        }

        if (!password.matches(PASSWORD_PATTERN)) {
            return validator(context, "A senha deve conter ao menos uma letra maiúscula, uma letra minúscula, um número, um caractere especial, e no mínimo 8 caracteres");
        }

        return true;
    }

    /**
     * Adiciona uma mensagem de erro ao contexto de validação.
     * <p>
     * Este método desativa a violação de restrição padrão e adiciona uma mensagem personalizada de erro no contexto da validação.
     *
     * @param context O contexto de validação para adicionar a mensagem de erro.
     * @param messageError A mensagem de erro a ser exibida quando a validação falhar.
     * @return `false`, indicando que a validação falhou.
     */
    private boolean validator(ConstraintValidatorContext context, String messageError) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(messageError)
                .addConstraintViolation();

        return false;
    }

}
