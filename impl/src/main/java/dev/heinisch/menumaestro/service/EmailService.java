package dev.heinisch.menumaestro.service;

import dev.heinisch.menumaestro.properties.EmailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${spring.application.uri}")
    private String appUrl;

    private final EmailProperties emailProperties;
    private final JavaMailSender mailSender;

    public void sendPasswordResetEmail(String to, String token) {

        String subject = "Password Reset Request";
        String message = """
                <html>
                <body>
                    <h2>Password Reset Request</h2>
                    <p>We received a request to reset your password. You can reset your password using the following link:</p>
                    <a href=\"""" + appUrl + "/reset-password?token=" + token + "\">Reset Password</a>" + """
                    <p>If you did not request a password reset, please ignore this email or contact support if you have questions.</p>
                    <br>
                    <p>Thank you,</p>
                    <p><strong>The MenuMaestro Team</strong></p>
                </body>
                </html>
                """;

        sendEmail("password reset", to, subject, message, true);
    }

    public void sendEmailConfirmationEmail(String to, String username, String token) {
        String subject = "Confirm Your Email";
        String message = """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif;">
                <h2>Email Confirmation</h2>
                <p>We received a request to confirm your email address. Use the code below:</p>
                <!-- 8-digit code in boxes -->
                <div style="margin: 16px 0;">
                <span style="display:inline-block; width:40px; height:40px; border:1px solid #ccc; text-align:center; line-height:40px; font-size:18px; font-weight:bold; margin-right:4px;">""" + token.charAt(0) + "</span>" + """
                <span style="display:inline-block; width:40px; height:40px; border:1px solid #ccc; text-align:center; line-height:40px; font-size:18px; font-weight:bold; margin-right:4px;">""" + token.charAt(1) + "</span>" + """
                <span style="display:inline-block; width:40px; height:40px; border:1px solid #ccc; text-align:center; line-height:40px; font-size:18px; font-weight:bold; margin-right:4px;">""" + token.charAt(2) + "</span>" + """
                <span style="display:inline-block; width:40px; height:40px; border:1px solid #ccc; text-align:center; line-height:40px; font-size:18px; font-weight:bold; margin-right:4px;">""" + token.charAt(3) + "</span>" + """               
                <span style="display:inline-block; width:40px; height:40px; border:1px solid #ccc; text-align:center; line-height:40px; font-size:18px; font-weight:bold; margin-right:4px;">""" + token.charAt(4) + "</span>" + """
                <span style="display:inline-block; width:40px; height:40px; border:1px solid #ccc; text-align:center; line-height:40px; font-size:18px; font-weight:bold; margin-right:4px;">""" + token.charAt(5) + "</span>" + """
                <span style="display:inline-block; width:40px; height:40px; border:1px solid #ccc; text-align:center; line-height:40px; font-size:18px; font-weight:bold; margin-right:4px;">""" + token.charAt(6) + "</span>" + """
                <span style="display:inline-block; width:40px; height:40px; border:1px solid #ccc; text-align:center; line-height:40px; font-size:18px; font-weight:bold;">""" + token.charAt(7) + "</span>" + """
                </div>
                
                
                <p>Or click the link below to confirm your email:</p>
                """ + "<a href=\"" + appUrl + "/confirm-email?username=" + username + "&token=" + token + ">Confirm Email</a>" + """
                
                
                <p>If you did not request this, please ignore this email or contact support if you have questions.</p>
                <br>
                <p>Thank you,</p>
                <p><strong>The MenuMaestro Team</strong></p>
                </body>
                </html>
                """;
        sendEmail("email confirmation email", to, subject, message, true);
    }

    public void sendIngredientRejectNotification(String to, String ingredient, String replacement) {
        String subject = "Ingredient Request Rejected";
        ingredient = StringEscapeUtils.escapeHtml4(ingredient);
        replacement = StringEscapeUtils.escapeHtml4(replacement);
        String descriptionLine = replacement != null
                ? "<p>Ingredient '%s' was replaced with '%s'</p>".formatted(ingredient, replacement)
                : "Ingredient '%s' was removed without a replacement ingredient.".formatted(ingredient);
        String message = """
                <html>
                <body>
                    <h2>Ingredient Request Rejected</h2>
                    <p>Your request for an ingredient was rejected by an administrator.</p>
                """ + descriptionLine +
                """
                            <p>Please check all recipes where you used the ingredient.</p>
                            <br>
                            <p>Thank you,</p>
                            <p><strong>The MenuMaestro Team</strong></p>
                        </body>
                        </html>
                        """;
        sendEmail("ingredient rejected", to, subject, message, true);
    }

    public void ingredientAcceptNotification(String to, String ingredient) {
        String subject = "Ingredient Request Accepted";
        ingredient = StringEscapeUtils.escapeHtml4(ingredient);
        String message = """
                <html>
                <body>
                    <h2>Ingredient Request Accepted</h2>
                    <p>Your request for an ingredient was accepted by an administrator. The ingredient is now globally visible and can be used normally.</p>
                <p>Ingredient:\s""" + ingredient +
                """
                            </p>
                            <br>
                            <p><strong>The MenuMaestro Team</strong></p>
                        </body>
                        </html>
                        """;
        sendEmail("ingredient accepted", to, subject, message, true);
    }


    private void sendEmail(String mailMessageKind, String to, String subject, String message, boolean isHtml) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(to);
            helper.setFrom(emailProperties.getUsername());
            helper.setSubject(subject);
            helper.setText(message, true);

            mailSender.send(mimeMessage);
            log.info("Successfully send email of kind {} to {}", mailMessageKind, to);
        } catch (MessagingException | MailSendException e) {
            log.error("Failed to send email of kind {} to {}. Exception: {}", mailMessageKind, to, e.getMessage());
        }
    }
}
