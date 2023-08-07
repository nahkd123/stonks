# Welcome to Stonks2 developer's documentations!
This documentations is for mod/plugin developers that wants to integrate Stonks2 with their mods/plugins, or for developers that wants to use Stonks2 Core for their own "stock market".

## Using Stonks2
### Fabric
The only build system that you can use to make Fabric mods is Gradle (although someone managed to make mods with Maven).

```groovy
repositories {
    maven { url 'https://jitpack.io/' }
}

dependencies {
    modImplementation 'com.github.nahkd123.stonks:stonks-fabric:1.20-SNAPSHOT'

    // Tagged release (if any):
    // modImplementation 'com.github.nahkd123.stonks:stonks-fabric:2.0.0+1.20.1'
}
```

### Bukkit
Bukkit platform will be supported at a later date. My current priority is Fabric.
