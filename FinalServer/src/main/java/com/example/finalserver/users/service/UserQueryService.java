package com.example.FinalServer.users.service;


import com.example.FinalServer.common.exception.InvalidCredentialsException;
import com.example.FinalServer.domain.user.entity.User;
import com.example.FinalServer.domain.user.repository.UserRepository;
import com.example.FinalServer.users.dto.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserQueryService {

  private final UserRepository userRepository; // 유저 리포지토리

  // 내 정보 조회
  public UserProfileResponse getMe(Long userId) {
    User user = userRepository.findById(userId)
                              .orElseThrow(() -> new InvalidCredentialsException("인증 정보를 확인할 수 없습니다."));
    return new UserProfileResponse(user.getId(), user.getEmail());
  }
}
