package no.entur.nuska.repository;

import java.util.List;
import org.rutebanken.helper.storage.repository.BlobStoreRepository;
import org.springframework.core.io.ByteArrayResource;

public interface NuskaBlobStoreRepository extends BlobStoreRepository {
  ByteArrayResource getLatestBlob(String path);

  /**
   * Retrieve the list of files in the blob store under the given path.
   */
  List<BlobStoreFile> listBlobs(String path);
}
