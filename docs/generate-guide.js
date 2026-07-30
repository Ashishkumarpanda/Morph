const fs = require("fs");
const path = require("path");
const {
  Document,
  Packer,
  Paragraph,
  TextRun,
  HeadingLevel,
  Table,
  TableRow,
  TableCell,
  WidthType,
  AlignmentType,
  PageBreak,
  BorderStyle,
  ShadingType,
} = require("docx");

const CODE_FONT = "Consolas";
const CODE_SIZE = 20; // half-points, 10pt

function title(text) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_1,
    spacing: { before: 360, after: 200 },
    children: [new TextRun({ text, bold: true, size: 32 })],
  });
}

function heading2(text) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_2,
    spacing: { before: 280, after: 160 },
    children: [new TextRun({ text, bold: true, size: 28 })],
  });
}

function heading3(text) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_3,
    spacing: { before: 200, after: 120 },
    children: [new TextRun({ text, bold: true, size: 24 })],
  });
}

function body(text, options = {}) {
  return new Paragraph({
    spacing: { after: 160, line: 276 },
    children: [
      new TextRun({
        text,
        size: 22,
        bold: options.bold || false,
        italics: options.italics || false,
      }),
    ],
  });
}

function bullet(text) {
  return new Paragraph({
    bullet: { level: 0 },
    spacing: { after: 80 },
    children: [new TextRun({ text, size: 22 })],
  });
}

function codeBlock(lines) {
  const text = Array.isArray(lines) ? lines.join("\n") : lines;
  return new Paragraph({
    spacing: { before: 120, after: 120 },
    shading: { type: ShadingType.CLEAR, fill: "F4F4F4" },
    indent: { left: 360, right: 360 },
    children: [
      new TextRun({
        text,
        font: CODE_FONT,
        size: CODE_SIZE,
      }),
    ],
  });
}

function table(headers, rows) {
  const headerCells = headers.map(
    (h) =>
      new TableCell({
        width: { size: 100 / headers.length, type: WidthType.PERCENTAGE },
        shading: { fill: "E8EEF4", type: ShadingType.CLEAR },
        children: [
          new Paragraph({
            children: [new TextRun({ text: h, bold: true, size: 20 })],
          }),
        ],
      })
  );

  const dataRows = rows.map(
    (row) =>
      new TableRow({
        children: row.map(
          (cell) =>
            new TableCell({
              width: { size: 100 / headers.length, type: WidthType.PERCENTAGE },
              children: [
                new Paragraph({
                  children: [new TextRun({ text: cell, size: 20 })],
                }),
              ],
            })
        ),
      })
  );

  return new Table({
    width: { size: 100, type: WidthType.PERCENTAGE },
    rows: [new TableRow({ children: headerCells }), ...dataRows],
  });
}

function spacer() {
  return new Paragraph({ spacing: { after: 120 }, children: [] });
}

