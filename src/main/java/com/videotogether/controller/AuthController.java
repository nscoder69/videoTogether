package com.videotogether.controller;

import com.videotogether.model.User;
import com.videotogether.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private boolean isValidUsername(String username) {
        return username != null && username.matches("^[a-zA-Z0-9_]{4,20}$");
    }

    private boolean isValidPassword(String password) {
        return password != null && password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, String> payload) {
        Map<String, String> response = new HashMap<>();
        String username = payload.get("username");
        String password = payload.get("password");
        String profilePic = payload.get("profilePic");

        if (!isValidUsername(username)) {
            response.put("error", "Username must be 4-20 characters long and contain only letters, numbers, and underscores.");
            return ResponseEntity.badRequest().body(response);
        }

        if (!isValidPassword(password)) {
            response.put("error", "Password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one number, and one special character.");
            return ResponseEntity.badRequest().body(response);
        }

        if (userRepository.findByUsername(username).isPresent()) {
            response.put("error", "Username is already taken");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        User user = new User(username, passwordEncoder.encode(password), "USER");
        if (profilePic != null && !profilePic.trim().isEmpty()) {
            user.setProfilePic(profilePic);
        }
        userRepository.save(user);

        response.put("message", "Registration successful");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        String username = payload.get("username");
        String password = payload.get("password");

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

            response.put("message", "Login successful");
            response.put("username", username);
            
            // Check if admin
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            response.put("isAdmin", isAdmin);

            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                response.put("isFrozen", user.isFrozen());
                response.put("profilePic", user.getProfilePic());
            } else {
                response.put("isFrozen", false);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        response.put("username", authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        response.put("isAdmin", isAdmin);

        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user != null) {
            response.put("isFrozen", user.isFrozen());
            response.put("profilePic", user.getProfilePic());
        } else {
            response.put("isFrozen", false);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/count")
    public ResponseEntity<Long> getUsersCount(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userRepository.count());
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, String>> updateProfile(@RequestBody Map<String, String> payload, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String currentUsername = authentication.getName();
        User user = userRepository.findByUsername(currentUsername).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String newUsername = payload.get("username");
        String newPassword = payload.get("password");
        String newProfilePic = payload.get("profilePic");

        Map<String, String> response = new HashMap<>();

        if (newUsername != null && !newUsername.trim().isEmpty() && !newUsername.equals(currentUsername)) {
            if (!isValidUsername(newUsername)) {
                response.put("error", "Username must be 4-20 characters long and contain only letters, numbers, and underscores.");
                return ResponseEntity.badRequest().body(response);
            }
            if (userRepository.findByUsername(newUsername).isPresent()) {
                response.put("error", "Username is already taken");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            user.setUsername(newUsername);
            Authentication newAuth = new UsernamePasswordAuthenticationToken(user.getUsername(), authentication.getCredentials(), authentication.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(newAuth);
        }

        if (newPassword != null && !newPassword.trim().isEmpty()) {
            if (!isValidPassword(newPassword)) {
                response.put("error", "Password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one number, and one special character.");
                return ResponseEntity.badRequest().body(response);
            }
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        if (newProfilePic != null) {
            user.setProfilePic(newProfilePic);
        }

        userRepository.save(user);
        response.put("message", "Profile updated successfully");
        response.put("username", user.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<java.util.List<User>> getAllUsers(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping("/users/{id}/freeze")
    public ResponseEntity<Map<String, String>> freezeUser(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        user.setFrozen(true);
        userRepository.save(user);
        Map<String, String> res = new HashMap<>();
        res.put("message", "User frozen");
        return ResponseEntity.ok(res);
    }

    @PostMapping("/users/{id}/unfreeze")
    public ResponseEntity<Map<String, String>> unfreezeUser(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        user.setFrozen(false);
        userRepository.save(user);
        Map<String, String> res = new HashMap<>();
        res.put("message", "User unfrozen");
        return ResponseEntity.ok(res);
    }
}
