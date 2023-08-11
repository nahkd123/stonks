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
package stonks.fabric.menu;

import static net.minecraft.text.Text.translatableWithFallback;

import java.util.Optional;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import stonks.core.market.ComputedOffersList;
import stonks.core.market.Offer;
import stonks.core.product.Product;
import stonks.fabric.StonksFabricUtils;

// @formatter:off
public class MenuText {
	public static final Text icons$border = translatableWithFallback("stonks.menu.icon.border", " ");
	public static final Text icons$previousMenu = translatableWithFallback("stonks.menu.icon.previousMenu", "\u00a77<-- \u00a7aBack");
	public static final Text icons$mainMenu = translatableWithFallback("stonks.menu.icon.mainMenu", "\u00a7eMarket menu");
	public static final Text icons$mainMenu$0 = translatableWithFallback("stonks.menu.icon.mainMenu.0", "\u00a77Click to go back to market menu");
	public static final Text icons$viewOffers = translatableWithFallback("stonks.menu.icon.viewOffers", "\u00a7eView offers");
	public static final Text icons$viewOffer$0 = translatableWithFallback("stonks.menu.icon.viewOffers.0", "\u00a77Click to view your offers");
	public static final Text icons$scrollUp = translatableWithFallback("stonks.menu.icon.scrollUp", "\u00a77Scroll Up");
	public static Text icons$scrollUp$0(int page, int maxPages) { return translatableWithFallback("stonks.menu.icon.scrollUp.0", "\u00a77Current page: %s\u00a77/%s", page + 1, maxPages); }
	public static final Text icons$scrollDown = translatableWithFallback("stonks.menu.icon.scrollDown", "\u00a77Scroll Down");
	public static Text icons$scrollDown$0(int page, int maxPages) { return translatableWithFallback("stonks.menu.icon.scrollDown.0", "\u00a77Current page: %s\u00a77/%s", page + 1, maxPages); }
	public static final Text icons$previousPage = translatableWithFallback("stonks.menu.icon.previousPage", "\u00a77<-- Previous Page");
	public static Text icons$previousPage$0(int page, int maxPages) { return translatableWithFallback("stonks.menu.icon.previousPage.0", "\u00a77Current page: %s\u00a77/%s", page + 1, maxPages); }
	public static final Text icons$nextPage = translatableWithFallback("stonks.menu.icon.nextPage", "\u00a77Next Page -->");
	public static Text icons$nextPage$0(int page, int maxPages) { return translatableWithFallback("stonks.menu.icon.nextPage.0", "\u00a77Current page: %s\u00a77/%s", page + 1, maxPages); }
	public static Text icons$loading(String v) { return translatableWithFallback("stonks.menu.icon.loading", "\u00a77Please wait %s", v); }

	public static final Text errors = translatableWithFallback("stonks.menu.error", "\u00a7cAn error occured!");
	public static final Text errors$categoriesList = translatableWithFallback("stonks.menu.error.categoriesList", "\u00a77Failed to get categories list");
	public static final Text errors$quickPriceDetails = translatableWithFallback("stonks.menu.error.quickPriceDetails", "\u00a77Failed to query quick price details");

	public static final Text menus$mainMenu = translatableWithFallback("stonks.menu.mainMenu", "Market");
	public static final Text menus$mainMenu$category$selected = translatableWithFallback("stonks.menu.mainMenu.category.selected", "\u00a77Selected");
	public static final Text menus$mainMenu$category$unselected = translatableWithFallback("stonks.menu.mainMenu.category.unselected", "\u00a77Click to select");
	public static Text menus$mainMenu$product$instantBuy(Optional<Double> instantBuyPrice) { return translatableWithFallback("stonks.menu.mainMenu.product.instantBuy", "\u00a77Instant Buy: %s", StonksFabricUtils.currencyText(instantBuyPrice, true)); }
	public static Text menus$mainMenu$product$instantSell(Optional<Double> instantSellPrice) { return translatableWithFallback("stonks.menu.mainMenu.product.instantSell", "\u00a77Instant Sell: %s", StonksFabricUtils.currencyText(instantSellPrice, true)); }
	public static final Text menus$mainMenu$product$clickToOpen = translatableWithFallback("stonks.menu.mainMenu.product.clickToOpen", "\u00a7eClick \u00a77to open product info");

