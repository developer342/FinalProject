package com.example.FinalServer.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter; // JWT 인증필터

    @Bean // 비밀번호 인코더
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * JWT 기반 보안 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 엑셀/CSV 내보내기는 인증 없이 허용
                        .requestMatchers(
                                "/api/entries/export",
                                "/api/entries/export.xlsx"
                        ).permitAll()

                        // 공개 엔드포인트
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/users/**",
                                "/api/admin/import/**",
                                "/api/products/**",
                                "/api/gpt/refine"
                        ).permitAll()

                        // 나머지 /api/**는 인증 필요
                        .requestMatchers("/api/**").authenticated()

                        // 기타 요청도 인증 필요
                        .anyRequest().authenticated()
                )
                // JWT 필터 등록
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    /**
     * AuthenticationManager Bean (로그인 시 필요)
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * 보안 필터 체인에서 완전히 제외할 엔드포인트
     * (엑셀/CSV 다운로드는 스트림 응답이라 Security 필터와 충돌 방지)
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/api/entries/export",
                "/api/entries/export.xlsx"
        );
    }
}
