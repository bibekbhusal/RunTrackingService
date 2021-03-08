package com.bhusalb.runtrackingservice.configuration.security;

import com.bhusalb.runtrackingservice.libs.jwt.JsonWebTokenHelper;
import com.bhusalb.runtrackingservice.models.User;
import com.bhusalb.runtrackingservice.repos.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JsonWebTokenHelper jwtHelper;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal (final HttpServletRequest request,
                                     final HttpServletResponse response,
                                     final FilterChain filterChain)
        throws ServletException, IOException {
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.isBlank(header) || !header.startsWith("Bearer")) {
            log.info(String.format("Header %s is empty or does not start with Bearer.", header));
            filterChain.doFilter(request, response);
            return;
        }

        final String token = header.replace("Bearer", "").trim();
        if (StringUtils.isBlank(token)) {
            log.info("Token is empty in the header.");
            filterChain.doFilter(request, response);
            return;
        }

        final String userId = jwtHelper.getUserId(token);
        if (userId == null) {
            log.info("UserId is missing in token: " + token);
            filterChain.doFilter(request, response);
            return;
        }

        final User user = userRepository.getById(new ObjectId(userId));

        final UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
    }
}
