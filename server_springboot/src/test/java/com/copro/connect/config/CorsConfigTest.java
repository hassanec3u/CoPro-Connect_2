package com.copro.connect.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests CorsConfig")
class CorsConfigTest {

    private CorsConfig corsConfig;

    @BeforeEach
    void setUp() {
        corsConfig = new CorsConfig();
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "http://localhost:4200,http://localhost:3000");
    }

    @Test
    @DisplayName("corsConfigurationSource retourne une source configurée")
    void corsConfigurationSource_returnsConfiguredSource() {
        CorsConfigurationSource source = corsConfig.corsConfigurationSource();

        assertThat(source).isNotNull().isInstanceOf(UrlBasedCorsConfigurationSource.class);

        CorsConfiguration config = ((UrlBasedCorsConfigurationSource) source)
                .getCorsConfiguration(new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/test"));

        assertThat(config).isNotNull();
        assertThat(config.getAllowedOrigins()).containsExactly("http://localhost:4200", "http://localhost:3000");
        assertThat(config.getAllowedMethods()).contains("GET", "POST", "PUT", "DELETE", "OPTIONS");
        assertThat(config.getAllowCredentials()).isTrue();
        assertThat(config.getExposedHeaders()).contains("Authorization");
        assertThat(config.getMaxAge()).isEqualTo(3600L);
    }
}
