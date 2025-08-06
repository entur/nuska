package no.entur.nuska.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.contrib.nio.testing.LocalStorageHelper;
import java.util.List;
import java.util.stream.Stream;
import no.entur.nuska.model.DatasetImport;
import no.entur.nuska.repository.NuskaGcsBlobStoreRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

class NisabaBlobStoreServiceTest {

  private static final String TEST_CODESPACE = "atb";
  private static final String IMPORT_KEY_1 = "atb_2021-01-01T00_00_00.01";
  private static final String IMPORT_KEY_2 = "atb_2022-01-01T00_00_00.01";
  private static final String IMPORT_KEY_3 = "atb_2023-01-01T00_00_00.01";

  private static Storage storage;

  @BeforeAll
  static void setUp() {
    storage = LocalStorageHelper.getOptions().getService();
    Stream
      .of(IMPORT_KEY_1, IMPORT_KEY_2, IMPORT_KEY_3)
      .sorted()
      .forEach(importKey -> {
        BlobInfo blobInfo = BlobInfo
          .newBuilder(
            BlobId.of(
              "nisaba-exchange",
              "imported/" + TEST_CODESPACE + "/" + importKey + ".zip"
            )
          )
          .build();
        storage.create(blobInfo, ("Content for file " + importKey).getBytes());
        // the time resolution of the blob update field is 1s.
        // wait at least 1s to ensure that the creation dates are different.
        waitMilli(1001);
      });
  }

  @AfterAll
  static void tearDown() throws Exception {
    storage.close();
  }

  @Test
  void findLatestBlob() {
    NisabaBlobStoreService service = new NisabaBlobStoreService(
      "nisaba-exchange",
      new NuskaGcsBlobStoreRepository(storage)
    );

    ByteArrayResource latestBlob = service.getLatestBlob(TEST_CODESPACE);
    assertNotNull(latestBlob);
    assertTrue(latestBlob.exists());
    assertThat(
      latestBlob.getFilename(),
      is("imported/" + TEST_CODESPACE + "/" + IMPORT_KEY_3 + ".zip")
    );
    byte[] bytes = latestBlob.getByteArray();
    assertTrue(bytes.length > 0);
    assertThat(new String(bytes), is("Content for file " + IMPORT_KEY_3));
  }

  @Test
  void getBlob() {
    NisabaBlobStoreService service = new NisabaBlobStoreService(
      "nisaba-exchange",
      new NuskaGcsBlobStoreRepository(storage)
    );

    ByteArrayResource blob = service.getBlob(TEST_CODESPACE, IMPORT_KEY_2);
    assertNotNull(blob);
    assertTrue(blob.exists());
    assertThat(
      blob.getFilename(),
      is("imported/" + TEST_CODESPACE + "/" + IMPORT_KEY_2 + ".zip")
    );
    byte[] bytes = blob.getByteArray();
    assertTrue(bytes.length > 0);
    assertThat(new String(bytes), is("Content for file " + IMPORT_KEY_2));
  }

  @Test
  void listAllBlobs() {
    NisabaBlobStoreService service = new NisabaBlobStoreService(
      "nisaba-exchange",
      new NuskaGcsBlobStoreRepository(storage)
    );

    List<DatasetImport> importList = service.getImportList(TEST_CODESPACE);
    assertEquals(
      List.of(IMPORT_KEY_1, IMPORT_KEY_2, IMPORT_KEY_3),
      importList.stream().map(DatasetImport::importKey).toList()
    );
  }

  private static void waitMilli(long milli) {
    try {
      Thread.sleep(milli);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
