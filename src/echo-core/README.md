# Echo: Core Library

The Core library provides domain-specific functionality for a podcast search engine. This library is written in Java and therefore prodices a Java API. It should be accessible for Java-compatible technologies. We've tested interoperability with Scala.

Specifically, the library provides solutions for the following issues:

* Database entity classes, see the [echo.core.domain.entity](src/main/java/echo/core/domain/entity/) package.

* Data transfer objects (DTO) for domain information. The standard DTOs are Java interfaces. This library builds on [immutables](https://immutables.github.io) to provide immutables and modifiable implementations of these interfaces. The instances are safe to use with Java-based actor libraries, since they withstand the perils of shared memory environments like the JVM.

* Interfaces to write structured information to and search within a [Lucene](https://lucene.apache.org) reverse index structure. See the [echo.core.index](src/main/java/echo/core/index/) package.

* A parser to transform [RSS 2.0](http://​cyber.​harvard.​edu/​rss/​rss.​html) and [Atom](https://​tools.​ietf.​org/​html/​rfc4287) based feed XML data to domain DTOs. The parser also extracts information from the iTunes namespaces `xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd"` as well as chapter marks from the [Podlove](https://​podlove.​org/​simple-​chapters/​) namespace `xmlns:psc="http://podlove.org/simple-chapters/"`. See the [echo.core.parse.rss](src/main/java/echo/core/parse/rss/) package. The `FeedParser` interfaces has one implementation based on the [ROME](https://rometools.github.io/rome/) framework. 

* APIs for podcast directories to find unindexed podcast feeds. The only complete implementation is for the [Fyyd](https://fyyd.de) podcast directory. See the [echo.core.parse.api](src/main/java/echo/core/parse/api/) package.

* An HTTP client wrapper for that is mindful of common issues regarding Podcast feeds (e.g. links to media files where websites are expected). See [echo.core.http.HttpClient](src/main/scala/echo/core/http/HttpClient.scala).

In order to use this library within a search engine architecture, it must be installed into the [local Maven repository](https://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html). Use [Gradle](https://gradle.org) to build the library JAR and add it to Maven local:

    ./gradlew clean build publishToMavenLocal

This library also provides an interactive REPL to test some features. Run the REPL application with:

    ./gradlew run