/*
 *
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

package no.entur.nuska.service;

import java.io.InputStream;
import no.entur.nuska.repository.NuskaBlobStoreRepository;
import org.springframework.core.io.ByteArrayResource;

public abstract class AbstractBlobStoreService {

  protected final NuskaBlobStoreRepository repository;

  protected AbstractBlobStoreService(
    String containerName,
    NuskaBlobStoreRepository repository
  ) {
    this.repository = repository;
    this.repository.setContainerName(containerName);
  }

  public InputStream getBlob(String name) {
    return repository.getBlob(name);
  }

  public ByteArrayResource getLatestBlob(String codespace) {
    return repository.getLatestBlob("imported/" + codespace);
  }
}
