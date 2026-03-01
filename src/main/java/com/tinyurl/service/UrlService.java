package com.tinyurl.service;

import com.tinyurl.model.UrlDto;
import com.tinyurl.model.UrlMapping;
import com.tinyurl.repository.UrlMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UrlService {

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SHORT_CODE_LENGTH = 7;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UrlMappingRepository repository;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Shorten a URL. Returns existing mapping if the same URL was already shortened.
     */
    public UrlDto.ShortenResponse shorten(UrlDto.ShortenRequest request) {
        // Re-use existing short code if the URL was already shortened
        Optional<UrlMapping> existing = repository.findByOriginalUrl(request.getUrl());
        if (existing.isPresent() && !existing.get().isExpired()) {
            return toResponse(existing.get());
        }

        String shortCode = resolveShortCode(request.getCustomAlias());

        LocalDateTime expiresAt = request.getExpiryDays() != null
                ? LocalDateTime.now().plusDays(request.getExpiryDays())
                : null;

        UrlMapping mapping = UrlMapping.builder()
                .shortCode(shortCode)
                .originalUrl(request.getUrl())
                .expiresAt(expiresAt)
                .build();

        repository.save(mapping);
        return toResponse(mapping);
    }

    /**
     * Resolve a short code to the original URL and increment the click counter.
     */
    public String resolve(String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("Short URL not found: " + shortCode));

        if (mapping.isExpired()) {
            throw new IllegalStateException("This short URL has expired.");
        }

        repository.incrementClickCount(shortCode);
        return mapping.getOriginalUrl();
    }

    /**
     * Get statistics for a given short code.
     */
    public UrlDto.StatsResponse getStats(String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("Short URL not found: " + shortCode));

        UrlDto.StatsResponse stats = new UrlDto.StatsResponse();
        stats.setShortCode(mapping.getShortCode());
        stats.setOriginalUrl(mapping.getOriginalUrl());
        stats.setClickCount(mapping.getClickCount());
        stats.setCreatedAt(mapping.getCreatedAt());
        stats.setExpiresAt(mapping.getExpiresAt());
        return stats;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String resolveShortCode(String customAlias) {
        if (customAlias != null && !customAlias.isBlank()) {
            if (repository.existsByShortCode(customAlias)) {
                throw new IllegalArgumentException("Custom alias '" + customAlias + "' is already taken.");
            }
            return customAlias;
        }
        return generateUniqueCode();
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = generateRandomCode();
        } while (repository.existsByShortCode(code));
        return code;
    }

    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(SHORT_CODE_LENGTH);
        for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    private UrlDto.ShortenResponse toResponse(UrlMapping mapping) {
        UrlDto.ShortenResponse response = new UrlDto.ShortenResponse();
        response.setOriginalUrl(mapping.getOriginalUrl());
        response.setShortCode(mapping.getShortCode());
        response.setShortUrl(baseUrl + "/" + mapping.getShortCode());
        response.setCreatedAt(mapping.getCreatedAt());
        response.setExpiresAt(mapping.getExpiresAt());
        return response;
    }
}