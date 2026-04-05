package com.example.SmartLearning.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.SmartLearning.Enum.Role;
import com.example.SmartLearning.model.User;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    
    @Value("${app.email.from}")
    private String fromEmail;
    
    @Value("${app.name}")
    private String appName;

    @Value("${app.frontend.url}")
    private String frontendUrl;


    public void sendPasswordResetEmail(String toEmail, String resetToken) {
            String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;

            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(fromEmail);
                helper.setTo(toEmail);
                helper.setSubject("Password Reset Request - SkillPath");

                String htmlContent = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "<style>" +
                    "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                    ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                    ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
                    ".content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }" +
                    ".button { display: inline-block; padding: 15px 30px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; text-decoration: none; border-radius: 5px; font-weight: bold; margin: 20px 0; }" +
                    ".footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }" +"</style>" +
                    "</head>" +
                    "<body>" +
                    "<div class='container'>" +
                    "<div class='header'>" +
                    "<h1>🔐 Password Reset Request</h1>" +
                    "</div>" +
                    "<div class='content'>" +
                    "<p>Hello,</p>" +
                    "<p>You requested to reset your password for your SkillPath account.</p>" +
                    "<p>Click the button below to reset your password:</p>" +
                    "<p style='text-align: center;'>" +
                    "<a href='" + resetUrl + "' class='button'>Reset Password</a>" +
                    "</p>" +
                    "<p><strong>This link will expire in 1 hour.</strong></p>" +
                    "<p>If you didn't request this password reset, please ignore this email. Your password will remain unchanged.</p>" +
                    "<div class='footer'>" +
                    "<p>Best regards,<br>The SkillPath Team</p>" +
                    "<p>This is an automated message, please do not reply.</p>" +
                    "</div>" +
                    "</div>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    
    public void sendWelcomeEmailSimple(User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Bienvenue sur " + appName + " !");
            
            String emailBody = String.format(
                "Bonjour %s,\n\n" +
                "Bienvenue sur %s !\n\n" +
                "Votre compte a été créé avec succès via Google.\n" +
                "Rôle : %s\n\n" +
                "Merci de nous rejoindre !\n\n" +
                "Cordialement,\n" +
                "L'équipe %s",
                user.getUsername(),
                appName,
                user.getRole(),
                appName
            );
            
            message.setText(emailBody);
            mailSender.send(message);
            
            System.out.println("Email de bienvenue envoyé à : " + user.getEmail());
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
        }
    }
    
    
    public void sendWelcomeEmailHTML(User user) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("🎉 Bienvenue sur " + appName + " !");

            String dashboardUrl = getDashboardUrl(user);
            
            
            String htmlContent = String.format(
                            "<html>" +
                "<body style='margin: 0; padding: 0; background-color: #f4f7fa; font-family: -apple-system, BlinkMacSystemFont, Segoe UI, Roboto, Helvetica Neue, Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);'>" +
                
                "<!-- Header avec gradient -->" +
                "<div style='background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px 30px; text-align: center;'>" +
                "<h1 style='color: #ffffff; margin: 0; font-size: 28px; font-weight: 600;'>🎉 Bienvenue sur %s !</h1>" +
                "</div>" +
                
                "<!-- Contenu principal -->" +
                "<div style='padding: 40px 30px;'>" +
                "<p style='font-size: 16px; color: #333; margin: 0 0 20px;'>Bonjour <strong style='color: #667eea;'>%s</strong>,</p>" +
                "<p style='font-size: 15px; color: #555; line-height: 1.6; margin: 0 0 30px;'>Votre compte a été créé avec succès via <strong>Google</strong>. Nous sommes ravis de vous compter parmi nous ! 🚀</p>" +
                
                "<!-- Carte d'informations -->" +
                "<div style='background: linear-gradient(135deg, #f5f7fa 0%%, #c3cfe2 100%%); padding: 25px; border-radius: 12px; margin: 30px 0; border-left: 4px solid #667eea;'>" +
                "<p style='margin: 0 0 12px; font-size: 14px; color: #666;'><strong style='color: #333; font-size: 15px;'>📧 Email :</strong> <span style='color: #667eea;'>%s</span></p>" +
                "<p style='margin: 0; font-size: 14px; color: #666;'><strong style='color: #333; font-size: 15px;'>👤 Rôle :</strong> <span style='color: #667eea;'>%s</span></p>" +
                "</div>" +
                
                "<!-- Bouton CTA -->" +
                "<div style='text-align: center; margin: 40px 0;'>" +
                "<a href='%s' style='display: inline-block; padding: 16px 40px; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: #ffffff; text-decoration: none; border-radius: 50px; font-weight: 600; font-size: 16px; box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4); transition: transform 0.3s ease;'>" +
                "✨ Accéder à mon tableau de bord" +
                "</a>" +
                "</div>" +
                
                "<p style='font-size: 15px; color: #555; margin: 30px 0 0; text-align: center;'>Merci de nous rejoindre ! 💜</p>" +
                "</div>" +
                
                "<!-- Footer -->" +
                "<div style='background-color: #f8f9fa; padding: 30px; text-align: center; border-top: 1px solid #e9ecef;'>" +
                "<p style='margin: 0; font-size: 13px; color: #6c757d; line-height: 1.8;'>" +
                "Cordialement,<br>" +
                "<strong style='color: #667eea;'>L'équipe %s</strong>" +
                "</p>" +
                "</div>" +
                
                "</div>" +
                "</body>" +
                "</html>",
                appName,
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                dashboardUrl,
                appName
            );
            
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
            
            System.out.println("Email HTML de bienvenue envoyé à : " + user.getEmail());
            
        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'email HTML : " + e.getMessage());
            sendWelcomeEmailSimple(user);
        }
    }
  
   private String getDashboardUrl(User user) {
        Role role = user.getRole();
        String path = (role == Role.FORMATEUR) ? "/trainer" : "/dashboard"; 
        return frontendUrl + path ;
    }

    

}
