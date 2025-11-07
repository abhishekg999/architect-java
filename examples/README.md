# Architect Examples

## Quick Start

1. Build and install architect:
```bash
cd ..
mvn clean install
```

2. Build examples:
```bash
cd examples
mvn clean compile
```

3. Run example:
```bash
mvn exec:java
```

## What Happens

The `@Architect` annotation on the `User` constructor triggers generation of `UserBuilder` with:
- Staged interfaces forcing `name()` then `age()` in order (matching constructor parameter order)
- Optional setters for `email()` and `phone()` in final stage (for `Optional<T>` parameters)
- Type-safe `.build()` method that calls the annotated constructor

Generated code appears in `target/generated-sources/annotations/`.

## Benefits of Constructor Annotation

- **Explicit contract** - constructor signature defines the builder API
- **No ambiguity** - works with multiple constructors
- **Records compatible** - can annotate canonical constructor
- **Compile-time validation** - fails if constructor doesn't exist

## Features Demonstrated

### User Example
- Constructor overloading for defaults
- `@Nullable` - Nullable field (null if not set)
- `Optional<String>` - Optional wrapper (Optional.empty() if not set)

### Product Example  
- Multiple constructors (only @Architect annotated one generates builder)
- Constructor overloading for default values
- Mix of mandatory and optional parameters

### Email Example (Record)
- Compact canonical constructor syntax
- Constructor overloading in records
- Mix of @Nullable and Optional<T>
- Shows record compatibility

