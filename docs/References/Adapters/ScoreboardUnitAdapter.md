# `ScoreboardUnitAdapter`
This adapter converts products with `scoreboard-objective` construction data into scoreboard scores and vice versa. This adapter doesn't have any extra configurations.

=== "Fabric"
    ```naharaconfig
    useAdapter stonks.fabric.adapter.provided.ScoreboardUnitAdapter
    ```

## Construction data
```naharaconfig
construction scoreboard-objective <Objective name>
```

- Construction data must have `scoreboard-objective` as prefix.
- `<Objective name>`: Name of scoreboard objective. If the scoreboard objective doesn't exists, it will creates a new one with `dummy` criterion.

!!! tip "What can you do with `ScoreboardUnitAdapter`?"
    - Essences system where each player can spend their essences (removing score from scoreboard objective) to enchant their items without spending XP. Players can farm those essences so they can sell them to other players through Stonks.
    - If you are creative enough, you can figure out what to do with this adapter!
