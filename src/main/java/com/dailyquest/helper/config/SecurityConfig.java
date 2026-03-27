package com.dailyquest.helper.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 지금 구조는 자체 API + 정적 html 방식이라 CSRF는 끄는 편이 편함
                .csrf(AbstractHttpConfigurer::disable)

                // formLogin을 끄지 않으면 Spring Security 기본 로그인 페이지가 뜸
                .formLogin(AbstractHttpConfigurer::disable)

                // 기본 로그아웃 페이지도 끔 (우리는 /api/auth/logout 사용)
                .logout(AbstractHttpConfigurer::disable)

                // 필요 시 iframe/H2 콘솔 용도 아니면 굳이 없어도 되지만 무난하게 둠
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                // 세션은 사용하되 Spring Security 로그인 세션이 아니라 우리가 직접 HttpSession 사용
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                // 인증 안 된 API 접근 시 기본 로그인 페이지로 보내지 말고 401 반환
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new HttpStatusEntryPoint(UNAUTHORIZED))
                )

                .authorizeHttpRequests(auth -> auth
                        // 정적 페이지 허용
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/login.html",
                                "/settings.html",
                                "/favicon.ico"
                        ).permitAll()

                        // 정적 리소스 허용
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/static/**"
                        ).permitAll()

                        // 인증 관련 API 허용
                        .requestMatchers("/api/auth/**").permitAll()

                        // 게임/퀘스트 API도 허용
                        // 실제 로그인 필요 여부는 Controller/UserService 로직에서 처리
                        .requestMatchers("/api/games/**", "/api/quests/**").permitAll()

                        // OPTIONS 요청 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 나머지도 우선 허용
                        .anyRequest().permitAll()
                )

                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}