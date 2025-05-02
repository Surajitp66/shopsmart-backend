package com.shopsmart.base.service;

import com.shopsmart.base.dao.LoginResponse;
import com.shopsmart.base.dto.LoginRequest;
import com.shopsmart.base.dto.RegisterRequest;
import com.shopsmart.base.dto.RegisterResponse;
import com.shopsmart.base.exception.EmailAlreadyExistsException;
import com.shopsmart.base.exception.InvalidCredentialsException;
import com.shopsmart.base.model.Role;
import com.shopsmart.base.model.User;
import com.shopsmart.base.repository.UserRepository;
import com.shopsmart.base.utill.JwtUtil;
import com.shopsmart.base.utill.UserMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class AuthenticationService {

    private static final Logger log = LogManager.getLogger(AuthenticationService.class);
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    // Register a new user
    public RegisterResponse registerUser(RegisterRequest request){
        log.info("Attempting to register user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email already registered - {}", request.getEmail());
            throw new EmailAlreadyExistsException("Email already registered");
        }

        User user = UserMapper.mapToUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Collections.singleton(Role.USER)); // Default role

        userRepository.save(user);
        log.info("User registered successfully with email: {}", request.getEmail());

        return new RegisterResponse("User registered successfully");
    }

    // Login existing user
    public LoginResponse loginUser(LoginRequest request){
        log.info("Login attempt for email: {}", request.getEmail());

        try {
            // Trigger authentication
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),request.getPassword()
                    )
            );
            log.info("Authentication successful for email: {}", request.getEmail());
        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials for email: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        } catch (Exception e) {
            log.error("Unexpected error during authentication", e);
            throw new RuntimeException("Authentication failed due to internal error: " + e.getMessage(),e);
        }


        UserDetails userDetails= userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtUtil.generateToken(userDetails.getUsername());

        return new LoginResponse(token);
    }
}
