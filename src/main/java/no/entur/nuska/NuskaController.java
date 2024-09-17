package no.entur.nuska;

import no.entur.nuska.security.NuskaAuthorizationService;
import no.entur.nuska.service.NisabaBlobStoreService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
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

    try {
      canAccessBlocks(codespace);
      ByteArrayResource latestBlob = blobStoreService.getLatestBlob(codespace);

      if (latestBlob != null && latestBlob.exists()) {
        return ResponseEntity
          .ok()
          .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + latestBlob.getFilename() + "\""
          )
          .body(latestBlob);
      } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
      }
    } catch (Exception e) {
      throw new NuskaException("Failed to download timetable data", e);
    }
  }

  private void canAccessBlocks(String codespace) {
    try {
      authorizationService.verifyBlockViewerPrivileges(codespace);
    } catch (Exception e) {
      throw new NuskaException("No block viewer privileges");
    }
  }
}
