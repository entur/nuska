package no.entur.nuska.model;

import static org.junit.jupiter.api.Assertions.*;

import no.entur.nuska.BadRequestException;
import org.junit.jupiter.api.Test;

class NetexDsjVariantTest {

  @Test
  void testResolveMissingParameter() {
    assertEquals(NetexDsjVariant.DEFAULT, NetexDsjVariant.resolve(null));
    assertEquals(NetexDsjVariant.DEFAULT, NetexDsjVariant.resolve(""));
    assertEquals(NetexDsjVariant.DEFAULT, NetexDsjVariant.resolve("  "));
  }

  @Test
  void testResolveLegacy() {
    assertEquals(NetexDsjVariant.LEGACY, NetexDsjVariant.resolve("legacy"));
    assertEquals(NetexDsjVariant.LEGACY, NetexDsjVariant.resolve(" LEGACY "));
  }

  @Test
  void testResolveNew() {
    assertEquals(NetexDsjVariant.NEW, NetexDsjVariant.resolve("new"));
    assertEquals(NetexDsjVariant.NEW, NetexDsjVariant.resolve(" New "));
  }

  @Test
  void testResolveInvalidValue() {
    assertThrows(
      BadRequestException.class,
      () -> NetexDsjVariant.resolve("old")
    );
  }

  @Test
  void testResolveDefaultVariantNotAccepted() {
    assertThrows(
      BadRequestException.class,
      () -> NetexDsjVariant.resolve("default")
    );
  }

  @Test
  void testSubPath() {
    assertEquals("imported/", NetexDsjVariant.DEFAULT.subPath());
    assertEquals("imported-dsj-legacy/", NetexDsjVariant.LEGACY.subPath());
    assertEquals("imported-dsj-new/", NetexDsjVariant.NEW.subPath());
  }
}
