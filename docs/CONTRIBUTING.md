# Contributing to Porthole

Thank you for your interest in contributing to Porthole! This document covers the development workflow, code standards, and expectations for contributions.

## Architecture Overview

Porthole is a Kotlin-only Android app built with:
- **Jetpack Compose** for UI
- **Hilt** for dependency injection
- **Kotlin Coroutines + Flow** for async operations
- **DataStore** for preferences

The app follows a unidirectional data flow pattern:
```
UI (Composables) → ViewModels → Business Logic (Managers) → Data (DataStore)
```

Key packages:
- `session/` — Session lifecycle, state machine, countdown timer
- `network/` — Gateway resolution, URL allowlisting, connectivity checking
- `webview/` — WebView sandboxing and navigation interception
- `notification/` — Foreground service and persistent notifications
- `settings/` — DataStore-backed preferences
- `ui/` — Compose screens, ViewModels, theme, components

For the full architecture guide, see [ARCHITECTURE.md](ARCHITECTURE.md).

## Development Setup

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK with API 34

### Building

```bash
git clone https://github.com/sfoerster-dev/porthole.git
cd porthole
./gradlew assembleDebug
```

### Running Tests

```bash
# Unit tests
./gradlew testDebugUnitTest

# Lint checks
./gradlew ktlintCheck

# Static analysis
./gradlew detekt

# Android lint
./gradlew lintDebug

# All checks
./gradlew ktlintCheck detekt testDebugUnitTest lintDebug
```

## Code Style

### Kotlin Style

Porthole uses [ktlint](https://pinterest.github.io/ktlint/) with the Android ruleset for code formatting. The configuration is in `app/build.gradle.kts`.

Key conventions:
- 4-space indentation
- Max line length: 120 characters
- Trailing commas in multi-line parameter lists
- No wildcard imports

To auto-format before committing:
```bash
./gradlew ktlintFormat
```

### Static Analysis

[detekt](https://detekt.dev/) is configured in `app/detekt.yml`. Notable rules:
- Magic numbers must be in companion objects or top-level constants
- Functions annotated with `@Composable` are exempt from `FunctionNaming` rules
- Long parameter lists are allowed in constructors (up to 10 params for DI)

### Code Conventions

- Every public class and function must have KDoc documentation
- No `GlobalScope` — all coroutines use structured concurrency
- No magic numbers — use named constants with comments
- All WebView interactions must happen on the main thread
- ViewModels handle all business logic — no logic in Composables
- Use `StateFlow` for observable state, not `LiveData`

## Making Changes

### Branch Naming

- `feature/short-description` for new features
- `fix/short-description` for bug fixes
- `docs/short-description` for documentation changes

### Commit Messages

Write clear, concise commit messages:
- Use imperative mood ("Add feature" not "Added feature")
- First line under 72 characters
- Reference issue numbers where applicable

### Pull Request Expectations

1. **One logical change per PR** — Don't mix features, bug fixes, and refactoring
2. **Tests required** — New business logic must have unit tests
3. **Lint clean** — `ktlintCheck` and `detekt` must pass with zero warnings
4. **Documentation** — Update relevant docs if behavior changes
5. **Security review** — Any changes to `webview/`, `session/`, or `network/` packages require extra scrutiny. Explain the security implications in the PR description.
6. **No new permissions** — Porthole uses a minimal permission set. Adding a new permission requires strong justification and maintainer approval.

### PR Template

```markdown
## What

Brief description of the change.

## Why

Motivation and context.

## Security Implications

Does this change affect the WebView, session lifecycle, or network behavior?
If yes, explain the security impact.

## Testing

How was this tested? Include relevant test output or screenshots.
```

## Security Disclosure

If you discover a security vulnerability in Porthole, **do not open a public issue**. Instead:

1. Email the details to security at stevenfoerster.com
2. Include:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fix (if any)
3. Allow reasonable time for a fix before public disclosure

We take security seriously and will respond promptly to responsible disclosures.

## What We're Looking For

Good contributions include:
- Bug fixes with regression tests
- Improvements to WebView sandboxing
- Better captive portal detection heuristics
- Accessibility improvements
- Translations
- Documentation improvements

Contributions that will likely be declined:
- Features that store user data
- Analytics or telemetry
- Network calls from app code (only the WebView makes network calls)
- Dependencies on third-party networking libraries
- Features requiring additional permissions beyond the current set

## License

By contributing to Porthole, you agree that your contributions will be licensed under the Apache License 2.0, the same license as the project.
