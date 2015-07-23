package io.dropwizard.bundles.version;

import static com.google.common.base.Preconditions.checkNotNull;

import io.dropwizard.Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * A Dropwizard bundle that will expose a version number of the application via
 * a servlet on the admin console port. The way that the bundle discovers the
 * application's version number is configurable via a {@code VersionSupplier}.
 * The provided {@code VersionSupplier} implementation will be called a single
 * time and the value it returns will be memoized for the life of the JVM.
 */
public class VersionBundle implements Bundle {
    private static final String DEFAULT_URL = "/version";

    private final VersionSupplier supplier;
    private final String url;

    /**
     * Construct the VersionBundle using the provided version number supplier.
     * The version number will be exposed on the Dropwizard admin port on the
     * default URL.
     *
     * @param supplier
     *            The version number supplier.
     */
    public VersionBundle(VersionSupplier supplier) {
        this(supplier, DEFAULT_URL);
    }

    /**
     * Construct a VersionBundle using the provided version number supplier. The
     * version number will be exposed on the Dropwizard admin port at the
     * specified URL.
     *
     * @param supplier
     *            The version number supplier.
     * @param url
     *            The URL to expose the version number on.
     */
    public VersionBundle(VersionSupplier supplier, String url) {
        checkNotNull(supplier);
        checkNotNull(url);

        this.supplier = supplier;
        this.url = url;
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        // Nothing to do here
    }

    @Override
    public void run(Environment environment) {
        VersionServlet servlet = new VersionServlet(supplier, environment.getObjectMapper());
        environment.servlets().addServlet("version", servlet).addMapping(url);
        environment.admin().addServlet("version", servlet).addMapping(url);
    }
}
