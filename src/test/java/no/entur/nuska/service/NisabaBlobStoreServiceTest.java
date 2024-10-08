package no.entur.nuska.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.contrib.nio.testing.LocalStorageHelper;
import java.io.IOException;
import java.util.stream.IntStream;
import no.entur.nuska.repository.NuskaGcsBlobStoreRepository;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

class NisabaBlobStoreServiceTest {

  @Test
  void happyTest() throws IOException {
    Storage storage = LocalStorageHelper.getOptions().getService();

    IntStream
      .rangeClosed(1, 3)
      .forEach(i -> {
        BlobInfo blobInfo = BlobInfo
          .newBuilder(
            BlobId.of("nisaba-exchange", "imported/atb/someFile" + i + ".txt")
          )
          .build();
        storage.create(blobInfo, ("A file text " + i).getBytes());
      });

    NisabaBlobStoreService service = new NisabaBlobStoreService(
      "nisaba-exchange",
      new NuskaGcsBlobStoreRepository(storage)
    );

    ByteArrayResource latestBlob = service.getLatestBlob("atb");
    assertNotNull(latestBlob);
    assertTrue(latestBlob.exists());
    assertThat(latestBlob.getFilename(), is("imported/atb/someFile3.txt"));
    byte[] bytes = latestBlob.getByteArray();
    assertTrue(bytes.length > 0);
    assertThat(new String(bytes), is("A file text 3"));
  }
}
