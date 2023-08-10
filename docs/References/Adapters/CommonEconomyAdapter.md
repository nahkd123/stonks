# `CommonEconomyAdapter` (Fabric)
!!! note
    This adapter only available for Stonks for Fabric. For economy on Bukkit, please use `VaultEconomyAdapter`.

This adapter allows you to use economy system from other mods that uses [Patbox's Common Economy API](https://github.com/Patbox/common-economy-api).

=== "Fabric"
    ```naharaconfig
    useAdapter stonks.fabric.adapter.provided.CommonEconomyAdapter
        // ID of economy system you want to use
        // Default value is "stonks:default_account", except Stonks doesn't actually
        // provides economy system.
        id modid:mod_economy_system

        // Decimal points
        // Because Common Economy API uses integers for raw balance, you have to include
        // this if you are using economy system with floating points.
        // Default value is 0
        decimals 0
    ```
