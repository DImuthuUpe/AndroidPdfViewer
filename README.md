
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
implementation 'com.github.xposed73:AndroidPdfViewer:1.0.0'
```
