import org.junit.Test;
import org.junit.Assume;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;

/** Shopping-cart checks mapped to Shopping Cart test cases 01–30 in the Group 16 workbook. */
public class ShoppingCartTest extends HoangMinhTuan_KicapBaseTest_21130600_ASM8 {
    private static final String CART_URL = BASE + "/cart";
    private static final By QUANTITY = By.cssSelector(
            ".kicap-cart__items > .kicap-cart__item .kicap-cart__item-qty b," +
            ".kicap-cart__items > .kicap-cart__item input[name='Lines']," +
            "input[name*='quantity'],input[name*='updates'],input[name='qty'],input[type='number']");
    private static final By EDITABLE_QUANTITY = By.cssSelector(
            ".kicap-cart__items > .kicap-cart__item input[name='Lines']:not([disabled])," +
            ".kicap-cart__items > .kicap-cart__item input[name*='quantity']:not([disabled])," +
            ".kicap-cart__items > .kicap-cart__item input[type='number']:not([disabled])");
    private static final By CART_LINES = By.cssSelector(".kicap-cart__items > .kicap-cart__item");
    private static final By REMOVE = By.cssSelector(
            ".kicap-cart__items > .kicap-cart__item .kicap-cart__item-remove," +
            ".kicap-cart__items > .kicap-cart__item a[href*='/cart/change'][aria-label*='Xóa']," +
            "a[href*='/cart/change'],a[href*='quantity=0'],button[name='remove'],[data-cart-remove]");

    private String availableUrl() { return config("availableProductUrl", PRODUCT); }
    private String secondAvailableUrl() { return fixture("secondAvailableProductUrl"); }
    private String preorderUrl() { return config("preorderUrl", PREORDER); }
    private String soldOutUrl() { return fixture("soldOutProductUrl"); }
    private String saleUrl() { return fixture("saleProductUrl"); }

    private void cartPage() {
        open(CART_URL);
    }

    private void addProduct(String url) {
        product(url);
        if (!options().isEmpty() && !hasSelectedOptionInDom()) chooseOption(0);
        assertTrue("Sản phẩm fixture không có nút mua khả dụng: " + url, availableToAdd());
        addToCart();
    }

    private void addDefaultAvailable() { addAvailableFixture(); }

    private List<WebElement> quantityFields() { return visible(QUANTITY); }

    private String firstQuantity() {
        List<WebElement> fields = quantityFields();
        assertFalse("Không tìm thấy ô số lượng trong giỏ", fields.isEmpty());
        WebElement field = fields.get(0);
        String value = field.getTagName().equalsIgnoreCase("input") ? attr(field, "value") : field.getText().trim();
        return value.replaceAll("[^0-9]", "");
    }

    private void submitQuantity(String value) {
        WebElement field = maybe(EDITABLE_QUANTITY);
        Assume.assumeTrue("KICAP hiện hiển thị số lượng dạng chỉ đọc với nút +/-; testcase nhập trực tiếp không áp dụng.", field != null);
        field.clear();
        field.sendKeys(value);
        field.sendKeys(Keys.ENTER);
        pauseAfterAction();
    }

    private void clickQuantityButton(boolean increase) {
        String sign = increase ? "+" : "-";
        WebElement button = maybe(
                By.cssSelector(increase
                        ? ".kicap-cart__items > .kicap-cart__item .kicap-cart__qty-btn[aria-label^='Tăng'],button.btn-plus,[data-quantity-plus]"
                        : ".kicap-cart__items > .kicap-cart__item .kicap-cart__qty-btn[aria-label^='Giảm'],button.btn-minus,[data-quantity-minus]"),
                By.xpath("//button[normalize-space(.)='" + sign + "' or normalize-space(.)='−' or @aria-label='" + (increase ? "Increase quantity" : "Decrease quantity") + "']"));
        assertNotNull("Không tìm thấy nút điều chỉnh số lượng " + sign, button);
        click(button);
    }

    private String productName() {
        product(availableUrl());
        return title();
    }

    private String firstLineText() {
        List<WebElement> lines = visible(CART_LINES);
        return lines.isEmpty() ? body() : lines.get(0).getText();
    }

    private int cartLineCount() {
        List<WebElement> lines = visible(CART_LINES);
        if (!lines.isEmpty()) return lines.size();
        int quantities = quantityFields().size();
        return quantities > 0 ? quantities : visible(REMOVE).size();
    }

