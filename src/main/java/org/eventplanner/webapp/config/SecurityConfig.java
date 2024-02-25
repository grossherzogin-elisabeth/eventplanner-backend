package org.eventplanner.webapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public final Logger log = LoggerFactory.getLogger(this.getClass());

    @Bean
    public SecurityFilterChain oidcClientCustomizer(
            HttpSecurity http,
            BackChannelInformingLogoutHandler backChannelInformingLogoutHandler
    ) throws Exception {
        // disable csrf protection
        http.csrf(AbstractHttpConfigurer::disable);

        http.oauth2Login(login -> {
            login.defaultSuccessUrl("/login-ok");
            login.userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.userAuthoritiesMapper(oAuthGrantedAuthoritiesMapper()));
        });

        http.exceptionHandling(exceptionHandling -> {
            exceptionHandling.defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    new AntPathRequestMatcher("/api/**"));
        });

        http.logout(logout -> {
            logout.addLogoutHandler(backChannelInformingLogoutHandler);
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
        var sub = oidcUserAuthority.getIdToken().getClaimAsString("sub");
        var email = oidcUserAuthority.getIdToken().getClaimAsString("email");

        log.debug("Hello " + email);

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
        if (roles instanceof Collection<?> roleCollection) {
            for (var item: roleCollection) {
                if (item instanceof String role) {
                    resultStream.accept(role);
                }
            }
        }
        resultStream.accept(Role.ANY);
        return resultStream.build();
    }
}
