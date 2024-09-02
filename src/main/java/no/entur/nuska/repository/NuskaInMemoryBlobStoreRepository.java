package no.entur.nuska.repository;

import java.io.InputStream;
import java.util.Map;
import org.rutebanken.helper.storage.repository.InMemoryBlobStoreRepository;

public class NuskaInMemoryBlobStoreRepository
  extends InMemoryBlobStoreRepository
  implements NuskaBlobStoreRepository {

  public NuskaInMemoryBlobStoreRepository(
    Map<String, Map<String, byte[]>> blobsInContainers
  ) {
    super(blobsInContainers);
  }

  @Override
  public InputStream getLatestBlob(String path) {
    return null;
  }
}
