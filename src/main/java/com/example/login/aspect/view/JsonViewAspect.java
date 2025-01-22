package com.example.login.aspect.view;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Component;

/**
 * Aspecto responsável por aplicar diferentes visualizações JSON em respostas baseadas no parâmetro "view" na URI.
 * <p>
 * Este aspecto intercepta métodos anotados com mapeamentos de requisição do Spring (`@RequestMapping`,
 * `@GetMapping`, `@PostMapping`, etc.) e modifica a resposta para incluir uma visualização específica do JSON.
 * As visualizações são determinadas com base em um parâmetro na URI (`/view/{view}`) e utilizam as classes
 * de visualização definidas no pacote `Views`.
 */
@Aspect
@Component
@RequiredArgsConstructor
public class JsonViewAspect {

    private final HttpServletRequest request;

    /**
     * Intercepta métodos com mapeamentos de requisição e aplica a visualização JSON apropriada.
     * <p>
     * Este método verifica se a resposta é uma instância de `ResponseEntity` e, caso positivo, ajusta a serialização
     * do corpo da resposta usando a visualização JSON determinada pelo parâmetro `view` na URI.
     *
     * @param joinPoint O ponto de junção do método sendo interceptado.
     * @return A resposta processada com a visualização JSON aplicada.
     * @throws Throwable Se ocorrer alguma exceção ao executar o método original.
     */
    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PatchMapping)")
    public Object applyJsonView(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        if (result instanceof ResponseEntity<?> responseEntity) {
            String viewParam = extractPathVariable();
            Class<?> viewClass = resolveViewClass(viewParam);

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

    /**
     * Extrai o valor do parâmetro "view" da URI da requisição.
     * <p>
     * Este método busca no caminho da URI a presença de um segmento "view" seguido por um valor.
     *
     * @return O valor do parâmetro "view", ou `null` se não estiver presente.
     */
    private String extractPathVariable() {
        String uri = request.getRequestURI();
        String[] parts = uri.split("/");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equalsIgnoreCase("view") && i + 1 < parts.length) {
                return parts[i + 1];
            }
        }
        return null;
    }

    /**
     * Resolve a classe de visualização JSON com base no parâmetro fornecido.
     * <p>
     * Este método mapeia o valor do parâmetro `view` para uma classe específica de visualização definida em `Views`.
     * Caso o parâmetro seja nulo ou inválido, a visualização padrão (`Views.Basic.class`) será utilizada.
     *
     * @param view O valor do parâmetro "view".
     * @return A classe de visualização correspondente.
     */
    private Class<?> resolveViewClass(String view) {
        if (view == null || view.isBlank()) {
            return Views.Basic.class;
        }
        return switch (view.toLowerCase()) {
            case "regular" -> Views.Regular.class;
            case "details" -> Views.Details.class;
            case "complete" -> Views.Complete.class;
            default -> Views.Basic.class;
        };
    }
}
