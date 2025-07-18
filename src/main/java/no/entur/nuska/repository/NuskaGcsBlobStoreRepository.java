package no.entur.nuska.repository;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import no.entur.nuska.NuskaByteArrayResource;
import no.entur.nuska.NuskaException;
import org.rutebanken.helper.gcp.BlobStoreHelper;
import org.rutebanken.helper.gcp.repository.GcsBlobStoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;

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
  public ByteArrayResource getLatestBlob(String path) {
    try (Storage storage = storage()) {
      Optional<Blob> latestBlob = storage
        .list(containerName(), Storage.BlobListOption.prefix(path))
        .streamAll()
        .max(Comparator.comparing(Blob::getUpdateTimeOffsetDateTime));

      if (latestBlob.isPresent()) {
        LOGGER.debug("Most recent file: {}", latestBlob.get().getName());
        BlobStoreHelper.validateBlob(latestBlob.get());
        return new NuskaByteArrayResource(
          latestBlob.get().getContent(),
          latestBlob.get().getName()
        );
      } else {
        LOGGER.warn("Bucket is empty or unable to fetch the files.");
        return null;
      }
    } catch (Exception e) {
      throw new NuskaException(e);
    }
  }

  @Override
  public List<BlobStoreFile> listBlobs(String path) {
    List<BlobStoreFile> files = new ArrayList<>();
    Iterator<Blob> blobIterator = BlobStoreHelper.listAllBlobsRecursively(
      storage(),
      containerName(),
      path
    );
    blobIterator.forEachRemaining(blob ->
      files.add(
        new BlobStoreFile(
          blob.asBlobInfo().getName(),
          blob.asBlobInfo().getUpdateTimeOffsetDateTime().toInstant()
        )
      )
    );
    return files
      .stream()
      .sorted(Comparator.comparing(BlobStoreFile::creationDate))
      .toList();
  }
}
