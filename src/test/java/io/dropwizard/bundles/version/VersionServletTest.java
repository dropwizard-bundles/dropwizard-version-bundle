package io.dropwizard.bundles.version;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import io.dropwizard.jackson.Jackson;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VersionServletTest {
  private static final ObjectMapper OBJECT_MAPPER = Jackson.newObjectMapper();
  private static final String PATH = "/version";

  private final ServletTester tester = new ServletTester();
  private final VersionSupplier supplier = mock(VersionSupplier.class);

  @Before
  public void setup() throws Exception {
    tester.addServlet(new ServletHolder(new VersionServlet(supplier, OBJECT_MAPPER)), PATH);
    tester.start();
  }

  @After
  public void teardown() throws Exception {
    tester.stop();
  }

  @Test
  public void testNonNullApplicationVersion() {
    when(supplier.getApplicationVersion()).thenReturn("version");

    HttpTester.Response response = get();
    assertEquals(200, response.getStatus());

    JsonNode root = fromJson(response.getContent());
    assertEquals("version", root.get("application").textValue());
  }

  @Test
  public void testNullApplicationVersion() {
    when(supplier.getApplicationVersion()).thenReturn(null);

    HttpTester.Response response = get();
    assertEquals(200, response.getStatus());

    JsonNode root = fromJson(response.getContent());
    assertNull(root.get("application").textValue());
  }

  @Test
  public void testThrowsApplicationVersionException() {
    RuntimeException exception = new RuntimeException();
    when(supplier.getApplicationVersion()).thenThrow(exception);

    HttpTester.Response response = get();
    assertEquals(500, response.getStatus());
  }

  @Test
  public void testNonNullDependencyVersion() {
    when(supplier.getDependencyVersions()).thenReturn(map("guava", "version"));

    HttpTester.Response response = get();
    assertEquals(200, response.getStatus());

    JsonNode root = fromJson(response.getContent());
    assertEquals("version", root.get("dependencies").get("guava").textValue());
  }

  @Test
  public void testNullDependencyVersion() {
    when(supplier.getDependencyVersions()).thenReturn(map("guava", null));

    HttpTester.Response response = get();
    assertEquals(200, response.getStatus());

    JsonNode root = fromJson(response.getContent());
    assertNull(root.get("dependencies").get("guava").textValue());
  }

  @Test
  public void testThrowsDependencyVersionException() {
    RuntimeException exception = new RuntimeException();
    when(supplier.getDependencyVersions()).thenThrow(exception);

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
      throw new RuntimeException(e);
    }

    return response;
  }

  private static JsonNode fromJson(String s) {
    try {
      return OBJECT_MAPPER.readTree(s);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static Map<String, String> map(String key, String value) {
    Map<String, String> m = Maps.newHashMap();
    m.put(key, value);
    return m;
  }
}
