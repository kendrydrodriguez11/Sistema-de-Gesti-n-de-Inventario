package com.example.auth.controller;

import com.example.auth.controller.required.LoginUser;
import com.example.auth.service.AuthService;
import com.example.auth.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    private final S3Service s3Service;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam(value = "bucket", required = false, defaultValue = "my-inventory-bucket") String bucketName) {
        try {
            logger.info("Registering user: {} with email: {}", username, email);
            Map<String, Object> urlCreated = authService.registerUser(username, email, password, bucketName);
            logger.info("User registered successfully: {}", username);
            return ResponseEntity.ok(urlCreated);

        } catch (RuntimeException e) {
            logger.error("Error in register: {} | Localized: {}", e.getMessage(), e.getLocalizedMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", "error",
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Unexpected error in register", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "error", "Error interno del servidor"
            ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> authenticate(@RequestBody LoginUser user) {
        try {
            logger.info("Login attempt for user: {}", user.getUsername());
            String token = authService.authenticateUser(user);
            Map<String, String> response = new HashMap<>();
            response.put("token", token);

            logger.info("Login successful for user: {}", user.getUsername());
            return ResponseEntity.ok()
                    .header("Authorization", "Bearer " + token)
                    .body(response);

        } catch (Exception e) {
            logger.warn("Authentication failed for user {}: {}", user.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales inválidas"));
        }
    }

    /**
     * Genera una presigned URL para que el frontend suba un archivo directamente a S3.
     * El archivo nunca pasa por el servidor.
     * Requiere token JWT válido (ruta protegida).
     */
    @GetMapping("/presigned-url/upload")
    public ResponseEntity<Map<String, String>> getUploadUrl(
            @RequestParam String bucket,
            @RequestParam String key) {
        try {
            String url = s3Service.generatePresignedPutUrl(bucket, key);
            String fileUrl = "https://" + bucket + ".s3.amazonaws.com/" + key;
            return ResponseEntity.ok(Map.of(
                    "uploadUrl", url,
                    "fileUrl", fileUrl
            ));
        } catch (Exception e) {
            logger.error("Error generating presigned upload URL", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudo generar la URL de subida"));
        }
    }

    /**
     * Genera una presigned URL para que el frontend descargue/visualice un archivo de S3.
     * Requiere token JWT válido (ruta protegida).
     */
    @GetMapping("/presigned-url/download")
    public ResponseEntity<Map<String, String>> getDownloadUrl(
            @RequestParam String bucket,
            @RequestParam String key) {
        try {
            String url = s3Service.generatePresignedGetUrl(bucket, key);
            return ResponseEntity.ok(Map.of("downloadUrl", url));
        } catch (Exception e) {
            logger.error("Error generating presigned download URL", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudo generar la URL de descarga"));
        }
    }
}