	public static final Text menus$viewOffers = translatableWithFallback("stonks.menu.viewOffers", "Market > Offers");
	public static final Text menus$viewOffers$retrying = translatableWithFallback("stonks.menu.viewOffers.retrying", "\u00a77Retrying in few seconds, please wait...");
	public static final Text menus$viewOffers$noOffers = translatableWithFallback("stonks.menu.viewOffers.noOffers", "\u00a7cNo offers!");
	public static final Text menus$viewOffers$noOffers$0 = translatableWithFallback("stonks.menu.viewOffers.noOffers.0", "\u00a77Go back and place offers to see");
	public static final Text menus$viewOffers$noOffers$1 = translatableWithFallback("stonks.menu.viewOffers.noOffers.1", "\u00a77them here!");
	public static final Text menus$viewOffers$offer$clickToOpen = translatableWithFallback("stonks.menu.viewOffers.offer.clickToOpen", "\u00a77Click to open");
	public static Text menus$viewOffers$offer$buy(Offer offer) { return translatableWithFallback("stonks.menu.viewOffers.offer.buy", "\u00a7a\u00a7lBUY OFFER: %s", Text.literal(offer.getProduct().getProductName()).styled(s -> s.withColor(Formatting.AQUA))); }
	public static Text menus$viewOffers$offer$sell(Offer offer) { return translatableWithFallback("stonks.menu.viewOffers.offer.sell", "\u00a7e\u00a7lSELL OFFER: %s", Text.literal(offer.getProduct().getProductName()).styled(s -> s.withColor(Formatting.AQUA))); }
	public static Text menus$viewOffers$offer$progress(Offer offer) {
		return translatableWithFallback("stonks.menu.viewOffers.offer.progress", "\u00a77Progress: %s %s/%s/%s",
			StonksFabricUtils.progressBar(20, Formatting.DARK_GRAY,
				new double[] { offer.getClaimedUnits() / (double) offer.getTotalUnits(), offer.getFilledUnits() / (double) offer.getTotalUnits() },
				new Formatting[] { Formatting.GREEN, Formatting.YELLOW }),
			Text.literal(Integer.toString(offer.getClaimedUnits())).styled(s -> s.withColor(Formatting.GREEN)),
			Text.literal(Integer.toString(offer.getFilledUnits())).styled(s -> s.withColor(Formatting.YELLOW)),
			Text.literal(Integer.toString(offer.getTotalUnits())).styled(s -> s.withColor(Formatting.WHITE)));
	}
	public static final Text menus$viewOffers$offer$progressLegends = translatableWithFallback("stonks.menu.viewOffers.offer.progressLegends", "\u00a78(Claimed/Filled/Total)");
	public static Text menus$viewOffers$offer$pricePerUnit(Offer offer) { return translatableWithFallback("stonks.menu.viewOffers.offer.pricePerUnit", "\u00a77Price per unit: %s", StonksFabricUtils.currencyText(Optional.of(offer.getPricePerUnit()), true)); }
	public static Text menus$viewOffers$offer$totalPrice(Offer offer) { return translatableWithFallback("stonks.menu.viewOffers.offer.totalPrice", "\u00a77Total price: %s", StonksFabricUtils.currencyText(Optional.of(offer.getPricePerUnit() * offer.getTotalUnits()), true)); }

	public static Text menus$offerInfo(Offer offer) { return translatableWithFallback("stonks.menu.offerInfo", "Market > Offers > %s", offer.getProduct().getProductName()); }
	public static final Text menus$offerInfo$claimOffer = translatableWithFallback("stonks.menu.offerInfo.claimOffer", "\u00a7eClaim offer");
	public static Text menus$offerInfo$claimOffer$units(int unitsToClaim) { return translatableWithFallback("stonks.menu.offerInfo.claimOffer.units", "\u00a77You have %s \u00a77units to claim", Text.literal(unitsToClaim + " units").styled(s -> s.withColor(Formatting.AQUA))); }
	public static Text menus$offerInfo$claimOffer$money(int unitsToClaim, Offer offer) { return translatableWithFallback("stonks.menu.offerInfo.claimOffer.money", "\u00a77You have %s \u00a77to claim", StonksFabricUtils.currencyText(Optional.of(unitsToClaim * offer.getPricePerUnit()), true)); }
	public static final Text menus$offerInfo$claimOffer$clickToClaim = translatableWithFallback("stonks.menu.offerInfo.claimOffer.clickToClaim", "\u00a77Click to claim all");
	public static final Text menus$offerInfo$claimOffer$noClaim = translatableWithFallback("stonks.menu.offerInfo.claimOffer.noClaim", "\u00a7cCan't claim");
	public static final Text menus$offerInfo$claimOffer$claiming = translatableWithFallback("stonks.menu.offerInfo.claimOffer.claiming", "\u00a77Claiming...");
	public static final Text menus$offerInfo$claimOffer$claimFailed = translatableWithFallback("stonks.menu.offerInfo.claimOffer.claimFailed", "\u00a7cClaim failed!");
	public static final Text menus$offerInfo$cancelOffer = translatableWithFallback("stonks.menu.offerInfo.cancelOffer", "\u00a7eCancel offer");
	public static final Text menus$offerInfo$cancelOffer$0 = Text.translatableWithFallback("stonks.menu.offerInfo.cancelOffer.0", "\u00a77Pending items/money will be refuned");
	public static final Text menus$offerInfo$cancelOffer$clickToCancel = Text.translatableWithFallback("stonks.menu.offerInfo.cancelOffer.clickToCancel", "\u00a77Click to cancel");
	public static final Text menus$offerInfo$cancelOffer$cancelling = Text.translatableWithFallback("stonks.menu.offerInfo.cancelOffer.cancelling", "\u00a77Cancelling...");
	public static final Text menus$offerInfo$cancelOffer$cancelFailed = Text.translatableWithFallback("stonks.menu.offerInfo.cancelOffer.cancelFailed", "\u00a7cCancel failed!");

