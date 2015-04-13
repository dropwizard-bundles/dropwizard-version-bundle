package io.dropwizard.bundles.version;

import com.google.common.base.Supplier;
import com.google.common.io.Closeables;
import java.io.IOException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.common.base.Preconditions.checkNotNull;

class VersionServlet extends HttpServlet {
  private final Supplier<String> supplier;

  VersionServlet(Supplier<String> supplier) {
    this.supplier = checkNotNull(supplier);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String version = supplier.get();
    if (version == null) {
      resp.sendError(500, "Unable to determine version.");
      return;
    }

    resp.setStatus(200);
    resp.setContentType("text/plain");

    ServletOutputStream out = resp.getOutputStream();
    try {
      out.print(version);
    } finally {
      Closeables.close(out, false);
    }
  }
}
