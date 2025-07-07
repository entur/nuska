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

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import no.entur.nuska.NuskaByteArrayResource;
import no.entur.nuska.NuskaException;
import no.entur.nuska.repository.NuskaBlobStoreRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

/**
 * Operations on blobs in the nisaba bucket.
 */
@Service
public class NisabaBlobStoreService {

  private static final String IMPORTED_SUB_PATH = "imported/";
  private static final int MAX_NUM_IMPORT = 100;
  private static final String ZIP_EXTENSION = ".zip";

  private final NuskaBlobStoreRepository repository;

  public NisabaBlobStoreService(
    @Value("${blobstore.gcs.nisaba.container.name}") String containerName,
    NuskaBlobStoreRepository repository
  ) {
    this.repository = repository;
    this.repository.setContainerName(containerName);
  }

  /**
   * Return the most recent file for the given codespace.
   */
  public ByteArrayResource getLatestBlob(String codespace) {
    return repository.getLatestBlob(IMPORTED_SUB_PATH + codespace);
  }

  /**
   * Return the file identified by the given codespace and import key.
   * The import key can be retrieved from the Kafka message posted in the topic rutedata-dataset-import-event-xxx
   */
  public ByteArrayResource getBlob(String codespace, String importKey) {
    InputStream blob = repository.getBlob(blobName(codespace, importKey));
    if (blob == null) {
      return null;
    }
    try {
      return new NuskaByteArrayResource(
        blob.readAllBytes(),
        blobName(codespace, importKey)
      );
    } catch (IOException e) {
      throw new NuskaException(e);
    }
  }

  public List<NetexImport> getImportList(String codespace) {
    return repository
      .listBlobs(IMPORTED_SUB_PATH + codespace)
      .stream()
      .map(file -> new NetexImport(importKey(file.name()), file.creationDate()))
      .sorted(Comparator.comparing(NetexImport::creationDate))
      .limit(MAX_NUM_IMPORT)
      .toList();
  }

  private String importKey(String fileName) {
    return fileName
      .substring(fileName.lastIndexOf('/') + 1)
      .replace(ZIP_EXTENSION, "");
  }

  private static String blobName(String codespace, String importKey) {
    return IMPORTED_SUB_PATH + codespace + '/' + importKey + ZIP_EXTENSION;
  }
}