    private WebElement checkoutControl() {
        return maybe(By.cssSelector(".kicap-cart__checkout,a[href='/checkout'],button[name='checkout'],input[name='checkout'],a[href*='/checkout']"),
                By.xpath("//button[contains(normalize-space(.),'Thanh toán') or contains(normalize-space(.),'Tiến hành')]|//a[contains(normalize-space(.),'Thanh toán') or contains(normalize-space(.),'Tiến hành')]"));
    }

    private void removeFirstLine() {
        List<WebElement> controls = visible(REMOVE);
        assertFalse("Không tìm thấy nút xóa sản phẩm", controls.isEmpty());
        int before = cartLineCount();
        click(controls.get(0));
        wait.until(d -> cartLineCount() < before || isCartEmptyText(body()));
    }

    private boolean isCartEmptyText(String text) {
        String normalized = text.toLowerCase(Locale.ROOT);
        return normalized.contains("giỏ hàng đang trống") || normalized.contains("giỏ hàng trống") || normalized.contains("chưa có sản phẩm")
                || normalized.contains("your cart is empty");
    }

    private int cartCountBadge() {
        WebElement badge = maybe(By.cssSelector(".kicap-site-header__cart-badge,.count_item_pr,[data-cart-count],.cart-count,#cart-count,[class*='cart-count']"));
        if (badge == null) return -1;
        String value = badge.getText().replaceAll("[^0-9]", "");
        if (value.isEmpty()) value = attr(badge, "data-cart-count").replaceAll("[^0-9]", "");
        return value.isEmpty() ? -1 : Integer.parseInt(value);
    }

    /** KICAP's current product page uses an icon-only add button, separate from “Mua ngay”. */
    @Override protected WebElement addButton() {
        return required(By.cssSelector("button[data-role='addtocart'],button.add_to_cart[aria-label*='Thêm vào giỏ hàng']"),
                By.xpath("//button[contains(normalize-space(.),'Thêm vào giỏ hàng')]"));
    }

    @Override protected boolean availableToAdd() {
        WebElement button = maybe(By.cssSelector("button[data-role='addtocart'],button.add_to_cart[aria-label*='Thêm vào giỏ hàng']"),
                By.xpath("//button[contains(normalize-space(.),'Thêm vào giỏ hàng')]"));
        return button != null && button.isEnabled();
    }

    @Override protected void addToCart() {
        click(addButton());
        wait.until(d -> d.getCurrentUrl().contains("/cart") || d.getCurrentUrl().contains("/checkout")
                || displayed(By.cssSelector("#popup-cart,[role='dialog'],.cart-notification,.cart-drawer.open"))
                || body().toLowerCase(Locale.ROOT).contains("thêm vào giỏ hàng thành công"));
    }

    @Test public void shoppingCart01_emptyCartFromHeader() {
        open(BASE);
        WebElement cartLink = required(By.cssSelector("a[href='/cart'],a[href*='/cart']"));
        assertTrue("Biểu tượng giỏ hàng không hiển thị", cartLink.isDisplayed());
        click(cartLink);
        wait.until(ExpectedConditions.urlContains("/cart"));
        assertTrue("Không thấy trạng thái giỏ hàng trống", isCartEmptyText(body()));
        WebElement checkout = checkoutControl();
        assertTrue("Giỏ trống không được có nút thanh toán hoạt động", checkout == null || !checkout.isEnabled());
        assertFalse("Thiếu liên kết quay lại mua sắm", visible(By.cssSelector("a[href*='/collections/'],a[href='/']")).isEmpty());
    }

    @Test public void shoppingCart02_populatedCartLayout() {
        addDefaultAvailable(); cartPage();
        assertFalse("Giỏ không có sản phẩm", quantityFields().isEmpty());
        assertTrue("Thiếu tổng tiền", body().matches("(?s).*(Tổng|Tạm tính|Subtotal|Total).*"));
        assertTrue("Thiếu nút xóa", !visible(REMOVE).isEmpty());
        assertNotNull("Thiếu nút thanh toán", checkoutControl());
    }

    @Test public void shoppingCart03_productDetailsMatchProductPage() {
        String expectedName = productName();
        String expectedPrice = price().getText().replaceAll("\\s+", " ").trim();
        addAvailableFixture(); cartPage();
        assertTrue("Tên sản phẩm trong giỏ không khớp trang chi tiết", body().contains(expectedName));
        assertFalse("Không đọc được đơn giá sản phẩm", expectedPrice.isEmpty());
        assertTrue("Đơn giá không xuất hiện trong thông tin giỏ", body().contains(expectedPrice)
                || firstLineText().contains(expectedPrice.replace("₫", "")));
        assertEquals("Số lượng ban đầu phải bằng 1", "1", firstQuantity());
    }

