package io.dropwizard.bundles.version.suppliers;

import com.google.common.collect.Maps;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import io.dropwizard.bundles.version.VersionSupplier;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.regex.Pattern;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A version number supplier that reads the pom.properties file that maven includes inside of jars
 * that it packages.
 */
public class MavenVersionSupplier implements VersionSupplier {
  private static final Logger LOG = LoggerFactory.getLogger(MavenVersionSupplier.class);
  private static final Pattern POM_PROPERTIES = Pattern.compile("pom\\.properties");

  private final String version;
  private final SortedMap<String, String> dependencyVersions = Maps.newTreeMap();

  /**
   * Construct a MavenVersionSupplier that uses the specified group and artifact as the main
   * artifact whose version to report.
   *
   * @param mainArtifactGroupId The maven group of the main artifact.
   * @param mainArtifactId      The maven artifact id of the main artifact.
   */
  public MavenVersionSupplier(String mainArtifactGroupId, String mainArtifactId) {
    Configuration config = new ConfigurationBuilder()
        .setUrls(ClasspathHelper.forJavaClassPath())
        .addScanners(new ResourcesScanner());

    Set<String> paths = new Reflections(config).getResources(POM_PROPERTIES);
    for (String path : paths) {
      MavenArtifact artifact = load(path);

      // Use maven style keys for artifacts "<groupId>:<artifactId>".
      String key = String.format("%s:%s", artifact.getGroupId(), artifact.getArtifactId());
      dependencyVersions.put(key, artifact.getVersion());
    }

    String key = String.format("%s:%s", mainArtifactGroupId, mainArtifactId);
    version = dependencyVersions.get(key);
  }

  @Override
  public String getApplicationVersion() {
    return version;
  }

  @Override
  public Map<String, String> getDependencyVersions() {
    return dependencyVersions;
  }

  /**
   * Load a given pom.properties file and convert its contents into a {@code MavenArtifact}
   * instance.  If the path is not loadable or for some reason isn't structured like a standard
   * maven pom.properties file, then {@code null} is returned instead.
   */
  private static MavenArtifact load(String path) {
    URL url = Resources.getResource(path);

    ByteSource source;
    try {
      source = Resources.asByteSource(url);
    } catch (IllegalArgumentException e) {
      LOG.warn("Unable to find maven pom.properties file: path:{}", path, e);
      return null;
    }

    InputStream in;
    try {
      in = source.openBufferedStream();
    } catch (IOException e) {
      LOG.warn("Unable to open maven pom.properties file: path:{}", path, e);
      return null;
    }

    Properties props = new Properties();
    try {
      props.load(in);
    } catch (IOException e) {
      LOG.warn("Error while loading maven pom.properties file: path:{}", path, e);
      return null;
    }

    String groupId = props.getProperty("groupId");
    String artifactId = props.getProperty("artifactId");
    String version = props.getProperty("version");

    if (groupId == null || artifactId == null || version == null) {
      LOG.warn("Property missing in pom.properties file: path:{}, group:{}, artifact:{}, "
               + "version:{}", groupId, artifactId, version);
      return null;
    }

    return new MavenArtifact(groupId, artifactId, version);
  }

  private static final class MavenArtifact {
    private final String groupId;
    private final String artifactId;
    private final String version;

    MavenArtifact(String groupId, String artifactId, String version) {
      this.groupId = checkNotNull(groupId);
      this.artifactId = checkNotNull(artifactId);
      this.version = checkNotNull(version);
    }

    public String getGroupId() {
      return groupId;
    }

    public String getArtifactId() {
      return artifactId;
    }

    public String getVersion() {
      return version;
    }
  }
}
