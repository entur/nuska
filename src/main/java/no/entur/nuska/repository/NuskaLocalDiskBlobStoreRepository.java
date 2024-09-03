package no.entur.nuska.repository;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import org.rutebanken.helper.storage.repository.LocalDiskBlobStoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NuskaLocalDiskBlobStoreRepository
  extends LocalDiskBlobStoreRepository
  implements NuskaBlobStoreRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(
    NuskaLocalDiskBlobStoreRepository.class
  );

  public NuskaLocalDiskBlobStoreRepository(String baseFolder) {
    super(baseFolder);
  }

  @Override
  public InputStream getLatestBlob(String path) {
    LOGGER.debug("get latest blob called in local-disk blob");

    File dir = new File(getContainerFolder(), path);
    if (dir.isDirectory()) {
      Optional<File> opFile = Arrays
        .stream(Objects.requireNonNull(dir.listFiles(File::isFile)))
        .max(Comparator.comparingLong(File::lastModified));

      if (opFile.isPresent()) {
        return getBlob(opFile.get().getAbsolutePath());
      }
    }
    return null;
  }
}
