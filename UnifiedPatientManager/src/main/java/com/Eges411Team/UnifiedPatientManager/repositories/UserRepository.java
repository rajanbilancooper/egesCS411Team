package com.Eges411Team.UnifiedPatientManager.repositories;
import org.springframework.stereotype.Repository;

import com.Eges411Team.UnifiedPatientManager.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
//Current package declaration

//Imports @Repository annotation from Spring Framework, marks the class as a DAO
import org.springframework.stereotype.Repository;

//Imports User entity class from the specified package
import com.Eges411Team.UnifiedPatientManager.entity.User;

//Imports JpaRepository interface from Spring Data JPA for CRUD operations
import org.springframework.data.jpa.repository.JpaRepository;

//Imports Optional class from Java Util package for handling nullable return values
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username); //OPTIONAL is used to avoid NullPointer Exceptions, will allow a null to be returned
    Optional<User> findById(Long id);
    boolean existsByUsername(String username);
    boolean existsById(Long id);
    
}
