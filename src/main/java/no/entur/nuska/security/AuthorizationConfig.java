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

package no.entur.nuska.security;

import java.util.Locale;
import org.entur.oauth2.JwtRoleAssignmentExtractor;
import org.rutebanken.helper.organisation.RoleAssignmentExtractor;
import org.rutebanken.helper.organisation.authorization.AuthorizationService;
import org.rutebanken.helper.organisation.authorization.DefaultAuthorizationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure authorization.
 */
@Configuration
public class AuthorizationConfig {

  @Bean
  public RoleAssignmentExtractor roleAssignmentExtractor() {
    return new JwtRoleAssignmentExtractor();
  }

  @Bean("authorizationService")
  public AuthorizationService<String> tokenBasedAuthorizationService(
    RoleAssignmentExtractor roleAssignmentExtractor
  ) {
    return new DefaultAuthorizationService<>(
      providerCodespace ->
        providerCodespace == null
          ? null
          : providerCodespace.toUpperCase(Locale.ROOT),
      roleAssignmentExtractor
    );
  }

  @Bean
  public NuskaAuthorizationService nuskaAuthorizationService(
    AuthorizationService<String> authorizationService
  ) {
    return new DefaultNuskaAuthorizationService(authorizationService);
  }
}
