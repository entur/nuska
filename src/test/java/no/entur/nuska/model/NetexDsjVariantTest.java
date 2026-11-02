package no.entur.nuska.model;

import static org.junit.jupiter.api.Assertions.*;

import no.entur.nuska.BadRequestException;
import org.junit.jupiter.api.Test;

class NetexDsjVariantTest {

  @Test
  void testResolveMissingParameterReturnsConfiguredDefault() {
    assertEquals(
      NetexDsjVariant.NEW,
      NetexDsjVariant.resolve(null, NetexDsjVariant.NEW)
    );
    assertEquals(
      NetexDsjVariant.NEW,
      NetexDsjVariant.resolve("", NetexDsjVariant.NEW)
    );
    assertEquals(
      NetexDsjVariant.LEGACY,
      NetexDsjVariant.resolve("  ", NetexDsjVariant.LEGACY)
    );
    assertEquals(
      NetexDsjVariant.DEFAULT,
      NetexDsjVariant.resolve(null, NetexDsjVariant.DEFAULT)
    );
  }

  @Test
  void testResolveLegacy() {
    assertEquals(
      NetexDsjVariant.LEGACY,
      NetexDsjVariant.resolve("legacy", NetexDsjVariant.NEW)
    );
    assertEquals(
      NetexDsjVariant.LEGACY,
      NetexDsjVariant.resolve(" LEGACY ", NetexDsjVariant.NEW)
    );
  }

  @Test
  void testResolveNew() {
    assertEquals(
      NetexDsjVariant.NEW,
      NetexDsjVariant.resolve("new", NetexDsjVariant.LEGACY)
    );
    assertEquals(
      NetexDsjVariant.NEW,
      NetexDsjVariant.resolve(" New ", NetexDsjVariant.LEGACY)
    );
  }

  @Test
  void testResolveInvalidValue() {
    assertThrows(
      BadRequestException.class,
      () -> NetexDsjVariant.resolve("old", NetexDsjVariant.NEW)
    );
  }

  @Test
  void testResolveDefaultVariantNotAccepted() {
    assertThrows(
      BadRequestException.class,
      () -> NetexDsjVariant.resolve("default", NetexDsjVariant.NEW)
    );
  }

  @Test
  void testFromConfiguration() {
    assertEquals(NetexDsjVariant.NEW, NetexDsjVariant.fromConfiguration("new"));
    assertEquals(
      NetexDsjVariant.LEGACY,
      NetexDsjVariant.fromConfiguration(" Legacy ")
    );
    assertEquals(
      NetexDsjVariant.DEFAULT,
      NetexDsjVariant.fromConfiguration("DEFAULT")
    );
  }

  @Test
  void testFromConfigurationInvalidValue() {
    assertThrows(
      IllegalArgumentException.class,
      () -> NetexDsjVariant.fromConfiguration("old")
    );
    assertThrows(
      IllegalArgumentException.class,
      () -> NetexDsjVariant.fromConfiguration(null)
    );
    assertThrows(
      IllegalArgumentException.class,
      () -> NetexDsjVariant.fromConfiguration(" ")
    );
  }

  @Test
  void testSubPath() {
    assertEquals("imported/", NetexDsjVariant.DEFAULT.subPath());
    assertEquals("imported-dsj-legacy/", NetexDsjVariant.LEGACY.subPath());
    assertEquals("imported-dsj-new/", NetexDsjVariant.NEW.subPath());
  }
}
