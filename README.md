
# Android PdfViewer

Supports 16 KB page size.


## Installation

Add to _build.gradle_:

1) Add JitPack to your repositories (Gradle 7+):

```gradle
// settings.gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

2) Add the dependency:

```gradle
implementation 'com.github.xposed73:AndroidPdfViewer:1.0.1'
```

## Publish (quickest: JitPack)

1) Push this project to a public GitHub repo under your account.
2) Create a release tag (example: `v1.0.1`).
3) Visit `https://jitpack.io/#xposed73/AndroidPdfViewer` and trigger a build for the tag.
4) Consumers can then use the dependency shown above.

Notes:
- If JitPack needs a specific JDK/Gradle, add a `jitpack.yml` with your settings.
- Keep tags semantic (e.g., `v1.0.2`) to publish updates.
