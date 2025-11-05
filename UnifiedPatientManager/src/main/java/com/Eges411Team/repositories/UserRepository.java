package com.Eges411Team.repositories;
import org.springframework.stereotype.Repository;

import com.Eges411Team.UnifiedPatientManager.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer{
    Optional<User> findByUsername(String username);
    Optional<User> findById(Long id);
    boolean existsByUsername(String username);
    boolean existsById(Long id);
    
}