	public static Text menus$productInfo(Product product) { return Text.translatableWithFallback("stonks.menu.productInfo", "Market > %s", product.getProductName()); }
	public static Text menus$productInfo$instantBuy(Optional<ComputedOffersList> computed) { return Text.translatableWithFallback("stonks.menu.productInfo.instantBuy", "\u00a7eInstant buy \u00a76(\u00a7eAvg. %s\u00a76)", StonksFabricUtils.currencyText(computed.map(v -> v.average()), false)); }
	public static Text menus$productInfo$instantSell(Optional<ComputedOffersList> computed) { return Text.translatableWithFallback("stonks.menu.productInfo.instantSell", "\u00a7eInstant sell \u00a76(\u00a7eAvg. %s\u00a76)", StonksFabricUtils.currencyText(computed.map(v -> v.average()), false)); }
	public static Text menus$productInfo$topOfferedPrice(Optional<Double> topPrice) { return Text.translatableWithFallback("stonks.menu.productInfo.topOfferedPrice", "\u00a77Top Offered Price: %s", StonksFabricUtils.currencyText(topPrice, true)); }
	public static Text menus$productInfo$avgOfferedPrice(Optional<ComputedOffersList> computed) { return Text.translatableWithFallback("stonks.menu.productInfo.avgOfferedPrice", "\u00a77Average Offered Price: %s", StonksFabricUtils.currencyText(computed.map(v -> v.average()), true)); }
	public static final Text menus$productInfo$noOffers = Text.translatableWithFallback("stonks.menu.productInfo.noOffers", "\u00a7cNo offers!");
	public static final Text menus$productInfo$clickToInstantBuy = Text.translatableWithFallback("stonks.menu.productInfo.clickToInstantBuy", "\u00a77Click to setup instant buy");
	public static final Text menus$productInfo$clickToInstantSell = Text.translatableWithFallback("stonks.menu.productInfo.clickToInstantSell", "\u00a77Click to sell all");
	public static final Text menus$productInfo$buyOffer = Text.translatableWithFallback("stonks.menu.productInfo.buyOffer", "\u00a7eCreate buy offer");
	public static final Text menus$productInfo$sellOffer = Text.translatableWithFallback("stonks.menu.productInfo.sellOffer", "\u00a7eCreate sell offer");
	public static final Text menus$productInfo$makeOffer = Text.translatableWithFallback("stonks.menu.productInfo.makeOffer", "\u00a77Click to make offer");
	public static final Text menus$productInfo$makeOffer$noOffers = Text.translatableWithFallback("stonks.menu.productInfo.makeOffer.noOffers", "\u00a77Click to become the first person to get rich");

