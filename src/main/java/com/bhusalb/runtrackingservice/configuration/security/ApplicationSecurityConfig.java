package com.bhusalb.runtrackingservice.configuration.security;

import com.bhusalb.runtrackingservice.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;

import static java.lang.String.format;

@EnableWebSecurity
@EnableGlobalMethodSecurity (
    prePostEnabled = true,
    securedEnabled = true,
    jsr250Enabled = true
)
@Slf4j
public class ApplicationSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String LOCAL_HOST_IP = "127.0. 0.1";

    private final UserService userService;
    private final JWTAuthenticationFilter authenticationFilter;
    private final PasswordEncoder passwordEncoder;

    @Value ("${springdoc.api-docs.path}")
    private String restApiDocPath;
    @Value ("${springdoc.swagger-ui.path}")
    private String swaggerPath;

    public ApplicationSecurityConfig (final UserService userService,
                                      final JWTAuthenticationFilter authenticationFilter,
                                      final PasswordEncoder passwordEncoder) {
        super();
        this.userService = userService;
        this.authenticationFilter = authenticationFilter;
        this.passwordEncoder = passwordEncoder;

        // Inherit security context in async function calls
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    @Override
    protected void configure (final AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure (HttpSecurity http) throws Exception {
        // Enable CORS and disable CSRF
        http = http.cors().and().csrf().disable();

        // Set session management to stateless
        http = http
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and();

        // Set unauthorized requests exception handler
        http = http.exceptionHandling().authenticationEntryPoint(
            (request, response, ex) -> {
                log.warn("Unauthorized request - {}", ex.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
            }).and();

        // Set permissions on endpoints
        http.authorizeRequests()
            // Swagger endpoints must be publicly accessible
            .antMatchers("/").permitAll()
            .antMatchers(format("%s/**", restApiDocPath)).permitAll()
            .antMatchers(format("%s/**", swaggerPath)).permitAll()
            // Our public endpoints
            .antMatchers("/v1/auth/**").permitAll()
            .antMatchers("/v1/auth/register/secured").hasIpAddress(LOCAL_HOST_IP)
            // Our private endpoints
            .anyRequest().authenticated();

        // Add JWT token filter
        http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean () throws Exception {
        return super.authenticationManagerBean();
    }
}
