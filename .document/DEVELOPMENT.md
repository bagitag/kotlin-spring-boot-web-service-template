# Development

<details>
  <summary>Table of Contents</summary>

- [Code formatting and linting](#code-formatting-and-linting)
- [Static code analysis](#static-code-analysis)
- [Dependency management](#dependency-management)
- [Kotlin Notebook](#kotlin-notebook)

</details>

## Code formatting and linting

The codebase is formatted by the default IntelliJ IDEA code style settings.

[Ktlint](http://ktlint.github.io/) is responsible for validating the formatting rules. It is configured to run automatically during the build process (in the `package` phase) via the [ktlint Maven Plugin](https://github.com/gantsign/ktlint-maven-plugin).

It is recommended to install the [Ktlint IntelliJ plugin](https://plugins.jetbrains.com/plugin/15057-ktlint) to ensure that the code is formatted correctly while you are writing it.

Further coding styles can be added by editing the [.editorconfig](../.editorconfig).

## Static code analysis

[Detekt](https://detekt.dev/) is configured as a static code analysis tool for the project. See [detekt-config.yml](../detekt-config.yml) for rules and configurations.

It is configured to run automatically during the build process (in the `validate` phase) via the [Detekt Maven Plugin](https://github.com/Ozsie/detekt-maven-plugin)

It is recommended to install the [Detekt IntelliJ plugin](https://plugins.jetbrains.com/plugin/10761-detekt) to ensure that the code is analyzed correctly while you are writing it.

## Dependency management

The [libyear-maven-plugin](https://github.com/mfoo/libyear-maven-plugin) is used to track and manage the age of dependencies in this project. It analyzes the project's dependencies, checks their release dates, and reports the "age" of each library. The plugin is configured to run during the build process (in the `package` phase) and will fail the build if any dependency is older than the configured threshold. See details in the [pom.xml](../pom.xml) file.

## Kotlin Notebook

The [attached kernel mode](https://www.jetbrains.com/help/idea/kotlin-notebook.html#attached-kernel-mode) of [Kotlin Notebook](https://www.jetbrains.com/help/idea/kotlin-notebook.html) is supported via the `kotlin-jupyter-spring-starter` Maven dependency.
