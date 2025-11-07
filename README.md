# Architect

Type-safe staged builder generator for Java 17+.

## Usage

Add to your project:

```xml
<dependency>
    <groupId>bet.ahh</groupId>
    <artifactId>architect</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

Annotate your constructor:

```java
import bet.ahh.architect.Architect;
import org.jetbrains.annotations.Nullable;

public class User {
    private final String name;
    private final int age;
    private final String nickname;
    private final Optional<String> email;

    @Architect
    public User(
        String name,
        int age,
        @Nullable String nickname,
        Optional<String> email
    ) {
        this.name = name;
        this.age = age;
        this.nickname = nickname;
        this.email = email;
    }
}
```

Generated builder enforces mandatory parameters in order:

```java
User user = UserBuilder.builder()
    .name("Alice")          // mandatory stage
    .age(30)                // mandatory stage
    .nickname("Ali")        // optional setter (nullable)
    .email("a@example.com") // optional setter (Optional<T>)
    .build();
```

## Parameter Classification

Parameters are classified based on annotations and types:

### Mandatory (Sequential Stages)
- Any parameter without `@Nullable`, `@Default`, or `Optional<T>` wrapper
- Must be set in the order defined in constructor
- Enforced at compile-time through staged interfaces

### Optional Setters
Two ways to mark parameters as optional:

1. **`@Nullable`** - Can be null (uses JetBrains annotations for IDE support)
   ```java
   @Nullable String nickname  // null if not set
   ```

2. **`Optional<T>`** - Wrapped in Optional (uses `Optional.empty()` if not set)
   ```java
   Optional<String> email  // Optional.empty() if not set
   ```

### Default Values
Use constructor overloading for defaults (standard Java pattern):

```java
public User(String name, int age) {
    this(name, age, null, Optional.empty());
}

@Architect  
public User(String name, int age, @Nullable String nickname, Optional<String> email) {
    // ...
}
```

## IDE Integration

Architect uses **JetBrains annotations** (`@Nullable`/`@NotNull`) for nullability, providing automatic IDE support:
- IntelliJ IDEA: null-safety warnings
- VS Code with Java extensions: nullability hints
- Eclipse with JDT: null analysis

Add to your project:
```xml
<dependency>
    <groupId>org.jetbrains</groupId>
    <artifactId>annotations</artifactId>
    <version>24.1.0</version>
    <scope>provided</scope>
</dependency>
```

## Build

```bash
mvn clean install
```

## Test Locally

```bash
mvn clean install
cd examples
mvn clean compile exec:java
```

See `examples/` directory for comprehensive examples including:
- `User` - class with @Nullable and constructor overloading
- `Product` - multiple constructors with defaults
- `Email` - Java record with compact constructor

