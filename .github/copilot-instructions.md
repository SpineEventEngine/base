# GitHub Copilot Instructions for spine-base

This repository contains common data types and utilities used by Spine SDK subprojects.

## Project Overview

- **Languages**: Kotlin (primary), Java (secondary)
- **Build Tool**: Gradle with Kotlin DSL
- **Static Analysis**: detekt, ErrorProne, Checkstyle, PMD
- **Testing**: JUnit 5, Kotest Assertions, Codecov
- **Architecture**: CQRS architecture patterns

## Quick Start

### Building the Project
```bash
./gradlew build
```

### After Modifying Protobuf Files
```bash
./gradlew clean build
```

### Documentation Changes Only
```bash
./gradlew dokka
```

## Coding Guidelines

### Kotlin Best Practices

**Prefer:**
- Kotlin idioms over Java-style approaches:
  - Extension functions
  - `when` expressions
  - Smart casts
  - Data classes and sealed classes
  - Immutable data structures
- Simple nouns over composite nouns (`user` > `userAccount`)
- Generic parameters over explicit variable types
- Kotlin DSL for Gradle files

**Avoid:**
- Mutable data structures
- Java-style verbosity (builders with setters)
- Redundant null checks (`?.let` misuse)
- Using `!!` unless clearly justified
- Type names in variable names (`userObject`, `itemList`)
- String duplication (use constants in companion objects)
- Mixing Groovy and Kotlin DSLs in build logic
- Reflection unless specifically requested

### Code Formatting
- Remove double empty lines
- Remove trailing space characters
- Follow [Spine Event Engine Documentation](https://github.com/SpineEventEngine/documentation/wiki) coding style

## Testing

- Do not use mocks, use stubs
- Prefer [Kotest assertions](https://kotest.io/docs/assertions/assertions.html) over JUnit or Google Truth
- Generate unit tests for APIs (handle edge cases/scenarios)
- Include automated tests for any code change that alters functionality

## Safety Rules

- ✅ All code must compile and pass static analysis
- ✅ Do not auto-update external dependencies
- ❌ Never use reflection or unsafe code without explicit approval
- ❌ No analytics or telemetry code
- ❌ No blocking calls inside coroutines

## Version Policy

We use [Semantic Versioning 2.0.0](https://semver.org/).

**When creating a new branch:**
1. Increment the patch version in `version.gradle.kts`
   - Retain zero-padding: `"2.0.0-SNAPSHOT.009"` → `"2.0.0-SNAPSHOT.010"`
2. Commit separately with message: `Bump version → $newVersion`
3. Rebuild: `./gradlew clean build`
4. Update `pom.xml`, `dependencies.md` and commit with: `Update dependency reports`

**Note:** PRs without version bumps will fail CI.

## Documentation

- Use KDoc style for public and internal APIs
- Avoid inline comments in production code unless necessary
- Inline comments are helpful in tests
- File and directory names should be formatted as code
- Follow [TODO comment format](https://github.com/SpineEventEngine/documentation/wiki/TODO-comments)

## Common Tasks

- **Adding a dependency**: Update relevant files in `buildSrc` directory
- **Creating a module**: Follow existing module structure patterns
- **Documentation changes**: Documentation-only changes do not require running tests

## Additional Resources

For comprehensive agent guidelines, see: [.agents/_TOC.md](../.agents/_TOC.md)
