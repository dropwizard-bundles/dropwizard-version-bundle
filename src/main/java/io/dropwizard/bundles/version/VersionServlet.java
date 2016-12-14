package io.dropwizard.bundles.version;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import io.dropwizard.jackson.Jackson;
import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A simple servlet that will return the application version along with the versions of its
 * dependencies as a JSON object.  For example:
 * <pre>
 *   {
 *     application: "1.0.0-SNAPSHOT",
 *     dependencies: {
 *       "com.google.guava:guava": "18.0",
 *       "io.dropwizard:dropwizard-core": "0.8.1"
 *     }
 *   }
 * </pre>
 *
 * <p><b>NOTE:</b> This class intentionally delays asking the provided {@code VersionSupplier} for
 * versions until the servlet is called.  Because of this, the supplier will be invoked for each
 * request to the servlet.  The provided implementation of {@code VersionSupplier} should memoize
 * its results so that a recalculation doesn't happen on every request.</p>
 */
class VersionServlet extends HttpServlet {
  private static final long serialVersionUID = 0L;

  private final VersionSupplier supplier;
  private final ObjectMapper objectMapper;

  VersionServlet(VersionSupplier supplier, ObjectMapper objectMapper) {
    this.supplier = checkNotNull(supplier);
    this.objectMapper = checkNotNull(objectMapper);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String applicationVersion;
    try {
      applicationVersion = supplier.getApplicationVersion();
    } catch (Throwable t) {
      resp.sendError(500, "Unable to determine application version.");
      return;
    }

    Map<String, String> dependencyVersions;
    try {
      dependencyVersions = supplier.getDependencyVersions();
    } catch (Throwable t) {
      resp.sendError(500, "Unable to determine dependency versions.");
      return;
    }

    Map<String, Object> response = Maps.newHashMap();
    response.put("application", applicationVersion);
    response.put("dependencies", dependencyVersions);

    // At this point we've collected all of the data we need, we know the result will be a success.
    resp.setStatus(200);
    resp.setContentType("application/json");

    ServletOutputStream out = resp.getOutputStream();
    try {
      objectMapper.writeValue(out, response);
    } finally {
      Closeables.close(out, false);
    }
  }
}
