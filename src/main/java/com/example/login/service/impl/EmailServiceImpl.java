package com.example.login.service.impl;

import com.example.login.cache.ValidationCodeCache;
import com.example.login.exception.BadRequestException;
import com.example.login.exception.InternalServerErrorException;
import com.example.login.repository.UserRepository;
import com.example.login.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    private final UserRepository userRepository;

    private final ValidationCodeCache validationCodeCache;

    /** Busca um valor de uma propriedade no properties e armazena na variável */
    @Value("${spring.mail.username}")
    private String senderEmail;

    /**
     * Envia um e-mail de registro ao usuário com um código de validação.
     *
     * @param receiverEmail O e-mail do destinatário que receberá o código de validação.
     */
    @Override
    public void sendRegisterEmail(String receiverEmail) {
        sendValidationEmail(receiverEmail);
    }

    /**
     * Valida o código de validação fornecido pelo usuário.
     * <p>
     * Este método verifica se o código de validação fornecido corresponde ao código armazenado em cache
     * para o e-mail do usuário. Caso o código esteja expirado ou inválido, lança uma exceção.
     * Após a validação bem-sucedida, o código é removido do cache, e o usuário é habilitado para login.
     *
     * @param email O e-mail do usuário cujo código será validado.
     * @param code O código de validação fornecido para validação.
     * @throws BadRequestException Se o usuário não for encontrado, ou se o código de validação for inválido ou expirado.
     */
    @Override
    public void validationCode(String email, String code) {
        var user = userRepository.findByEmail(email);
        if (user == null) {
            throw new BadRequestException("Usuário não encontrado!");
        }

        String cachedCode = validationCodeCache.getValidationCode(email);
        if (!code.equals(cachedCode)) {
            throw new BadRequestException("Código de validação expirado ou inválido.");
        }

        validationCodeCache.invalidateValidationCode(email);

        user.setEnabled(true);
        userRepository.save(user);
    }

    /**
     * Gera e envia um novo código de validação para o e-mail do usuário.
     * <p>
     * Este método invalida qualquer código de validação previamente armazenado
     * em cache para o e-mail e gera um novo código a ser enviado ao usuário.
     *
     * @param receiverEmail O e-mail do destinatário que receberá o novo código de validação.
     */
    @Override
    public void sendRefreshCode(String receiverEmail) {
        validationCodeCache.invalidateValidationCode(receiverEmail);
        sendValidationEmail(receiverEmail);
    }

    /**
     * Envia um e-mail de validação com um código gerado.
     * <p>
     * Este método gera um novo código de validação para o e-mail fornecido e o
     * envia ao destinatário. A mensagem contém o código em formato HTML.
     *
     * @param receiverEmail O e-mail do destinatário que receberá o código de validação.
     * @throws InternalServerErrorException Se ocorrer um erro durante o envio do e-mail.
     */
    private void sendValidationEmail(String receiverEmail) {
        String code = validationCodeCache.generateValidationCode(receiverEmail);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(senderEmail);
            helper.setTo(receiverEmail);
            helper.setSubject("Código de Validação");
            helper.setText("Seu código de validação é: <b>" + code + "</b>", true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new InternalServerErrorException("Erro durante o envio de e-mail");
        }
    }
}