const children = [
  new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { before: 2400, after: 400 },
    children: [
      new TextRun({ text: "Morph", bold: true, size: 72, color: "1F4E79" }),
    ],
  }),
  new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { after: 200 },
    children: [
      new TextRun({
        text: "Developer Guide",
        bold: true,
        size: 48,
        color: "2E75B6",
      }),
    ],
  }),
  new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { after: 200 },
    children: [
      new TextRun({
        text: "Lightweight, Annotation-Driven Entity ↔ DTO Mapping for Spring Boot",
        size: 26,
        italics: true,
      }),
    ],
  }),
  new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { before: 600, after: 120 },
    children: [new TextRun({ text: "Version 0.1.0-SNAPSHOT", size: 24 })],
  }),
  new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { after: 120 },
    children: [new TextRun({ text: "Ashish Kumar Panda", size: 24 })],
  }),
  new Paragraph({
    alignment: AlignmentType.CENTER,
    children: [
      new TextRun({ text: "morphmapper · Apache License 2.0", size: 22, color: "666666" }),
    ],
  }),
  new Paragraph({ children: [new PageBreak()] }),

  title("Table of Contents"),
  ...[
    "1. Introduction",
    "2. Why Morph?",
    "3. Requirements & Installation",
    "4. Quick Start",
    "5. Core API — Mapper",
    "6. Spring Boot Integration",
    "7. Annotations Reference",
    "8. Mapping Patterns",
    "9. JDBC Module",
    "10. JPA Module",
    "11. Configuration",
    "12. Testing",
    "13. Error Handling",
    "14. Best Practices",
    "15. Morph-Demo Walkthrough",
    "16. Maven Modules",
    "17. Roadmap & Support",
  ].map((item) => bullet(item)),
  new Paragraph({ children: [new PageBreak()] }),

  title("1. Introduction"),
  body(
    "Morph (Maven artifacts: dev.morph:zeromapper-*) is a lightweight, annotation-driven Java library that eliminates almost all manual Entity ↔ DTO mapping in Spring Boot applications."
  ),
  body(
    "Unlike ModelMapper, you do not configure a generic reflection engine. Unlike MapStruct, you do not write mapper interfaces or generate code at compile time. You define your DTOs with optional annotations — Morph maps everything else automatically."
  ),
  body("Morph is designed for developers who want:", { bold: false }),
  bullet("Zero boilerplate mapping in services and controllers"),
  bullet("Declarative mapping rules on DTO fields"),
  bullet("Spring Boot auto-configuration out of the box"),
  bullet("Optional JDBC and JPA modules for query result mapping"),
  bullet("High performance via cached metadata and MethodHandle-based property access"),

  title("2. Why Morph?"),
  body(
    "Every Spring Boot project accumulates hundreds of lines of mapping code: manual builders, MapStruct @Mapper interfaces, stream().map(...) chains, BeanPropertyRowMapper, and custom RowMapper implementations."
  ),
  body("Morph replaces all of that with a zero-config API:"),
  codeBlock([
    "UserDto dto = Mapper.map(user, UserDto.class);",
    "List<UserDto> dtos = Mapper.list(users, UserDto.class);",
    "User entity = Mapper.map(dto, User.class);",
  ]),
  heading2("Comparison at a Glance"),
  table(
    ["Approach", "Setup", "Code to Maintain"],
    [
      ["Manual mapping", "None", "High — every field, every DTO"],
      ["MapStruct", "Mapper interfaces + codegen", "Medium — interface per mapping pair"],
      ["ModelMapper", "Configuration + conventions", "Medium — runtime config tuning"],
      ["Morph", "DTO annotations only", "Low — annotate exceptions only"],
    ]
  ),
  spacer(),

  title("3. Requirements & Installation"),
  heading2("Requirements"),
  bullet("Java 17 or later"),
  bullet("Spring Boot 3+ (optional — use zeromapper-spring)"),
  bullet("Jakarta EE packages (for JPA module)"),
  bullet("Maven 3.9+ or Gradle 8+"),
  heading2("Maven Dependency — Spring Boot (Recommended)"),
  codeBlock([
    "<dependency>",
    "    <groupId>dev.morph</groupId>",
    "    <artifactId>zeromapper-spring</artifactId>",
    "    <version>0.1.0</version>",
    "</dependency>",
  ]),
  body(
    "For local development before Maven Central release, install Morph locally with mvn clean install in the Morph project, then use version 0.1.0-SNAPSHOT."
  ),
  heading2("Maven Dependency — Core Only (No Spring)"),
  codeBlock([
    "<dependency>",
    "    <groupId>dev.morph</groupId>",
    "    <artifactId>zeromapper-core</artifactId>",
    "    <version>0.1.0</version>",
    "</dependency>",
  ]),
  heading2("Optional Modules"),
  table(
    ["Module", "Artifact ID", "Purpose"],
    [
      ["JDBC", "zeromapper-jdbc", "JdbcTemplate row → DTO mapping"],
      ["JPA", "zeromapper-jpa", "JPA Tuple / native query mapping"],
      ["Test", "zeromapper-test", "JUnit assertion helpers (test scope)"],
    ]
  ),
  spacer(),

  title("4. Quick Start"),
  heading2("Step 1 — Define Your Entity"),
  codeBlock([
    "public class User {",
    "    private Long id;",
    "    private String firstName;",
    "    private String lastName;",
    "    private String password;",
    "    private UserStatus status;",
    "    private Address address;",
    "    // getters and setters",
    "}",
  ]),
  heading2("Step 2 — Define Your DTO"),
  codeBlock([
    "public class UserDto {",
    "    private Long id;",
    "    private String firstName;",
    "    private String lastName;",
    "",
    "    @Expression(\"firstName + ' ' + lastName\")",
    "    private String fullName;",
    "",
    "    @From(\"address.city\")",
    "    private String city;",
    "",
    "    @IgnoreMapping",
    "    private String password;",
    "",
    "    private String status;",
    "    // getters",
    "}",
  ]),
  heading2("Step 3 — Map"),
  codeBlock("UserDto dto = Mapper.map(userEntity, UserDto.class);"),
  body(
    "Fields with matching names map automatically. Annotations handle nested paths, computed fields, and exclusions."
  ),

  title("5. Core API — Mapper"),
  body(
    "dev.morph.Mapper is the static entry point for all mapping operations. It delegates to an internal MappingEngine (DefaultMappingEngine by default)."
  ),
  heading2("Methods"),
  table(
    ["Method", "Description"],
    [
      ["map(source, TargetClass.class)", "Create a new instance of TargetClass from source"],
      ["map(source, existingTarget)", "Map source fields into an existing target instance"],
      ["list(collection, TargetClass.class)", "Map a Collection to List<T>"],
      ["set(collection, TargetClass.class)", "Map a Collection to Set<T>"],
      ["stream(stream, TargetClass.class)", "Map elements of a Stream<T>"],
      ["useEngine(engine)", "Replace the global engine (used by Spring auto-config)"],
    ]
  ),
  spacer(),
  heading2("Example — Collection Mapping"),
  codeBlock([
    "List<User> users = userRepository.findAll();",
    "List<UserDto> dtos = Mapper.list(users, UserDto.class);",
  ]),
  heading2("Example — Bidirectional Mapping"),
  codeBlock([
    "UserDto dto = Mapper.map(entity, UserDto.class);   // Entity → DTO",
    "User entity = Mapper.map(dto, User.class);           // DTO → Entity",
  ]),
  heading2("Example — Update Existing Instance"),
  codeBlock([
    "User existing = userRepository.findById(id).orElseThrow();",
    "Mapper.map(incomingDto, existing);",
    "userRepository.save(existing);",
  ]),

  title("6. Spring Boot Integration"),
  body(
    "When zeromapper-spring is on the classpath, Morph auto-configures via MorphAutoConfiguration. No @EnableMorph or manual @Bean setup is required."
  ),
  heading2("Inject MorphMapper"),
  codeBlock([
    "@Service",
    "public class UserService {",
    "",
    "    private final MorphMapper mapper;",
    "",
    "    public UserService(MorphMapper mapper) {",
    "        this.mapper = mapper;",
    "    }",
    "",
    "    public List<UserDto> findAll() {",
    "        return mapper.list(userRepository.findAll(), UserDto.class);",
    "    }",
    "",
    "    public UserDto findById(Long id) {",
    "        User user = userRepository.findById(id).orElseThrow();",
    "        return mapper.map(user, UserDto.class);",
    "    }",
    "}",
  ]),
  heading2("MorphMapper Methods"),
  body("MorphMapper mirrors the static Mapper API and adds Spring Data support:"),
  bullet("map(source, targetType) — single object mapping"),
  bullet("map(source, existingTarget) — in-place mapping"),
  bullet("list(collection, targetType) — collection to List"),
  bullet("set(collection, targetType) — collection to Set"),
  bullet("stream(stream, targetType) — stream mapping"),
  bullet("page(page, targetType) — Spring Data Page mapping"),
  heading2("Auto-Configuration Details"),
  body("MorphAutoConfiguration registers:"),
  bullet("MappingEngine bean (DefaultMappingEngine unless overridden)"),
  bullet("MorphMapper bean wired to the MappingEngine"),
  bullet("MorphProperties bound to morph.* configuration prefix"),

  title("7. Annotations Reference"),
  table(
    ["Annotation", "Target", "Purpose"],
    [
      ["@From(\"path\")", "Field", "Map from nested or renamed source property"],
      ["@IgnoreMapping", "Field", "Exclude field from mapping"],
      ["@Expression(\"...\")", "Field", "Compute field value from expression"],
      ["@MapperIgnoreNull", "Type", "Do not write null values to target"],
      ["@MapperComponent", "Type", "Register custom Spring mapping component"],
    ]
  ),
  spacer(),
  heading2("@From — Nested and Renamed Properties"),
  body("Use dot-separated paths to map from nested source objects:"),
  codeBlock([
    "@From(\"address.city\")",
    "private String city;",
    "",
    "@From(\"address.country\")",
    "private String country;",
  ]),
  body("Morph navigates the path null-safely. If address is null, city remains null."),
  heading2("@Expression — Computed Fields"),
  body("Evaluate a SpEL-like expression against source properties:"),
  codeBlock([
    "@Expression(\"firstName + ' ' + lastName\")",
    "private String fullName;",
  ]),
  body("Note: Full SpEL support is on the roadmap. Current expression support covers basic concatenation and property access."),
  heading2("@IgnoreMapping — Security & Exclusions"),
  codeBlock([
    "@IgnoreMapping",
    "private String password;",
  ]),
  body("The password field is never copied from source to target, keeping sensitive data out of API responses."),
  heading2("@MapperIgnoreNull — Class-Level Null Handling"),
  body("When applied to a class, null values from the source are not written to the target, preserving existing values during partial updates."),
  heading2("@MapperComponent — Custom Spring Components"),
  body("Mark Spring beans that participate in custom mapping logic discoverable by Morph auto-configuration."),

  title("8. Mapping Patterns"),
  heading2("Automatic Field Matching"),
  body(
    "Fields with identical names and compatible types map automatically. No annotation required for simple cases like id → id, firstName → firstName."
  ),
  heading2("Enum Conversion"),
  body("Enums are converted to their name() string representation when mapping to String fields:"),
  codeBlock([
    "enum UserStatus { ACTIVE, INACTIVE }",
    "",
    "// UserStatus.ACTIVE → \"ACTIVE\" in UserDto.status",
  ]),
  heading2("UUID and Numeric Conversion"),
  body("Morph converts between UUID, String, and numeric types automatically where sensible."),
  heading2("Java Records"),
  codeBlock([
    "record UserRecord(Long id, String firstName, String lastName) {}",
    "",
    "UserRecord record = Mapper.map(userEntity, UserRecord.class);",
  ]),
  heading2("Map ↔ POJO"),
  body("Morph can map Map<String, Object> to POJOs — useful for JDBC rows and dynamic query results."),
  heading2("Collections, Sets, Streams, and Pages"),
  codeBlock([
    "List<UserDto> list = mapper.list(users, UserDto.class);",
    "Set<UserDto> set = mapper.set(users, UserDto.class);",
    "Stream<UserDto> stream = mapper.stream(userStream, UserDto.class);",
    "Page<UserDto> page = mapper.page(userPage, UserDto.class);",
  ]),

  title("9. JDBC Module"),
  body(
    "The zeromapper-jdbc module maps JDBC query results directly to DTOs without RowMapper or BeanPropertyRowMapper boilerplate."
  ),
  heading2("Dependency"),
  codeBlock([
    "<dependency>",
    "    <groupId>dev.morph</groupId>",
    "    <artifactId>zeromapper-jdbc</artifactId>",
    "    <version>0.1.0</version>",
    "</dependency>",
  ]),
  heading2("JdbcTemplate Query"),
  codeBlock([
    "List<UserDto> users = JdbcMapper.query(",
    "    jdbcTemplate,",
    "    \"SELECT id, first_name, last_name, status FROM users\",",
    "    UserDto.class",
    ");",
  ]),
  heading2("NamedParameterJdbcTemplate Query"),
  codeBlock([
    "Map<String, Object> params = Map.of(\"status\", \"ACTIVE\");",
    "List<UserDto> users = JdbcMapper.query(",
    "    namedJdbcTemplate,",
    "    \"SELECT * FROM users WHERE status = :status\",",
    "    params,",
    "    UserDto.class",
    ");",
  ]),
  body(
    "Column names are automatically converted from snake_case (first_name) to camelCase (firstName) for property matching."
  ),

  title("10. JPA Module"),
  body("The zeromapper-jpa module maps JPA Tuple and native query results to DTOs."),
  heading2("Dependency"),
  codeBlock([
    "<dependency>",
    "    <groupId>dev.morph</groupId>",
    "    <artifactId>zeromapper-jpa</artifactId>",
    "    <version>0.1.0</version>",
    "</dependency>",
  ]),
  heading2("Tuple Mapping"),
  codeBlock([
    "Tuple tuple = entityManager.createNativeQuery(",
    "    \"SELECT id, first_name AS firstName FROM users WHERE id = ?1\",",
    "    Tuple.class",
    ").setParameter(1, userId).getSingleResult();",
    "",
    "UserDto dto = JpaMapper.fromTuple(tuple, UserDto.class);",
  ]),
  heading2("Multiple Tuples"),
  codeBlock([
    "@SuppressWarnings(\"unchecked\")",
    "List<Tuple> tuples = query.getResultList();",
    "List<UserDto> dtos = JpaMapper.fromTuples(tuples, UserDto.class);",
  ]),

  title("11. Configuration"),
  body("Morph supports optional configuration via application.yml or application.properties:"),
  codeBlock([
    "morph:",
    "  debug: true",
  ]),
  body("When debug is enabled, Morph logs mapping operations — useful during development and troubleshooting."),
  heading2("Overriding MappingEngine"),
  body("Register your own MappingEngine bean to customize behavior. MorphAutoConfiguration respects @ConditionalOnMissingBean:"),
  codeBlock([
    "@Bean",
    "MappingEngine customMappingEngine() {",
    "    return DefaultMappingEngine.getInstance();",
    "}",
  ]),

  title("12. Testing"),
  heading2("Unit Test with MorphMapper"),
  codeBlock([
    "@SpringBootTest",
    "class MorphMappingTest {",
    "",
    "    @Autowired",
    "    private MorphMapper mapper;",
    "",
    "    @Test",
    "    void mapsEntityToDto() {",
    "        User user = new User(1L, \"Ada\", \"Lovelace\", \"secret\",",
    "            UserStatus.ACTIVE, new Address(\"London\", \"UK\"));",
    "",
    "        UserDto dto = mapper.map(user, UserDto.class);",
    "",
    "        assertThat(dto.getFullName()).isEqualTo(\"Ada Lovelace\");",
    "        assertThat(dto.getCity()).isEqualTo(\"London\");",
    "        assertThat(dto.getPassword()).isNull();",
    "    }",
    "}",
  ]),
  heading2("MorphAssertions Helper (zeromapper-test)"),
  codeBlock([
    "<dependency>",
    "    <groupId>dev.morph</groupId>",
    "    <artifactId>zeromapper-test</artifactId>",
    "    <version>0.1.0</version>",
    "    <scope>test</scope>",
    "</dependency>",
    "",
    "MorphAssertions.assertMapsTo(source, UserDto.class, \"id\", \"firstName\");",
  ]),
  heading2("REST API Integration Test"),
  codeBlock([
    "@SpringBootTest",
    "@AutoConfigureMockMvc",
    "class UserControllerTest {",
    "",
    "    @Test",
    "    void listsUsersWithMorphMappedFields() throws Exception {",
    "        mockMvc.perform(get(\"/api/users\"))",
    "            .andExpect(jsonPath(\"$[0].fullName\", is(\"Ada Lovelace\")))",
    "            .andExpect(jsonPath(\"$[0].password\").doesNotExist());",
    "    }",
    "}",
  ]),

  title("13. Error Handling"),
  body("Morph throws meaningful exceptions to help diagnose mapping failures:"),
  table(
    ["Exception", "When"],
    [
      ["FieldNotFoundException", "Source property path in @From does not exist"],
      ["TypeConversionException", "Incompatible types cannot be converted"],
      ["CircularReferenceException", "Circular object graph detected during mapping"],
      ["MorphException", "Base exception for all Morph errors"],
    ]
  ),
  spacer(),
  body("Example: If @From(\"address.zipCode\") references a non-existent property, Morph throws FieldNotFoundException with a clear message instead of silently mapping null."),

  title("14. Best Practices"),
  bullet("Use DTOs for API responses — never expose entities directly"),
  bullet("Apply @IgnoreMapping on sensitive fields (password, tokens, internal IDs)"),
  bullet("Use @From for nested properties instead of flattening entities"),
  bullet("Use @Expression for display-only computed fields (fullName, label, summary)"),
  bullet("Inject MorphMapper in Spring services — avoid static Mapper in production code for testability"),
  bullet("Keep DTOs focused — one DTO per use case (UserDto, UserSummaryDto, UserAdminDto)"),
  bullet("Use zeromapper-jdbc/jpa for query projections instead of manual row parsing"),
  bullet("Enable morph.debug during development, disable in production"),

  title("15. Morph-Demo Walkthrough"),
  body(
    "The Morph-Demo project (github.com/Ashishkumarpanda/Morph-Demo) is a runnable Spring Boot reference application."
  ),
  heading2("Project Structure"),
  codeBlock([
    "Morph-Demo/",
    "├── domain/          User, Address, UserStatus (entities)",
    "├── dto/             UserDto (annotated DTO)",
    "├── repository/      In-memory UserRepository",
    "├── service/         UserService (uses MorphMapper)",
    "└── web/             UserController (REST API)",
  ]),
  heading2("Run the Demo"),
  codeBlock([
    "cd Morph && mvn clean install",
    "cd Morph-Demo && mvn spring-boot:run",
    "curl http://localhost:8080/api/users/1",
  ]),
  heading2("Sample API Response"),
  codeBlock([
    "{",
    "  \"id\": 1,",
    "  \"firstName\": \"Ada\",",
    "  \"lastName\": \"Lovelace\",",
    "  \"fullName\": \"Ada Lovelace\",",
    "  \"city\": \"London\",",
    "  \"country\": \"UK\",",
    "  \"status\": \"ACTIVE\"",
    "}",
  ]),
  body("Note: password is excluded via @IgnoreMapping — it never appears in JSON responses."),

  title("16. Maven Modules"),
  table(
    ["Module", "Artifact", "Description"],
    [
      ["Annotations", "zeromapper-annotations", "@From, @Expression, @IgnoreMapping, etc."],
      ["Core", "zeromapper-core", "Mapping engine, type conversion, caching"],
      ["Spring", "zeromapper-spring", "Spring Boot auto-configuration + MorphMapper"],
      ["JDBC", "zeromapper-jdbc", "JdbcTemplate row mapping"],
      ["JPA", "zeromapper-jpa", "JPA Tuple and native query mapping"],
      ["Test", "zeromapper-test", "JUnit assertion helpers"],
      ["Parent", "zeromapper-parent", "BOM / dependency management POM"],
    ]
  ),
  spacer(),
  heading2("Transitive Dependencies (zeromapper-spring)"),
  body("Adding zeromapper-spring automatically pulls in:"),
  bullet("zeromapper-core → zeromapper-annotations"),
  bullet("spring-boot-autoconfigure"),
  bullet("spring-data-commons (for Page mapping)"),

  title("17. Roadmap & Support"),
  heading2("Roadmap"),
  bullet("Full SpEL expression support"),
  bullet("Lombok @Builder integration"),
  bullet("Jackson JsonNode mapping module"),
  bullet("MapStruct / ModelMapper benchmark suite"),
  bullet("Maven Central release (0.1.0)"),
  heading2("Build Morph from Source"),
  codeBlock([
    "git clone https://github.com/Ashishkumarpanda/Morph.git",
    "cd Morph",
    "mvn verify",
  ]),
  heading2("License"),
  body("Morph is licensed under the Apache License, Version 2.0."),
  heading2("Links"),
  bullet("Morph Library: https://github.com/Ashishkumarpanda/Morph"),
  bullet("Morph Demo: https://github.com/Ashishkumarpanda/Morph-Demo"),
  bullet("Group ID: dev.morph"),
  bullet("Maven Central: Coming in 0.1.0 release"),
];

const doc = new Document({
  creator: "Ashish Kumar Panda",
  title: "Morph Developer Guide",
  description: "Complete developer guide for the Morph Entity-DTO mapping library",
  sections: [
    {
      properties: {
        page: {
          margin: { top: 1440, right: 1440, bottom: 1440, left: 1440 },
        },
      },
      children,
    },
  ],
});

const outputPath = path.join(__dirname, "Morph-Developer-Guide.docx");

Packer.toBuffer(doc).then((buffer) => {
  fs.writeFileSync(outputPath, buffer);
  console.log("Created:", outputPath);
});