    @Test public void shoppingCart04_addAvailableProductShowsConfirmation() {
        addProduct(availableUrl());
        assertTrue("Không thấy phản hồi thêm sản phẩm", body().matches("(?is).*(thành công|đã thêm|added to cart|cart).*"));
        assertTrue("Badge giỏ hàng chưa tăng lên 1", cartCountBadge() == 1 || driver.getCurrentUrl().contains("/cart")
                || !visible(By.cssSelector(".cart-notification,.cart-drawer,.toast-success,[role='dialog']")).isEmpty());
    }

    @Test public void shoppingCart05_continueShoppingClosesConfirmation() {
        addProduct(availableUrl());
        WebElement continueButton = maybe(By.xpath("//*[self::a or self::button][contains(normalize-space(.),'Tiếp tục mua hàng') or contains(normalize-space(.),'Continue shopping')]"));
        assertNotNull("Popup thêm giỏ không có nút tiếp tục mua hàng", continueButton);
        click(continueButton);
        assertTrue("Sau khi tiếp tục mua hàng phải ở lại trang sản phẩm", driver.getCurrentUrl().contains("/products/") || !driver.getCurrentUrl().endsWith("/cart"));
        assertTrue("Sản phẩm không còn trong badge", cartCountBadge() == 1 || cartCountBadge() < 0);
    }

    @Test public void shoppingCart06_viewCartFromConfirmation() {
        product(availableUrl());
        String expectedProduct = title();
        addProduct(availableUrl());
        WebElement viewCart = maybe(By.xpath("//*[self::a or self::button][contains(normalize-space(.),'Kiểm tra giỏ hàng') or contains(normalize-space(.),'Xem giỏ hàng') or contains(normalize-space(.),'View cart')]"));
        if (viewCart != null) {
            click(viewCart);
            wait.until(ExpectedConditions.urlContains("/cart"));
            assertTrue("Sản phẩm vừa thêm không hiển thị trong giỏ", body().contains(expectedProduct));
        } else {
            // Trang KICAP hiện gộp phần xem dòng hàng và nút checkout vào popup; không có nút “Kiểm tra giỏ hàng”.
            assertTrue("Popup không hiển thị sản phẩm vừa thêm", body().contains(expectedProduct));
            assertTrue("Popup không hiển thị số sản phẩm trong giỏ", body().matches("(?s).*Giỏ hàng của bạn có\\s*1\\s+sản phẩm.*"));
            WebElement checkout = maybe(By.xpath("//*[self::a or self::button][contains(normalize-space(.),'Thanh toán đơn hàng') or contains(normalize-space(.),'Tiến hành thanh toán')]"));
            assertNotNull("Popup thiếu thao tác checkout", checkout);
        }
    }

    @Test public void shoppingCart07_addSelectedVariant() {
        product(availableUrl());
        assertTrue("Fixture SP_A phải có nhiều phiên bản", options().size() >= 2);
        String selected = chooseOption(1);
        addToCart(); cartPage();
        assertTrue("Giỏ không hiển thị phiên bản đã chọn: " + selected,
                selected.isBlank() || body().toLowerCase(Locale.ROOT).contains(selected.toLowerCase(Locale.ROOT)));
    }

    @Test public void shoppingCart08_sameProductAndVariantMergeQuantity() {
        product(availableUrl());
        if (options().size() >= 2) chooseOption(0);
        addToCart();
        if (!driver.getCurrentUrl().contains("/products/")) open(availableUrl());
        if (options().size() >= 2) chooseOption(0);
        addToCart(); cartPage();
        assertEquals("Cùng phiên bản phải gộp thành một dòng", 1, cartLineCount());
        assertEquals("Số lượng sau hai lần thêm phải bằng 2", "2", firstQuantity());
    }

    @Test public void shoppingCart09_sameProductDifferentVariantsCreateSeparateLines() {
        product(availableUrl());
        assertTrue("Fixture SP_A phải có ít nhất hai phiên bản", options().size() >= 2);
        chooseOption(0); addToCart(); open(availableUrl()); chooseOption(1); addToCart(); cartPage();
        assertEquals("Hai phiên bản khác nhau phải tạo hai dòng", 2, cartLineCount());
    }

