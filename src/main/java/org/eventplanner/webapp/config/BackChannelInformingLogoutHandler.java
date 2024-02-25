package org.eventplanner.webapp.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;

@Component
public class BackChannelInformingLogoutHandler implements LogoutHandler {

    private static final Logger LOG = LoggerFactory.getLogger(BackChannelInformingLogoutHandler.class);
    private final Set<String> knownClientRegistrationIds;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final RestTemplate restTemplate = new RestTemplateBuilder().build();

    public BackChannelInformingLogoutHandler(
            final ClientRegistrationRepository clientRegistrationRepository,
            final OAuth2ClientProperties oAuth2ClientProperties
    ) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.knownClientRegistrationIds = Set.copyOf(oAuth2ClientProperties.getRegistration().keySet());
    }

    @Override
    public void logout(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Authentication authentication
    ) {
        if (authentication instanceof OAuth2AuthenticationToken oAuth2Token) {
            for (final var clientRegistrationId : knownClientRegistrationIds) {
                final var clientRegistration = clientRegistrationRepository.findByRegistrationId(clientRegistrationId);
                if (clientRegistration.getRegistrationId().equals(oAuth2Token.getAuthorizedClientRegistrationId())) {
                    if (oAuth2Token.getPrincipal() instanceof OidcUser oidcUser) {
                        performLogoutOnOidcClient(clientRegistration, oidcUser);
                    }
                }
            }
        }
    }

    private void performLogoutOnOidcClient(
            final ClientRegistration clientRegistration,
            final OidcUser oidcUser
    ) {
        final var logoutEndpoint = getLogoutUri(clientRegistration.getProviderDetails().getConfigurationMetadata());
        if (logoutEndpoint == null) {
            LOG.warn(format(
                    "The OpenID-Connect client %s does not support logout",
                    clientRegistration.getClientName()
            ));
            return;
        }

        final var logoutUri = UriComponentsBuilder.fromUri(logoutEndpoint)
                .queryParam("id_token_hint", oidcUser.getIdToken().getTokenValue())
                .encode()
                .build()
                .toUri();
        final var logoutResult = restTemplate.getForEntity(logoutUri, Void.class);
        if (logoutResult.getStatusCode().isError()) {
            LOG.error(format(
                    "The OpenID-Connect client %s responded with an error while attempting to logout user %s",
                    clientRegistration.getClientName(),
                    oidcUser.getName()
            ));
        }
    }

    private URI getLogoutUri(final Map<String, Object> endpointMetadata) {
        if (endpointMetadata.get("end_session_endpoint") instanceof String endpointUri) {
            return URI.create(endpointUri);
        }
        return null;
    }
}
