package com.hivetech.springbootsecurity.controller;

import com.hivetech.springbootsecurity.model.ERole;
import com.hivetech.springbootsecurity.model.Role;
import com.hivetech.springbootsecurity.model.User;
import com.hivetech.springbootsecurity.payload.request.LoginRequest;
import com.hivetech.springbootsecurity.payload.request.SignupRequest;
import com.hivetech.springbootsecurity.payload.response.JwtResponse;
import com.hivetech.springbootsecurity.payload.response.MessageResponse;
import com.hivetech.springbootsecurity.repository.RoleRepository;
import com.hivetech.springbootsecurity.repository.UserRepository;
import com.hivetech.springbootsecurity.security.jwt.JwtUtils;
import com.hivetech.springbootsecurity.service.impl.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authentication(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        try {
            if (userRepository.existsByUserName(signUpRequest.getUsername())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Username is already taken!"));
            }

            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Email is already in use!"));
            }

            // Create new user's account
            User user = new User(signUpRequest.getUsername(),
                    signUpRequest.getEmail(),
                    encoder.encode(signUpRequest.getPassword()));

            Set<String> strRoles = signUpRequest.getRole();
            Set<Role> roles = new HashSet<>();
            Role modRole = new Role();

            if (strRoles == null) {
                modRole = roleRepository.findByName(ERole.ROLE_USER);
                roles.add(modRole);
            } else {
                for (String role : strRoles) {
                    switch (role) {
                        case "admin":
                            modRole = roleRepository.findByName(ERole.ROLE_ADMIN);
                            roles.add(modRole);

                            break;
                        case "mod":
                            modRole = roleRepository.findByName(ERole.ROLE_MODERATOR);
                            roles.add(modRole);

                            break;
                        default:
                            modRole = roleRepository.findByName(ERole.ROLE_USER);
                            roles.add(modRole);
                    }
                }
            }
            user.setRoles(roles);
            user.setUserName(signUpRequest.getUsername());
            userRepository.save(user);
        } catch (Exception e) {
            new Exception("Error: Role");
        }
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}
