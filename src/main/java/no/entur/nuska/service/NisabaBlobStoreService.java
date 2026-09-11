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
import no.entur.nuska.model.DatasetImport;
import no.entur.nuska.model.NetexDsjVariant;
import no.entur.nuska.repository.NuskaBlobStoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

/**
 * Operations on blobs in the nisaba bucket.
 */
@Service
public class NisabaBlobStoreService {

  private static final Logger LOGGER = LoggerFactory.getLogger(
    NisabaBlobStoreService.class
  );

  private static final int MAX_NUM_IMPORT = 10;
  private static final String ZIP_EXTENSION = ".zip";

  private final NuskaBlobStoreRepository repository;

  public NisabaBlobStoreService(
    @Value(
      "${blobstore.gcs.nisaba.container.name:nisaba-exchange}"
    ) String containerName,
    NuskaBlobStoreRepository repository
  ) {
    this.repository = repository;
    this.repository.setContainerName(containerName);
  }

  /**
   * Return the most recent file for the given codespace, in the given NeTEx variant.
   */
  public ByteArrayResource getLatestBlob(
    String codespace,
    NetexDsjVariant variant
  ) {
    ByteArrayResource blob = repository.getLatestBlob(
      variant.subPath() + codespace
    );
    if (blob == null && variant != NetexDsjVariant.DEFAULT) {
      LOGGER.warn(
        "No dataset for codespace '{}' in the folder '{}', falling back to the default folder",
        codespace,
        variant.subPath()
      );
      blob =
        repository.getLatestBlob(NetexDsjVariant.DEFAULT.subPath() + codespace);
    }
    return blob;
  }

  /**
   * Return the file identified by the given codespace and import key, in the given NeTEx variant.
   * The import key can be retrieved from the Kafka message posted in the topic rutedata-dataset-import-event-xxx
   */
  public ByteArrayResource getBlob(
    String codespace,
    String importKey,
    NetexDsjVariant variant
  ) {
    ByteArrayResource blob = getBlob(blobName(variant, codespace, importKey));
    if (blob == null && variant != NetexDsjVariant.DEFAULT) {
      LOGGER.warn(
        "No dataset for codespace '{}' and import key '{}' in the folder '{}', falling back to the default folder",
        codespace,
        importKey,
        variant.subPath()
      );
      blob = getBlob(blobName(NetexDsjVariant.DEFAULT, codespace, importKey));
    }
    return blob;
  }

  public List<DatasetImport> getImportList(String codespace) {
    return repository
      .listBlobs(NetexDsjVariant.DEFAULT.subPath() + codespace)
      .stream()
      .map(file ->
        new DatasetImport(importKey(file.name()), file.creationDate())
      )
      .sorted(Comparator.comparing(DatasetImport::creationDate).reversed())
      .limit(MAX_NUM_IMPORT)
      .sorted(Comparator.comparing(DatasetImport::creationDate))
      .toList();
  }

  private String importKey(String fileName) {
    return fileName
      .substring(fileName.lastIndexOf('/') + 1)
      .replace(ZIP_EXTENSION, "");
  }

  private ByteArrayResource getBlob(String blobName) {
    InputStream blob = repository.getBlob(blobName);
    if (blob == null) {
      return null;
    }
    try {
      return new NuskaByteArrayResource(blob.readAllBytes(), blobName);
    } catch (IOException e) {
      throw new NuskaException(e);
    }
  }

  private static String blobName(
    NetexDsjVariant variant,
    String codespace,
    String importKey
  ) {
    return variant.subPath() + codespace + '/' + importKey + ZIP_EXTENSION;
  }
}
