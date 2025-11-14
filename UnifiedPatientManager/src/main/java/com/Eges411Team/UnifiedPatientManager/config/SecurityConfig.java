package com.Eges411Team.UnifiedPatientManager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    //Provides the application's PasswordEncoder bean. Needed to hash passwords and OTPs.
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoder is stateless and safe to use as a singleton bean.
        return new BCryptPasswordEncoder();
    }

}
