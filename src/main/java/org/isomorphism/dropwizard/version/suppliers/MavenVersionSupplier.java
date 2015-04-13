package org.isomorphism.dropwizard.version.suppliers;

import com.google.common.io.ByteSource;
import com.google.common.io.Closeables;
import com.google.common.io.Resources;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import org.isomorphism.dropwizard.version.VersionSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A version number supplier that reads the pom.properties file that maven includes inside of jars
 * that it packages.
 */
public class MavenVersionSupplier implements VersionSupplier {
  private static final Logger LOG = LoggerFactory.getLogger(MavenVersionSupplier.class);

  private final String group;
  private final String artifact;
  private final String path;

  /**
   * Construct a MavenVersionSupplier that uses the specified group and artifact as the artifact
   * whose version to report.
   *
   * @param group    The maven group of the main artifact.
   * @param artifact The maven artifact id of the main artifact.
   */
  public MavenVersionSupplier(String group, String artifact) {
    this.group = checkNotNull(group);
    this.artifact = checkNotNull(artifact);
    this.path = String.format("META-INF/maven/%s/%s/pom.properties", group, artifact);
  }

  /**
   * Read the artifact's version from the pom.properties file in the classpath.
   */
  @Override
  public String get() {
    ByteSource source;
    try {
      URL url = Resources.getResource(path);
      source = Resources.asByteSource(url);
    } catch (IllegalArgumentException e) {
      LOG.warn("Unable to find maven pom.properties file: group:{}, artifact:{}, path:{}",
               group, artifact, path, e);
      return null;
    }

    InputStream in;
    try {
      in = source.openBufferedStream();
    } catch (IOException e) {
      LOG.error("Unable to open maven pom.properties file: group:{}, artifact:{}, path:{}",
                group, artifact, path, e);
      return null;
    }

    String version;
    try {
      Properties props = new Properties();
      try {
        props.load(in);
      } catch (IOException e) {
        LOG.error("Error while loading maven pom.properties file: group:{}, artifact:{}, path:{}",
                  group, artifact, path, e);
        return null;
      }

      version = props.getProperty("version");
      LOG.debug("Loaded the version for maven artifact: group:{}, artifact:{}, path:{}, version:{}",
                group, artifact, path, version);
    } finally {
      Closeables.closeQuietly(in);
    }

    return version;
  }
}
