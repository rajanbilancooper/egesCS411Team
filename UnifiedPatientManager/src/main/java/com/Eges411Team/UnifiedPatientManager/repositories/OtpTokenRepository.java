package com.Eges411Team.UnifiedPatientManager.repositories;

//Imports OtpToken entity class from specified package
import com.Eges411Team.UnifiedPatientManager.entity.OtpToken;

//Imports JpaRepository interface from Spring Data JPA for CRUD operations
import org.springframework.data.jpa.repository.JpaRepository;

//Imports Repository annotation from Spring Framework, marks the class as a DAO
import org.springframework.stereotype.Repository;

//Imports Modifying and Query annotations from Spring Data JPA for custom queries
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

//Imports optional, allos us to handle nullable return values
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    
    // Find the most recent valid OTP for a user
     @Query("SELECT o FROM OtpToken o WHERE o.user.userId = :userId " +
           "AND o.expiresAt > CURRENT_TIMESTAMP " +
           "ORDER BY o.createdAt DESC")
    Optional<OtpToken> findValidOtp(@Param("userId") Integer userId);
   
    //Invalidates unused OTPs 
    @Modifying
    @Query("UPDATE OtpToken o SET o.used = true WHERE o.user.userId = :userId " +
          " AND o.used = false")
    void invalidateUnusedOtps(@Param("userId") Integer userId);
}

