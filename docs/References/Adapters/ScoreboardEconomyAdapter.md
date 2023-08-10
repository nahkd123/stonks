# `ScoreboardEconomyAdapter`
This adapter allows you to use scoreboard score for player's balance.

=== "Fabric"
    ```naharaconfig
    useAdapter stonks.fabric.adapter.provided.ScoreboardEconomyAdapter
        // Name of scoreboard objective you want to use
        // If the objective doesn't exists, it will creates a new "dummy" objective
        // Default value is "balance"
        objective balance

        // Decimal points (Eg: 2 decimal points -> 200 score == $2.00)
        // If you don't want decimals, set this to 0
        // Default value is 2
        decimals 2
    ```
