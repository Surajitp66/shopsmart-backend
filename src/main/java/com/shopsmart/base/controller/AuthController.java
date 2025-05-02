package com.shopsmart.base.controller;

import com.shopsmart.base.dao.LoginResponse;
import com.shopsmart.base.dto.LoginRequest;
import com.shopsmart.base.dto.RegisterRequest;
import com.shopsmart.base.dto.RegisterResponse;
import com.shopsmart.base.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerUser(@RequestBody RegisterRequest request){
        RegisterResponse response = authService.registerUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginRequest request){
        LoginResponse response = authService.loginUser(request);
        return ResponseEntity.ok(response);
    }
}
