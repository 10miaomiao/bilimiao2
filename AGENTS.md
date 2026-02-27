# AGENTS.md - Agent Guidelines for Bilimiao

## Project Overview

Bilimiao is an Android third-party Bilibili client written in Kotlin. It uses Jetpack Compose for UI, Kodein for dependency injection, and kotlinx.serialization for JSON parsing.

## Build Commands

### Basic Build
```bash
./gradlew assembleFullDebug          # Debug build (full flavor)
./gradlew assembleFossDebug          # FOSS flavor debug build
./gradlew assembleFullRelease       # Release build (full flavor)
./gradlew assembleFossRelease       # Release build (foss flavor)
./gradlew -Pchannel=Github assembleFullRelease  # Release with channel
```

### Running Tests
```bash
./gradlew test                       # Run all unit tests
./gradlew testFullDebugUnitTest      # Run unit tests for fullDebug variant
./gradlew testFossDebugUnitTest     # Run unit tests for fossDebug variant

# Run a single test class
./gradlew test --tests "com.a10miaomiao.bilimiao.ExampleUnitTest"

# Run a specific test method
./gradlew test --tests "com.a10miaomiao.bilimiao.ExampleUnitTest.addition_isCorrect"
```

### Lint and Analysis
```bash
./gradlew lint                       # Run lint on all modules
./gradlew app:lintFullDebug          # Run lint on specific variant
./gradlew lintAnalyzeFullDebugUnitTest  # Analyze lint for unit tests
```

### Other Useful Commands
```bash
./gradlew clean                      # Clean build artifacts
./gradlew build                      # Full build with tests
./gradlew :app:dependencies         # Show app dependencies
```

## Code Style Guidelines

### Kotlin Conventions

1. **Naming**
   - Classes/Objects/Interfaces: `PascalCase` (e.g., `ArchiveApi`, `ResultInfo`)
   - Functions/Properties: `camelCase` (e.g., `getCookie`, `isSuccess`)
   - Constants: `UPPER_SNAKE_CASE` (e.g., `GET`, `POST`)
   - Packages: lowercase (e.g., `com.a10miaomiao.bilimiao.comm.apis`)

2. **Imports**
   - Use explicit imports (no wildcard imports unless from same package)
   - Group imports: standard library, Android, third-party, project-specific
   - Sort alphabetically within groups

3. **Formatting**
   - Use 4 spaces for indentation (Kotlin default)
   - Max line length: 120 characters (Android Studio default)
   - Opening brace on same line for classes/functions
   - Use expression bodies for simple functions when appropriate

4. **Types**
   - Use Kotlin's type system (no primitive wrappers unless needed)
   - Use nullable types (`?`) appropriately
   - Prefer `val` over `var` unless mutation is required
   - Use `data class` for data holders with `equals`/`hashCode`/`toString`
   - Use `@Serializable` from kotlinx.serialization for JSON-serializable classes

5. **Null Safety**
   - Use safe call operator (`?.`) and elvis operator (`?:`)
   - Prefer `lateinit var` over nullable when initialization is guaranteed
   - Use `?.let` for null checks

6. **Coroutines**
   - Use `suspend` functions for asynchronous operations
   - Use structured concurrency (viewModelScope, lifecycleScope)
   - Handle exceptions with `try-catch` or `CoroutineExceptionHandler`

### Android-Specific Guidelines

1. **Activities/Fragments**
   - Follow Android lifecycle patterns
   - Use ViewBinding or Compose instead of findViewById
   - Handle runtime permissions properly

2. **ViewModels**
   - Use AndroidX ViewModel with Kodein injection
   - Expose state via `StateFlow` or `LiveData`
   - Handle configuration changes automatically

3. **Networking**
   - Use OkHttp for HTTP requests
   - Parse responses with kotlinx.serialization
   - Handle errors gracefully with custom exceptions

4. **Dependency Injection**
   - Use Kodein modules for organizing dependencies
   - Prefer constructor injection when possible
   - Lazy delegate for expensive dependencies

5. **Error Handling**
   - Create custom exceptions for domain-specific errors (see `comm/exception/`)
   - Return Result types or use try-catch for expected errors
   - Log errors appropriately (use `miaoLogger()`)

### Data Classes

```kotlin
@Serializable
data class ResultInfo<T>(
    val code: Int,
    val `data`: T,
    val message: String,
    val ttl: Int,
) {
    val isSuccess get() = code == 0
}
```

### API Classes

```kotlin
class ArchiveApi {
    fun relation(aid: String) = MiaoHttp.request {
        url = BiliApiService.biliApi(
            "x/web-interface/archive/relation",
            "aid" to aid
        )
    }
}
```

### Project Structure

```
app/src/main/java/com/a10miaomiao/bilimiao/
├── activity/          # Android Activities
├── comm/              # Shared components
│   ├── apis/          # API definitions
│   ├── datastore/     # DataStore preferences
│   ├── entity/        # Data models
│   ├── exception/     # Custom exceptions
│   ├── network/       # Networking utilities
│   └── utils/         # Utility classes
├── page/              # Page/Fragment implementations
├── service/           # Background services
├── store/             # State management
└── widget/            # Custom views
```

### Test Structure

```
app/src/test/java/     # Unit tests (JUnit 4)
app/src/androidTest/   # Instrumentation tests
```

### Common Gradle Variants

- **Flavors**: `full` (with Baidu stats, Geetest), `foss` (FOSS version)
- **Build Types**: `debug`, `release`, `benchmark`

### Important Configuration Files

- `gradle/libs.versions.toml` - Version catalog for dependencies
- `app/build.gradle.kts` - App module build configuration
- `build.gradle.kts` - Root build configuration

### Notes

- The project uses Java 17 to build, targets Java 1.8 bytecode
- Min SDK: 21, Target/Compile SDK: 35
- No ktlint or detekt configured - use Android Studio's formatter
- Kotlin code style is set to "official" in gradle.properties
