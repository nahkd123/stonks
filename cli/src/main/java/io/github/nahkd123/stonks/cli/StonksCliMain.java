/*
 * Copyright (c) 2023-2025 nahkd
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.nahkd123.stonks.cli;

import java.io.BufferedReader;
import java.io.Console;
import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.nahkd123.stonks.service.ManagableMarketService;
import io.github.nahkd123.stonks.service.Offer;
import io.github.nahkd123.stonks.service.OfferOverviewEntry;
import io.github.nahkd123.stonks.service.OfferType;
import io.github.nahkd123.stonks.service.Product;
import io.github.nahkd123.stonks.service.ProductOffersOverview;
import io.github.nahkd123.stonks.service.ProductOverview;
import io.github.nahkd123.stonks.service.provider.MarketServiceHost;
import io.github.nahkd123.stonks.service.provider.MarketServiceProvider;
import io.github.nahkd123.stonks.utils.dynamic.DynamicReader;
import io.github.nahkd123.stonks.utils.dynamic.json.JsonDynamicReader;

public class StonksCliMain {
	public static void main(String[] args) throws Throwable {
		new StonksCliMain(new ArgumentType.Arguments(args)).run();
	}

	// Service
	private Map<String, MarketServiceProvider<?>> providers;
	private MarketServiceProvider<?> provider;
	private String config;
	private Path configFile;

	// Servers
	private List<MarketServerInfo> servers = new ArrayList<>();

	// Automation
	private String inputCommand;
	private Path inputPath;
	private boolean retainSession;

	public StonksCliMain(ArgumentType.Arguments args) {
		providers = ServiceLoader.load(MarketServiceProvider.class).stream()
			.map(v -> v.get())
			.collect(Collectors.toMap(MarketServiceProvider::getProviderName, Function.identity()));
		args.accept(options);
	}

	private List<Option<?>> options = List.of(
		new Option<>("Show quick help docs", ArgumentType.VOID, this::showHelp, "-?", "-h", "--help"),
		new Option<>("Specify service type (required)", ArgumentType.STRING, type -> {
			provider = providers.get(type);
			if (provider == null) {
				System.err.println("Market service provider '%s' is not installed or not included in classpath"
					.formatted(type));
				System.exit(1);
			}
		}, "-s", "--service"),
		new Option<>("Configure market service from config string", ArgumentType.STRING, v -> {
			if (configFile != null) {
				System.err.println("Warning: --config-file was previously specified, --config is overrding");
				configFile = null;
			}
			config = v;
		}, "-c", "--config"),
		new Option<>("Configure market service from config file", ArgumentType.PATH, v -> {
			if (config != null) {
				System.err.println("Warning: --config was previously specified, --config-file is overrding");
				this.config = null;
			}
			configFile = v;
		}, "-f", "--config-file"),
		new Option<>("Run command after service started then exit", ArgumentType.STRING, v -> {
			if (inputPath != null) {
				System.err.println("Warning: --input-file was previously specified, --command is overriding");
				inputPath = null;
			}
			inputCommand = v;
		}, "-C", "--command"),
		new Option<>("Run commands from script after service started then exit", ArgumentType.PATH, v -> {
			if (inputPath != null) {
				System.err.println("Warning: --command was previously specified, --input-file is overriding");
				inputCommand = null;
			}
			inputPath = v;
		}, "-i", "--input-file"),
		new Option<>("Keep prompt session open after script execution", ArgumentType.VOID, v -> retainSession = true, "--retain-session"),
		new Option<>("Start market server and listen connections on Unix socket", ArgumentType.PATH, v -> {
			servers.add(new MarketServerInfo(StandardProtocolFamily.UNIX, UnixDomainSocketAddress.of(v)));
		}, "--server-unix"),
		new Option<>("Start market server and listen connections on TCP socket", ArgumentType.STRING, v -> {
			Pattern pat = Pattern.compile("^(?<host>[A-Za-z0-9._-]+?):(?<port>\\d+)$");
			Matcher matcher = pat.matcher(v);

			if (!matcher.matches()) {
				System.err.println("%s is not a valid TCP socket address. The format is <host>:<port>");
				return;
			}

			String host = matcher.group("host");
			int port = Integer.parseInt(matcher.group("port"));
			servers.add(new MarketServerInfo(null, InetSocketAddress.createUnresolved(host, port)));
		}, "--server-tcp"));

	public void run() {
		try {
			onCliRun();
		} catch (Throwable e) {
			// TODO print friendly error message
			e.printStackTrace();
		}
	}

	public void onCliRun() throws Throwable {
		if (provider == null) {
			System.err.println("No market service provider is specified (missing --service option)");
			System.err.println("Use '-?' switch for help");
			System.exit(1);
		}

		System.out.println("Using service provider: %s".formatted(provider.getProviderName()));
		BufferedReader fileReader = null;
		DynamicReader configReader;

		if (config != null) {
			System.out.println("Using service configuration: \"%s\"".formatted(config));
			configReader = new StringOnlyDynamicReader(config);
		} else if (configFile != null) {
			System.out.println("Using service configuration from file: %s".formatted(configFile));
			fileReader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8);
			configReader = new JsonDynamicReader(fileReader);
		} else {
			System.out.println("Not using service configuration");
			configReader = null;
		}

		MarketServiceHost serviceHost = provider.createHost(configReader != null ? configReader : null);
		if (fileReader != null) fileReader.close();
		System.out.println("Create market service host! Starting service...");
		serviceHost.startService();
		System.out.println("Service started successfully!");

		for (MarketServerInfo server : servers) {
			System.out.println("Starting server with listener on %s".formatted(server));
			server.startServer(serviceHost.getService());
		}

		boolean hasScript = inputCommand != null || inputPath != null;
		if (inputCommand != null) {
			System.out.println("(stonks --command) " + inputCommand);
			executeCommand(inputCommand, serviceHost);
		} else if (inputPath != null) {
			System.out.println("Executing script '%s'".formatted(inputPath));

			for (String line : Files.readAllLines(inputPath, StandardCharsets.UTF_8)) {
				System.out.println("(stonks --input-file) " + line);
				executeCommand(line, serviceHost);
			}
		}

		if (!hasScript || retainSession) {
			if (hasScript) System.out.println("--retain-session was specified");

			Console console = System.console();
			if (console != null) {
				while (true) {
					String command = console.readLine("(stonks prompt) ");
					executeCommand(command, serviceHost);
				}
			} else {
				System.err.println("Unable to get console (likely running in IDE?)");

				try (Scanner scanner = new Scanner(System.in)) {
					while (true) {
						System.out.print("(stonks prompt) ");
						String command = scanner.nextLine();
						executeCommand(command, serviceHost);
					}
				}
			}
		}
	}

	private void showHelp(Void v) {
		System.out.println("Usage:");
		System.out.println("  stonks-cli [...options]");
		System.out.println();
		System.out.println("Options:");

		int leftWidth = 0;
		List<String[]> table = new ArrayList<>();

		for (Option<?> option : options) {
			String switches = Stream.of(option.aliases()).collect(Collectors.joining(", "));
			leftWidth = Math.max(leftWidth, switches.length());
			table.add(new String[] { switches, option.description() });
		}

		int leftWidth0 = leftWidth;
		table.forEach(line -> System.out.println(("  %-" + leftWidth0 + "s  %s").formatted(line[0], line[1])));
		System.exit(1);
	}

	// @formatter:off
	private List<Command<MarketServiceHost>> commands = List.of(
		new Command<>("^(h(elp)?|\\?)$", "help | ?", (matcher, host) -> showCommandHelp()),
		new Command<>("^(e(xit)?|q(uit)?|b(ye)?|s(top)?)$", "exit | bye | quit | stop", (matcher, host) -> stop(host)),
		new Command<>("^c(atalog)?$", "catalog", (matcher, host) -> printCatalog(host)),
		new Command<>("^p(roduct)? (?<id>[A-Za-z0-9]+?|(\"|').+?\3) c(reate)?$", "product <id> create", this::createProduct),
		new Command<>("^p(roduct)? (?<id>[A-Za-z0-9]+?|(\"|').+?\3) d(elete)?$", "product <id> delete", this::deleteProduct),
		new Command<>("^p(roduct)? (?<id>[A-Za-z0-9]+?|(\"|').+?\3) o(verview)?$", "product <id> overview", this::productOverview),
		new Command<>(
			"^p(roduct)? (?<id>[A-Za-z0-9]+?|(\\\"|').+?\\3) (instant buy|ib) (?<units>\\d+?)"
			+ "( with balance (?<balance>\\d+?))?"
			+ "( with slippage (?<price>\\d+) (?<rate>\\d+(\\.\\d+)?%?))?$",
			"product <id> instant buy <units> [with balance <balance>] [with slippage <price> <rate>]",
			this::instantBuy),
		new Command<>(
			"^p(roduct)? (?<id>[A-Za-z0-9]+?|(\\\"|').+?\\3) (instant sell|is) (?<units>\\d+?)"
			+ "( with slippage (?<price>\\d+) (?<rate>\\d+(\\.\\d+)?%?))?$",
			"product <id> instant sell <units> [with slippage <price> <rate>]",
			this::instantSell),
		new Command<>(
			"^p(roduct)? (?<id>[A-Za-z0-9]+?|(\\\"|').+?\\3) (offer buy|ob) (?<units>\\d+)"
			+ " for (?<price>\\d+) each"
			+ "( by (?<owner>[A-Fa-f0-9]{8}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{12}|@[A-Za-z0-9_.]+))?$",
			"product <id> offer buy <units> for <price per unit> each [by <UUID | @name>]",
			(matcher, host) -> offer(matcher, host, OfferType.BUY)),
		new Command<>(
			"^p(roduct)? (?<id>[A-Za-z0-9]+?|(\\\"|').+?\\3) (offer sell|os) (?<units>\\d+)"
			+ " for (?<price>\\d+) each"
			+ "( by (?<owner>[A-Fa-f0-9]{8}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{12}|@[A-Za-z0-9_.]+))?$",
			"product <id> offer sell <units> for <price per unit> each [by <UUID | @name>]",
			(matcher, host) -> offer(matcher, host, OfferType.SELL)),
		new Command<>("^offer by (?<owner>[A-Fa-f0-9]{8}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{12}|@[A-Za-z0-9_.]+)$", "offer by <UUID | @name>", this::offersByUser),
		new Command<>("^offer (?<offer>[A-Fa-f0-9]{8}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{12})$", "offer <UUID>", this::offerById),
		new Command<>("^offer (?<offer>[A-Fa-f0-9]{8}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{12}) claim$", "offer <UUID> claim", this::claimOffer),
		new Command<>("^offer (?<offer>[A-Fa-f0-9]{8}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{12}) cancel$", "offer <UUID> cancel", this::cancelOffer));
	// @formatter:on

	private void showCommandHelp() {
		System.out.println("Commands:");
		commands.forEach(c -> System.out.println("  %s".formatted(c.friendlyPattern())));
	}

	private void stop(MarketServiceHost host) {
		System.out.println("Shutting down servers...");

		servers.forEach(s -> {
			System.out.println("  Shutting down %s".formatted(s));
			s.stopServer();
		});

		System.out.println("Stopping service...");
		host.stopService();

		System.out.println("Goodbye!");
		System.exit(0);
	}

	private void printCatalog(MarketServiceHost host) {
		Set<? extends Product> catalog = host.getService().queryCatalog().join();
		System.out.println("Market catalog (%d):".formatted(catalog.size()));
		for (Product p : catalog) System.out.println("  - %s".formatted(p.getId()));
	}

	private Product getProductById(MarketServiceHost host, String id) {
		if ((id.startsWith("\"") && id.endsWith("\"")) || (id.startsWith("'") && id.endsWith("'")))
			id = id.substring(1, id.length() - 1);
		String id0 = id;
		return host.getService().queryCatalog().join()
			.stream()
			.filter(p -> p.getId().equals(id0))
			.findAny()
			.orElse(null);
	}

	private void createProduct(Matcher matcher, MarketServiceHost host) {
		if (!(host.getService() instanceof ManagableMarketService mgr)) {
			System.err.println("Current market service does not allow managing the catalog");
			return;
		}

		String productId = matcher.group("id");
		Product product = getProductById(host, productId);

		if (product != null) {
			System.err.println("Product with ID '%s' already exists!".formatted(productId));
			return;
		}

		product = mgr.createProduct(productId).join();
		System.out.println("Created product with ID '%s'".formatted(product.getId()));
	}

	private void deleteProduct(Matcher matcher, MarketServiceHost host) {
		if (!(host.getService() instanceof ManagableMarketService mgr)) {
			System.err.println("Current market service does not allow managing the catalog");
			return;
		}

		String productId = matcher.group("id");
		Product product = getProductById(host, productId);

		if (product == null) {
			System.err.println("Product with ID '%s' does not exists!".formatted(productId));
			return;
		}

		mgr.deleteProduct(product).join();
		System.out.println("Deleted product with ID '%s'".formatted(product.getId()));
		System.out.println("This also deleted all offers referring to this product.");
	}

	private void productOverview(Matcher matcher, MarketServiceHost host) {
		String productId = matcher.group("id");
		Product product = getProductById(host, productId);

		if (product == null) {
			System.err.println("Product with ID '%s' does not exists!".formatted(productId));
			return;
		}

		ProductOverview overview = product.queryOverview().join();
		System.out.println("Product overview for '%s':".formatted(product.getId()));
		printOverviewSet(overview.buyOffers());
		printOverviewSet(overview.sellOffers());
	}

	private void instantBuy(Matcher matcher, MarketServiceHost host) {
		String productId = matcher.group("id");
		long units = Long.parseLong(matcher.group("units"));
		long balance = matcher.group("balance") != null ? Long.parseLong(matcher.group("balance")) : Long.MAX_VALUE;
		String rateStr = matcher.group("rate");
		double rate = rateStr != null
			? (rateStr.endsWith("%")
				? Double.parseDouble(rateStr.substring(0, rateStr.length() - 1)) / 100d
				: Double.parseDouble(matcher.group("rate")))
			: 0d;
		Product.SlippageOption slippage = matcher.group("price") != null
			? new Product.SlippageOption(Long.parseLong(matcher.group("price")), rate)
			: null;
		Product product = getProductById(host, productId);

		if (product == null) {
			System.err.println("Product with ID '%s' does not exists!".formatted(productId));
			return;
		}

		Product.InstantBuyResult result = product.instantBuy(balance, units, slippage).join();
		System.out.println("Bought %d of %s and refunded %s (-%s)".formatted(
			result.bought(),
			product.getId(),
			currency(result.leftoverBalance()),
			currency(balance - result.leftoverBalance())));
	}

	private void instantSell(Matcher matcher, MarketServiceHost host) {
		String productId = matcher.group("id");
		long units = Long.parseLong(matcher.group("units"));
		String rateStr = matcher.group("rate");
		double rate = rateStr != null
			? (rateStr.endsWith("%")
				? Double.parseDouble(rateStr.substring(0, rateStr.length() - 1)) / 100d
				: Double.parseDouble(matcher.group("rate")))
			: 0d;
		Product.SlippageOption slippage = matcher.group("price") != null
			? new Product.SlippageOption(Long.parseLong(matcher.group("price")), rate)
			: null;
		Product product = getProductById(host, productId);

		if (product == null) {
			System.err.println("Product with ID '%s' does not exists!".formatted(productId));
			return;
		}

		Product.InstantSellResult result = product.instantSell(units, slippage).join();
		System.out.println("Sold %d of %s and refunded %d units (+%s)".formatted(
			units - result.leftoverUnits(),
			product.getId(),
			result.leftoverUnits(),
			currency(result.earning())));
	}

	private void offer(Matcher matcher, MarketServiceHost host, OfferType type) {
		String productId = matcher.group("id");
		long units = Long.parseLong(matcher.group("units"));
		long price = Long.parseLong(matcher.group("price"));
		String ownerStr = matcher.group("owner");
		UUID owner = ownerStr != null
			? ownerStr.startsWith("@")
				? UUID.nameUUIDFromBytes(ownerStr.substring(1).getBytes(StandardCharsets.UTF_8))
				: UUID.fromString(ownerStr)
			: new UUID(0L, 0L);
		Product product = getProductById(host, productId);

		if (product == null) {
			System.err.println("Product with ID '%s' does not exists!".formatted(productId));
			return;
		}

		Offer offer = product.placeOffer(owner, type, price, units).join();
		System.out.println("Placed offer with ID %s by %s: %s".formatted(
			offer.id(),
			ownerStr,
			Offer.toString(offer, this::currency)));
	}

	private void offersByUser(Matcher matcher, MarketServiceHost host) {
		String ownerStr = matcher.group("owner");
		UUID owner = ownerStr != null
			? ownerStr.startsWith("@")
				? UUID.nameUUIDFromBytes(ownerStr.substring(1).getBytes(StandardCharsets.UTF_8))
				: UUID.fromString(ownerStr)
			: new UUID(0L, 0L);

		List<? extends Offer> offers = host.getService().queryUserOffers(owner).join();
		System.out.println("%s currently have %d offers".formatted(ownerStr, offers.size()));
		offers.forEach(entry -> {
			System.out.println("  - [%s]: %s".formatted(entry.id(), Offer.toString(entry, this::currency)));
		});
	}

	private void offerById(Matcher matcher, MarketServiceHost host) {
		UUID id = UUID.fromString(matcher.group("offer"));
		Offer offer = host.getService().queryOffer(id).join();
		Offer.Status status = offer.queryStatus().join();
		ProductOverview productOverview = offer.product().queryOverview().join();
		ProductOffersOverview set = offer.type() == OfferType.BUY
			? productOverview.buyOffers()
			: productOverview.sellOffers();

		System.out.println("Offer ID %s:".formatted(id));
		System.out.println("  Owner's UUID: %s".formatted(offer.owner()));
		System.out.println("  Price per unit: %s (%s top average)".formatted(
			currency(offer.price()),
			currency(set.averagePrice())));
		System.out.println("  Total units: %s (worth %s this offer, %s top average)".formatted(
			offer.totalUnits(),
			currency(offer.price() * offer.totalUnits()),
			currency(set.averagePrice() * offer.totalUnits())));
		System.out.println("  Progress (Filled/Claimed/Total): %d/%d/%d".formatted(
			status.filledUnits(),
			status.claimedUnits(),
			offer.totalUnits()));
		System.out.println(status.removed()
			? "  This offer has been removed from market"
			: "  This offer is currently on market");
	}

	private void claimOffer(Matcher matcher, MarketServiceHost host) {
		UUID id = UUID.fromString(matcher.group("offer"));
		Offer offer = host.getService().queryOffer(id).join();
		Offer.ClaimResult result = offer.claimOffer().join();
		System.out.println("Claimed %d units from offer %s, %d units remaining".formatted(
			result.claimedUnits(),
			id,
			result.pendingUnits()));
		if (result.remove()) System.out.println("Offer has been removed from market");
	}

	private void cancelOffer(Matcher matcher, MarketServiceHost host) {
		UUID id = UUID.fromString(matcher.group("offer"));
		Offer offer = host.getService().queryOffer(id).join();
		Offer.ClaimResult result = offer.cancelOffer().join();
		System.out.println("Claimed %d units from offer %s, %d units remaining".formatted(
			result.claimedUnits(),
			id,
			result.pendingUnits()));
		System.out.println("Canceled offer %s, refunding %s".formatted(
			id,
			offer.type() == OfferType.BUY
				? currency(offer.price() * result.pendingUnits())
				: result.pendingUnits()));
		if (result.remove()) System.out.println("Offer has been removed from market");
	}

	public void executeCommand(String command, MarketServiceHost serviceHost) {
		command = command.trim();
		if (command.isBlank()) return;

		for (Command<MarketServiceHost> c : commands) {
			Matcher matcher = c.pattern().matcher(command);

			if (matcher.matches()) {
				try {
					c.callback().accept(matcher, serviceHost);
				} catch (Throwable t) {
					t.printStackTrace();
					System.err.println("An error occurred while executing '%s'".formatted(command));
				}

				return;
			}
		}

		System.err.println("Unrecognized command: %s".formatted(command));
	}

	private void printOverviewSet(ProductOffersOverview set) {
		System.out.println("  Top %s offers: AVG %s / COUNT %d".formatted(
			set.type() == OfferType.BUY ? "buy" : "sell",
			currency(set.averagePrice()),
			set.topOffers().size()));

		if (set.topOffers().size() == 0) {
			System.out.println("    (no one is %s this product)".formatted(set.type() == OfferType.BUY
				? "buying"
				: "selling"));
			return;
		}

		for (OfferOverviewEntry entry : set.topOffers()) {
			System.out.println("    %s %d units at %s/ea for total of %s".formatted(
				set.type() == OfferType.BUY ? "Buying" : "Selling",
				entry.units(),
				currency(entry.price()),
				currency(entry.price() * entry.units())));
		}
	}

	private String currency(long value) {
		return "$%d.%02d".formatted(value / 100, value % 100); // TODO
	}
}
