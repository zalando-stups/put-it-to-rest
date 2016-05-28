# Put it to REST!

[![Relaxing frog](docs/frog.jpg)](https://pixabay.com/en/frog-meadow-relaxed-relaxation-fig-1109795/)

[![Build Status](https://img.shields.io/travis/zalando/put-it-to-rest.svg)](https://travis-ci.org/zalando/put-it-to-rest)
[![Coverage Status](https://img.shields.io/coveralls/zalando/put-it-to-rest.svg)](https://coveralls.io/r/zalando/put-it-to-rest)
[![Release](https://img.shields.io/github/release/zalando/put-it-to-rest.svg)](https://github.com/zalando/put-it-to-rest/releases)
[![Maven Central](https://img.shields.io/maven-central/v/org.zalando/put-it-to-rest-parent.svg)](https://maven-badges.herokuapp.com/maven-central/org.zalando/put-it-to-rest)

Spring Boot REST client auto configuration

- **Technology stack**: Spring Boot
- **Status**:  Alpha

```yaml
rest.clients:
  example:
    base-url: http://example.com
    oauth.scopes: [uid]
```

```java
@Autowired
@Qualifier("example")
private Rest example;

example.execute(GET, "/");
```

## Features

- TODO

## Dependencies

- Java 8
- Any build tool using Maven Central, or direct download
- Spring Boot
- Apache HTTP Client
- [Riptide](https://github.com/zalando/riptide)
- [Logbook](https://github.com/zalando/logbook) (optional)
- [Tracer](https://github.com/zalando/tracer) (optional)
- [Tokens](https://github.com/zalando-stups/tokens)
- [STUPS Spring OAuth2 Client](https://github.com/zalando-stups/stups-spring-oauth2-support/tree/master/stups-spring-oauth2-client)
- [Jackson 2](https://github.com/FasterXML/jackson)

## Installation

Add the following dependency to your project:

```xml
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>put-it-to-rest</artifactId>
    <version>${put-it-to-rest.version}</version>
</dependency>
```

## Configuration

```yaml
rest:
  oauth:
    access-token-url: https://auth.example.com
    scheduling-period: 10
    timeouts:
      connect: 1
      read: 2
  clients:
    example:
      base-url: https://example.com
      oauth:
        scopes:
          - uid
          - example.read
      timeouts:
        connect: 5
        read: 5
```

| Configuration                        | Type            | Default                                            |
|--------------------------------------|-----------------|----------------------------------------------------|
| `rest.oauth.access-token-url`        | `URI`           | required, can be overridden by `ACCESS_TOKEN_URL`  |
| `rest.oauth.scheduling-period`       | `int` (seconds) | `5`                                                |
| `rest.oauth.timeouts.connect`        | `int` (seconds) | `1`                                                |
| `rest.oauth.timeouts.read`           | `int` (seconds) | `2`                                                |
| `rest.clients.<id>.base-url`         | `URI`           | none                                               |
| `rest.clients.<id>.oauth`            |                 | none, disables OAuth2 security if omitted          |
| `rest.clients.<id>.oauth.scopes`     | `List<String>`  | none                                               |
| `rest.clients.<id>.timeouts.connect` | `int` (seconds) | `5`                                                |
| `rest.clients.<id>.timeouts.read`    | `int` (seconds) | `5`                                                |

`restAccessToken` `AccessTokens`

### Beans

TODO bean names, qualifier, client id

| Bean Name                              | Bean Type                                   |
|----------------------------------------|---------------------------------------------|
| `exampleHttpClient`                    | `HttpClient`                                |
| `exampleClientHttpRequestFactory`      | `ClientHttpRequestFactory`                  |
| `exampleHttpMessageConverters`         | `HttpMessageConverters`                     |
| `exampleRestTemplate`                  | `RestTemplate` or `StupsOAuth2RestTemplate` |
| `exampleRest`                          | `Rest`                                      |
| `exampleHttpAsyncClient`               | `HttpAsyncClient`                           |
| `exampleAsyncClientHttpRequestFactory` | `AsyncClientHttpRequestFactory`             |
| `exampleAsyncRestTemplate`             | `AsyncRestTemplate`                         |
| `exampleAsyncRest`                     | `AsyncRest`                                 |

![Client Dependency Graph](docs/graph.png)

## Usage

TODO

## Getting Help

If you have questions, concerns, bug reports, etc., please file an issue in this repository's
[Issue Tracker](issues).

## Getting Involved/Contributing

To contribute, simply make a pull request and add a brief description (1-2 sentences) of your addition or change. For
more details, check the [contribution guidelines](CONTRIBUTING.md).
