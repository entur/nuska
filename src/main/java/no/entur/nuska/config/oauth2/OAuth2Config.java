/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 */

package no.entur.nuska.config.oauth2;

import java.util.Map;
import org.entur.oauth2.multiissuer.MultiIssuerAuthenticationManagerResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class OAuth2Config {

  @Bean
  @Profile("!test")
  public MultiIssuerAuthenticationManagerResolver multiIssuerAuthenticationManagerResolver(
    @Value(
      "${nuska.oauth2.resourceserver.auth0.entur.partner.jwt.audience}"
    ) String enturPartnerAuth0Audience,
    @Value(
      "${nuska.oauth2.resourceserver.auth0.entur.partner.jwt.issuer-uri}"
    ) String enturPartnerAuth0Issuer,
    @Value(
      "${nuska.oauth2.resourceserver.auth0.entur.internal.jwt.audience}"
    ) String enturInternalAuth0Audience,
    @Value(
      "${nuska.oauth2.resourceserver.auth0.entur.internal.jwt.issuer-uri}"
    ) String enturInternalAuth0Issuer,
    EnturPartnerAuth0RolesClaimAdapter enturPartnerAuth0RolesClaimAdapter
  ) {
    return new NuskaMultiIssuerAuthenticationManagerResolver(
      enturInternalAuth0Audience,
      enturInternalAuth0Issuer,
      enturPartnerAuth0Audience,
      enturPartnerAuth0Issuer,
      enturPartnerAuth0RolesClaimAdapter
    );
  }

  @Bean
  @Profile("!test")
  public EnturPartnerAuth0RolesClaimAdapter enturPartnerAuth0RolesClaimAdapter(
    @Value(
      "#{${netex.export.block.authorization}}"
    ) Map<String, String> authorizedProvidersForNetexBlocksConsumer,
    @Value(
      "#{${nuska.oauth2.resourceserver.auth0.partner.organisations}}"
    ) Map<Long, String> rutebankenOrganisations,
    @Value(
      "${nuska.oauth2.resourceserver.auth0.partner.admin.activated:false}"
    ) boolean administratorAccessActivated
  ) {
    return new EnturPartnerAuth0RolesClaimAdapter(
      authorizedProvidersForNetexBlocksConsumer,
      rutebankenOrganisations,
      administratorAccessActivated
    );
  }
}
