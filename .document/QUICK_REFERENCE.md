# Quick Reference Card - Template Customization

## TL;DR - Customization Roadmap

### Option A: Only Rename Project (Keep All Modules)
**Time:** ~30-45 minutes | **Difficulty:** Easy

```bash
# 1. Use IDE Refactor > Rename on package com.example.templateproject
# 2. Update all pom.xml files:
#    - groupId: com.example → com.yourcompany
#    - artifactId: template-project → your-project-name
# 3. Update docker-compose.yml service/image names
# 4. Run: mvn clean install
```

---

### Option B: Rename Project + Remove Client Module
**Time:** ~1-1.5 hours | **Difficulty:** Medium

```bash
# 1. Follow Option A steps
# 2. Remove from root pom.xml:
#    <module>template-project-client</module>
# 3. Remove from template-project-core/pom.xml:
#    <dependency>...template-project-client</dependency>
# 4. In template-project-core/src/main/kotlin:
#    - Remove: import com.example.templateproject.client.*
#    - Remove: JsonPlaceholderService parameter from ExampleService
#    - Remove: getWordCountForUsers() method
# 5. Update ExampleServiceTest to remove JsonPlaceholderService mocks
# 6. Remove from template-project-jacoco-report/pom.xml:
#    <dependency>...template-project-client</dependency>
# 7. Delete: template-project-client directory
# 8. Run: mvn clean install
```

---

### Option C: Rename Project + Remove Persistence Module
**Time:** ~1.5-2 hours | **Difficulty:** Medium-High

```bash
# 1. Follow Option A steps
# 2. Remove from root pom.xml:
#    <module>template-project-persistence</module>
# 3. Remove from template-project-core/pom.xml:
#    <dependency>...template-project-persistence</dependency>
# 4. In template-project-core/src/main/kotlin:
#    - Remove: import com.example.templateproject.persistence.*
#    - Remove: ExampleRepository, exampleMapper from ExampleService
#    - Refactor methods to not use repository (in-memory or stateless)
# 5. Update ExampleServiceTest to remove repository mocks
# 6. Remove from template-project-web/pom.xml:
#    - spring-boot-jdbc-test
#    - spring-boot-testcontainers
#    - testcontainers-postgresql
# 7. Remove from docker-compose.yml:
#    - Entire database service block
#    - All spring.datasource.* properties
#    - All spring.liquibase.* properties
# 8. Delete: template-project-persistence directory
# 9. Run: mvn clean install
```

---

### Option D: Rename Project + Remove Both Modules
**Time:** ~2-2.5 hours | **Difficulty:** High

Combine steps from Option B and Option C, ensuring all references are cleaned up.

---

## File Structure After Each Option

### Current (All Modules)
```
.
├── template-project-api
├── template-project-persistence
├── template-project-client
├── template-project-core
├── template-project-web
├── template-project-jacoco-report
└── pom.xml (modules: api, persistence, client, core, web, jacoco-report)
```

### After Option B (No Client)
```
.
├── template-project-api
├── template-project-persistence
├── template-project-core
├── template-project-web
├── template-project-jacoco-report
└── pom.xml (modules: api, persistence, core, web, jacoco-report)
```

### After Option C (No Persistence)
```
.
├── template-project-api
├── template-project-client
├── template-project-core
├── template-project-web
├── template-project-jacoco-report
└── pom.xml (modules: api, client, core, web, jacoco-report)
```

### After Option D (No Client, No Persistence)
```
.
├── template-project-api
├── template-project-core
├── template-project-web
├── template-project-jacoco-report
└── pom.xml (modules: api, core, web, jacoco-report)
```

---

## Search & Replace Patterns

### Package Rename (Find & Replace in IDE)

| Find | Replace With | Files |
|------|--------------|-------|
| `com.example.templateproject` | `com.yourcompany.yourproject` | All `.kt`, `.xml` files |
| `template-project` | `your-project-name` | All `.xml`, `.yml` files |
| `templateproject` | `yourproject` | All `.properties`, `.yaml` files |

---

## Critical Pom.xml Changes

### Parent POM (root pom.xml)
```xml
<!-- BEFORE -->
<groupId>com.example</groupId>
<artifactId>template-project</artifactId>

<!-- AFTER -->
<groupId>com.yourcompany</groupId>
<artifactId>your-project-name</artifactId>
```

### Module POMs (each module/pom.xml)
```xml
<!-- BEFORE -->
<parent>
    <groupId>com.example</groupId>
    <artifactId>template-project</artifactId>
</parent>

<!-- AFTER -->
<parent>
    <groupId>com.yourcompany</groupId>
    <artifactId>your-project-name</artifactId>
</parent>
```

