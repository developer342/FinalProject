package com.example.FinalServer.auth.entity.user.repository;

import com.example.FinalServer.auth.entity.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);
}
