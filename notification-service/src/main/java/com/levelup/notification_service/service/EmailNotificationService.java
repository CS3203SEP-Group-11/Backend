package com.levelup.notification_service.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;

@Service
@Slf4j
public class EmailNotificationService {

    @Value("${sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from-email}")
    private String fromEmail;

    public boolean sendEmail(String to, String subject, String content) {
        log.info("Attempting to send email to: {}, subject: {}", to, subject);
        log.info("Using SendGrid API key: {}...", sendGridApiKey != null ? sendGridApiKey.substring(0, Math.min(10, sendGridApiKey.length())) : "NULL");
        log.info("From email: {}", fromEmail);
        
        Email fromEmailObj = new Email(fromEmail);
        Email toEmail = new Email(to);
        Content emailContent = new Content("text/plain", content);
        Mail mail = new Mail(fromEmailObj, subject, toEmail, emailContent);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            
            log.info("SendGrid response status: {}", response.getStatusCode());
            log.info("SendGrid response body: {}", response.getBody());
            log.info("SendGrid response headers: {}", response.getHeaders());
            
            boolean success = response.getStatusCode() >= 200 && response.getStatusCode() < 300;
            if (!success) {
                log.error("Failed to send email via SendGrid. Status: {}, Body: {}", response.getStatusCode(), response.getBody());
            }
            log.info("Email send result: {}", success ? "SUCCESS" : "FAILED");
            
            return success;
        } catch (IOException ex) {
            log.error("SendGrid IOException: ", ex);
            return false;
        } catch (Exception ex) {
            log.error("SendGrid unexpected error: ", ex);
            return false;
        }
    }
}
