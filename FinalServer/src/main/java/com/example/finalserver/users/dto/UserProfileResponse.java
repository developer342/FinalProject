package com.example.FinalServer.users.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserProfileResponse {

  private Long id;
  private String email;
  private String nickname;
}
