package no.entur.nuska.repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import no.entur.nuska.NuskaByteArrayResource;
import no.entur.nuska.NuskaException;
import org.rutebanken.helper.storage.repository.LocalDiskBlobStoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;

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
  public ByteArrayResource getLatestBlob(String path) {
    LOGGER.debug("get latest blob called in local-disk blob");

    File dir = new File(getContainerFolder(), path);
    if (dir.isDirectory()) {
      Optional<File> opFile = Arrays
        .stream(Objects.requireNonNull(dir.listFiles(File::isFile)))
        .filter(this::isValidFile)
        .max(Comparator.comparingLong(File::lastModified));

      if (opFile.isPresent()) {
        try {
          return new NuskaByteArrayResource(
            getBlob(opFile.get().getAbsolutePath()).readAllBytes(),
            opFile.get().getName()
          );
        } catch (IOException e) {
          throw new NuskaException(e);
        }
      }
    }
    return null;
  }

  private boolean isValidFile(File file) {
    try {
      Path path = file.toPath();
      return Files.isRegularFile(path) && !Files.isHidden(path);
    } catch (IOException e) {
      return false;
    }
  }
}
