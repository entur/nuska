package no.entur.nuska.repository;

import java.io.InputStream;
import org.rutebanken.helper.storage.repository.BlobStoreRepository;

public interface NuskaBlobStoreRepository extends BlobStoreRepository {
  InputStream getLatestBlob(String path);
}
