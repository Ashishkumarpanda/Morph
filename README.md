# Morph

[![Maven Central](https://img.shields.io/maven-central/v/io.github.ashishkumarpanda/zeromapper-spring)](https://central.sonatype.com/artifact/io.github.ashishkumarpanda/zeromapper-spring/0.1.0)

**Morph** (Maven artifacts: `io.github.ashishkumarpanda:zeromapper-*`) is a lightweight, annotation-driven Java library that eliminates almost all manual Entity ↔ DTO mapping in Spring Boot applications.

- **Website:** [ashishkumarpanda.github.io/Morph-Portfolio](https://ashishkumarpanda.github.io/Morph-Portfolio/)
- **Developer guide:** [Download Word doc](https://ashishkumarpanda.github.io/Morph-Portfolio/assets/Morph-Developer-Guide.docx)
- **Demo app:** [Morph-Demo](https://github.com/Ashishkumarpanda/Morph-Demo)

Unlike ModelMapper, you do not configure a generic reflection engine. Unlike MapStruct, you do not write mapper interfaces. You create DTOs — Morph maps everything else automatically.

## Why Morph?

Every Spring Boot project accumulates hundreds of lines of mapping code:

- builders and setters
- MapStruct `@Mapper` interfaces
- `stream().map(...)`
- `BeanPropertyRowMapper` and custom `RowMapper` implementations

Morph removes that boilerplate with a zero-config API:

```java
UserDto dto = Mapper.map(user, UserDto.class);
List<UserDto> dtos = Mapper.list(users, UserDto.class);
User entity = Mapper.map(dto, User.class);
```

## Requirements

- Java 17+
- Spring Boot 3+ (optional, via `zeromapper-spring`)
- Jakarta EE packages

## Quick Start

### Maven

```xml
<dependency>
    <groupId>io.github.ashishkumarpanda</groupId>
    <artifactId>zeromapper-spring</artifactId>
    <version>0.1.0</version>
</dependency>
```

For core-only usage (no Spring):

```xml
<dependency>
    <groupId>io.github.ashishkumarpanda</groupId>
    <artifactId>zeromapper-core</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Basic mapping

```java
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;

    @From("address.city")
    private String city;

    @Expression("firstName + ' ' + lastName")
    private String fullName;

    @IgnoreMapping
    private String password;
}

UserDto dto = Mapper.map(userEntity, UserDto.class);
```

Matching field names map automatically. No annotations required for simple cases.

## Modules

| Module | Purpose |
|--------|---------|
| `zeromapper-annotations` | `@From`, `@IgnoreMapping`, `@Expression`, etc. |
| `zeromapper-core` | Mapping engine, type conversion, caching |
| `zeromapper-spring` | Spring Boot auto-configuration + starter |
| `zeromapper-jdbc` | `JdbcTemplate` / native query row mapping |
| `zeromapper-jpa` | JPA `Tuple` and native query mapping |
| `zeromapper-test` | JUnit assertion helpers |
| `zeromapper-benchmarks` | JMH performance benchmarks |

## JDBC

```java
List<UserDto> users = JdbcMapper.query(jdbcTemplate, "SELECT * FROM users", UserDto.class);
```

No `RowMapper`. No `BeanPropertyRowMapper`.

## Spring Boot

Auto-configuration is enabled automatically when `zeromapper-spring` is on the classpath:

```java
@Service
class UserService {
    private final MorphMapper mapper;

    UserService(MorphMapper mapper) {
        this.mapper = mapper;
    }

    UserDto toDto(User user) {
        return mapper.map(user, UserDto.class);
    }
}
```

Optional configuration:

```yaml
morph:
  debug: true
```

## Annotations

| Annotation | Purpose |
|------------|---------|
| `@From("path")` | Map from nested/renamed source property |
| `@IgnoreMapping` | Skip a field |
| `@Expression("...")` | Computed field |
| `@MapperIgnoreNull` | Do not write null values |
| `@MapperComponent` | Register custom Spring mapping component |

## Features

- Entity ↔ DTO bidirectional mapping
- Collection, `Set`, `Stream`, and `Page` mapping
- Nested property paths with null-safe navigation
- Enum, UUID, numeric, and date/time conversion
- Java `record` and constructor mapping
- `Map<String, Object>` ↔ POJO
- Cached metadata and MethodHandle-based access
- Meaningful exceptions for missing fields, conversion failures, and circular references

## Build

```bash
mvn verify
```

Benchmarks:

```bash
mvn -pl zeromapper-benchmarks package
java -jar zeromapper-benchmarks/target/benchmarks.jar
```

## Documentation

The full developer guide (annotations, Spring Boot setup, JDBC/JPA, testing, and best practices) is available on the [Morph Portfolio site](https://ashishkumarpanda.github.io/Morph-Portfolio/#docs).

## Roadmap

- [ ] Full SpEL expression support
- [ ] Lombok `@Builder` integration
- [ ] Jackson `JsonNode` mapping module
- [ ] MapStruct / ModelMapper benchmark suite

## License

Apache License 2.0 — see [LICENSE](LICENSE).
