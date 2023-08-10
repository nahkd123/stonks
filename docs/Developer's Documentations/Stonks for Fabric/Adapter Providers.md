# Adapter Providers
!!! note
    Both product adapters and economy adapters use the same `StonksFabricAdapter` interface.

## Product adapters
Product adapters converts Stonks products into in-game stuffs, such as [items](../../References/Adapters/ItemsAdapter.md), [scoreboard scores](../../References/Adapters/ScoreboardUnitAdapter.md) or something from your mod (selling mana, anyone?)

To create your own product adapter, simple implement `StonksFabricAdapter` interface. Here is an example of an adapter that allows player to sell their experience levels for money:

```java
public class ExperienceOrbsAdapter implements StonksFabricAdapter {
    private static final String CONSTRUCTION_DATA = "experience-orbs"; //(1)!

    @Override
	public ItemStack createDisplayStack/*(2)!*/(Product product) {
		return new ItemStack(Items.EXPERIENCE_BOTTLE);
	}

    @Override
    public int getUnits(ServerPlayerEntity player, Product product) {
        if (!product.getProductConstructionData().equals(CONSTRUCTION_DATA)) return -1;
        return player.experienceLevel;
    }

    @Override
    public boolean addUnitsTo(ServerPlayerEntity player, Product product, int amount) {
        if (!product.getProductConstructionData().equals(CONSTRUCTION_DATA)) return -1;
        player.setExperienceLevel(player.experienceLevel + amount);
        return true;
    }

    @Override
    public boolean removeUnitsFrom(ServerPlayerEntity player, Product product, int amount) {
        if (!product.getProductConstructionData().equals(CONSTRUCTION_DATA)) return -1;
        player.setExperienceLevel(player.experienceLevel - amount);
        return true;
    }
}
```

1.  This adapter only applies to products with `experience-orbs` construction data.
2.  Display stack will be used to show on product menu, offers list and offer options menu.

## Economy adapters
Economy adapters allows Stonks to access player's money account, along with withdrawing/depositing money to their account.

!!! note
    Normally, you don't need to create your own economy adapters. If your mod uses a custom economy system, you can register your economy system to Patbox's Common Economy API. Beware though: this API only allows you to use integer (`long` to be precise) for player's account.

To create your own product adapter, simple implement `StonksFabricAdapter` interface. Here is an example:

```java
public class MyMoneyAdapter implements StonksFabricAdapter {
    @Override
	public double accountBalance(ServerPlayerEntity player) {
        return MyMoney.getBalance(player);
    }

    @Override
	public boolean accountDeposit(ServerPlayerEntity player, double money) {
        MyMoney.deposit(player, money);
        return true; //(1)!
    }

    @Override
	public boolean accountWithdraw(ServerPlayerEntity player, double money) {
        MyMoney.withdraw(player, money);
        return true;
    }
}
```

1.  The main reason why you have to return `true` here is to prevent Stonks from using other adapters for economy. Normally it should be fine to return `false` if there is no "hybird" adapters, but Stonks will freak out when no economy adapters succeed in accepting withdraw/deposit request.

## Registering your provider
Providers should be registered before server starts, and the proper way to do this is by registering them inside your mod entry point (which can be a static method or `ModInitializer#onInitialize()`):

```java
// Assuming all of your adapters are in com.example package
import com.example.*;

StonksProvidersRegistry.registerAdapter(ExperienceOrbsAdapter.class, (server, config /*(1)!*/) -> {
    return new ExperienceOrbsAdapter();
});
StonksProvidersRegistry.registerAdapter(MyMoneyAdapter.class, (server, config) -> {
    return new MyMoneyAdapter();
});
```

1.  You can use `config` to read configurations for your adapter under `useAdapter` entry.

## Using your adapters
To use your adapters, use `useAdapter [Qualified adapter class name]` for each adapter you want to use. You can use `useAdapter` multiple times for a single adapter, each with different configurations:

```naharaconfig
useAdapter com.example.ExperienceOrbsAdapter
useAdapter com.example.MyMoneyAdapter

// This will add another ExperienceOrbsAdapter, but it won't do anything because
// the first ExperienceOrbsAdapter already claimed any products with "experience-orb"
// construction data.
useAdapter com.example.ExperienceOrbsAdapter
    adapterConfigValue 69 nice
```

## Auto register your adapters
In addition to configuration file, you can get your adapter added every time server starts by using `StonksFabricAdapterCallback`:

```java
import stonks.fabric.adapter.StonksFabricAdapterCallback;

StonksFabricAdapterCallback.EVENT.register((server, adapters) -> {
    adapters.add(new ExperienceOrbsAdapter());
});
```

!!! note
    Adapters added in this way will not have access to configuration (if the code above isn't obvious enough).