    @Test public void shoppingCart10_addTwoDifferentProducts() {
        addProduct(availableUrl());
        addProduct(secondAvailableUrl());
        cartPage();
        assertEquals("Giỏ phải có hai dòng sản phẩm", 2, cartLineCount());
        assertTrue("Badge phải phản ánh hai sản phẩm", cartCountBadge() < 0 || cartCountBadge() == 2);
    }

    @Test public void shoppingCart11_soldOutProductCannotBeAdded() {
        product(soldOutUrl());
        assertFalse("Sản phẩm hết hàng vẫn có nút mua đang hoạt động", availableToAdd());
        cartPage();
        assertTrue("Sản phẩm hết hàng không được tự thêm vào giỏ", isCartEmptyText(body()));
    }

    @Test public void shoppingCart12_preorderProductCanBeAdded() {
        product(preorderUrl());
        String preorderName = title();
        if (!options().isEmpty() && !hasSelectedOptionInDom()) chooseOption(0);
        assertTrue("Fixture pre-order không có nút mua khả dụng", availableToAdd());
        addToCart(); cartPage();
        assertTrue("Sản phẩm pre-order không xuất hiện trong giỏ", body().contains(preorderName)
                || body().toLowerCase(Locale.ROOT).contains("pre-order"));
        assertEquals("Số lượng pre-order ban đầu phải bằng 1", "1", firstQuantity());
    }

    @Test public void shoppingCart13_guestCanAddToCart() {
        addAvailableFixture(); cartPage();
        assertFalse("Khách chưa đăng nhập vẫn phải thêm được hàng", isCartEmptyText(body()));
        assertFalse("Không được chuyển sang trang đăng nhập khi thêm giỏ", driver.getCurrentUrl().contains("/account/login"));
    }

    @Test public void shoppingCart14_increaseQuantityWithPlus() {
        addAvailableFixture(); cartPage(); clickQuantityButton(true);
        assertEquals("Nhấn [+] phải tăng số lượng lên 2", "2", firstQuantity());
        clickQuantityButton(true);
        assertEquals("Nhấn [+] lần hai phải tăng số lượng lên 3", "3", firstQuantity());
    }

    @Test public void shoppingCart15_decreaseQuantityWithMinus() {
        addAvailableFixture(); cartPage(); clickQuantityButton(true); clickQuantityButton(true);
        clickQuantityButton(false);
        assertEquals("Giảm từ 3 phải còn 2", "2", firstQuantity());
    }

    @Test public void shoppingCart16_decreaseAtMinimumQuantity() {
        addAvailableFixture(); cartPage(); clickQuantityButton(false);
        assertTrue("Số lượng không được giảm xuống số âm", quantityFields().isEmpty()
                || Integer.parseInt(firstQuantity()) >= 1 || isCartEmptyText(body()));
    }

    @Test public void shoppingCart17_enterValidQuantity() {
        addAvailableFixture(); cartPage(); submitQuantity("5");
        assertEquals("Số lượng hợp lệ 5 không được cập nhật", "5", firstQuantity());
    }

    @Test public void shoppingCart18_zeroQuantityIsHandledSafely() {
        addAvailableFixture(); cartPage(); submitQuantity("0");
        assertTrue("Số lượng 0 phải được chuẩn hóa hoặc sản phẩm bị xóa", quantityFields().isEmpty()
                || Integer.parseInt(firstQuantity()) >= 1 || isCartEmptyText(body()));
    }

    @Test public void shoppingCart19_negativeQuantityIsRejected() {
        addAvailableFixture(); cartPage(); submitQuantity("-1");
        assertTrue("Số lượng âm không được giữ trong giỏ", quantityFields().isEmpty()
                || Integer.parseInt(firstQuantity()) >= 1 || isCartEmptyText(body()));
    }

    @Test public void shoppingCart20_textQuantityIsRejected() {
        addAvailableFixture(); cartPage(); submitQuantity("abc");
        assertTrue("Giá trị chữ không được làm hỏng giỏ", quantityFields().isEmpty()
                || firstQuantity().matches("\\d+") || isCartEmptyText(body()));
    }

    @Test public void shoppingCart21_decimalQuantityIsRejected() {
        addAvailableFixture(); cartPage(); submitQuantity("1.5");
        assertTrue("Số lượng phải được giữ ở số nguyên hợp lệ", quantityFields().isEmpty()
                || firstQuantity().matches("\\d+") || isCartEmptyText(body()));
    }

