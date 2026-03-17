package com.example.SmartLearning.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.SmartLearning.DTO.ChangePasswordRequest;
import com.example.SmartLearning.DTO.FormateurProfileResponse;
import com.example.SmartLearning.DTO.UpdateFormateurProfileRequest;
import com.example.SmartLearning.Enum.Role;
import com.example.SmartLearning.Repository.FormateurRepository;
import com.example.SmartLearning.Repository.UserRepository;
import com.example.SmartLearning.model.Formateur;
import com.example.SmartLearning.model.User;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FormateurProfileService {

    private final UserRepository userRepository;
    private final FormateurRepository formateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    public FormateurProfileResponse getProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.FORMATEUR) {
            throw new RuntimeException("Access denied");
        }

        Formateur formateur = formateurRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new RuntimeException("Formateur not found"));

        return FormateurProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .picture(user.getPicture())
                .bio(formateur.getBio())
                .provider(user.getProvider() != null ? user.getProvider().name() : "LOCAL")
                .specialization(formateur.getSpecialization())
                .build();
    }

    @Transactional
    public FormateurProfileResponse updateProfile(String email,
            UpdateFormateurProfileRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.FORMATEUR) {
            throw new RuntimeException("Access denied");
        }

        Formateur formateur = formateurRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new RuntimeException("Formateur not found"));

        formateur.setBio(request.getBio());
        formateur.setSpecialization(request.getSpecialization());

        formateurRepository.save(formateur);

        return getProfile(email);
    }

    public void changePassword(String email, ChangePasswordRequest request) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
        throw new RuntimeException("Current password is incorrect");
    }

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
// ✅ Envoie email de confirmation
    sendPasswordChangedEmail(user.getEmail(), user.getUsername());
}

private void sendPasswordChangedEmail(String email, String username) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(email);
    message.setSubject("Password Changed Successfully - SkillPath");
    message.setText(
        "Hello " + username + ",\n\n" +
        "Your password has been changed successfully.\n\n" +
        "If you did not make this change, please contact support immediately.\n\n" +
        "Best regards,\nSkillPath Team"
    );
    mailSender.send(message);
}
}
