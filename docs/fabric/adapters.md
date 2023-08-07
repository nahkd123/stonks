# Adapters
Adapters are used to convert Stonks2 Core products into its equivalents in your game, as well as providing economy support for Stonks.

## Example use cases
- An adapter that allows you to trade "essences" on Stonks2 players' market
- An adapter that add economy support for economy mod.

## Existing adapters
Most of the time you don't need to create a new adapter for your own need. Here are existing adapters:

### ``stonks.fabric.adapter.provided.ItemsAdapter``
Convert products with ``item`` prefixes in construction data.

```
useAdapter stonks.fabric.adapter.provided.ItemsAdapter
    // This adapter does not have any configurations
```

```
construction item namespace:id{NbtData: [ \
    "anything you want in here...", \
    "just like /give command", \
    ] \
}
```

### ``stonks.fabric.adapter.provided.ScoreboardUnitAdapter``
Turn your scoreboard objective scores into products.

```
useAdapter stonks.fabric.adapter.provided.ScoreboardUnitAdapter
    // This adapter does not have any configurations
```

```
construction scoreboard-objective myObjective
```

### ``stonks.fabric.adapter.provided.ScoreboardEconomyAdapter``
Turn your scoreboard objective into economy system.

```
useAdapter stonks.fabric.adapter.provided.ScoreboardEconomyAdapter
    // Name of your objective to use
    objective balance

    // Decimal points. In this case, 200 score == $2
    decimals 2
```

### ``stonks.fabric.adapter.provided.CommonEconomyAdapter``
Use Patbox's Common Economy API. If your economy mod uses decimals, you must specify it.

```
useAdapter stonks.fabric.adapter.provided.CommonEconomyAdapter
    // ID of the account that's registered with Patbox's Common Economy API
    id modid:mod_account

    // Decimal points. In this case, 200 in raw balance == $2
    decimals 2
```

## Creating a new adapter
To create a new adapter, register it with StonksProvidersRegistry:

```java
import stonks.fabric.provider.StonksProvidersRegistry;

StonksProvidersRegistry.registerAdapter(MyAdapter.class, (server, config) -> new MyAdapter());
```

After that you can use ``useAdapter com.example.package.MyAdapter`` to use your adapter.
