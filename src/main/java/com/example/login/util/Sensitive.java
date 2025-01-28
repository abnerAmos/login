package com.example.login.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Anotação para marcar campos sensíveis
@Retention(RetentionPolicy.RUNTIME) // A anotação estará disponível em tempo de execução
@Target(ElementType.FIELD) // A anotação será aplicada a campos
public @interface Sensitive {
}