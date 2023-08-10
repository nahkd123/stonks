# Configuration
## File location
- On server: `[server root]/config/stonks/config`
- On client: `.minecraft/config/stonks/config`

## Configuration syntax
!!! info
    This configuration syntax is based on [Nahara's Toolkit - Configurations](https://github.com/nahkd123/nahara-toolkit).

```naharaconfig
// This is a comment

// This is a key-value pair configuration entry
key value

// You can have multiple key-value pairs with same key and different values
key yet another value

// This is nested key
// "name" and "author" belongs to "section"
section my_section_id
    name My section name
    author My name

// You can define an entry with no value
// Usually for toggling something
keyWithNoValue
```

## Stonks configuration
!!! info
    This configuration can be used with Fabric or Bukkit version of Stonks.

### Registering service
Each server can only have 1 running Stonks service, which must be configured manually in configuration file. Simply use `useService [Service Name]` to register it with your server:

=== "Fabric"
    ```naharaconfig
    useService stonks.fabric.service.IntegratedStonksService
        // Some services may requires you to configure something
        // You can configure your service by adding nested entries under "useService"
    ```

For a list of services see [here](../References/Services/IntegratedStonksService.md).

### Registering adapters
Unlike services, you can register multiple adapters inside your configuration file. Adapters are used to convert Stonks products into equivalents in your server and vice versa.

=== "Fabric"
    ```naharaconfig
    useAdapter stonks.fabric.adapter.provided.ItemsAdapter
        // Some services may requires you to configure something
        // You can configure your service by adding nested entries under "useAdapter"
    ```