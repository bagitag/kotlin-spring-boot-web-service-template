# Development

Guidelines and tools for developing the project.

---

<details>
  <summary>Table of Contents</summary>

- [Code formatting and linting](#code-formatting-and-linting)
- [Static code analysis](#static-code-analysis)
- [Dependency management](#dependency-management)
- [Kotlin Notebook](#kotlin-notebook)

</details>

## Code formatting and linting

The codebase uses the default IntelliJ IDEA code style settings.

[Ktlint](http://ktlint.github.io/) validates the formatting rules and runs automatically during the build's `package` phase via the [ktlint Maven Plugin](https://github.com/gantsign/ktlint-maven-plugin).

Install the [Ktlint IntelliJ plugin](https://plugins.jetbrains.com/plugin/15057-ktlint) to format code as you edit.

Additional coding styles can be configured in [.editorconfig](../.editorconfig).

## Static code analysis

[Detekt](https://detekt.dev/) is configured as project's static code analysis tool. See [detekt-config.yml](../detekt-config.yml) for rules and configuration.

The [Detekt Maven Plugin](https://github.com/Ozsie/detekt-maven-plugin) is configured to run automatically during the build's `validate` phase. 

Install the [Detekt IntelliJ plugin](https://plugins.jetbrains.com/plugin/10761-detekt) to get analysis feedback as you code.

## Dependency management

The [libyear-maven-plugin](https://github.com/mfoo/libyear-maven-plugin) is used to track and manage the age of dependencies. It analyzes the dependencies, checks their release dates, and reports the "age" of each library. The plugin is configured to run during the build process (in the `package` phase) and the build fails if any dependency is older than the configured threshold. See details in the [pom.xml](../pom.xml) file.

> [!WARNING]
> This plugin is currently not working, so it is commented out.

## Kotlin Notebook

The [attached kernel mode](https://www.jetbrains.com/help/idea/kotlin-notebook.html#attached-kernel-mode) of [Kotlin Notebook](https://www.jetbrains.com/help/idea/kotlin-notebook.html) is supported via the `kotlin-jupyter-spring-starter` Maven dependency. Build the project with the `-Pdev` profile to include the dependency.
