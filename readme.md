# dropwizard-version-bundle

A [Dropwizard][dropwizard] bundle that exposes the version of your application via the admin port.

[![Build Status](https://secure.travis-ci.org/bbeck/dropwizard-version-bundle.png?branch=master)]
(http://travis-ci.org/bbeck/dropwizard-version-bundle)


## Getting Started

Just add this maven dependency to get started:

```xml
<dependency>
  <groupId>org.isomorphism.dropwizard</groupId>
  <artifactId>dropwizard-version-bundle</artifactId>
  <version>0.1.0</version>
</dependency>
```

Add the bundle to your environment using your choice of version supplier:

```java
public class MyApplication extends Application<Configuration> {
  @Override
  public void initialize(Bootstrap<Configuration> bootstrap) {
    VersionSupplier supplier = new MavenVersionSupplier("group", "artifact");
    bootstrap.addBundle(new VersionBundle(supplier));
  }

  @Override
  public void run(Configuration cfg, Environment env) throws Exception {
    // ...
  }
}
```

Now you can access the the `/version` URL on the admin port of your application to see the version
of your application.

For example if your application were running on `localhost` with the admin server on port 8081 then
something like the following would show you your application's version.

```bash
curl localhost:8081/version
```


## Customizing version supplier

By default the bundle only comes with a single version supplier `MavenVersionSupplier` that will
discover the version information for a particular maven artifact by reading the `pom.properties`
file in a maven produced jar.

The `VersionSupplier` that plugs into the bundle is customizable however so you can define your own
implementation if your needs are different.  If you implement a new `VersionSupplier` that is
generally useful for others then please feel free to submit a pull request.


[dropwizard]: http://dropwizard.io