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
    
      // Find the most recent valid (unused, unexpired) OTP for a user
      Optional<OtpToken> findFirstByUser_IdAndExpiresAtAfterAndUsedFalseOrderByCreatedAtDesc(Long userId, java.time.LocalDateTime now);
   
      // Invalidates unused OTPs for the given user
      @Modifying
      @Query("UPDATE OtpToken o SET o.used = true WHERE o.user.id = :userId AND o.used = false")
      void invalidateUnusedOtps(@Param("userId") Long userId);
}

