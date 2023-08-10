# Service Providers
Sometimes `IntegratedStonksService` is not enough, especially when you want to use Stonks with a database of your choice. That's why you need to create your own service provider.

## Creating your service
Create your own service by implementing `StonksService` interface. See [this](<../Stonks Core/Using Stonks Core.md#custom-service-implementation>) for more information.

## Registering your provider
Providers should be registered before server starts, and the proper way to do this is by registering them inside your mod entry point (which can be a static method or `ModInitializer#onInitialize()`):

```java
// Assuming your service is com.example.MyStonksService
import com.example.MyStonksService;

StonksProvidersRegistry.registerService(MyStonksService.class, (server /*(1)!*/, config /*(2)!*/) -> {
    // `config` contains all configurations under `useService` config entry
    int myConfiguredValue = config.firstChild("myConfiguredValue")
        .flatMap(v -> v.getValue(Integer::parseInt))
        .orElse(0);
    return new MyStonksService(myConfiguredValue);
});
```

1.  yarn mapping: `MinecraftServer`
2.  `nahara.common.configurations.Config`, a.k.a Nahara's Toolkit Configurations

## Using your service
To use your service, use `useService [Qualified service class name]`:

```naharaconfig
useService com.example.MyStonksService
    myConfiguredValue 420
```
