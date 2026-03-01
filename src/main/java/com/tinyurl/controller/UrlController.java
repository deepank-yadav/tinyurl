package com.tinyurl.controller;

import com.tinyurl.model.UrlDto;
import com.tinyurl.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@Tag(name = "TinyURL", description = "Endpoints for shortening URLs, redirecting, and viewing statistics")
public class UrlController {

    private final UrlService urlService;

    // ── Shorten ──────────────────────────────────────────────────────────────

    @Operation(
            summary     = "Shorten a URL",
            description = """
                Accepts a long URL and returns a short code.
                If the same URL was already shortened (and hasn't expired), the existing mapping is returned.
                You may optionally supply a **custom alias** and/or an **expiry** in days.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description  = "Short URL created successfully",
                    content      = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema    = @Schema(implementation = UrlDto.ShortenResponse.class),
                            examples  = @ExampleObject(value = """
                    {
                      "originalUrl": "https://www.example.com/some/very/long/path",
                      "shortUrl":    "http://localhost:8080/abc1234",
                      "shortCode":   "abc1234",
                      "createdAt":   "2024-01-15T10:30:00",
                      "expiresAt":   null
                    }
                    """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid URL or alias already taken",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """{"error": "Custom alias 'my-link' is already taken."}""")))
    })
    @PostMapping("/api/shorten")
    public ResponseEntity<UrlDto.ShortenResponse> shorten(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "URL to shorten with optional alias and expiry",
                    required    = true,
                    content     = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema    = @Schema(implementation = UrlDto.ShortenRequest.class),
                            examples  = @ExampleObject(value = """
                        {
                          "url":         "https://www.example.com/some/very/long/path?query=value",
                          "customAlias": "my-link",
                          "expiryDays":  30
                        }
                        """)
                    )
            )
            @Valid @RequestBody UrlDto.ShortenRequest request) {

        UrlDto.ShortenResponse response = urlService.shorten(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── Redirect ─────────────────────────────────────────────────────────────

    @Operation(
            summary     = "Redirect to original URL",
            description = "Resolves the short code and returns an HTTP 302 redirect to the original URL. Click count is incremented on each call."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Redirect to the original URL",
                    headers = @Header(name = "Location", description = "The original long URL")),
            @ApiResponse(responseCode = "400", description = "Short code not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """{"error": "Short URL not found: abc1234"}"""))),
            @ApiResponse(responseCode = "410", description = "Short URL has expired",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """{"error": "This short URL has expired."}""")))
    })
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @Parameter(description = "The short code to resolve", example = "abc1234")
            @PathVariable String shortCode) {

        String originalUrl = urlService.resolve(shortCode);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    @Operation(
            summary     = "Get short URL statistics",
            description = "Returns click count, creation date, and expiry information for a given short code."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description  = "Statistics retrieved successfully",
                    content      = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema    = @Schema(implementation = UrlDto.StatsResponse.class),
                            examples  = @ExampleObject(value = """
                    {
                      "shortCode":   "abc1234",
                      "originalUrl": "https://www.example.com/some/very/long/path",
                      "clickCount":  42,
                      "createdAt":   "2024-01-15T10:30:00",
                      "expiresAt":   null
                    }
                    """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Short code not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """{"error": "Short URL not found: abc1234"}""")))
    })
    @GetMapping("/api/stats/{shortCode}")
    public ResponseEntity<UrlDto.StatsResponse> stats(
            @Parameter(description = "The short code to look up statistics for", example = "abc1234")
            @PathVariable String shortCode) {

        return ResponseEntity.ok(urlService.getStats(shortCode));
    }
}