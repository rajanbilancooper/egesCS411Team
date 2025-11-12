package com.Eges411Team.UnifiedPatientManager.controller;

import com.Eges411Team.UnifiedPatientManager.DTOs.responses.UserResponseDTO;
import com.Eges411Team.UnifiedPatientManager.entity.User;
import com.Eges411Team.UnifiedPatientManager.services.UserService;
import com.Eges411Team.UnifiedPatientManager.DTOs.requests.UserRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

// REST controller for user-related endpoints
@RestController
@RequestMapping("/api/users")
public class UserController {   
    @Autowired
    // dependency injection of UserService - means we can use its methods here
    private UserService userService;

    // Endpoint to get user by ID
    @GetMapping("/{id}")
    // ResponseEntity is a Spring class that represents an HTTP response, including status code, headers, and body -
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        // use the userService to get the user by ID
        Optional<User> user = userService.getUserById(id);
        
        // if user is found, return 200 OK with UserResponseDTO, else return 404 Not Found
        return user.map(u -> ResponseEntity.ok(new UserResponseDTO(u.getId(), u.getUsername(), u.getEmail(), u.getFirstName(), u.getLastName())))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Endpoint to get user by username
    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponseDTO> getUserByUsername(@PathVariable String username) {

        // use the userService to get the user by username -- optional bc user may not exist and return null
        Optional<User> user = userService.getUserByUsername(username);

        // returns 200 OK with UserResponseDTO if user is found, else 404 Not Found
        return user.map(u -> ResponseEntity.ok(new UserResponseDTO(u.getId(), u.getUsername(), u.getEmail(), u.getFirstName(), u.getLastName())))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Endpoint to create a new user
    @PostMapping
    // postmapping means this method handles POST requests to /api/users
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserRequestDTO userRequestDTO) {
        //create a user
        User user = new User();

        // set the user fields from the request DTO
        user.setUsername(userRequestDTO.getUsername());
        user.setEmail(userRequestDTO.getEmail());
        user.setPassword(userRequestDTO.getPassword());
        user.setFirstName(userRequestDTO.getFirstName());
        user.setLastName(userRequestDTO.getLastName());

        // save the user using the userService
        User savedUser = userService.saveUser(user);

        // return 200 OK with the saved user's details
        return ResponseEntity.ok(new UserResponseDTO(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail(), 
            savedUser.getFirstName(), savedUser.getLastName()));
    }

    // method to update user information
    // TODO - add authorization checks to ensure only authorized users (e.g., Doctors) can update user info
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @RequestBody UserRequestDTO userRequestDTO) {
        // retrieve the existing user by ID
        Optional<User> existingUserOpt = userService.getUserById(id);

        // check if the user we want exists
        if (existingUserOpt.isEmpty()) {
            // if user not found, return 404 Not Found
            return ResponseEntity.notFound().build();
        }

        // update the existing user's information using the data from the request DTO, even if unchanged
        User existingUser = existingUserOpt.get();
        existingUser.setUsername(userRequestDTO.getUsername());
        existingUser.setEmail(userRequestDTO.getEmail());
        existingUser.setPassword(userRequestDTO.getPassword());
        existingUser.setFirstName(userRequestDTO.getFirstName());
        existingUser.setLastName(userRequestDTO.getLastName());

        // phone number update
        existingUser.setPhoneNumber(userRequestDTO.getPhoneNumber());

        // address?
        existingUser.setAddress(userRequestDTO.getAddress());

        // save the updated user
        User updatedUser = userService.updateUser(existingUser);

        // return 200 OK with the updated user's details - only the fields in UserResponseDTO - we don't want to
        // show password hash or other sensitive info like phone number and address
        return ResponseEntity.ok(new UserResponseDTO(updatedUser.getId(), updatedUser.getUsername(), updatedUser.getEmail(),
            updatedUser.getFirstName(), updatedUser.getLastName()));
    }

    // what if we want to update a prescription? -- done in PrescriptionController?
}