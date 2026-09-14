package no.entur.nuska.model;

import java.util.Locale;
import no.entur.nuska.BadRequestException;

/**
 * The variants of an imported dataset available in the nisaba bucket during the transition to the
 * NeTEx 1.16 DatedServiceJourney structure.
 * <p>
 * The import pipeline stores every uploaded dataset in three folders of the bucket: the dataset as
 * uploaded (NeTEx 1.16 after the upgrade), a copy downgraded to NeTEx 1.15, and a copy of one of
 * these two variants in the default folder.
 */
public enum NetexDsjVariant {
  /**
   * The variant selected as default by the import pipeline.
   */
  DEFAULT("imported/"),
  /**
   * NeTEx 1.15, obtained by downgrading the uploaded dataset.
   */
  LEGACY("imported-dsj-legacy/"),
  /**
   * NeTEx 1.16, the dataset as uploaded by the data provider.
   */
  NEW("imported-dsj-new/");

  private final String subPath;

  NetexDsjVariant(String subPath) {
    this.subPath = subPath;
  }

  /**
   * Folder of the nisaba bucket holding this variant of the imported datasets.
   */
  public String subPath() {
    return subPath;
  }

  /**
   * Resolve the {@code dsjcompatibility} request parameter of the timetable API.
   *
   * @param dsjCompatibility the request parameter, {@code legacy} or {@code new}; null or blank for
   *                         the default variant.
   * @throws BadRequestException if the parameter has another value.
   */
  public static NetexDsjVariant resolve(String dsjCompatibility) {
    if (dsjCompatibility == null || dsjCompatibility.isBlank()) {
      return DEFAULT;
    }
    NetexDsjVariant variant;
    try {
      variant =
        NetexDsjVariant.valueOf(
          dsjCompatibility.trim().toUpperCase(Locale.ROOT)
        );
    } catch (IllegalArgumentException e) {
      variant = DEFAULT;
    }
    if (variant == DEFAULT) {
      throw new BadRequestException(
        "Invalid value for parameter dsjcompatibility: '" +
        dsjCompatibility +
        "', expected legacy or new"
      );
    }
    return variant;
  }
}
