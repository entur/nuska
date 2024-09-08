package no.entur.nuska.repository;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import java.io.InputStream;
import java.util.Comparator;
import java.util.Optional;
import no.entur.nuska.NuskaException;
import org.rutebanken.helper.gcp.BlobStoreHelper;
import org.rutebanken.helper.gcp.repository.GcsBlobStoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NuskaGcsBlobStoreRepository
  extends GcsBlobStoreRepository
  implements NuskaBlobStoreRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(
    NuskaGcsBlobStoreRepository.class
  );

  public NuskaGcsBlobStoreRepository(String projectId, String credentialPath) {
    super(projectId, credentialPath);
  }

  public NuskaGcsBlobStoreRepository(Storage storage) {
    super(storage);
  }

  @Override
  public InputStream getLatestBlob(String path) {
    try (Storage storage = storage()) {
      Optional<Blob> latestBlob = storage
        .list(
          containerName(),
          new Storage.BlobListOption[] { Storage.BlobListOption.prefix(path) }
        )
        .streamAll()
        .max(Comparator.comparing(Blob::getUpdateTimeOffsetDateTime));

      if (latestBlob.isPresent()) {
        LOGGER.debug("Most recent file: {}", latestBlob.get().getName());
        return BlobStoreHelper.getBlobInputStream(latestBlob.get());
      } else {
        LOGGER.warn("Bucket is empty or unable to fetch the files.");
        return null;
      }
    } catch (Exception e) {
      throw new NuskaException(e);
    }
  }
}
