package org.eventplanner.webapp.config;

import org.eventplanner.webapp.users.models.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class OAuthSecurityConfig {

    private final String loginSuccessUrl;
    private final List<String> admins;
    private final boolean everyoneIsAdmin;

    public OAuthSecurityConfig(
        @Value("${custom.login-success-url}") String loginSuccessUrl,
        @Value("${custom.admins}") String admins,
        @Value("${custom.everyone-is-admin}") String everyoneIsAdmin
    ) {
        this.loginSuccessUrl = loginSuccessUrl;
        this.admins = Arrays.stream(admins.split(",")).map(String::trim).toList();
        this.everyoneIsAdmin = "true".equals(everyoneIsAdmin);
    }

    @Bean
    public SecurityFilterChain oidcClientCustomizer(
        HttpSecurity http,
        OAuthLogoutHandler oauthLogoutHandler
    ) throws Exception {
        http.oauth2Login(oauth2Login -> {
            // open frontend home page after login
            oauth2Login.defaultSuccessUrl(loginSuccessUrl, true);
            oauth2Login.failureUrl(loginSuccessUrl);
            oauth2Login.authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint.baseUri("/auth/login"));
            oauth2Login.userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.userAuthoritiesMapper(oAuthGrantedAuthoritiesMapper()));
        });

        http.logout(logout -> {
            logout.logoutUrl("/auth/logout");
            logout.addLogoutHandler(oauthLogoutHandler);
            logout.logoutSuccessUrl(loginSuccessUrl);
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
        var email = oidcUserAuthority.getIdToken().getEmail();

        if (everyoneIsAdmin || admins.contains(email)) {
            resultStream.add(Role.ADMIN.value());
        } else {
            resultStream.add(Role.TEAM_MEMBER.value());
        }
        var roles = oidcUserAuthority.getIdToken().getClaimAsStringList("ROLES");
        if (roles != null) {
            for (String role : roles) {
                resultStream.add(role);
            }
        }
        return resultStream.build();
    }

    private Stream<String> extractOAuthRoles(OAuth2UserAuthority oAuth2UserAuthority) {
        var roles = oAuth2UserAuthority.getAttributes().get("ROLES");
        var email = oAuth2UserAuthority.getAttributes().get("email");
        Stream.Builder<String> resultStream = Stream.builder();
        if (everyoneIsAdmin || admins.contains(email)) {
            resultStream.add(Role.ADMIN.value());
        } else {
            resultStream.add(Role.TEAM_MEMBER.value());
        }
        if (roles instanceof Collection<?> roleCollection) {
            for (var item : roleCollection) {
                if (item instanceof String role) {
                    resultStream.accept(role);
                }
            }
        }
        return resultStream.build();
    }

}
