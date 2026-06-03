package com.dducwsjvbe.backendjava.service;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;


import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j(topic = "Mail-Service")
@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.from}")
    private String emailFrom;

    @KafkaListener(topics = "up-status-product-topic",groupId = "up-status-product-group")
    public void sendMail(String messageMail) throws MessagingException, UnsupportedEncodingException {
        log.info("sending...");
        String[]arr = messageMail.split(",");
        String recipients = arr[0].substring(arr[0].indexOf('=')+1);
        String subject = arr[1].substring(arr[1].indexOf('=')+1);
        String content = arr[2].substring(arr[2].indexOf('=')+1);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        //thay vì để ng nhận nhìn cả email ta để dducwsjvbe thôi họ trỏ vào ms thấy email
        helper.setFrom(emailFrom, "dducwsjvbe");
        if (recipients.contains(",")) {
            helper.setTo(InternetAddress.parse(recipients));
        } else {
            helper.setTo(recipients);
        }
//        if (files != null) {
//            for (MultipartFile file : files) {
//                helper.addAttachment(file.getOriginalFilename(), file);
//            }
//        }
        helper.setSubject(subject);
        helper.setText(content, true);
        mailSender.send(message);
        log.info("send mail successfully,recipients={}", recipients);
    }

    public void sendConfirmLink(String emailTo, Long userId, String secretCode) throws MessagingException, UnsupportedEncodingException {
        log.info("sending mail comfirm account...");
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
        Context context = new Context();
        String linkComfirm =String.format("http://localhost:8080/user/comfirm/%s?secretCode=%s", userId,secretCode);
        Map<String, Object> properties = new HashMap<>();
        properties.put("linkComfirm", linkComfirm);
        context.setVariables(properties);
        helper.setFrom(emailFrom, "dducwsjvbe");
        helper.setSubject("please comfirm your account");
        helper.setTo(emailTo);
        String html = templateEngine.process("comfirm-email.html", context);
        helper.setText(html, true);
        mailSender.send(message);
        log.info("send mail successfully,emailTo={}", emailTo);
    }

    @KafkaListener(topics = "comfirm-account-topic",groupId = "comfirm-account-group" )
    public void sendConfirmLinkByKafka(String message) throws MessagingException, UnsupportedEncodingException {
        log.info("sending mail comfirm account...");
        String[]arr = message.split(",");
        String emailTo = arr[0].substring(arr[0].indexOf('=')+1);
        String userId = arr[1].substring(arr[1].indexOf('=')+1);
        String secretCode = arr[2].substring(arr[2].indexOf('=')+1);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
        Context context = new Context();

        String linkComfirm =String.format("http://localhost:8080/auth/comfirm/%s?secretCode=%s", userId,secretCode);

        Map<String, Object> properties = new HashMap<>();
        properties.put("linkComfirm", linkComfirm);
        context.setVariables(properties);

        helper.setFrom(emailFrom, "dducwsjvbe");
        helper.setSubject("please comfirm your account");
        helper.setTo(emailTo);
        String html = templateEngine.process("comfirm-email.html", context);
        helper.setText(html, true);

        mailSender.send(mimeMessage);
        log.info("send mail successfully,emailTo={}", emailTo);
    }

    @KafkaListener(topics = "comfirm-forgot-password-topic",groupId = "comfirm-forgot-password-group" )
    public void sendConfirmResetPasswordLinkByKafka(String message) throws MessagingException, UnsupportedEncodingException {
        log.info("sending mail comfirm reset password...");
        String[]arr = message.split(",");
        String emailTo = arr[0].substring(arr[0].indexOf('=')+1);
        String userId = arr[1].substring(arr[1].indexOf('=')+1);
        String newPassword = arr[2].substring(arr[2].indexOf('=')+1);
        String secretCode = arr[3].substring(arr[3].indexOf('=')+1);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
        Context context = new Context();

        String linkReset =String.format("http://localhost:8080/auth/comfirm-reset-password/%s?secretCode=%s&newPassword=%s", userId,secretCode,newPassword);

        Map<String, Object> properties = new HashMap<>();
        properties.put("linkReset", linkReset);
        context.setVariables(properties);

        helper.setFrom(emailFrom, "dducwsjvbe");
        helper.setSubject("please comfirm your account");
        helper.setTo(emailTo);
        String html = templateEngine.process("comfirm-email-reset-password.html", context);
        helper.setText(html, true);

        mailSender.send(mimeMessage);
        log.info("send mail successfully,emailTo={}", emailTo);
    }
}
