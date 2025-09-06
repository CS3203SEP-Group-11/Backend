package com.levelup.notification_service.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class EmailNotificationService {

    @Value("${sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from-email}")
    private String fromEmail;

    public boolean sendEmail(String to, String subject, String content) {
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
            return response.getStatusCode() >= 200 && response.getStatusCode() < 300;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
