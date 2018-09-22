package io.dropwizard.bundles.version;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Dropwizard bundle that will expose a version number of the application via a servlet on the
 * admin console port.  The way that the bundle discovers the application's version number is
 * configurable via a {@code VersionSupplier}.  The provided {@code VersionSupplier} implementation
 * will be called a single time and the value it returns will be memoized for the life of the JVM.
 */
public abstract class ConfiguredVersionBundle<T extends Configuration>
        implements ConfiguredBundle<T> {
  private static final String DEFAULT_URL = "/version";

  private final String url;

  /**
   * Construct the VersionBundle.  The version number
   * will be exposed on the Dropwizard admin port on the default URL.
   */
  public ConfiguredVersionBundle() {
    this(DEFAULT_URL);
  }

  /**
   * Construct a VersionBundle.  The version number will
   * be exposed on the Dropwizard admin port at the specified URL.
   *
   * @param url The URL to expose the version number on.
   */
  public ConfiguredVersionBundle(String url) {
    checkNotNull(url);

    this.url = url;
  }

  public abstract VersionSupplier provideSupplier(T configuration);

  @Override
  public void initialize(Bootstrap<?> bootstrap) {
    // Nothing to do here
  }

  @Override
  public void run(T configuration, Environment environment) {
    VersionServlet servlet = new VersionServlet(provideSupplier(configuration),
            environment.getObjectMapper());
    environment.admin().addServlet("version", servlet).addMapping(url);
  }
}
