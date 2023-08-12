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

// Value can be extended to multiple lines by putting "\" at the end of each
// line. Really useful when writing NBT data.
nbtData { \
    display: { \
        Name: '{"text": "Cursed Paper", "color": "#FF0000", "font": "minecraft:alt"}', \
        Lore: [] \
    } \
}

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

### Platform configuration
Platform configuration allows you to configure currency decimal points, tax, top offer price delta thing and category icons.

=== "All"
    ```naharaconfig
    platformConfig
        // Number of currency decimal points. Used for parsing player's input.
        decimals 2

        // Tax rate. A value of 1 means it will takes 100% of incomes.
        tax 0.00

        // Price delta for top offer. Used for "Top offer +/- $0.1" button
        // in offer price setup menu.
        topOfferPriceDelta 0.1

        // Category icons
        // If you don't specify icons here, it will defaults to minecraft:paper
        categoryIcon foods minecraft:carrot
        categoryIcon specials minecraft:diamond
    ```

    - `decimals`: Decimal points for currency system. You must change this to ensure player can't type an   extremely small number, like `0.0000001` for example. It should be equals to number of decimal points from    economy adapters.
    - `tax`: Tax rate (**not** in percentage!). Setting the value to `0.01` means it will takes 1% from player's    incomes.
    - `topOfferPriceDelta`: Price delta thing for quick offer button (the "Top offer +/- $0.x" button to be     precise).

### Registering service
Each server can only have 1 running Stonks service, which must be configured manually in configuration file. Simply use `useService [Service Name]` to register it with your server:

=== "Fabric"
    ```naharaconfig
    useService stonks.fabric.service.IntegratedStonksService
        // Some services may requires you to configure something
        // You can configure your service by adding nested entries under "useService"
    ```

For a list of services, see [References/Services](../References/Services/IntegratedStonksService.md).

### Registering adapters
Unlike services, you can register multiple adapters inside your configuration file. Adapters are used to convert Stonks products into equivalents in your server and vice versa.

=== "Fabric"
    ```naharaconfig
    useAdapter stonks.fabric.adapter.provided.ItemsAdapter
        // Some services may requires you to configure something
        // You can configure your service by adding nested entries under "useAdapter"
    ```

For a list of adapters, see [References/Adapters](../References/Adapters/CommonEconomyAdapter.md).
