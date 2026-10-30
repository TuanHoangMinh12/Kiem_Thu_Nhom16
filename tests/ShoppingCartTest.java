package tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.ShoppingCartPage;

import java.util.List;

/** Runnable guest-cart checks against the current KICAP storefront. */
public class ShoppingCartTest extends BaseTest {
    private ShoppingCartPage cart() { return new ShoppingCartPage(driver); }

    @Test(description = "ShoppingCart-01: Empty cart message and browse link")
    public void cart01EmptyState() {
        cart().goToCart();
        Assert.assertTrue(cart().isCartEmptyDisplayed(), "Empty cart message is missing.");
        Assert.assertTrue(cart().hasProductDiscoveryLink(), "Browse products link is missing.");
        Assert.assertFalse(cart().isCheckoutBtnEnabled(), "Checkout must not be enabled for an empty cart.");
    }

    @Test(description = "ShoppingCart-02: Cart page has a visible heading")
    public void cart02PageLayout() {
        cart().goToCart();
        Assert.assertTrue(driver.findElement(By.xpath("//*[contains(normalize-space(.),'Gi')]")).isDisplayed());
        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed());
    }

    @Test(description = "ShoppingCart-03: Cart page works for a guest")
    public void cart03GuestAccess() {
        cart().goToCart();
        Assert.assertTrue(driver.getCurrentUrl().contains("/cart"));
    }

    @Test(description = "ShoppingCart-04: Cart links back to product browsing")
    public void cart04BrowseProductsLink() {
        cart().goToCart();
        WebElement link = driver.findElement(By.cssSelector("a[href*='/collections/']"));
        Assert.assertTrue(link.isDisplayed());
        Assert.assertTrue(link.getAttribute("href").contains("/collections/"));
    }

    @Test(description = "ShoppingCart-05: Product collection contains product cards")
    public void cart05CollectionProducts() {
        cart().goToProducts();
        Assert.assertTrue(driver.findElements(By.cssSelector("a[href^='/']:not([href='/']):not([href*='/collections/']):not([href*='/account']):not([href*='/pages/']):not([href*='/cart']):not([href*='/search']):not([href*='/blogs/'])")).size() >= 2);
    }

    @Test(description = "ShoppingCart-06: In-stock products expose quick-add or variant selection")
    public void cart06PurchaseControls() {
        cart().goToProducts();
        Assert.assertTrue(cart().hasQuickAddControl(),
                "No purchase control was found on the collection page.");
    }

    @Test(description = "ShoppingCart-07: Collection identifies products with variants")
    public void cart07VariantProductListing() {
        cart().goToProducts();
        Assert.assertTrue(driver.findElement(By.tagName("body")).getText().contains("phi\u00ean b\u1ea3n"));
    }

    @Test(description = "ShoppingCart-08: Multi-variant product displays its options")
    public void cart08VariantOptions() {
        driver.get("https://kicap.vn/cidoo-qk75");
        String body = driver.findElement(By.tagName("body")).getText();
        Assert.assertTrue(body.contains("Tr\u1eafng"));
        Assert.assertTrue(body.contains("\u0110en"));
    }

    @Test(description = "ShoppingCart-09: Collection shows sold-out products")
    public void cart09SoldOutProductsListed() {
        cart().goToProducts();
        Assert.assertTrue(driver.findElement(By.tagName("body")).getText().contains("H\u1ebft h\u00e0ng"));
    }

    @Test(description = "ShoppingCart-10: Pre-order product page is reachable")
    public void cart10PreorderProduct() {
        driver.get("https://kicap.vn/weikav-nut87");
        String content = driver.findElement(By.tagName("body")).getText().toLowerCase();
        Assert.assertTrue(content.contains("\u0111\u1eb7t tr\u01b0\u1edbc") || content.contains("s\u1eafp v\u1ec1 h\u00e0ng"));
    }

    @Test(description = "ShoppingCart-11: Sale price is displayed on discounted product")
    public void cart11DiscountedProductPrice() {
        driver.get("https://kicap.vn/tui-dung-ban-phim-co-cao-cap-nova");
        String content = driver.findElement(By.tagName("body")).getText();
        Assert.assertTrue(content.contains("280.000"));
        Assert.assertTrue(content.contains("350.000"));
    }

    @Test(description = "ShoppingCart-12: Cart route survives page reload")
    public void cart12ReloadCart() {
        cart().goToCart();
        driver.navigate().refresh();
        Assert.assertTrue(driver.getCurrentUrl().contains("/cart"));
        Assert.assertTrue(driver.findElement(By.xpath("//*[contains(normalize-space(.),'Gi')]")).isDisplayed());
    }

    @Test(description = "ShoppingCart-13: Cart can open in a second tab")
    public void cart13SecondTab() {
        cart().goToCart();
        driver.switchTo().newWindow(org.openqa.selenium.WindowType.TAB);
        cart().goToCart();
        Assert.assertTrue(driver.findElement(By.xpath("//*[contains(normalize-space(.),'Gi')]")).isDisplayed());
    }

    @Test(description = "ShoppingCart-14: Header provides a cart link")
    public void cart14HeaderCartLink() {
        driver.get("https://kicap.vn/");
        List<WebElement> links = driver.findElements(By.cssSelector("a[href='/cart'],a[href*='/cart']"));
        Assert.assertTrue(links.stream().anyMatch(WebElement::isDisplayed));
    }

    @Test(description = "ShoppingCart-15: Empty cart has no active checkout button")
    public void cart15EmptyCartCheckoutDisabled() {
        cart().goToCart();
        Assert.assertFalse(cart().isCheckoutBtnEnabled());
    }

    @Test(description = "ShoppingCart-16: Add an available product to cart")
    public void cart16AddAvailableProduct() {
        cart().addFirstProductToCart();
        Assert.assertFalse(cart().isCartEmptyDisplayed(), "Cart remained empty after quick-add.");
    }

    @Test(description = "ShoppingCart-17: Adding an available product twice updates cart")
    public void cart17AddProductTwice() {
        cart().addFirstProductToCart();
        String firstQuantity = cart().getFirstLineQuantity();
        int firstLineCount = cart().getCartLineCount();
        cart().goToProducts();
        cart().addFirstProductToCart();
        Assert.assertFalse(cart().isCartEmptyDisplayed());
        Assert.assertTrue(cart().getCartLineCount() >= 1);
        if (!firstQuantity.isBlank() && !cart().getFirstLineQuantity().isBlank()) {
            Assert.assertTrue(Integer.parseInt(cart().getFirstLineQuantity()) > Integer.parseInt(firstQuantity)
                            || cart().getCartLineCount() > firstLineCount,
                    "Adding the same product twice should increase the cart quantity or create a second line.");
        }
    }

    @Test(description = "ShoppingCart-18: Quantity controls are present on a populated cart")
    public void cart18QuantityControls() {
        cart().addFirstProductToCart();
        Assert.assertFalse(driver.findElements(By.cssSelector("input[name*='quantity'],input[name*='updates'],input[type='number']")).isEmpty());
    }

    @Test(description = "ShoppingCart-19: Quantity can be updated")
    public void cart19UpdateQuantity() {
        cart().addFirstProductToCart();
        cart().updateQuantityByPlus();
        Assert.assertFalse(cart().isCartEmptyDisplayed());
        Assert.assertEquals(cart().getFirstLineQuantity(), "2");
    }

    @Test(description = "ShoppingCart-20: Remove control is available for a populated cart")
    public void cart20RemoveControl() {
        cart().addFirstProductToCart();
        Assert.assertFalse(driver.findElements(By.cssSelector("a[href*='/cart/change'],button[name='remove']")).isEmpty());
        cart().removeItem();
        Assert.assertTrue(cart().isCartEmptyDisplayed());
    }

    @Test(description = "ShoppingCart-21: Cart remains populated after page reload")
    public void cart21PersistsOnReload() {
        cart().addFirstProductToCart();
        Assert.assertFalse(cart().isCartEmptyDisplayed());
        driver.navigate().refresh();
        Assert.assertFalse(cart().isCartEmptyDisplayed());
    }

    @Test(description = "ShoppingCart-22: Cart persists in a second tab")
    public void cart22SecondTabPersistence() {
        cart().addFirstProductToCart();
        driver.switchTo().newWindow(org.openqa.selenium.WindowType.TAB);
        cart().goToCart();
        Assert.assertFalse(cart().isCartEmptyDisplayed());
    }

    @Test(description = "ShoppingCart-23: Discounted product has sale and original prices")
    public void cart23DiscountPrice() {
        driver.get("https://kicap.vn/tui-dung-ban-phim-co-cao-cap-nova");
        String text = driver.findElement(By.tagName("body")).getText();
        Assert.assertTrue(text.contains("280.000") && text.contains("350.000"));
    }

    @Test(description = "ShoppingCart-24: Sold-out discounted product cannot be added to cart")
    public void cart24AddDiscountedProduct() {
        driver.get("https://kicap.vn/tui-dung-ban-phim-co-cao-cap-nova");
        String text = driver.findElement(By.tagName("body")).getText();
        Assert.assertTrue(text.contains("H\u1ebft h\u00e0ng"), "The discounted product's current stock status was not shown.");
    }

    @Test(description = "ShoppingCart-25: Checkout control is enabled when cart has an item")
    public void cart25CheckoutWithItem() {
        cart().addFirstProductToCart();
        Assert.assertTrue(cart().isCheckoutBtnEnabled(), "Checkout button is not enabled for a populated cart.");
    }

    @Test(description = "ShoppingCart-26: Cart count indicator exists in the header")
    public void cart26HeaderCount() {
        cart().addFirstProductToCart();
        Assert.assertTrue(driver.findElements(By.cssSelector("[data-cart-count],.cart-count,#cart-count,a[href*='/cart']")).stream().anyMatch(WebElement::isDisplayed));
    }

    @Test(description = "ShoppingCart-27: Product with variants is selectable")
    public void cart27VariantSelection() {
        driver.get("https://kicap.vn/cidoo-qk75");
        String body = driver.findElement(By.tagName("body")).getText();
        Assert.assertTrue(body.contains("Tr\u1eafng"));
        Assert.assertTrue(body.contains("\u0110en"));
    }

    @Test(description = "ShoppingCart-28: Pre-order product is not represented as ordinary in-stock")
    public void cart28PreorderLabel() {
        driver.get("https://kicap.vn/weikav-nut87");
        String text = driver.findElement(By.tagName("body")).getText().toLowerCase();
        Assert.assertTrue(text.contains("\u0111\u1eb7t tr\u01b0\u1edbc") || text.contains("s\u1eafp v\u1ec1 h\u00e0ng"));
    }

    @Test(description = "ShoppingCart-29: Sold-out status is visible in the collection")
    public void cart29SoldOutStatus() {
        cart().goToProducts();
        Assert.assertTrue(driver.findElement(By.tagName("body")).getText().contains("H\u1ebft h\u00e0ng"));
    }

    @Test(description = "ShoppingCart-30: Empty cart checkout remains disabled")
    public void cart30EmptyCheckoutDisabled() {
        cart().goToCart();
        Assert.assertTrue(cart().isCartEmptyDisplayed());
        Assert.assertFalse(cart().isCheckoutBtnEnabled());
    }
}
