package io.dropwizard.bundles.version;

import java.util.Map;

/**
 * Supplier interface that provides an application's version number.
 */
public interface VersionSupplier {
  /**
   * Return the application's version number.
   */
  String getApplicationVersion();

  /**
   * Return the version numbers of dependencies.
   */
  Map<String, String> getDependencyVersions();
}
