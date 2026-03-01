package com.tinyurl.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;

public class UrlDto {

    @Data
    public static class ShortenRequest {

        @NotBlank(message = "URL must not be blank")
        @Pattern(
                regexp = "^(https?://).+",
                message = "URL must start with http:// or https://"
        )
        private String url;

        // Optional: custom alias (e.g. "my-blog")
        private String customAlias;

        // Optional: expiry in days (null = never expires)
        private Integer expiryDays;
    }

    @Data
    public static class ShortenResponse {
        private String originalUrl;
        private String shortUrl;
        private String shortCode;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
    }

    @Data
    public static class StatsResponse {
        private String shortCode;
        private String originalUrl;
        private Long clickCount;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
    }
}