package no.entur.nuska;

import java.io.InputStream;
import java.nio.file.*;
import no.entur.nuska.security.NuskaAuthorizationService;
import no.entur.nuska.service.NisabaBlobStoreService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
class NuskaController {

  private final NuskaAuthorizationService authorizationService;
  private final NisabaBlobStoreService blobStoreService;

  public NuskaController(
    NuskaAuthorizationService authorizationService,
    NisabaBlobStoreService blobStoreService
  ) {
    this.authorizationService = authorizationService;
    this.blobStoreService = blobStoreService;
  }

  @GetMapping("timetable-data/{codespace}")
  public ResponseEntity<Resource> downloadTimetableData(
    @PathVariable(value = "codespace") String codespace
  ) {
    if (codespace == null) {
      throw new NuskaException("No codespace provided");
    }

    // TODO: check that the codespace is a valid codespace in baba.
    //  Marduk is doing it by caching the codespaces from baba at the startup.
    //  Do we need to do the same here?

    try {
      canAccessBlocks(codespace);
      InputStream latestBlob = blobStoreService.getLatestBlob(codespace);
      String nuskaWorkingDirectory =
        "/Users/mansoor.sajjad/entur-local/working/nuska";
      Path temporaryPath = Paths.get(
        nuskaWorkingDirectory + "/" + codespace + ".zip"
      );
      Files.copy(
        latestBlob,
        temporaryPath,
        StandardCopyOption.REPLACE_EXISTING
      );

      Resource resource = new UrlResource(temporaryPath.toUri());

      if (resource.exists()) {
        return ResponseEntity
          .ok()
          .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + resource.getFilename() + "\""
          )
          .body(resource);
      } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
      }
    } catch (Exception e) {
      throw new NuskaException("Failed to download Netex", e);
    }
  }

  private void canAccessBlocks(String codespace) {
    try {
      authorizationService.verifyAdministratorPrivileges();
    } catch (Exception e) {
      try {
        authorizationService.verifyBlockViewerPrivileges(codespace);
      } catch (Exception ex) {
        throw new NuskaException("No block viewer privileges");
      }
    }
  }
}
