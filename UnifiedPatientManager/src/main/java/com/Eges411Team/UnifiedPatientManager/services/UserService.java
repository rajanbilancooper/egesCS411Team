package com.Eges411Team.UnifiedPatientManager.services;

import com.Eges411Team.UnifiedPatientManager.entity.User;
import com.Eges411Team.UnifiedPatientManager.entity.UserSession;
import com.Eges411Team.UnifiedPatientManager.repositories.UserRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.UserSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
@Service
public class UserService {
    
    @Autowired
    // dependency injection of UserRepository - means we can use its methods here
    private UserRepository userRepository;

    @Autowired
    // dependency injection of UserSessionRepository - means we can use its methods here
    private UserSessionRepository userSessionRepository;


    // *********** use case methods for retrieving a user from the database ******************

    // finds a user by their ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // finds a user by their username
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // *********************************************************************************

    
    // public UserSession createUserSession(User user) {
        
    // }

    // *********** use case methods for saving a user to the database ******************
    
    // save a user to the database - only method needed for storing a new user
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    // *********************************************************************************


}