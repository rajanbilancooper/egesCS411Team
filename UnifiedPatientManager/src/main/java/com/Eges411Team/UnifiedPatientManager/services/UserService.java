package com.Eges411Team.UnifiedPatientManager.services;

import com.Eges411Team.UnifiedPatientManager.entity.User;
import com.Eges411Team.UnifiedPatientManager.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class UserService {
    
    @Autowired
    // dependency injection of UserRepository - means we can use its methods here
    private UserRepository userRepository;

    // *********** use case methods for retrieving a user from the database and editing ******************

    // finds a user by their ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // finds a user by their username
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // update a user's information in the database - only a Doctor can perform this action
    // must run authorization check in the controller before calling this method
    public User updateUser(User user) {
        return userRepository.save(user);
    }


    // *********************************************************************************

    // *********** use case methods for saving a user to the database ******************
    
    // save a user to the database - only method needed for storing a new user
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    // *********************************************************************************

    // ** all methods for user sessions will be in their own service class ** 


}