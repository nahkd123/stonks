# `IntegratedStonksService`
This service stores all market data locally (in your world folder to be precise).

=== "Fabric"
    ```naharaconfig
    useService stonks.fabric.service.IntegratedStonksService
        category category_id
            name Category name
            product product_id
                name Product name
                construction Product construction data. See "Adapters" for more information.
            // You can have more than 1 product
            product another_product_id
            product another_another_product_id
            // ...

        // You can have more than 1 category
        category another_category_id
        category another_another_category_id
        // ...
    ```

## Categories
You can define a new category by adding `category` entry under `useService`:

```naharaconfig
useService ...
    category category_id
        // Category name
        // Support for complicated styling will be provided in the future
        name Category name
```

!!! tip
    Want to use colors in your category name? You can use the legacy color code (`ALT + 0167`) in the category name. It works for both Fabric and Bukkit, but make sure to save your configuration as UTF-8 plain text and run your server with UTF-8 encoding.

## Products
You can define a new product by adding `product` entry under `category`:

```naharaconfig
category category_id
    // ...
    product product_id
        name Product name
        construction Product construction data. See "Adapters" for more information.
```

### Construction data
Construction data are used to tell adapters how to convert Stonks product into items/units/whatever that is and vice versa.

For example: An adapter accepts `essence essence_id` construction data and tells Stonks how much essences player have, along with converting add/remove products operations into add/remove essences, respectively.

!!! note
    3rd party mods/plugins can register their own adapters to Stonks. If you are a developer, see Developer's Documentations to learn how to register your adapters.

## Market data file
Market data file can be found inside your world save:
- For server: `world/stonks.bin`
- For client: `.minecraft/saves/<World Name>/stonks.bin`

You can inspect market data using ImHex with [Stonks `.hexpat`](https://github.com/nahkd123/stonks/blob/main/1.20.x/tools/stonks-offer-data.hexpat) pattern.