	public static Text menus$instantBuy(Product product) { return Text.translatableWithFallback("stonks.menu.instantBuy", "Market > %s > Instant buy", product.getProductName()); }
	public static final Text menus$instantBuy$customAmount = Text.translatableWithFallback("stonks.menu.instantBuy.customAmount", "\u00a7eCustom amount");
	public static final Text menus$instantBuy$customAmount$0 = Text.translatableWithFallback("stonks.menu.instantBuy.customAmount.0", "\u00a77Click to specify amount");
	public static Text menus$instantBuy$fixedAmount(int amount) { return Text.translatableWithFallback("stonks.menu.instantBuy.fixedAmount", "\u00a7eInstant buy x%s", Text.literal(Integer.toString(amount)).styled(s -> s.withColor(Formatting.YELLOW))); }
	public static Text menus$instantBuy$averagePrice(int amount, double originalPricePerUnit) { return Text.translatableWithFallback("stonks.menu.instantBuy.averagePrice", "\u00a77Average price: %s", StonksFabricUtils.currencyText(Optional.of(amount * originalPricePerUnit), true)); }
	public static Text menus$instantBuy$minimumBalance(double moneyToSpend) { return Text.translatableWithFallback("stonks.menu.instantBuy.minimumBalance", "\u00a77Minimum balance: %s", StonksFabricUtils.currencyText(Optional.of(moneyToSpend), true)); }
	public static final Text menus$instantBuy$0 = Text.translatableWithFallback("stonks.menu.instantBuy.0", "\u00a78Having minimum balance is required to");
	public static final Text menus$instantBuy$1 = Text.translatableWithFallback("stonks.menu.instantBuy.1", "\u00a78avoid your buy request from failing.");
	public static final Text menus$instantBuy$holdShift = Text.translatableWithFallback("stonks.menu.instantBuy.holdShift", "\u00a77Hold Shift to keep this menu opened");
	public static final Text menus$instantBuy$clickToBuy = Text.translatableWithFallback("stonks.menu.instantBuy.clickToBuy", "\u00a77Click to instantly buy");
	public static final Text menus$instantBuy$noBuy = Text.translatableWithFallback("stonks.menu.instantBuy.noBuy", "\u00a7cCan't instant buy");
	public static final Text menus$instantBuy$buying = Text.translatableWithFallback("stonks.menu.instantBuy.buying", "\u00a7cCan't buy now");
	public static final Text menus$instantBuy$buying$0 = Text.translatableWithFallback("stonks.menu.instantBuy.buying.0", "\u00a77Buying product...");
	public static final Text menus$instantBuy$confirm = Text.translatableWithFallback("stonks.menu.instantBuy.confirm", "\u00a7eConfirm instant buy");
	public static Text menus$instantBuy$confirm$0(int amount, Product product) {
		return Text.translatableWithFallback("stonks.menu.instantBuy.confirm.0", "\u00a77%s\u00a77x %s",
			Text.literal(Integer.toString(amount)).styled(s -> s.withColor(Formatting.GRAY)),
			Text.literal(product.getProductName()).styled(s -> s.withColor(Formatting.GRAY)));
	}

