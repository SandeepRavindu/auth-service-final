package com.creatorboost.auth_service.handler;

import com.creatorboost.auth_service.entiy.UserEntity;
import com.creatorboost.auth_service.entiy.UserRole;
import com.creatorboost.auth_service.repository.UserRepository;
import com.creatorboost.auth_service.service.AppUserDetailsService;
import com.creatorboost.auth_service.service.ProfileServiceImpl;
import com.creatorboost.auth_service.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private static final Logger logger = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);
    private final UserRepository userRepository;
    private final AppUserDetailsService userDetailsService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        logger.info("OAuth2 login successful");
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            email = oAuth2User.getAttribute("login"); // GitHub username as fallback
        }

        String name = oAuth2User.getAttribute("name");
        if (name == null) {
            name = oAuth2User.getAttribute("login"); // GitHub username as fallback for name
        }

        String imageUrl = oAuth2User.getAttribute("picture"); // Google
        if (imageUrl == null) {
            imageUrl = oAuth2User.getAttribute("avatar_url"); // GitHub
        }
        logger.info("Authenticated OAuth2 user: email={}, name={}, imageUrl={}", email, name, imageUrl);

        // Check if user exists
        // Check if user exists
        String finalName = name;
        String finalEmail = email;
        String finalImageUrl = imageUrl;
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    // Create new user
                    UserEntity newUser = UserEntity.builder()
                            .userId(UUID.randomUUID().toString())
                            .email(finalEmail)
                            .name(finalName)
                            .imageUrl(finalImageUrl)
                            .role(UserRole.CLIENT) // Default role
                            .password("") // No password for OAuth2
                            .isAccountVerified(true)
                            .createdAt(Instant.now())
                            .build();
                    userRepository.save(newUser);
                    logger.info("New OAuth2 user created: {}", finalEmail);
                    return newUser;
                });

        // Load UserDetails
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // Generate JWT including role
        String jwt = jwtUtil.generateToken(userDetails, userEntity.getRole().name());

        // Add JWT as HttpOnly cookie
        Cookie cookie = new Cookie("jwt", jwt);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 10); // 10 hours
        cookie.setSecure(false); // Use true if using HTTPS
        response.addCookie(cookie);
        logger.info("JWT cookie set for: {}", email);

        // Redirect user to frontend
        response.sendRedirect("http://localhost:5173");
    }
}