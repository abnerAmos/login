package com.example.login.view;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class JsonViewAspect {

    private final HttpServletRequest request;

    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PatchMapping)")
    public Object applyJsonView(ProceedingJoinPoint joinPoint) throws Throwable {
        // Executa o método original e obtém o resultado
        Object result = joinPoint.proceed();

        if (result instanceof ResponseEntity<?> responseEntity) {
            // Extrai o valor do PathVariable "view" da URI
            String viewParam = extractPathVariable();
            Class<?> viewClass = resolveViewClass(viewParam);

            // Envolva o corpo no MappingJacksonValue para aplicar a JsonView
            Object body = responseEntity.getBody();
            MappingJacksonValue mapping = new MappingJacksonValue(body);
            mapping.setSerializationView(viewClass);
            return ResponseEntity
                    .status(responseEntity.getStatusCode())
                    .headers(responseEntity.getHeaders())
                    .body(mapping);
        }

        return result;
    }

    private String extractPathVariable() {
        // Extrai o valor do PathVariable "view" da URI
        String uri = request.getRequestURI();
        String[] parts = uri.split("/");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equalsIgnoreCase("view") && i + 1 < parts.length) {
                return parts[i + 1];
            }
        }
        return null; // Retorna null se não encontrado
    }

    private Class<?> resolveViewClass(String view) {
        if (view == null || view.isBlank()) {
            return Views.Basic.class; // Retorna a visão padrão
        }
        return switch (view.toLowerCase()) {
            case "regular" -> Views.Regular.class;
            case "details" -> Views.Details.class;
            case "complete" -> Views.Complete.class;
            default -> Views.Basic.class;
        };
    }
}