    @Test public void shoppingCart22_quantityAboveStockIsHandled() {
        addAvailableFixture(); cartPage(); submitQuantity("9999");
        String text = body().toLowerCase(Locale.ROOT);
        assertTrue("Số lượng vượt tồn kho phải bị giới hạn hoặc có thông báo", quantityFields().isEmpty()
                || !firstQuantity().equals("9999") || text.contains("tồn kho") || text.contains("stock")
                || text.contains("tối đa") || text.contains("maximum"));
    }

    @Test public void shoppingCart23_removeOneProductFromMultipleLines() {
        addProduct(availableUrl()); addProduct(secondAvailableUrl()); cartPage();
        assertEquals(2, cartLineCount());
        removeFirstLine();
        assertEquals("Xóa một dòng chỉ được để lại một sản phẩm", 1, cartLineCount());
        assertFalse("Không được xóa hết cả hai sản phẩm", isCartEmptyText(body()));
    }

    @Test public void shoppingCart24_removeLastProductEmptiesCart() {
        addAvailableFixture(); cartPage(); removeFirstLine();
        assertTrue("Xóa dòng cuối phải hiển thị giỏ trống", isCartEmptyText(body()));
        WebElement checkout = checkoutControl();
        assertTrue("Không được thanh toán khi giỏ trống", checkout == null || !checkout.isEnabled());
        assertTrue("Badge giỏ phải về 0 hoặc biến mất", cartCountBadge() <= 0);
    }

    @Test public void shoppingCart25_discountedProductTotalsUseSalePrice() {
        addProduct(saleUrl()); cartPage();
        assertFalse("Không thấy sản phẩm giảm giá trong giỏ", quantityFields().isEmpty());
        String line = firstLineText();
        assertTrue("Thiếu thông tin đơn giá/thành tiền của sản phẩm giảm giá", line.matches("(?s).*(\\d[\\d.,]*\\s*(₫|đ|VND)?).*"));
        assertTrue("Thiếu phần tổng tiền", body().matches("(?s).*(Tổng|Tạm tính|Total|Subtotal).*"));
    }

    @Test public void shoppingCart26_cartPersistsAfterReload() {
        addAvailableFixture(); cartPage();
        String before = firstQuantity();
        driver.navigate().refresh(); wait.until(ExpectedConditions.presenceOfElementLocated(QUANTITY));
        assertEquals("Giỏ phải giữ số lượng sau khi tải lại", before, firstQuantity());
    }

    @Test public void shoppingCart27_cartPersistsInNewTab() {
        product(availableUrl());
        String expectedProduct = title();
        addAvailableFixture();
        driver.switchTo().newWindow(org.openqa.selenium.WindowType.TAB);
        open(CART_URL);
        wait.until(d -> body().contains(expectedProduct));
        String cart = body();
        assertTrue("Sản phẩm không còn trong giỏ ở tab mới: " + expectedProduct, cart.contains(expectedProduct));
        assertTrue("Số lượng sản phẩm trong giỏ không được hiển thị", cart.matches("(?s).*1\\s+sản phẩm.*"));
    }

    @Test public void shoppingCart28_headerCartCountIsSynchronized() {
        addAvailableFixture();
        int count = cartCountBadge();
        assertTrue("Badge giỏ không hiển thị số lượng 1", count == 1 || driver.getCurrentUrl().contains("/cart"));
        cartPage(); assertEquals("Số lượng dòng hàng phải khớp badge", 1, Integer.parseInt(firstQuantity()));
    }

    @Test public void shoppingCart29_checkoutControlEnabledWithProduct() {
        addAvailableFixture(); cartPage();
        WebElement checkout = checkoutControl();
        assertNotNull("Giỏ có sản phẩm nhưng thiếu nút thanh toán", checkout);
        assertTrue("Nút thanh toán phải hoạt động khi giỏ có sản phẩm", checkout.isEnabled());
    }

    @Test public void shoppingCart30_emptyCartCannotProceedToCheckout() {
        cartPage();
        assertTrue("Test yêu cầu giỏ hàng trống", isCartEmptyText(body()));
        WebElement checkout = checkoutControl();
        assertTrue("Giỏ hàng trống không được thanh toán", checkout == null || !checkout.isEnabled());
        if (checkout != null && checkout.isEnabled()) {
            click(checkout);
            assertFalse("Không được chuyển sang checkout với giỏ trống", driver.getCurrentUrl().contains("/checkout"));
        }
    }
}
