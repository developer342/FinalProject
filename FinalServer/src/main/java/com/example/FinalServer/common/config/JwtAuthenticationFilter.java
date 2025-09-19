package com.example.FinalServer.common.config;

import com.example.FinalServer.common.util.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider; // JWT 유틸

  @Override
  protected void doFilterInternal(
          HttpServletRequest request,
          HttpServletResponse response,
          FilterChain filterChain
  ) throws ServletException, IOException {

    String header = request.getHeader("Authorization"); // 헤더읽기

    if (header != null && header.startsWith("Bearer ")) {
      String token = header.substring(7); // 토큰추출

      if (jwtTokenProvider.validateToken(token)) { // 유효검증
        Long userId = jwtTokenProvider.getUserId(token); // 사용자ID

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, null); // 인증토큰

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication); // 컨텍스트저장
      }
    }

    filterChain.doFilter(request, response); // 다음필터
  }
}
