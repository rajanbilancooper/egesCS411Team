//Current package declaration
package com.Eges411Team.UnifiedPatientManager.repositories;

//Imports @Repository annotation from Spring Framework, marks the class as a DAO
import org.springframework.stereotype.Repository;

//Imports User entity class from the specified package
import com.Eges411Team.UnifiedPatientManager.entity.User;

//Imports JpaRepository interface from Spring Data JPA for CRUD operations
import org.springframework.data.jpa.repository.JpaRepository;

//Imports Optional class from Java Util package for handling nullable return values
import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username); //OPTIONAL is used to avoid NullPointer Exceptions, will allow a null to be returned
    boolean existsByUsername(String username);
    Optional<User> findByFirstNameAndLastName(String firstName, String lastName);
    Optional<User> findByFirstNameAndLastNameAndDateOfBirth(String firstName, String lastName, java.time.LocalDateTime dateOfBirth);
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstNamePart, String lastNamePart);
    Optional<User> findByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);
    
    
}
