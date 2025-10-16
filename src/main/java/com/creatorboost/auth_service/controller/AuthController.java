package com.creatorboost.auth_service.controller;

import com.creatorboost.auth_service.entiy.UserEntity;
import com.creatorboost.auth_service.io.AuthRequest;
import com.creatorboost.auth_service.io.AuthResponse;
import com.creatorboost.auth_service.repository.UserRepository;
import com.creatorboost.auth_service.service.AppUserDetailsService;
import com.creatorboost.auth_service.service.ProfileService;
import com.creatorboost.auth_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.View;

import java.util.HashMap;
import java.util.Map;



@RestController
@RequiredArgsConstructor

public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final View error;
    private final AppUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final ProfileService profileService;
    private final UserRepository userRepository;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request){
        try {
            authenticate(request.getEmail(), request.getPassword());
            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
            // Fetch user entity to get role
            UserEntity userEntity = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            // Check if user is suspended
            if (userEntity.isSuspended()) {
                Map<String,Object> map = new HashMap<>();
                map.put("error", true);
                map.put("message", "User account is suspended");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(map);
            }


            String jwt = jwtUtil.generateToken(userDetails, userEntity.getRole().name());
            ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(60 * 60 * 10) // 10 hours
                    .sameSite("Strict")
                    //.sameSite("None") // For cross-site requests, ensure to use Secure flag in production
                    .build();
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new AuthResponse(userDetails.getUsername(), jwt, userEntity.getRole().name()));

        }catch (BadCredentialsException ex){
            Map<String,Object> map = new HashMap<>();
            map.put("error",true);
            map.put("message", "Invalid credentials");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }catch (DisabledException ex) {
            Map<String, Object> map = new HashMap<>();
            map.put("error", true);
            map.put("message", "User is disabled");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }catch (Exception ex) {
            Map<String, Object> map = new HashMap<>();
            map.put("error", true);
            map.put("message", "An error occurred during authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

    }

    private void authenticate(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }

    @GetMapping("/is-authenticated")
    public ResponseEntity<?> isAuthenticated(@CurrentSecurityContext(expression = "authentication.name") String email) {
       return  ResponseEntity.ok(email != null);

    }

    @PostMapping("/send-reset-otp")
    public void sendResetOtp(@RequestParam("email") String email) {
       try{
           profileService.sendResetOtp(email);
       }catch(Exception e){
              throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to send reset OTP", e);
       }
    }

    @PostMapping("/send-otp")
    public void sendVerifyOtp(@CurrentSecurityContext(expression = "authentication.name") String email) {
        try {
            profileService.sendOtp(email);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to send OTP", e);
        }
    }

    @PostMapping("/verify-otp")
    public void verifyOtp( @RequestBody Map<String,Object> request,
            @CurrentSecurityContext(expression = "authentication.name") String email) {

        if(request.get("otp").toString()==null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing OTP");
        }

        try {
            profileService.verifyOtp(email, request.get("otp").toString());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP", e);
        }
    }

}
