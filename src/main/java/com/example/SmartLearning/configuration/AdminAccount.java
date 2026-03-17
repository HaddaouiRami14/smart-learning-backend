package com.example.SmartLearning.configuration;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.SmartLearning.Enum.Role;
import com.example.SmartLearning.Repository.UserRepository;
import com.example.SmartLearning.model.User;
import java.util.Optional;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminAccount {
    private final UserRepository userRepository;

    @PostConstruct
    public void createAdminaccount(){
        Optional<User> OptionalAdmin = userRepository.findByRole(Role.ADMIN);
        if(OptionalAdmin.isEmpty()){
            User admin = new User();
            admin.setUsername("Admin");
            admin.setEmail("Admin@gmail.com");
            admin.setPassword(new BCryptPasswordEncoder().encode("adminnimda"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            System.out.println("Admin account created with username 'Admin' and password 'admin'");
           }
           else{
            System.out.println("Admin account already exists.");
           }
    }
}
