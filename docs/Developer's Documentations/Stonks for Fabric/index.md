# Introduction
Stonks for Fabric is the front-end for Stonks Core as a Fabric server-side mod. "Server-side mod" doesn't always means it only works on dedicated server; it also works on singleplayer!

## Getting started
Add Stonks for Fabric as mod dependency:

```groovy hl_lines="6"
dependencies {
    maven { url = 'https://jitpack.io/' }
}

dependencies {
    modImplementation 'com.github.nahkd123.stonks:stonks-fabric:main-SNAPSHOT' // (1)!
}
```

1.  Replace `main-SNAPSHOT` with tag or commit hash to use Stonks with specific version/commit.

## What do to next?
- Register your own service provider
- Register your own adapter
