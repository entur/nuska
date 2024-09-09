package no.entur.nuska.repository;

import org.rutebanken.helper.storage.repository.BlobStoreRepository;
import org.springframework.core.io.ByteArrayResource;

public interface NuskaBlobStoreRepository extends BlobStoreRepository {
  ByteArrayResource getLatestBlob(String path);
}
