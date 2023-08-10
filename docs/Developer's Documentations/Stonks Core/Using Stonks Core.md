# Using Stonks Core
## Getting Stonks Core library
Stonks Core is available through JitPack:

=== "Gradle (Groovy)"
    ```groovy hl_lines="6"
    repositories {
        maven { url = 'https://jitpack.io/' } // (1)!
    }

    dependencies {
        implementation 'com.github.nahkd123.stonks:stonks-core:main-SNAPSHOT' // (2)!
    }
    ```

    1.  There can be more than 1 repository.
    2.  Replace `main-SNAPSHOT` with tag or commit hash to use Stonks Core with specific version/commit.
=== "Maven"
    ```xml
    <repositories>
        <repository> <!--(1)!-->
            <id>JitPack</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
    <dependencies>
        <dependency>
            <groupId>com.github.nahkd123.stonks</groupId>
            <artifactId>stonks-core</artifactId>
            <version>main-SNAPSHOT</version> <!--(2)!-->
        </dependency>
    </dependencies>
    ```

    1.  There can be more than 1 repository.
    2.  Replace `main-SNAPSHOT` with tag or commit hash to use Stonks Core with specific version/commit.

## Using Stonks Core in your project
### Creating service
Stonks Core provides `StonksMemoryService`, a service that stores everything inside memory. For a small project, this is sufficient enough.

```java
import stonks.core.service.memory.*;

StonksMemoryService service = new StonksMemoryService();

// Adding category
// Products information are provided by services so front-ends doesn't have to
// rely on configurations to know all products, thanks to construction data.
MemoryCategory category = new MemoryCategory("category_id", "My Category");
service.getModifiableCategories().add(category);

// Adding product
String constructionData = "Construction data here..."; //(1)!
MemoryProduct product = new MemoryProduct(category, "product_id", "My Product", constructionData);
category.getModifiableMockProducts().add(product);
```

1.  Construction data can be anything. It can be JSON, it can be data that's compatible with Stonks adapters, or it can be base64 of an executable binary. It's up to you to decide!
    
    Note that construction data is what will be used to construct visuals on front-ends.

### Making service calls
In production environment, service calls takes time to get processed, so all methods in `StonksService` return `Task<?>`, which are [Nahara's Toolkit Tasks](https://github.com/nahkd123/nahara-toolkit):

```java
public interface StonksService {
	public Task<List<Category>> queryAllCategories();
	public Task<ProductMarketOverview> queryProductMarketOverview(Product product);
	public Task<List<Offer>> getOffers(UUID offerer);
	public Task<Offer> claimOffer(Offer offer);
	public Task<Offer> cancelOffer(Offer offer);
	public Task<Offer> listOffer(UUID offerer, Product product, OfferType type, int units, double pricePerUnit);
	public Task<InstantOfferExecuteResult> instantOffer(Product product, OfferType type, int units, double balance);
    //(1)!
}
```

1.  There are more service calls, but some of them are not included in this code block to save spaces.

Since we are not in production environment, we want to obtain result without writing extra code just to deal with potential errors/wait time. For that, you can use `#!java .await()`:

```java
List<Category> categories = service.queryAllCategories().await();
Product product = categories.stream()
    .filter(p -> p.getProductId().equals("product_id"))
    .findFirst().get();
ProductMarketOverview overview = service.queryProductMarketOverview(product).await();
OverviewOffersList buyOffers = overview.getBuyOffers();
// ...
```

### Custom service implementation
Implement your own service by implementing `StonksService` interface. You can take a look at source code for [StonksMemoryService](https://github.com/nahkd123/stonks/blob/main/1.20.x/core/src/main/java/stonks/core/service/memory/StonksMemoryService.java) if you need example.
