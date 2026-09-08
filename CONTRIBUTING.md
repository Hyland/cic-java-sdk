# Contributing to cic-java-sdk

Thank you for your interest in contributing to the Hyland CIC Java SDK!

## Contributor License Agreement (CLA)

Before Hyland can accept any contribution (code, tests, or documentation),
you must sign Hyland's Contributor License Agreement (CLA), modeled on the
Apache Software Foundation's CLA. This grants Hyland the rights needed to
distribute your contribution while you retain ownership of your work.

> **TODO:** link to the finalized Individual/Corporate CLA and signing
> process once published. Do not submit a pull request containing
> substantial contributions until the CLA process is available.

## Getting Started

1. Fork the repository and create a feature branch from `main`.
2. Ensure you have JDK 17 installed (`maven.compiler.release` in the root `pom.xml`).
3. Build and test the project:

   ```bash
   mvn --batch-mode -nsu install
   ```

4. The build enforces code formatting via the Spotless Maven plugin
   (`sdk-formatter.xml` / `sdk.importorder`). Run it before committing:

   ```bash
   mvn spotless:apply
   ```

## Submitting Changes

- Keep pull requests focused on a single change/topic.
- Include unit tests for new behavior and update documentation (README,
  Javadoc) where relevant.
- Ensure `mvn --batch-mode -nsu install` passes locally before opening a PR.
- Describe the motivation and context for the change in the PR description.

## Reporting Issues

Please use GitHub Issues to report bugs or request features. Include steps
to reproduce, expected vs. actual behavior, and relevant SDK/module
versions.

## Code of Conduct

This project follows the [Code of Conduct](CODE_OF_CONDUCT.md). By
participating, you are expected to uphold it.

## Security Issues

Do not report security vulnerabilities via public GitHub issues. See
[SECURITY.md](SECURITY.md) for our responsible disclosure process.
