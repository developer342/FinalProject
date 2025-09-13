package com.example.FinalServer.users.controller;

import com.example.FinalServer.common.exception.InvalidCredentialsException;
import com.example.FinalServer.domain.user.entity.User;
import com.example.FinalServer.domain.user.repository.UserRepository;
import com.example.FinalServer.users.dto.UserProfileResponse;
import com.example.FinalServer.users.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersController {

  private final UserQueryService userQueryService; // 서비스 주입

  @GetMapping("/me") // 내 정보 조회
  public UserProfileResponse me(Authentication authentication) {
    Long userId = (Long) authentication.getPrincipal(); // Bearer → userId
    return userQueryService.getMe(userId); // 서비스 위임
  }
}