### Dependencies Between Modules
```xml
<!-- BEFORE -->
<dependency>
    <groupId>com.example</groupId>
    <artifactId>template-project-core</artifactId>
    <version>${project.parent.version}</version>
</dependency>

<!-- AFTER -->
<dependency>
    <groupId>com.yourcompany</groupId>
    <artifactId>your-project-name-core</artifactId>
    <version>${project.parent.version}</version>
</dependency>
```

---

## Module Dependency Matrix

| Depends On | api | persistence | client | core | web |
|-----------|-----|-------------|--------|------|-----|
| **api** | - | - | - | - | - |
| **persistence** | ✓ | - | - | - | - |
| **client** | ✓ | - | - | - | - |
| **core** | ✓ | ✓ | ✓ | - | - |
| **web** | - | - | - | ✓ | - |
| **jacoco-report** | ✓ | ✓ | ✓ | ✓ | ✓ |

**Legend:**
- ✓ = Has dependency
- \- = No dependency

**Important:** When removing a module, you must also remove it from all modules that depend on it.

---

## Validation Commands

```bash
# After customization, run these commands:

# 1. Validate POM structure
mvn clean validate

# 2. Compile all modules
mvn clean compile

# 3. Run all tests
mvn clean test

# 4. Full build with integration tests
mvn clean install

# 5. Check for dependency conflicts
mvn dependency:tree

# 6. Verify module resolution
mvn reactor:summary
```

---

## Common Issues & Quick Fixes

### Issue: "Cannot find symbol" after package rename
```
Solution: 
1. Right-click project → Maven → Reimport
2. Invalidate IDE cache: File → Invalidate Caches...
3. Rebuild project: Build → Rebuild Project
```

### Issue: Tests fail after removing persistence
```
Solution:
1. Remove imports of persistence classes from test files
2. Remove @MockK annotations for Repository/Entity classes
3. Refactor test methods that use database
```

### Issue: Docker compose fails "service not found"
```
Solution:
1. If removed persistence: Remove 'database' service from docker-compose.yml
2. Remove 'depends_on: [database]' from server service
3. Remove all spring.datasource.* environment variables
```

### Issue: POM shows "module not found" errors
```
Solution:
1. Ensure module removed from root <modules> section
2. Ensure all <dependency> references updated to new artifactId
3. Run: mvn clean dependency:resolve
```

---

## Automation with AI Agent

For fully automated customization, use the comprehensive prompt in CUSTOMIZATION.md Part 6.

**To use:**
1. Copy the AI Agent Prompt from CUSTOMIZATION.md
2. Fill in your parameters:
   ```
   - [USER_GROUP_ID] = com.acme
   - [USER_ARTIFACT_ID] = my-service
   - [USER_PACKAGE_NAME] = com.acme.myservice
   - [USER_DESCRIPTION] = My awesome service
   - [YES/NO] = Remove Client Module
   - [YES/NO] = Remove Persistence Module
   ```
3. Submit to GitHub Copilot or similar AI assistant
4. Review and apply the suggested changes

---

## Pre-Customization Checklist

- [ ] Project is in Git and committed
- [ ] You have a backup of the project
- [ ] You know your target:
  - [ ] New group ID
  - [ ] New artifact ID
  - [ ] New package name
  - [ ] Modules to keep
- [ ] Java IDE is set to Java 21+
- [ ] Maven is available in terminal
- [ ] All IDEs and editors are closed (to avoid file lock issues)

---

## Post-Customization Verification

- [ ] `mvn clean install` succeeds with all tests passing
- [ ] No red squiggles in IDE (all imports resolved)
- [ ] Docker compose runs: `docker-compose up`
- [ ] Health check endpoint responds: `curl http://localhost:8080/actuator/health`
- [ ] Swagger UI loads: `http://localhost:8080/swagger-ui.html`
- [ ] All git changes staged and committed

---

## Documentation to Update

After customization, update these files:

1. **README.md** 
   - Change project name and description
   - Update any project-specific instructions

2. **.document/FEATURES.md**
   - Reference removed modules if applicable

3. **.document/DEVELOPMENT.md**
   - Update package names in examples
   - Update any module-specific setup steps

4. **docker-compose.yml**
   - Update container and image names
   - Remove sections for removed modules

---

## Need Help?

Refer to the full guide:
- **CUSTOMIZATION.md** - Complete step-by-step guide
- **README.md** - Project overview
- **DEVELOPMENT.md** - Development setup

Or contact: Use AI Agent with the comprehensive prompt in Part 6 of CUSTOMIZATION.md

---

**Last Updated:** March 2026
**Version:** 1.0

