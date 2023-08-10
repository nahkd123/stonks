# `ItemsAdapter`
This adapter converts products with `item` construction data into items in your server and vice versa. This adapter doesn't have any extra configurations.

=== "Fabric"
    ```naharaconfig
    useAdapter stonks.fabric.adapter.provided.ItemsAdapter
    ```

## Construction data
```naharaconfig
construction item [namespace:]<item_id>
construction item [namespace:]<item_id>{NBTData...}
```

- Construction data must have `item` as prefix.
- `[namespace:]<item_id>`: The item ID. Because this adapter supports namespaced item IDs, you can use items from other mods, like `mycoolmod:steel` for example. Namespace is `minecraft` by default.
- `{NBTData...}`: NBT string. If you are using custom items, you might want to add this NBT data at the end of construction data.
