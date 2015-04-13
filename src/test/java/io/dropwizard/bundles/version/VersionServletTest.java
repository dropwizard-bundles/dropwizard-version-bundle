package io.dropwizard.bundles.version;

import com.google.common.base.Throwables;
import java.nio.ByteBuffer;
import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VersionServletTest {
  private static final String PATH = "/version";

  private final ServletTester tester = new ServletTester();
  private final SettableVersionSupplier supplier = new SettableVersionSupplier();

  @Before
  public void setup() throws Exception {
    tester.addServlet(new ServletHolder(new VersionServlet(supplier)), PATH);
    tester.start();
  }

  @After
  public void teardown() throws Exception {
    tester.stop();
  }

  @Test
  public void testNonNullVersion() {
    supplier.set("version");

    HttpTester.Response response = get();
    assertEquals(200, response.getStatus());
    assertEquals("version", response.getContent());
  }

  @Test
  public void testNullVersion() {
    supplier.set(null);

    HttpTester.Response response = get();
    assertEquals(500, response.getStatus());
  }

  private HttpTester.Response get() {
    HttpTester.Request request = HttpTester.newRequest();
    request.setMethod("GET");
    request.setVersion("HTTP/1.0");
    request.setURI(PATH);

    HttpTester.Response response;
    try {
      ByteBuffer raw = request.generate();
      ByteBuffer responses = tester.getResponses(raw);
      response = HttpTester.parseResponse(responses);
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }

    return response;
  }

  private static final class SettableVersionSupplier implements VersionSupplier {
    private String version;

    public void set(String version) {
      this.version = version;
    }

    @Override
    public String get() {
      return version;
    }
  }
}
