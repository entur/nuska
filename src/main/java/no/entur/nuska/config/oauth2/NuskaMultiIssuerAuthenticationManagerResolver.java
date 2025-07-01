package no.entur.nuska.config.oauth2;

import java.util.List;
import org.entur.oauth2.AudienceValidator;
import org.entur.oauth2.JwtRoleAssignmentExtractor;
import org.entur.oauth2.multiissuer.MultiIssuerAuthenticationManagerResolver;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;

/**
 * Resolve the @{@link AuthenticationManager} that should authenticate the current JWT token.
 * This is achieved by extracting the issuer from the token and matching it against either the Entur Partner
 * issuer URI or the RoR Auth0 issuer URI.
 * The two @{@link AuthenticationManager}s (one for Entur Partner Auth0, one for RoR Auth0) are instantiated during the first request and then cached.
 */
public class NuskaMultiIssuerAuthenticationManagerResolver
  extends MultiIssuerAuthenticationManagerResolver {

  private final String enturInternalAuth0Issuer;
  private final String enturInternalAuth0Audience;
  private final String enturPartnerAuth0Issuer;
  private final String enturPartnerAuth0Audience;

  public NuskaMultiIssuerAuthenticationManagerResolver(
    String enturInternalAuth0Audience,
    String enturInternalAuth0Issuer,
    String enturPartnerAuth0Audience,
    String enturPartnerAuth0Issuer
  ) {
    super(
      enturInternalAuth0Audience,
      enturInternalAuth0Issuer,
      enturPartnerAuth0Audience,
      enturPartnerAuth0Issuer,
      null,
      null,
      null
    );
    this.enturInternalAuth0Issuer = enturInternalAuth0Issuer;
    this.enturInternalAuth0Audience = enturInternalAuth0Audience;
    this.enturPartnerAuth0Issuer = enturPartnerAuth0Issuer;
    this.enturPartnerAuth0Audience = enturPartnerAuth0Audience;
  }

  /**
   * Build a @{@link JwtDecoder} for Entur Partner Auth0 tenant.
   *
   * @return a @{@link JwtDecoder} for Auth0.
   */
  @Override
  protected JwtDecoder enturPartnerAuth0JwtDecoder() {
    return getJwtDecoder(enturPartnerAuth0Issuer, enturPartnerAuth0Audience);
  }

  @Override
  protected JwtDecoder enturInternalAuth0JwtDecoder() {
    return getJwtDecoder(enturInternalAuth0Issuer, enturInternalAuth0Audience);
  }

  private JwtDecoder getJwtDecoder(
    String enturAuth0Issuer,
    String enturAuth0Audience
  ) {
    NimbusJwtDecoder jwtDecoder = JwtDecoders.fromOidcIssuerLocation(
      enturAuth0Issuer
    );

    OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(
      List.of(enturAuth0Audience)
    );
    OAuth2TokenValidator<Jwt> withIssuer =
      JwtValidators.createDefaultWithIssuer(enturAuth0Issuer);
    OAuth2TokenValidator<Jwt> withAudience =
      new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);
    jwtDecoder.setJwtValidator(withAudience);
    return jwtDecoder;
  }
}
