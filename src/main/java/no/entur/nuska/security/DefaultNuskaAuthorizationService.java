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

import org.rutebanken.helper.organisation.authorization.AuthorizationService;

public class DefaultNuskaAuthorizationService
  implements NuskaAuthorizationService {

  private final AuthorizationService<String> authorizationService;

  public DefaultNuskaAuthorizationService(
    AuthorizationService<String> authorizationService
  ) {
    this.authorizationService = authorizationService;
  }

  @Override
  public void verifyBlockViewerPrivileges(String providerId) {
    authorizationService.validateViewBlockData(providerId);
  }
}
