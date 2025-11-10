package com.Eges411Team.UnifiedPatientManager.repositories;

import com.Eges411Team.UnifiedPatientManager.entity.User;
import com.Eges411Team.UnifiedPatientManager.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


//Imports user entity class and UserSession entity class from specified packages
import com.Eges411Team.UnifiedPatientManager.entity.User;
import com.Eges411Team.UnifiedPatientManager.entity.UserSession;

//Imports JpaRepo from Spring framework, allows the class to with CRUD features
import org.springframework.data.jpa.repository.JpaRepository;

//Imports Modifying and Query annotations from Spring Data JPA for custom queries
import org.springframework.data.jpa.repository.Modifying;

//Allows us to define custom Queries with the Query annotation
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

//Imports Repository annotation from Spring Framework, marks the class as a DAO
import org.springframework.stereotype.Repository;

//Imports standard list interface from java, allows us to use lists 
import java.util.List;

//Allows return types to be null without causing NullPointerExceptions
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Integer> {
    
    // Find all sessions for a specific user
    List<UserSession> findByUserAndActive(User user, boolean active);
    
    // Find an active session by its token
    Optional<UserSession> findBySessionTokenAndActive(String sessionToken, boolean active);
    
    //Deactivate all sessions for a specific user
    @Modifying
    @Query("UPDATE UserSession s SET s.active = false WHERE s.user.userId = :userId")
    void deactivateAllUserSessions(@Param("userId") Integer userId);

    //Deactivate all sessions
    @Modifying
    @Query("UPDATE UserSession s SET s.active = false")
    void deactivateAllSessions();
    
}
