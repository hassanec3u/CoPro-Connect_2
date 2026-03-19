package com.copro.connect.service;

import com.copro.connect.exception.EmailSendException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${mfa.sender.email}")
    private String senderEmail;

    @Value("${mfa.sender.name}")
    private String senderName;

    @Async
    public void sendMfaCode(String toEmail, String code, String userName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail, senderName);
            helper.setTo(toEmail);
            helper.setSubject("CoPro Connect — Verification Code");

            String htmlContent = buildMfaEmailHtml(code, userName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("MFA code sent to {}", maskEmail(toEmail));
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send MFA email to {}", maskEmail(toEmail), e);
            throw new EmailSendException("Unable to send verification email", e);
        }
    }

    private String buildMfaEmailHtml(String code, String userName) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="margin:0;padding:0;font-family:'Segoe UI',Arial,sans-serif;background:#f4f6f8;">
              <div style="max-width:480px;margin:40px auto;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,0.08);">
                <div style="background:#2563eb;padding:28px 32px;text-align:center;">
                  <h1 style="color:#ffffff;margin:0;font-size:22px;">🏢 CoPro Connect</h1>
                </div>
                <div style="padding:32px;">
                  <p style="color:#374151;font-size:15px;margin:0 0 8px;">Hello <strong>%s</strong>,</p>
                  <p style="color:#6b7280;font-size:14px;margin:0 0 24px;">Here is your verification code to sign in:</p>
                  <div style="background:#f0f5ff;border:2px solid #2563eb;border-radius:10px;padding:20px;text-align:center;margin:0 0 24px;">
                    <span style="font-size:36px;font-weight:700;letter-spacing:8px;color:#1e40af;">%s</span>
                  </div>
                  <p style="color:#6b7280;font-size:13px;margin:0 0 6px;">⏱ This code expires in <strong>5 minutes</strong>.</p>
                  <p style="color:#9ca3af;font-size:12px;margin:0;">If you did not request this code, please ignore this email.</p>
                </div>
                <div style="background:#f9fafb;padding:16px 32px;text-align:center;border-top:1px solid #e5e7eb;">
                  <p style="color:#9ca3af;font-size:11px;margin:0;">CoPro Connect — Property Management</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(userName, code);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@");
        String name = parts[0];
        if (name.length() <= 2) return name.charAt(0) + "***@" + parts[1];
        return name.substring(0, 2) + "***@" + parts[1];
    }
}
