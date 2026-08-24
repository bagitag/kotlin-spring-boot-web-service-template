# 🚀 Spring Boot Web Service Template - Customization Guide

**Version:** 1.0  
**Date:** March 2026  
**Language:** Kotlin  
**Framework:** Spring Boot 4.0.3

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Project Structure](#project-structure)
3. [Step 1: Rename Project & Packages](#step-1-rename-project--packages)
4. [Step 2: Remove Modules](#step-2-remove-modules)
5. [Automated Scripts](#automated-scripts)
6. [Verification Checklist](#verification-checklist)
7. [Troubleshooting](#troubleshooting)

---

## Overview

This Spring Boot template project is designed to be customized for your specific needs. The customization process involves two main tasks:

1. **Renaming the project and its packages** - Change from `template-project` and `com.example.templateproject` to your desired names
2. **Removing optional modules** - Keep only the modules you need (client and/or persistence modules are optional)

The project follows Maven's multi-module structure with clear separation of concerns:
- **api**: Data transfer objects and request/response models
- **core**: Business logic and service layer
- **web**: Controller layer, REST endpoints, exception handling
- **persistence**: Database access, entities, repositories (OPTIONAL)
- **client**: HTTP client integrations with external services (OPTIONAL)
- **jacoco-report**: Test coverage reporting

---

## Project Structure

```
template-project/
├── pom.xml (parent)
├── template-project-api/          [DTO models]
├── template-project-core/         [Business logic - ALWAYS NEEDED]
├── template-project-web/          [REST controllers - ALWAYS NEEDED]
├── template-project-persistence/  [Database layer - OPTIONAL]
├── template-project-client/       [External service clients - OPTIONAL]
└── template-project-jacoco-report/ [Test coverage - ALWAYS NEEDED]
```

### Module Dependencies

```
template-project-web
    ↓ depends on
template-project-core
    ↓ depends on
    ├── template-project-api
    ├── template-project-persistence (OPTIONAL)
    └── template-project-client (OPTIONAL)
```

---

## Step 1: Rename Project & Packages

### What Needs to be Renamed

1. **Project names and artifact IDs**:
   - Parent POM: `template-project` → `your-project-name`
   - Module names: `template-project-*` → `your-project-name-*`
   - Artifact IDs in pom.xml files

2. **Package names**:
   - `com.example.templateproject` → `com.yourcompany.yourproject`
   - All file paths: `src/main/kotlin/com/example/templateproject/` → `src/main/kotlin/com/yourcompany/yourproject/`

3. **References in code**:
   - Class references in pom.xml (like `<start-class>`)
   - Import statements in .kt files
   - Property file names and references
   - Configuration files

4. **Configuration & Resource Files**:
   - Spring Boot application.yml/properties files
   - Property files: `*-dev.properties`, `*-docker.properties`, `*-openapi.properties`
   - Liquibase changelog files (if using persistence)
   - Docker and docker-compose configurations

### Files That Must Be Updated

#### Root pom.xml
- `<artifactId>template-project</artifactId>` → `<artifactId>your-project-name</artifactId>`
- `<name>` and `<description>`
- Module names in `<modules>` section

#### Each Module's pom.xml
- `<artifactId>template-project-*</artifactId>` → `<artifactId>your-project-name-*</artifactId>`
- All `<dependency>` references to `com.example:template-project-*` → `com.yourcompany:your-project-name-*`

#### Source Code Files
- All Kotlin files: package declarations and imports
- Directory structure: `com/example/templateproject/` → `com/yourcompany/yourproject/`

#### Configuration Files
- `application.yml` and `application-*.properties`
- All references to `persistence-`, `client-`, `core-` properties files
- `logback-spring.xml` (logging configuration)
- `liquibase.properties` (if using persistence)

#### Other Files
- `Dockerfile`: JAR file references
- `docker-compose.yml` and `docker-compose-dev.yml`
- `openapi.yaml`: Package references in generated API specs
- `pom.xml` in JaCoCo configuration: class excludes and includes

### Key Configuration References in TemplateApplication.kt

```kotlin
@PropertySource(
    value = [
        "classpath:persistence-${spring.profiles.active:default}.properties",
        "classpath:client-${spring.profiles.active:default}.properties",
        "classpath:core-${spring.profiles.active:default}.properties",
    ],
    ignoreResourceNotFound = true,
)
```

These property files need to exist or the `ignoreResourceNotFound = true` will handle missing ones.

---

## Step 2: Remove Modules

### When to Remove Modules

#### Remove **persistence** module if:
- Your service doesn't need a database
- You're building a stateless API gateway
- Data is only cached in memory or Redis
- External services handle all data storage

#### Remove **client** module if:
- Your service doesn't call other external services
- You're building a standalone monolithic service
- All integration happens through message queues (not HTTP)

### Removing the Persistence Module

#### Files to Delete
1. Delete entire directory: `template-project-persistence/`

#### POM Files to Update

**Root pom.xml:**
- Remove `<module>template-project-persistence</module>`

**template-project-core/pom.xml:**
- Remove the entire dependency:
  ```xml
  <dependency>
      <groupId>com.example</groupId>
      <artifactId>template-project-persistence</artifactId>
      <version>${project.parent.version}</version>
  </dependency>
  ```

**template-project-web/pom.xml:**
- No changes needed (web doesn't depend on persistence directly)

#### Code Files to Update

**template-project-web/src/main/kotlin/com/example/templateproject/TemplateApplication.kt:**
- Remove or comment out the reference in `@PropertySource`:
  ```kotlin
  // "classpath:persistence-${spring.profiles.active:default}.properties",
  ```

**template-project-core/src/main/kotlin/com/example/templateproject/core/configuration/DatabaseCacheConfiguration.kt:**
- This file configures caching with database as fallback
- Either delete it or modify it to use only in-memory/Redis caching

**Root pom.xml (JaCoCo configuration):**
- Remove or update exclusions containing `persistence`:
  ```xml
  <exclude>com/example/templateproject/persistence/entity/history/*</exclude>
  ```

#### Resource Files to Delete
- `template-project-persistence/src/main/resources/`:
  - `persistence-*.properties` files
  - `liquibase/` directory (all migration files)
  - `META-INF/` directory

#### Database Configuration
- If using `application-*.yml`, remove database connection settings:
  ```yaml
  spring:
    datasource: ...
    jpa: ...
    liquibase: ...
  ```
- Remove dependencies from root pom.xml if they're only for persistence:
  - `spring-boot-starter-data-jpa`
  - `spring-boot-starter-liquibase`
  - PostgreSQL driver
  - H2 database

### Removing the Client Module

#### Files to Delete
1. Delete entire directory: `template-project-client/`

#### POM Files to Update

**Root pom.xml:**
- Remove `<module>template-project-client</module>`

**template-project-core/pom.xml:**
- Remove the entire dependency:
  ```xml
  <dependency>
      <groupId>com.example</groupId>
      <artifactId>template-project-client</artifactId>
      <version>${project.parent.version}</version>
  </dependency>
  ```

**template-project-web/pom.xml:**
- No changes needed (web doesn't depend on client directly)

#### Code Files to Update

**template-project-web/src/main/kotlin/com/example/templateproject/TemplateApplication.kt:**
- Remove or comment out the reference in `@PropertySource`:
  ```kotlin
  // "classpath:client-${spring.profiles.active:default}.properties",
  ```

**template-project-core/src/main/kotlin/com/example/templateproject/core/service/ExampleService.kt:**
- This likely has dependencies on `JsonPlaceholderService` or other client services
- Either remove calls to external services or replace with mock implementations

**template-project-web/src/main/kotlin/com/example/templateproject/web/configuration/RetryConfiguration.kt:**
- This configures retry policies for client HTTP calls
- Can be deleted if no external services are called

#### Resource Files to Delete
- `template-project-client/src/main/resources/`:
  - `client-*.properties` files

#### Client Dependencies
- Remove client-related dependencies from root pom.xml:
  - `spring-boot-starter-validation` (if not used elsewhere)
  - Caffeine cache library
  - Spring Retry
  - Resilience4j (circuit breaker)

#### Configuration Files
- Remove from `application-*.yml`:
  ```yaml
  external-services:
    jsonplaceholder:
      base-url: ...
  ```

### Removing Both Modules

If removing both persistence and client:

1. Follow both removal procedures above
2. Simplify **template-project-core**:
   - Delete `DatabaseCacheConfiguration.kt`
   - Simplify `ExampleService.kt` to not use external data sources
   - Delete or simplify `ExampleMapper.kt` if it maps to/from repository entities

3. Simplify **template-project-api**:
   - Keep only the DTOs you need
   - Remove pagination/sorting DTOs if not needed

4. Update **Dockerfile**:
   - Remove any database initialization scripts
   - Keep only core application startup

---

## Automated Scripts

Two PowerShell scripts are provided to automate these tasks:

### 1. `rename-project.ps1` - Project & Package Renaming

**Usage:**
```powershell
.\rename-project.ps1 -OldProjectName "template-project" -NewProjectName "my-awesome-service" `
                    -OldPackage "com.example.templateproject" -NewPackage "com.mycompany.myservice"
```

**Parameters:**
- `OldProjectName`: Current Maven artifact ID (default: `template-project`)
- `NewProjectName`: New Maven artifact ID (e.g., `my-awesome-service`)
- `OldPackage`: Current Kotlin package (default: `com.example.templateproject`)
- `NewPackage`: New Kotlin package (e.g., `com.mycompany.myservice`)

**What it does:**
- Renames all directories matching the old project name
- Updates all pom.xml files
- Renames and updates all Kotlin files
- Updates property files
- Updates Docker configurations
- Updates OpenAPI configuration
- Updates resource files (logback, liquibase, etc.)

**Safety:**
- Creates a backup of pom.xml files (`.bak`)
- Dry-run option available to preview changes
- Validates package naming conventions

### 2. `remove-modules.ps1` - Module Removal

**Usage:**
```powershell
.\remove-modules.ps1 -RemovePersistence -RemoveClient
```

**Parameters:**
- `RemovePersistence`: Remove the persistence module (switch)
- `RemoveClient`: Remove the client module (switch)
- `ProjectName`: Name of the root directory (default: `template-project`)
- `BackupFirst`: Create backup before deleting (default: `$true`)
- `DryRun`: Preview changes without executing (switch)

**Examples:**

```powershell
# Remove only persistence
.\remove-modules.ps1 -RemovePersistence

# Remove only client
.\remove-modules.ps1 -RemoveClient

# Remove both
.\remove-modules.ps1 -RemovePersistence -RemoveClient

# Preview changes
.\remove-modules.ps1 -RemovePersistence -DryRun
```

**What it does:**
- Deletes specified module directories
- Updates root pom.xml to remove module references
- Updates core module pom.xml to remove dependencies
- Updates TemplateApplication.kt property sources
- Removes property files for deleted modules
- Updates JaCoCo configuration
- Creates backups before deletion

**Safety:**
- Automatic backup creation (can be disabled)
- Dry-run mode to preview changes
- Validation before deletion
- Rollback instructions provided

---

## Verification Checklist

After customization (manual or automated), verify:

### 1. Project Structure
- [ ] No `template-project` folders remain
- [ ] No `com/example/templateproject` packages remain
- [ ] Directory structure matches new naming: `com/yourcompany/yourproject/`

### 2. POM Files
- [ ] Root `pom.xml`: Parent artifact ID updated
- [ ] Root `pom.xml`: Module section matches active modules
- [ ] All module `pom.xml`: Artifact IDs updated
- [ ] All dependencies use new group/artifact IDs
- [ ] Dependencies removed for deleted modules

### 3. Code Files
- [ ] All `.kt` files have correct package declarations
- [ ] All imports use new package names
- [ ] No remaining `com.example` references in code
- [ ] `TemplateApplication.kt` references correct property files

### 4. Configuration Files
- [ ] `application.yml` exists with correct settings
- [ ] `application-*.properties` exist for your modules
- [ ] Property file references in `@PropertySource` are valid
- [ ] `logback-spring.xml` references correct packages

### 5. If Persistence Removed
- [ ] `persistence-*.properties` removed or not referenced
- [ ] Database configuration removed from `application-*.yml`
- [ ] `DatabaseCacheConfiguration.kt` deleted or modified
- [ ] Liquibase files deleted (unless using alternative migration tool)

### 6. If Client Removed
- [ ] `client-*.properties` removed or not referenced
- [ ] External service configuration removed from `application-*.yml`
- [ ] `JsonPlaceholderService` not referenced in code
- [ ] `RetryConfiguration.kt` deleted or modified

### 7. Build & Compilation
- [ ] `mvn clean verify` completes successfully
- [ ] No compilation errors
- [ ] All tests pass
- [ ] JaCoCo coverage report generates
- [ ] Application starts without errors: `mvn spring-boot:run`

### 8. Docker & Deployment
- [ ] `Dockerfile` references correct JAR file
- [ ] `docker-compose.yml` uses correct service names
- [ ] `openapi.yaml` contains correct package names
- [ ] Docker image builds successfully

---

## Troubleshooting

### Compilation Errors

#### "Cannot find symbol" for old package names
**Solution:** Ensure all files have been renamed:
```powershell
# Search for old package references
cd template-project
grep -r "com.example.templateproject" .
grep -r "template-project" . --include="*.xml" --include="*.yml" --include="*.yaml"
```

#### "Artifact not found" in Maven build
**Solution:** Check pom.xml dependencies match the new names:
```bash
mvn dependency:tree
```

### Runtime Errors

#### "Could not load resource" for property files
**Solution:** Verify property files exist in `src/main/resources/`:
```powershell
Get-ChildItem -Recurse -Filter "*-dev.properties" -Include "core-*", "client-*", "persistence-*"
```

#### Spring context fails to start
**Solution:** Check `TemplateApplication.kt` for non-existent property file references:
```kotlin
@PropertySource(
    value = [
        "classpath:persistence-${spring.profiles.active:default}.properties",  // Removed module?
        "classpath:client-${spring.profiles.active:default}.properties",       // Removed module?
        "classpath:core-${spring.profiles.active:default}.properties",
    ],
    ignoreResourceNotFound = true,  // This handles missing files gracefully
)
```

### Module Removal Issues

#### Build fails after removing persistence
**Solution:** 
- Verify `template-project-core/pom.xml` doesn't have persistence dependency
- Remove `DatabaseCacheConfiguration.kt` if it exists
- Check `ExampleService.kt` doesn't use `ExampleRepository`

#### Build fails after removing client
**Solution:**
- Verify `template-project-core/pom.xml` doesn't have client dependency
- Check `ExampleService.kt` doesn't use `JsonPlaceholderService`
- Remove `RetryConfiguration.kt` from web module

### Test Failures

#### Tests reference deleted modules
**Solution:**
```bash
# Find test files referencing deleted modules
grep -r "client\|persistence" template-project-*/src/test/kotlin/ --include="*.kt"

# Delete or update test files accordingly
```

#### OpenAPI generation fails
**Solution:** Regenerate with:
```bash
mvn clean verify -Popenapi
```

---

## Next Steps After Customization

1. **Update documentation:**
   - Replace references in `README.md`
   - Update `CLIENT.md` if persistence module was removed
   - Update `PERSISTENCE.md` if removed or no longer applicable

2. **Configure for your environment:**
   - Update `application-dev.properties` with your settings
   - Update `application-docker.properties` for Docker deployment
   - Configure database connections (if using persistence)
   - Configure external service endpoints (if using client)

3. **Implement your business logic:**
   - Extend `ExampleService` with your service implementations
   - Create new DTOs in the api module
   - Add new repositories and entities (if using persistence)
   - Add new HTTP client integrations (if using client)
   - Create new REST controllers

4. **Set up CI/CD:**
   - Configure your repository secrets
   - Set up automated builds and deployments
   - Configure artifact repositories for Maven dependencies

5. **Customize Spring Boot features:**
   - Configure actuator endpoints
   - Set up observability (OpenTelemetry, Prometheus)
   - Configure caching strategies
   - Set up security configurations

---

## Quick Reference

### Common Replacement Patterns

| What | Old Value | New Value |
|------|-----------|-----------|
| Project Name | `template-project` | `my-awesome-service` |
| Package | `com.example.templateproject` | `com.mycompany.myservice` |
| Group ID | `com.example` | `com.mycompany` |
| Artifact ID | `template-project*` | `my-awesome-service*` |
| Directory | `com/example/templateproject` | `com/mycompany/myservice` |

### Maven Commands

```bash
# Build entire project
mvn clean package

# Build with test coverage
mvn clean verify

# Generate OpenAPI docs
mvn clean verify -Popenapi

# Run application locally
mvn spring-boot:run -pl template-project-web

# Build Docker image
mvn clean package && docker build -t my-service:latest .

# Run tests
mvn test

# Check for security vulnerabilities
mvn org.owasp:dependency-check-maven:check
```

### IDE Configuration

**IntelliJ IDEA:**
1. File → Project Structure → Project Settings
2. Ensure SDK is set to Java 21
3. Enable "Kotlin" facet if not detected
4. Mark source directories: Right-click → Mark Directory As → Sources Root
5. Run → Run Application

**VS Code with Extension Pack for Java:**
1. Install Spring Boot Extension Pack
2. Open command palette: Ctrl+Shift+P
3. Type "Spring Boot: Run" to start the application

---

## Support & Questions

For issues or questions about customization:

1. Check the [Troubleshooting](#troubleshooting) section
2. Review the original `CUSTOMIZATION_COMPLETE.md` for additional context
3. Consult Spring Boot documentation: https://spring.io/projects/spring-boot
4. Review Kotlin documentation: https://kotlinlang.org/docs/
5. Check Maven documentation: https://maven.apache.org/

---

**Last Updated:** March 2026  
**Template Version:** Spring Boot 4.0.3 with Kotlin 2.3.0