	public static Text menus$createOffer$buy(Product product) { return Text.translatableWithFallback("stonks.menu.createOffer.buy", "Market > %s > Buy offer", product.getProductName()); }
	public static Text menus$createOffer$sell(Product product) { return Text.translatableWithFallback("stonks.menu.createOffer.sell", "Market > %s > Sell offer", product.getProductName());}
	public static Text menus$createOffer$buyFixed(int amount) { return Text.translatableWithFallback("stonks.menu.createOffer.buyFixed", "\u00a7eBuy x%s", Text.literal(Integer.toString(amount)).styled(s -> s.withColor(Formatting.YELLOW))); }
	public static Text menus$createOffer$sellFixed(int amount) { return Text.translatableWithFallback("stonks.menu.createOffer.sellFixed", "\u00a7eSell x%s", Text.literal(Integer.toString(amount)).styled(s -> s.withColor(Formatting.YELLOW))); }
	public static final Text menus$createOffer$sellAll = Text.translatableWithFallback("stonks.menu.createOffer.sellAll", "\u00a7eSell everything!");
	public static final Text menus$createOffer$noOfferForYou = Text.translatableWithFallback("stonks.menu.createOffer.noOfferForYou", "\u00a7cCan't make this offer");
	public static final Text menus$createOffer$clickForPrice = Text.translatableWithFallback("stonks.menu.createOffer.clickForPrice", "\u00a77Click to configure offer pricing");
	public static final Text menus$createOffer$customAmount = Text.translatableWithFallback("stonks.menu.createOffer.customAmount", "\u00a7eCustom amount");
	public static final Text menus$createOffer$clickForAmount = Text.translatableWithFallback("stonks.menu.createOffer.clickForAmount", "\u00a77Click to specify amount");
	public static Text menus$createOffer$topBuyDelta(double delta) { return Text.translatableWithFallback("stonks.menu.createOffer.topBuyDelta", "\u00a7eTop offer + %s", StonksFabricUtils.currencyText(Optional.of(delta), false));}
	public static Text menus$createOffer$topSellDelta(double delta) { return Text.translatableWithFallback("stonks.menu.createOffer.topSellDelta", "\u00a7eTop offer - %s", StonksFabricUtils.currencyText(Optional.of(delta), false)); }
	public static final Text menus$createOffer$topOfferDelta = Text.translatableWithFallback("stonks.menu.createOffer.topOfferDelta.0", "\u00a77Get your offer filled first");
	public static final Text menus$createOffer$sameAsTopOffer = Text.translatableWithFallback("stonks.menu.createOffer.sameAsTopOffer", "\u00a7eSame as top offer");
	public static final Text menus$createOffer$averageOfTopOffers = Text.translatableWithFallback("stonks.menu.createOffer.averageOfTopOffers", "\u00a7eAverage of top offers");
	public static Text menus$createOffer$totalSpending(Optional<Double> topOfferDelta, int amount) { return Text.translatableWithFallback("stonks.menu.createOffer.totalSpending", "\u00a77Total spending: %s", StonksFabricUtils.currencyText(topOfferDelta.map(v -> v * amount), true)); }
	public static Text menus$createOffer$totalEarning(Optional<Double> topOfferDelta, int amount) { return Text.translatableWithFallback("stonks.menu.createOffer.totalEarning", "\u00a77Total earning: %s", StonksFabricUtils.currencyText(topOfferDelta.map(v -> v * amount), true)); }
	public static Text menus$createOffer$topOfferPrice(Optional<Double> pricePerUnit) { return Text.translatableWithFallback("stonks.menu.createOffer.topOfferPrice", "\u00a77Top offer: %s\u00a77/ea", StonksFabricUtils.currencyText(pricePerUnit, true)); }
	public static Text menus$createOffer$avgOfferPrice(Optional<Double> pricePerUnit) { return Text.translatableWithFallback("stonks.menu.createOffer.avgOfferPrice", "\u00a77Average price: %s\u00a77/ea", StonksFabricUtils.currencyText(pricePerUnit, true)); }
	public static Text menus$createOffer$yourOfferPrice(Optional<Double> pricePerUnit) { return Text.translatableWithFallback("stonks.menu.createOffer.yourOfferPrice", "\u00a77Your offer: %s\u00a77/ea", StonksFabricUtils.currencyText(pricePerUnit, true)); }
	public static final Text menus$createOffer$clickForConfirmation = Text.translatableWithFallback("stonks.menu.createOffer.clickToPlace", "\u00a77Click to place offer");
	public static final Text menus$createOffer$customPrice = Text.translatableWithFallback("stonks.menu.createOffer.customPrice", "\u00a7eCustom price");
	public static final Text menus$createOffer$customPrice$0 = Text.translatableWithFallback("stonks.menu.createOffer.customPrice.0", "\u00a77Get rich in your own way.");
	public static final Text menus$createOffer$clickForCustomPrice = Text.translatableWithFallback("stonks.menu.createOffer.clickForCustomPrice", "\u00a77Click to specify custom price");

	public static final Text messages$offerClaimFailed = translatableWithFallback("stonks.messages.offerClaimFailed", "\u00a7cAn error occured. Claim failed!");
	public static final Text messages$offerCancelFailed = Text.translatableWithFallback("stonks.messages.offerCancelFailed", "\u00a7cAn error occured. Cancel failed!");
	public static Text messages$offerCancelled(Offer newOffer, int refundUnits, double refundMoney) {
		return Text.translatableWithFallback("stonks.messages.offerCancelled", "Offer cancelled! Refunded %sx %s and %s",
			Text.literal(Integer.toString(refundUnits)).styled(s -> s.withColor(Formatting.AQUA)),
			newOffer.getProduct().getProductName(),
			StonksFabricUtils.currencyText(Optional.of(refundMoney), true));
	}
	public static Text messages$noUnitsToInstantSell(Product product) { return Text.translatableWithFallback("messages.noProductsToInstantSell", "\u00a7cYou don't have %s \u00a7cto sell!", product.getProductName()); }
	public static Text messages$noMoneyToInstantBuy(double moneyToSpend) { return Text.translatableWithFallback("messages.noMoneyToInstantBuy", "\u00a7cYou don't have %s \u00a7cto buy!", StonksFabricUtils.currencyText(Optional.of(moneyToSpend), true)); }
}
// @formatter:on
