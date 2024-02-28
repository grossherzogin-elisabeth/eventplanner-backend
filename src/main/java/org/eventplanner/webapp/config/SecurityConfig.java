package org.eventplanner.webapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Collection;
import java.util.stream.Stream;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final String loginSuccessUrl;

    public SecurityConfig(@Value("${custom.login-success-url}") String loginSuccessUrl) {
        this.loginSuccessUrl = loginSuccessUrl;
    }

    @Bean
    public SecurityFilterChain oidcClientCustomizer(
            HttpSecurity http,
            OAuthLogoutHandler oauthLogoutHandler
    ) throws Exception {
        // disable csrf protection
        http.csrf(AbstractHttpConfigurer::disable);

        http.oauth2Login(oauth2Login -> {
            // open frontend home page after login
            oauth2Login.defaultSuccessUrl(loginSuccessUrl, true);
            oauth2Login.authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint.baseUri("/auth/login"));
            oauth2Login.userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.userAuthoritiesMapper(oAuthGrantedAuthoritiesMapper()));
        });

        // By default, Spring redirects an unauthorized user to the login page. In this case we want to return a 401
        // error and let the frontend handle the redirect.
        http.exceptionHandling(exceptionHandling -> {
            exceptionHandling.defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    new AntPathRequestMatcher("/api/**"));
        });

        http.logout(logout -> {
            logout.logoutUrl("/auth/logout");
            logout.addLogoutHandler(oauthLogoutHandler);
        });

        http.oidcLogout(logout -> {
            logout.backChannel(withDefaults());
        });

        return http.build();
    }

    private GrantedAuthoritiesMapper oAuthGrantedAuthoritiesMapper() {
        return authorities -> authorities
                .stream()
                .flatMap(authority -> switch (authority) {
                    case OidcUserAuthority oidcAuthority -> extractOidcRoles(oidcAuthority);
                    case OAuth2UserAuthority oAuthAuthority -> extractOAuthRoles(oAuthAuthority);
                    default -> Stream.empty();
                })
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    private Stream<String> extractOidcRoles(OidcUserAuthority oidcUserAuthority) {
        Stream.Builder<String> resultStream = Stream.builder();
        resultStream.accept(Role.ANY);
        // var sub = oidcUserAuthority.getIdToken().getClaimAsString("sub");
        // var email = oidcUserAuthority.getIdToken().getClaimAsString("email");
        // TODO find application roles for sub or email
        if ("admin@grossherzogin-elisabeth.de".equals(oidcUserAuthority.getIdToken().getEmail())) {
            resultStream.accept(Role.ADMIN);
        }
        var roles = oidcUserAuthority.getIdToken().getClaimAsStringList("ROLES");
        if (roles != null) {
            for (String role : roles) {
                resultStream.accept(role);
            }
        }
        return resultStream.build();
    }

    private Stream<String> extractOAuthRoles(OAuth2UserAuthority oAuth2UserAuthority) {
        var roles = oAuth2UserAuthority.getAttributes().get("ROLES");
        Stream.Builder<String> resultStream = Stream.builder();
        if ("admin@grossherzogin-elisabeth.de".equals(oAuth2UserAuthority.getAttributes().get("email"))) {
            resultStream.accept(Role.ADMIN);
        }
        resultStream.accept(Role.ANY);
        if (roles instanceof Collection<?> roleCollection) {
            for (var item: roleCollection) {
                if (item instanceof String role) {
                    resultStream.accept(role);
                }
            }
        }
        return resultStream.build();
    }
}
