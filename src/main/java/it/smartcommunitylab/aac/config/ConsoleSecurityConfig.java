package it.smartcommunitylab.aac.config;

import java.util.Arrays;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import it.smartcommunitylab.aac.core.auth.Http401UnauthorizedEntryPoint;

/*
 * Security context for console endpoints
 * 
 * Builds a stateful context with no auth entrypoint
 */

@Configuration
@Order(25)
public class ConsoleSecurityConfig extends WebSecurityConfigurerAdapter {

    /*
     * Configure a separated security context for API
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {
        // match only console endpoints and require user access
        http.requestMatcher(getRequestMatcher())
                .authorizeRequests((authorizeRequests) -> authorizeRequests
                        .anyRequest().hasAnyAuthority("ROLE_USER"))
                // disable request cache, we override redirects but still better enforce it
                .requestCache((requestCache) -> requestCache.disable())
                .exceptionHandling()
                // use 401
                .authenticationEntryPoint(new Http401UnauthorizedEntryPoint())
                .accessDeniedPage("/accesserror")
                .and()
                // allow cors
                .cors().configurationSource(corsConfigurationSource())
                .and()
                // disable crsf for spa console
                .csrf()
                .disable()
                // we want a session for console
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
    }

    public RequestMatcher getRequestMatcher() {
        return new AntPathRequestMatcher(CONSOLE_PREFIX + "/**");

    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList("http://localhost:*"));
        config.setAllowedMethods(Arrays.asList("GET"));
        config.setAllowedHeaders(Arrays.asList("authorization", "range"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    public static final String CONSOLE_PREFIX = "/console";

}
