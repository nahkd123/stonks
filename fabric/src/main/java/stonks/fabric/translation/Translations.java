/*
 * Copyright (c) 2023 nahkd
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
package stonks.fabric.translation;

import static net.minecraft.text.Text.translatableWithFallback;

import java.util.Optional;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import stonks.core.market.ComputedOffersList;
import stonks.core.market.Offer;
import stonks.core.product.Product;
import stonks.fabric.PlatformConfig;
import stonks.fabric.StonksFabricUtils;

public final class Translations {
	// @formatter:off
	public static final class Icons {
		public static final Text Border = translatableWithFallback("stonks.menu.icon.border", " ");
		public static final Text PreviousMenu = translatableWithFallback("stonks.menu.icon.previousMenu", "\u00a77<-- \u00a7aBack");
		public static final Text MainMenu = translatableWithFallback("stonks.menu.icon.mainMenu", "\u00a7eMarket menu");
		public static final Text MainMenu$0 = translatableWithFallback("stonks.menu.icon.mainMenu.0", "\u00a77Click to go back to market menu");
		public static final Text ViewOffers = translatableWithFallback("stonks.menu.icon.viewOffers", "\u00a7eView offers");
		public static final Text ViewOffers$0 = translatableWithFallback("stonks.menu.icon.viewOffers.0", "\u00a77Click to view your offers");
		public static final Text ScrollUp = translatableWithFallback("stonks.menu.icon.scrollUp", "\u00a77Scroll Up");
		public static final Text ScrollUp$0(int page, int maxPages) { return translatableWithFallback("stonks.menu.icon.scrollUp.0", "\u00a77Current page: %s\u00a77/%s", page + 1, maxPages); }
		public static final Text ScrollDown = translatableWithFallback("stonks.menu.icon.scrollDown", "\u00a77Scroll Down");
		public static final Text ScrollDown$0(int page, int maxPages) { return translatableWithFallback("stonks.menu.icon.scrollDown.0", "\u00a77Current page: %s\u00a77/%s", page + 1, maxPages); }
		public static final Text PreviousPage = translatableWithFallback("stonks.menu.icon.previousPage", "\u00a77<-- Previous Page");
		public static final Text PreviousPage$0(int page, int maxPages) { return translatableWithFallback("stonks.menu.icon.previousPage.0", "\u00a77Current page: %s\u00a77/%s", page + 1, maxPages); }
		public static final Text NextPage = translatableWithFallback("stonks.menu.icon.nextPage", "\u00a77Next Page -->");
		public static final Text NextPage$0(int page, int maxPages) { return translatableWithFallback("stonks.menu.icon.nextPage.0", "\u00a77Current page: %s\u00a77/%s", page + 1, maxPages); }
		public static final Text Loading(String v) { return translatableWithFallback("stonks.menu.icon.loading", "\u00a77Please wait %s", v); }
	}
	public static final class Errors {
		public static final Text Errors = translatableWithFallback("stonks.menu.error", "\u00a7cAn error occured!");
		public static final Text CategoriesList = translatableWithFallback("stonks.menu.error.categoriesList", "\u00a77Failed to get categories list");
		public static final Text QuickPriceDetails = translatableWithFallback("stonks.menu.error.quickPriceDetails", "\u00a77Failed to query quick price details");
	}
	public static final class Menus {
		public static final class MainMenu {
			public static final Text MainMenu = translatableWithFallback("stonks.menu.mainMenu", "Market");
			public static final Text Category$Selected = translatableWithFallback("stonks.menu.mainMenu.category.selected", "\u00a77Selected");
			public static final Text Category$Unselected = translatableWithFallback("stonks.menu.mainMenu.category.unselected", "\u00a77Click to select");
			public static final Text Product$ClickToOpen = translatableWithFallback("stonks.menu.mainMenu.product.clickToOpen", "\u00a7eClick \u00a77to open product info");
			public static final Text product$instantBuy(Optional<Double> instantBuyPrice) { return translatableWithFallback("stonks.menu.mainMenu.product.instantBuy", "\u00a77Instant Buy: %s", StonksFabricUtils.currencyText(instantBuyPrice, true)); }
			public static final Text product$instantSell(Optional<Double> instantSellPrice) { return translatableWithFallback("stonks.menu.mainMenu.product.instantSell", "\u00a77Instant Sell: %s", StonksFabricUtils.currencyText(instantSellPrice, true)); }
		}
		public static final class ViewOffers {
			public static final Text ViewOffers = translatableWithFallback("stonks.menu.viewOffers", "Market > Offers");
			public static final Text Retrying = translatableWithFallback("stonks.menu.viewOffers.retrying", "\u00a77Retrying in few seconds, please wait...");
			public static final Text NoOffers = translatableWithFallback("stonks.menu.viewOffers.noOffers", "\u00a7cNo offers!");
			public static final Text NoOffers$0 = translatableWithFallback("stonks.menu.viewOffers.noOffers.0", "\u00a77Go back and place offers to see");
			public static final Text NoOffers$1 = translatableWithFallback("stonks.menu.viewOffers.noOffers.1", "\u00a77them here!");
			public static final Text Offer$ClickToOpen = translatableWithFallback("stonks.menu.viewOffers.offer.clickToOpen", "\u00a77Click to open");
			public static final Text Offer$ProgressLegends = translatableWithFallback("stonks.menu.viewOffers.offer.progressLegends", "\u00a78(Claimed/Filled/Total)");
			public static final Text Offer$Buy(Offer offer) { return translatableWithFallback("stonks.menu.viewOffers.offer.buy", "\u00a7a\u00a7lBUY OFFER: %s", productName(offer.getProduct())); }
			public static final Text Offer$Sell(Offer offer) { return translatableWithFallback("stonks.menu.viewOffers.offer.sell", "\u00a7e\u00a7lSELL OFFER: %s", productName(offer.getProduct())); }
			public static final Text Offer$Progress(Offer offer) {
				return translatableWithFallback("stonks.menu.viewOffers.offer.progress", "\u00a77Progress: %s %s/%s/%s",
					StonksFabricUtils.progressBar(20, Formatting.DARK_GRAY,
						new double[] { offer.getClaimedUnits() / (double) offer.getTotalUnits(), offer.getFilledUnits() / (double) offer.getTotalUnits() },
						new Formatting[] { Formatting.GREEN, Formatting.YELLOW }),
					Text.literal(Integer.toString(offer.getClaimedUnits())).styled(s -> s.withColor(Formatting.GREEN)),
					Text.literal(Integer.toString(offer.getFilledUnits())).styled(s -> s.withColor(Formatting.YELLOW)),
					Text.literal(Integer.toString(offer.getTotalUnits())).styled(s -> s.withColor(Formatting.WHITE)));
			}
			public static final Text Offer$PricePerUnit(Offer offer) { return translatableWithFallback("stonks.menu.viewOffers.offer.pricePerUnit", "\u00a77Price per unit: %s", currency(offer.getPricePerUnit())); }
			public static final Text Offer$TotalPrice(Offer offer) { return translatableWithFallback("stonks.menu.viewOffers.offer.totalPrice", "\u00a77Total price: %s", currency(offer.getPricePerUnit() * offer.getTotalUnits())); }
		}
		public static final class OfferInfo {
			public static final Text _OfferInfo(Offer offer) { return translatableWithFallback("stonks.menu.offerInfo", "Market > Offers > %s", offer.getProduct().getProductName()); }
			public static final Text ClaimOffer = translatableWithFallback("stonks.menu.offerInfo.claimOffer", "\u00a7eClaim offer");
			public static final Text ClaimOffer$ClickToClaim = translatableWithFallback("stonks.menu.offerInfo.claimOffer.clickToClaim", "\u00a77Click to claim all");
			public static final Text ClaimOffer$NoClaim = translatableWithFallback("stonks.menu.offerInfo.claimOffer.noClaim", "\u00a7cCan't claim");
			public static final Text ClaimOffer$Claiming = translatableWithFallback("stonks.menu.offerInfo.claimOffer.claiming", "\u00a77Claiming...");
			public static final Text ClaimOffer$ClaimFailed = translatableWithFallback("stonks.menu.offerInfo.claimOffer.claimFailed", "\u00a7cClaim failed!");
			public static final Text CancelOffer = translatableWithFallback("stonks.menu.offerInfo.cancelOffer", "\u00a7eCancel offer");
			public static final Text CancelOffer$0 = translatableWithFallback("stonks.menu.offerInfo.cancelOffer.0", "\u00a77Pending items/money will be refuned");
			public static final Text CancelOffer$ClickToCancel = translatableWithFallback("stonks.menu.offerInfo.cancelOffer.clickToCancel", "\u00a77Click to cancel");
			public static final Text CancelOffer$Cancelling = translatableWithFallback("stonks.menu.offerInfo.cancelOffer.cancelling", "\u00a77Cancelling...");
			public static final Text CancelOffer$CancelFailed = translatableWithFallback("stonks.menu.offerInfo.cancelOffer.cancelFailed", "\u00a7cCancel failed!");
			public static final Text ClaimOffer$Units(int unitsToClaim) { return translatableWithFallback("stonks.menu.offerInfo.claimOffer.units", "\u00a77You have %s \u00a77units to claim", units(unitsToClaim)); }
			public static final Text ClaimOffer$Money(int unitsToClaim, Offer offer) { return translatableWithFallback("stonks.menu.offerInfo.claimOffer.money", "\u00a77You have %s \u00a77to claim", currency(unitsToClaim * offer.getPricePerUnit())); }
			public static final Text ClaimOffer$MoneyWithTax(int unitsToClaim, Offer offer, PlatformConfig config) {
				return translatableWithFallback("stonks.menu.offerInfo.claimOffer.moneyWithTax", "\u00a77You have %s \u00a77to claim (%s\u00a77 tax incl.)",
					currency(config.applyTax(unitsToClaim * offer.getPricePerUnit())),
					StonksFabricUtils.taxText(config.tax).orElse(Text.literal("0%")));
			}
		}
		public static final class ProductInfo {
			public static final Text _ProductInfo(Product product) { return translatableWithFallback("stonks.menu.productInfo", "Market > %s", product.getProductName()); }
			public static final Text NoOffers = translatableWithFallback("stonks.menu.productInfo.noOffers", "\u00a7cNo offers!");
			public static final Text ClickToInstantBuy = translatableWithFallback("stonks.menu.productInfo.clickToInstantBuy", "\u00a77Click to setup instant buy");
			public static final Text ClickToInstantSell = translatableWithFallback("stonks.menu.productInfo.clickToInstantSell", "\u00a77Click to sell all");
			public static final Text BuyOffer = translatableWithFallback("stonks.menu.productInfo.buyOffer", "\u00a7eCreate buy offer");
			public static final Text SellOffer = translatableWithFallback("stonks.menu.productInfo.sellOffer", "\u00a7eCreate sell offer");
			public static final Text MakeOffer = translatableWithFallback("stonks.menu.productInfo.makeOffer", "\u00a77Click to make offer");
			public static final Text MakeOffer$NoOffers = translatableWithFallback("stonks.menu.productInfo.makeOffer.noOffers", "\u00a77Click to become the first person to get rich");
			public static final Text InstantBuy(Optional<ComputedOffersList> computed) { return translatableWithFallback("stonks.menu.productInfo.instantBuy", "\u00a7eInstant buy \u00a76(\u00a7eAvg. %s\u00a76)", StonksFabricUtils.currencyText(computed.map(v -> v.average()), false)); }
			public static final Text InstantSell(Optional<ComputedOffersList> computed) { return translatableWithFallback("stonks.menu.productInfo.instantSell", "\u00a7eInstant sell \u00a76(\u00a7eAvg. %s\u00a76)", StonksFabricUtils.currencyText(computed.map(v -> v.average()), false)); }
			public static final Text TopOfferedPrice(Optional<Double> topPrice) { return translatableWithFallback("stonks.menu.productInfo.topOfferedPrice", "\u00a77Top Offered Price: %s", StonksFabricUtils.currencyText(topPrice, true)); }
			public static final Text AvgOfferedPrice(Optional<ComputedOffersList> computed) { return translatableWithFallback("stonks.menu.productInfo.avgOfferedPrice", "\u00a77Average Offered Price: %s", StonksFabricUtils.currencyText(computed.map(v -> v.average()), true)); }
			public static final Text InstantSellTax(double tax) { return translatableWithFallback("stonks.menu.productInfo.instantSellTax", "\u00a77A small %s \u00a77tax will be applied", StonksFabricUtils.taxText(tax).orElse(Text.literal("0%"))); }
		}
		public static final class InstantBuy {
			public static final Text _InstantBuy(Product product) { return translatableWithFallback("stonks.menu.instantBuy", "Market > %s > Instant buy", product.getProductName()); }
			public static final Text CustomAmount = translatableWithFallback("stonks.menu.instantBuy.customAmount", "\u00a7eCustom amount");
			public static final Text CustomAmount$0 = translatableWithFallback("stonks.menu.instantBuy.customAmount.0", "\u00a77Click to specify amount");
			public static final Text GuideText$0 = translatableWithFallback("stonks.menu.instantBuy.0", "\u00a78Having minimum balance is required to");
			public static final Text GuideText$1 = translatableWithFallback("stonks.menu.instantBuy.1", "\u00a78avoid your buy request from failing.");
			public static final Text HoldShift = translatableWithFallback("stonks.menu.instantBuy.holdShift", "\u00a77Hold Shift to keep this menu opened");
			public static final Text ClickToBuy = translatableWithFallback("stonks.menu.instantBuy.clickToBuy", "\u00a77Click to instantly buy");
			public static final Text NoBuy = translatableWithFallback("stonks.menu.instantBuy.noBuy", "\u00a7cCan't instant buy");
			public static final Text Buying = translatableWithFallback("stonks.menu.instantBuy.buying", "\u00a7cCan't buy now");
			public static final Text Buying$0 = translatableWithFallback("stonks.menu.instantBuy.buying.0", "\u00a77Buying product...");
			public static final Text Confirm = translatableWithFallback("stonks.menu.instantBuy.confirm", "\u00a7eConfirm instant buy");
			public static final Text FixedAmount(int amount) { return translatableWithFallback("stonks.menu.instantBuy.fixedAmount", "\u00a7eInstant buy x%s", Text.literal(Integer.toString(amount)).styled(s -> s.withColor(Formatting.YELLOW))); }
			public static final Text AveragePrice(int amount, double originalPricePerUnit) { return translatableWithFallback("stonks.menu.instantBuy.averagePrice", "\u00a77Average price: %s", currency(amount * originalPricePerUnit)); }
			public static final Text MinimumBalance(double moneyToSpend) { return translatableWithFallback("stonks.menu.instantBuy.minimumBalance", "\u00a77Minimum balance: %s", currency(moneyToSpend)); }
			public static final Text Confirm$0(int amount, Product product) {
				return translatableWithFallback("stonks.menu.instantBuy.confirm.0", "\u00a77%s\u00a77x %s",
					Text.literal(Integer.toString(amount)).styled(s -> s.withColor(Formatting.GRAY)),
					Text.literal(product.getProductName()).styled(s -> s.withColor(Formatting.GRAY)));
			}
		}
		public static final class CreateOffer {
			public static final Text SellAll = translatableWithFallback("stonks.menu.createOffer.sellAll", "\u00a7eSell everything!");
			public static final Text NoOfferForYou = translatableWithFallback("stonks.menu.createOffer.noOfferForYou", "\u00a7cCan't make this offer");
			public static final Text ClickForPrice = translatableWithFallback("stonks.menu.createOffer.clickForPrice", "\u00a77Click to configure offer pricing");
			public static final Text CustomAmount = translatableWithFallback("stonks.menu.createOffer.customAmount", "\u00a7eCustom amount");
			public static final Text ClickForAmount = translatableWithFallback("stonks.menu.createOffer.clickForAmount", "\u00a77Click to specify amount");
			public static final Text TopOfferDelta = translatableWithFallback("stonks.menu.createOffer.topOfferDelta.0", "\u00a77Get your offer filled first");
			public static final Text SameAsTopOffer = translatableWithFallback("stonks.menu.createOffer.sameAsTopOffer", "\u00a7eSame as top offer");
			public static final Text AverageOfTopOffers = translatableWithFallback("stonks.menu.createOffer.averageOfTopOffers", "\u00a7eAverage of top offers");
			public static final Text ClickForConfirmation = translatableWithFallback("stonks.menu.createOffer.clickToPlace", "\u00a77Click to place offer");
			public static final Text CustomPrice = translatableWithFallback("stonks.menu.createOffer.customPrice", "\u00a7eCustom price");
			public static final Text CustomPrice$0 = translatableWithFallback("stonks.menu.createOffer.customPrice.0", "\u00a77Get rich in your own way.");
			public static final Text ClickForCustomPrice = translatableWithFallback("stonks.menu.createOffer.clickForCustomPrice", "\u00a77Click to specify custom price");
			public static final Text Title$Buy(Product product) { return translatableWithFallback("stonks.menu.createOffer.buy", "Market > %s > Buy offer", product.getProductName()); }
			public static final Text Title$Sell(Product product) { return translatableWithFallback("stonks.menu.createOffer.sell", "Market > %s > Sell offer", product.getProductName());}
			public static final Text BuyFixed(int amount) { return translatableWithFallback("stonks.menu.createOffer.buyFixed", "\u00a7eBuy x%s", Text.literal(Integer.toString(amount)).styled(s -> s.withColor(Formatting.YELLOW))); }
			public static final Text SellFixed(int amount) { return translatableWithFallback("stonks.menu.createOffer.sellFixed", "\u00a7eSell x%s", Text.literal(Integer.toString(amount)).styled(s -> s.withColor(Formatting.YELLOW))); }
			public static final Text TopBuyDelta(double delta) { return translatableWithFallback("stonks.menu.createOffer.topBuyDelta", "\u00a7eTop offer + %s", currency(delta));}
			public static final Text TopSellDelta(double delta) { return translatableWithFallback("stonks.menu.createOffer.topSellDelta", "\u00a7eTop offer - %s", currency(delta)); }
			public static final Text TotalSpending(Optional<Double> topOfferDelta, int amount) { return translatableWithFallback("stonks.menu.createOffer.totalSpending", "\u00a77Total spending: %s", StonksFabricUtils.currencyText(topOfferDelta.map(v -> v * amount), true)); }
			public static final Text TotalEarning(Optional<Double> topOfferDelta, int amount) { return translatableWithFallback("stonks.menu.createOffer.totalEarning", "\u00a77Total earning: %s", StonksFabricUtils.currencyText(topOfferDelta.map(v -> v * amount), true)); }
			public static final Text TopOfferPrice(Optional<Double> pricePerUnit) { return translatableWithFallback("stonks.menu.createOffer.topOfferPrice", "\u00a77Top offer: %s\u00a77", StonksFabricUtils.currencyText(pricePerUnit, true)); }
			public static final Text AvgOfferPrice(Optional<Double> pricePerUnit) { return translatableWithFallback("stonks.menu.createOffer.avgOfferPrice", "\u00a77Average price: %s\u00a77", StonksFabricUtils.currencyText(pricePerUnit, true)); }
			public static final Text YourOfferPrice(Optional<Double> pricePerUnit) { return translatableWithFallback("stonks.menu.createOffer.yourOfferPrice", "\u00a77Your offer: %s\u00a77", StonksFabricUtils.currencyText(pricePerUnit, true)); }
		}
		public static final class ConfirmOffer {
			public static final Text ConfirmOffer = translatableWithFallback("stonks.menu.confirmOffer", "Confirm offer");
			public static final Text Buy = translatableWithFallback("stonks.menu.confirmOffer.buy", "\u00a7aConfirm buy offer");
			public static final Text Sell = translatableWithFallback("stonks.menu.confirmOffer.sell", "\u00a7aConfirm sell offer");
			public static final Text ClickToConfirm = translatableWithFallback("stonks.menu.confirmOffer.clickToConfirm", "\u00a77Click to confirm");
			public static final Text Buying(Product product, int amount) { return translatableWithFallback("stonks.menu.confirmOffer.buying", "\u00a77Buying %s\u00a77x %s", Text.literal(Integer.toString(amount)).styled(s -> s.withColor(Formatting.GRAY)), Text.literal(product.getProductName()).styled(s -> s.withColor(Formatting.GRAY))); }
			public static final Text Selling(Product product, int amount) { return translatableWithFallback("stonks.menu.confirmOffer.selling", "\u00a77Selling %s\u00a77x %s", Text.literal(Integer.toString(amount)).styled(s -> s.withColor(Formatting.GRAY)), Text.literal(product.getProductName()).styled(s -> s.withColor(Formatting.GRAY))); }
			public static final Text TotalPrice(int amount, double pricePerUnit) { return translatableWithFallback("stonks.menu.confirmOffer.totalPrice", "\u00a77Total price: %s", currency(pricePerUnit * amount)); }
			public static final Text PricePerUnit(double pricePerUnit) { return translatableWithFallback("stonks.menu.confirmOffer.pricePerUnit", "\u00a77Price per unit: %s", currency(pricePerUnit)); }
		}
	}
	public static final class SignInputs {
		public static final Text Separator = translatableWithFallback("stonks.signInput.separator", "\u00a7f--------");
		public static final Text PriceInput = translatableWithFallback("stonks.signInput.priceInput", "Specify your price per unit");
		public static final Text AmountInput = translatableWithFallback("stonks.signInput.amountInput", "Specify your amount");
		public static final Text CurrentBuyTarget(int amount, Product product) { return translatableWithFallback("stonks.signInput.priceInput.currentBuyTargetWithAmount", "You're buying %sx %s", amount, product.getProductName()); }
		public static final Text CurrentSellTarget(int amount, Product product) { return translatableWithFallback("stonks.signInput.priceInput.currentSellTargetWithAmount", "You're selling %sx %s", amount, product.getProductName()); }
		public static final Text CurrentBuyTarget(Product product) { return translatableWithFallback("stonks.signInput.priceInput.currentBuyTarget", "You're buying %s", product.getProductName()); }
		public static final Text CurrentSellTarget(Product product) { return translatableWithFallback("stonks.signInput.priceInput.currentSellTarget", "You're selling %s", product.getProductName()); }
	}
	public static final class Messages {
		public static final Text OfferClaimFailed = translatableWithFallback("stonks.message.offerClaimFailed", "\u00a7cAn error occured. Claim failed!");
		public static final Text OfferCancelFailed = translatableWithFallback("stonks.message.offerCancelFailed", "\u00a7cAn error occured. Cancel failed!");
		public static final Text PriceMoreThanZero = translatableWithFallback("stonks.message.priceMoreThanZero", "\u00a7cYou must specify price more than $0");
		public static final Text AmountAtLeastOne = translatableWithFallback("stonks.message.amountAtLeastOne", "\u00a7cYou must specify at least 1");
		public static final Text PleaseWait = translatableWithFallback("stonks.message.pleaseWait", "Please wait...");
		public static final Text ErrorRefunding = translatableWithFallback("stonks.message.errorRefunding", "\u00a7cAn error occured, refunding all your stuffs");
		public static final Text NotAvailable = translatableWithFallback("stonks.message.notAvailable", "\u00a7cNot Available!");
		public static final Text NotAvailableShort = translatableWithFallback("stonks.message.notAvailable.short", "\u00a7cn/a");
		public static final Text OfferInfoText$Buy = translatableWithFallback("stonks.message.offerInfoText.buy", "\u00a7a\u00a7lBUY");
		public static final Text OfferInfoText$Sell = translatableWithFallback("stonks.message.offerInfoText.sell", "\u00a7e\u00a7lSELL");
		public static final Text OfferCancelled(Offer newOffer, int refundUnits, double refundMoney) { return translatableWithFallback("stonks.message.offerCancelled", "Offer cancelled! Refunded %sx %s and %s", units(refundUnits), productName(newOffer.getProduct()), currency(refundMoney)); }
		public static final Text NoUnitsToInstantSell(Product product) { return translatableWithFallback("stonks.message.noProductsToInstantSell", "\u00a7cYou don't have %s \u00a7cto sell!", product.getProductName()); }
		public static final Text NoMoneyToInstantBuy(double moneyToSpend) { return translatableWithFallback("stonks.message.noMoneyToInstantBuy", "\u00a7cYou don't have %s \u00a7cto buy!", currency(moneyToSpend)); }
		public static final Text NotEnoughMoney(double balance, double price) { return translatableWithFallback("stonks.message.notEnoughMoney", "\u00a7cNot enough money! (%s\u00a7c/%s\u00a7c)", currency(price), currency(balance)); }
		public static final Text NotEnoughItems(int currentAmount, int amount) { return translatableWithFallback("stonks.message.notEnoughItems", "\u00a7cNot enough items! (%s/%s\u00a7c)", amount, currentAmount); }
		public static final Text InvaildInput(String input) { return translatableWithFallback("stonks.message.invaildInput", "\u00a7cInvaild input: %s", input); }
		public static final Text Bought(int amount, Product product, double moneySpend) { return translatableWithFallback("stonks.message.bought", "Bought %sx %s for %s", units(amount), productName(product), currency(moneySpend)); }
		public static final Text BoughtWithExtras(int amount, Product product, double moneySpend, int unitsLeft) { return translatableWithFallback("stonks.message.boughtWithExtras", "Bought %sx %s for %s with %s units can't be bought", units(amount), productName(product), currency(moneySpend), units(unitsLeft)); }
		public static final Text Sold(int amount, Product product, double moneySpend) { return translatableWithFallback("stonks.message.sold", "Sold %sx %s for %s", units(amount), productName(product), currency(moneySpend)); }
		public static final Text SoldWithExtras(int amount, Product product, double moneySpend, int unitsLeft) { return translatableWithFallback("stonks.message.soldWithExtras", "Sold %sx %s for %s with %s units can't be sold", units(amount), productName(product), currency(moneySpend), units(unitsLeft)); }
		public static final Text PlacedBuyOffer(int units, Product product, double totalPrice, double pricePerUnit) { return translatableWithFallback("stonks.message.placedBuyOffer", "Placed buy offer: %sx %s for %s @ %s/each", units(units), productName(product), currency(totalPrice), currency(pricePerUnit)); }
		public static final Text PlacedSellOffer(int units, Product product, double totalPrice, double pricePerUnit) { return translatableWithFallback("stonks.message.placedSellOffer", "Placed sell offer: %sx %s for %s @ %s/each", units(units), productName(product), currency(totalPrice), currency(pricePerUnit)); }
		public static final Text Currency(double val) { return translatableWithFallback("stonks.message.currency", "\u00a7e$%s", Text.literal(StonksFabricUtils.CURRENCY_FORMATTER.format(val)).styled(s -> s.withColor(Formatting.YELLOW))); }
		public static final Text OfferInfoText(Text typeText, int totalAvailable, int offers, double pricePerUnit) { return translatableWithFallback("stonks.message.offerInfoText", "\u00a77%s %s\u00a77x from %s \u00a77offers for %s\u00a77/each", typeText, units(totalAvailable), units(offers), currency(pricePerUnit)); }
		public static final Text BuyOfferFilled(Offer offer) { return translatableWithFallback("stonks.message.buyOfferFilled", "\u00a77\u00a7l[\u00a7eStonks\u00a77\u00a7l]\u00a7r Your %sx %s buy offer has been filled!", units(offer.getTotalUnits()), productName(offer.getProduct())); }
		public static final Text SellOfferFilled(Offer offer) { return translatableWithFallback("stonks.message.sellOfferFilled", "\u00a77\u00a7l[\u00a7eStonks\u00a77\u00a7l]\u00a7r Your %sx %s sell offer has been filled!", units(offer.getTotalUnits()), productName(offer.getProduct())); }
	}
	// @formatter:on

	public static Text units(int units) {
		return Text.literal(Integer.toString(units)).styled(s -> s.withColor(Formatting.AQUA));
	}

	public static Text productName(Product product) {
		return Text.literal(product.getProductName()).styled(s -> s.withColor(Formatting.AQUA));
	}

	public static Text currency(double value) {
		return StonksFabricUtils.currencyText(Optional.of(value), true);
	}
}
