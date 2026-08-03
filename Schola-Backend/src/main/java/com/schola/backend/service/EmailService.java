package com.schola.backend.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class EmailService {

    @Value("${sendgrid.api.key}")
    private String sendgridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    public void sendOtp(String toEmail, String otp) {
        String subject = "Your Schola Password Reset Code";

        String html =
                "<div style='font-family: Georgia, serif; max-width: 480px;" +
                        " margin: 0 auto; padding: 32px; background: #FFFDF9;'>" +
                        "<h1 style='color: #1A1560; font-size: 28px; margin: 0;'>" +
                        "schola<span style='color: #C9A96E;'>.</span></h1>" +
                        "<hr style='border: none; border-top: 1px solid #EDE8E3; margin: 20px 0;'>" +
                        "<h2 style='color: #3D2C1E; font-size: 20px;'>Password Reset Code</h2>" +
                        "<p style='color: #A0785A; font-size: 14px; line-height: 1.6;'>" +
                        "Enter this code in the Schola app to reset your password. " +
                        "This code expires in <strong>10 minutes</strong>.</p>" +
                        "<div style='background: #F5EFE6; border-radius: 16px;" +
                        " padding: 28px; text-align: center; margin: 28px 0;'>" +
                        "<p style='color: #A0785A; font-size: 12px; margin: 0 0 12px;" +
                        " text-transform: uppercase; letter-spacing: 2px;'>Your OTP Code</p>" +
                        "<span style='font-size: 44px; font-weight: 900;" +
                        " letter-spacing: 16px; color: #1A1560;'>" + otp + "</span>" +
                        "</div>" +
                        "<p style='color: #C4B5A5; font-size: 12px;'>" +
                        "If you did not request this ignore this email." +
                        " Your password will not change.</p>" +
                        "<hr style='border: none; border-top: 1px solid #EDE8E3; margin: 20px 0;'>" +
                        "<p style='color: #C4B5A5; font-size: 11px; text-align: center;'>" +
                        "© 2026 Schola. All rights reserved.</p>" +
                        "</div>";

        sendEmail(toEmail, subject, html);
    }

    public void sendPasswordResetConfirmation(String toEmail, String name) {
        String subject = "Your Schola Password Was Reset Successfully";

        String html =
                "<div style='font-family: Georgia, serif; max-width: 480px;" +
                        " margin: 0 auto; padding: 32px; background: #FFFDF9;'>" +
                        "<h1 style='color: #1A1560; font-size: 28px; margin: 0;'>" +
                        "schola<span style='color: #C9A96E;'>.</span></h1>" +
                        "<hr style='border: none; border-top: 1px solid #EDE8E3; margin: 20px 0;'>" +
                        "<h2 style='color: #3D2C1E; font-size: 20px;'>Password Reset Successful</h2>" +
                        "<p style='color: #A0785A; font-size: 14px; line-height: 1.6;'>" +
                        "Hi <strong>" + name + "</strong>, your Schola password has been " +
                        "reset successfully. You can now sign in with your new password.</p>" +
                        "<div style='background: #F5EFE6; border-radius: 16px;" +
                        " padding: 20px; margin: 24px 0; text-align: center;'>" +
                        "<span style='font-size: 32px;'>✅</span>" +
                        "<p style='color: #3D2C1E; font-weight: 700; margin: 8px 0 0;'>" +
                        "Password updated successfully</p>" +
                        "</div>" +
                        "<p style='color: #C4B5A5; font-size: 12px;'>" +
                        "If you did not do this please contact us immediately at " +
                        "support@schola.app</p>" +
                        "<hr style='border: none; border-top: 1px solid #EDE8E3; margin: 20px 0;'>" +
                        "<p style='color: #C4B5A5; font-size: 11px; text-align: center;'>" +
                        "© 2026 Schola. All rights reserved.</p>" +
                        "</div>";

        sendEmail(toEmail, subject, html);
    }

    private void sendEmail(String toEmail, String subject, String html) {
        System.out.println("=== Attempting to send email ===");
        System.out.println("From: " + fromEmail);
        System.out.println("To: " + toEmail);
        System.out.println("API Key starts with: " +
                (sendgridApiKey != null ? sendgridApiKey.substring(0, 10) + "..." : "NULL"));

        if (sendgridApiKey == null || sendgridApiKey.isBlank()
                || sendgridApiKey.equals("your_sendgrid_api_key_here")) {
            System.err.println("ERROR: SendGrid API key not configured!");
            throw new RuntimeException("SendGrid API key not configured");
        }

        if (fromEmail == null || fromEmail.isBlank()) {
            System.err.println("ERROR: From email not configured!");
            throw new RuntimeException("From email not configured");
        }

        Email from    = new Email(fromEmail, "Schola");
        Email to      = new Email(toEmail);
        Content content = new Content("text/html", html);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendgridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            System.out.println("SendGrid response status: " + response.getStatusCode());
            System.out.println("SendGrid response body: " + response.getBody());
            System.out.println("SendGrid response headers: " + response.getHeaders());

            if (response.getStatusCode() == 202) {
                System.out.println("✅ Email sent successfully to: " + toEmail);
            } else {
                System.err.println("❌ SendGrid returned error: " + response.getStatusCode());
                System.err.println("Error body: " + response.getBody());
                throw new RuntimeException(
                        "SendGrid error " + response.getStatusCode() + ": " + response.getBody()
                );
            }
        } catch (IOException e) {
            System.err.println("❌ IOException sending email: " + e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
}