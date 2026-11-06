import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Assume;
import org.junit.rules.TestName;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.junit.Assert.*;

public abstract class HoangMinhTuan_KicapBaseTest_21130600_ASM8 {
    protected static final String BASE = "https://kicap.vn";
    // Phải nạp dữ liệu TRƯỚC khi khởi tạo PRODUCT/VARIANT.
    private static final Properties FIXTURE_PROPERTIES = loadFixtureProperties();
    protected static final String PRODUCT = config("productUrl", BASE + "/cidoo-qk75");
    protected static final String VARIANT = config("variantUrl", BASE + "/weikav-nut75");
    protected static final String PREORDER = config("preorderUrl", BASE + "/weikav-nut75");
    protected WebDriver driver;
    protected WebDriverWait wait;

    @Rule public TestName testName = new TestName();

    // Chỉ cần tạo kicap-test.properties trong thư mục chạy của IntelliJ.
    // -D... trong VM Options luôn có quyền ưu tiên cao nhất.
    private static Properties loadFixtureProperties() {
        Properties values = new Properties();
        Path file = Paths.get(System.getProperty("testDataFile", "kicap-test.properties"));
        if (Files.isRegularFile(file)) {
            try (InputStream input = Files.newInputStream(file)) {
                values.load(new java.io.InputStreamReader(input, java.nio.charset.StandardCharsets.UTF_8));
                System.out.println("[ASM8] Đã đọc dữ liệu kiểm thử: " + file.toAbsolutePath());
            } catch (IOException e) {
                throw new IllegalStateException("Không đọc được dữ liệu: " + file, e);
            }
        }
        return values;
    }

    protected static String config(String key, String defaultValue) {
        String fromVm = System.getProperty(key);
        if (fromVm != null && !fromVm.trim().isEmpty()) return fromVm.trim();
        return FIXTURE_PROPERTIES.getProperty(key, defaultValue).trim();
    }

    // Thời gian nghỉ sau các thao tác chính; chỉnh trong kicap-test.properties.
    // Đây là độ trễ để quan sát UI, KHÔNG thay thế WebDriverWait.
    protected void pauseAfterAction() {
        final long delayMs;
        try {
            delayMs = Long.parseLong(config("actionDelayMs", "0"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("actionDelayMs phải là số mili-giây, ví dụ 2000", e);
        }
        if (delayMs < 0) {
            throw new IllegalArgumentException("actionDelayMs không được âm: " + delayMs);
        }
        if (delayMs == 0) return;
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Bị ngắt trong lúc chờ giữa các thao tác Selenium", e);
        }
    }

    protected String fixture(String key) {
        String url = config(key, "");
        if (url.isEmpty()) {
            // Chỉ áp dụng mặc định cho kịch bản có dữ liệu tương đối phù hợp.
            // Các trường hợp giảm giá/hết hàng/đa nhóm vẫn bắt buộc dữ liệu riêng.
            switch (key) {
                case "availableProductUrl":
                case "availableVariantUrl":
                case "colorAvailableUrl":
                case "over800kAvailableUrl":
                    url = PRODUCT;
                    break;
                case "preorderUrl":
                    url = PREORDER;
                    break;
                default:
                    break;
            }
        }
        Assume.assumeTrue("SKIPPED: thiếu URL phù hợp. Điền '" + key + "' vào kicap-test.properties hoặc -D" + key + "=<URL>", !url.isEmpty());
        assertTrue("URL fixture không hợp lệ: " + key + "=" + url, url.startsWith("https://") || url.startsWith("http://"));
        return url;
    }

    protected void allowOrderOrSkip() {
        Assume.assumeTrue("SKIPPED: không tạo đơn hàng thật. Chỉ bật -DallowRealOrders=true trong môi trường kiểm thử được phép.",
                Boolean.parseBoolean(config("allowRealOrders", "false")));
    }

    @Before public void setUp() {
        ChromeOptions options = new ChromeOptions();
        if (Boolean.parseBoolean(config("headless", "false"))) options.addArguments("--headless=new");
        options.addArguments("--window-size=1440,900", "--disable-notifications");
        driver = new ChromeDriver(options);  // Selenium Manager tự tìm ChromeDriver tương thích
        wait = new WebDriverWait(driver, Duration.ofSeconds(12));
    }

    @After public void tearDown() {
        if (driver != null) {
            try {
                if (driver instanceof TakesScreenshot) {
                    File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                    Path dest = Paths.get("Envidence - Group16", getClass().getSimpleName() + "_" + testName.getMethodName() + ".png");
                    Files.createDirectories(dest.getParent());
                    Files.copy(src.toPath(), dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException | WebDriverException ignored) { }
            driver.quit();
        }
    }

    protected void open(String url) {
        driver.get(url);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        assertFalse("Website không được mở trang lỗi", driver.getTitle().toLowerCase(Locale.ROOT).contains("404"));
        pauseAfterAction(); // cho người xem quan sát sau khi chuyển trang
    }

    protected void product(String url) {
        open(url);
        assertTrue("Không hiển thị tiêu đề sản phẩm", displayed(By.cssSelector("h1")));
    }

    protected List<WebElement> visible(By selector) {
        List<WebElement> result = new ArrayList<>();
        try {
            for (WebElement el : driver.findElements(selector)) {
                try { if (el.isDisplayed()) result.add(el); } catch (StaleElementReferenceException ignored) { }
            }
        } catch (org.openqa.selenium.NoSuchElementException ignored) { }
        return result;
    }

    protected WebElement maybe(By... locators) {
        for (By locator : locators) {
            List<WebElement> list = visible(locator);
            if (!list.isEmpty()) return list.get(0);
        }
        return null;
    }

    protected WebElement required(By... locators) {
        WebElement element = maybe(locators);
        assertNotNull("Không tìm được phần tử. Kiểm tra locator với F12 trên Kicap.vn: " + Arrays.toString(locators), element);
        return element;
    }

    protected boolean displayed(By... selectors) { return maybe(selectors) != null; }
    protected String body() { return driver.findElement(By.tagName("body")).getText(); }
    protected String title() { return required(By.cssSelector("h1")).getText().trim(); }

    protected void click(WebElement element) {
        assertTrue("Nút đang bị vô hiệu hóa: " + element.getText(), element.isEnabled());
        try { element.click(); }
        catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", element);
            element.click();
        }
        pauseAfterAction(); // chờ sau khi nhấn nút / chọn phiên bản
    }

    protected void fill(WebElement element, String value) {
        element.click();
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        element.sendKeys(Keys.BACK_SPACE);
        element.sendKeys(value);
        element.sendKeys(Keys.TAB);
        pauseAfterAction(); // chờ sau khi điền xong một trường
    }

    protected void requireScenario(boolean condition, String reason) {
        Assume.assumeTrue("SKIPPED: dữ liệu/đặc tính trang không đáp ứng tiền điều kiện: " + reason, condition);
    }

    protected String attr(WebElement el, String name) { return Optional.ofNullable(el.getAttribute(name)).orElse(""); }
    protected long money(String value) {
        String raw = value.replaceAll("[^0-9]", "");
        assertFalse("Không có số tiền trong: " + value, raw.isEmpty());
        return Long.parseLong(raw);
    }

    protected long moneyOrZero(String value) {
        String v = value.trim().toLowerCase(Locale.ROOT);
        return (v.contains("miễn phí") || v.contains("free")) ? 0L : money(value);
    }

    // Phần sản phẩm: hỗ trợ selector phổ biến của Haravan/Sapo và thành phần HTML tùy biến.
    protected WebElement price() {
        return required(By.cssSelector(".product-price .price, .product-price, .price-box .special-price, .price-box .price, [data-product-price], .product__price"));
    }
    protected String sku() {
        Matcher m = Pattern.compile("(?i)SKU\\s*:\\s*([^\\s]+)").matcher(body());
        assertTrue("Không tìm thấy mã SKU", m.find());
        return m.group(1);
    }
    protected WebElement mainImage() {
        // Ưu tiên vùng gallery; dự phòng cho theme Kicap có cấu trúc HTML thay đổi.
        // Không đồng nhất "ảnh bất kỳ" với ảnh chính: cần ảnh sản phẩm lớn, nhìn thấy được.
        By[] selectors = {
                By.cssSelector(".product-main-image img, .product-image-main img, .product-gallery-main img, .product-single__photo img, [data-main-image] img, img[data-main-image]"),
                By.cssSelector(".product-gallery img, .product-image img, .product__media img, .product-detail img, .product-images img, .product-single img"),
                By.cssSelector("img[src*='/products/'], img[data-src*='/products/']")
        };
        for (By selector : selectors) {
            WebElement image = largestVisibleProductImage(selector);
            if (image != null) return image;
        }
        // Dự phòng: ảnh sản phẩm có thể nằm trong một slider không có class nêu trên.
        // Giới hạn ảnh phải lớn và nằm gần phần đầu trang, tránh ảnh footer/icon.
        WebElement fallback = largestVisibleProductImage(By.cssSelector("main img, [role='main'] img, img"));
        if (fallback != null) return fallback;
        throw new AssertionError("Không tìm được ảnh sản phẩm hiển thị (>=120px). Hãy kiểm tra ảnh/DOM trong screenshot của test.");
    }

    private WebElement largestVisibleProductImage(By selector) {
        WebElement best = null;
        long bestScore = Long.MIN_VALUE;
        for (WebElement img : visible(selector)) {
            try {
                org.openqa.selenium.Rectangle rect = img.getRect();
                long w = rect.getWidth(), h = rect.getHeight();
                if (w < 120 || h < 120) continue;
                Boolean thumb = (Boolean)((JavascriptExecutor)driver).executeScript(
                        "return !!arguments[0].closest('.thumbnail,.gallery-thumbs,.product-thumbnail,[class*=thumb],header,footer')", img);
                if (Boolean.TRUE.equals(thumb)) continue;
                String src = attr(img, "currentSrc");
                if (src.isEmpty()) src = attr(img, "src");
                if (src.isEmpty()) src = attr(img, "data-src");
                if (src.isEmpty()) continue;
                long score = w*h;
                if (src.contains("/products/")) score += 50_000_000L;
                // Vùng gallery thường xuất hiện đầu trang, trước mô tả chi tiết.
                if (rect.getY() < 1300) score += 1_000_000L;
                if (score > bestScore) { best = img; bestScore = score; }
            } catch (StaleElementReferenceException ignored) { }
        }
        return best;
    }
    protected List<WebElement> thumbs() {
        List<WebElement> x = visible(By.cssSelector(".product-thumbnail img, .thumbnail img, .gallery-thumbs img, [class*='thumb'] img"));
        return x;
    }
    protected List<WebElement> options() {
        // Không chọn radio ẩn trực tiếp: thao tác trên nhãn của từng tùy chọn.
        List<WebElement> x = visible(By.cssSelector(".swatch-element label, .variant-option label, .product-variants label, .product-variant label, label[for*='swatch'], label[for*='variant']"));
        if (x.isEmpty()) x = visible(By.cssSelector(".swatch-element, .variant-option, [data-variant-option]"));
        return x;
    }
    protected String chooseOption(int index) {
        List<WebElement> x = options();
        requireScenario(x.size() > index, "Sản phẩm cần ít nhất " + (index + 1) + " lựa chọn");
        WebElement target = x.get(index);
        String label = target.getText().trim();
        if (label.isEmpty()) label = attr(target, "title");
        click(target);
        return label;
    }
    protected String selectedOption() {
        // Nhiều website đặt <input checked> trước hoặc sau label, trong khi radio bị ẩn.
        for (WebElement radio : driver.findElements(By.cssSelector("input[type='radio']:checked"))) {
            try {
                String id = attr(radio, "id");
                if (!id.isEmpty()) {
                    List<WebElement> labels = driver.findElements(By.cssSelector("label[for='" + id.replace("'", "\\'") + "']"));
                    for (WebElement label : labels) if (!label.getText().trim().isEmpty()) return label.getText().trim();
                }
                String value = attr(radio, "value");
                if (!value.isEmpty() && !value.equalsIgnoreCase("on")) return value;
            } catch (StaleElementReferenceException ignored) { }
        }
        WebElement selected = maybe(By.cssSelector(".swatch-element.active label, .swatch-element.selected label, .variant-option.active, .variant-option.selected, label.active, label.selected, [aria-checked='true'], [aria-selected='true'], input[type='radio']:checked + label"));
        return selected == null ? "" : selected.getText().trim();
    }

    protected boolean hasSelectedOptionInDom() {
        if (!driver.findElements(By.cssSelector("input[type='radio']:checked, option:checked, [aria-checked='true'], [aria-selected='true'], .swatch-element.active, .swatch-element.selected, .variant-option.active, .variant-option.selected, label.active, label.selected")).isEmpty()) return true;
        return !selectedOption().isEmpty();
    }
    protected WebElement quantity() {
        return required(By.cssSelector("input[name='quantity'], input[name='qty'], input.quantity, .quantity input, input[type='number']"));
    }
    protected int qty() {
        String val = attr(quantity(), "value");
        assertTrue("Giá trị số lượng phải là số nguyên dương: " + val, val.matches("\\d+"));
        return Integer.parseInt(val);
    }
    protected WebElement plus() {
        return required(By.xpath("//button[normalize-space(.)='+'] | //button[normalize-space(.)='＋'] | //button[contains(@class,'plus')]"));
    }
    protected WebElement minus() {
        return required(By.xpath("//button[normalize-space(.)='–' or normalize-space(.)='-' or normalize-space(.)='−'] | //button[contains(@class,'minus')]"));
    }
    protected WebElement addButton() {
        return required(By.xpath("//button[contains(normalize-space(.),'Thêm vào giỏ') or contains(normalize-space(.),'Mua ngay')] | //input[contains(@value,'Thêm vào giỏ') or contains(@value,'Mua ngay')]"));
    }
    protected boolean availableToAdd() {
        WebElement add = maybe(By.xpath("//button[contains(normalize-space(.),'Thêm vào giỏ') or contains(normalize-space(.),'Mua ngay')] | //input[contains(@value,'Thêm vào giỏ') or contains(@value,'Mua ngay')]"));
        return add != null && add.isEnabled();
    }
    protected void addToCart() {
        click(addButton());
        // KHÔNG dùng body().contains("giỏ hàng") vì chữ "Giỏ hàng" có sẵn trong header.
        wait.until(d -> d.getCurrentUrl().contains("/cart") || d.getCurrentUrl().contains("/checkout") ||
                body().toLowerCase(Locale.ROOT).matches("(?s).*(đã thêm vào giỏ|thêm vào giỏ hàng thành công|đã thêm sản phẩm).*" ) ||
                displayed(By.cssSelector(".toast-success, .cart-notification, .notification-success, .cart-drawer.open, .mini-cart.active")));
    }
    protected void goCart() { open(BASE + "/cart"); }
    protected void goCheckout() {
        WebElement button = maybe(
                By.xpath("//a[contains(normalize-space(.),'Tiến hành thanh toán')] | //button[contains(normalize-space(.),'Tiến hành thanh toán')]"),
                By.xpath("//a[contains(normalize-space(.),'Thanh toán đơn hàng')] | //button[contains(normalize-space(.),'Thanh toán đơn hàng')]"),
                By.xpath("//a[contains(normalize-space(.),'Thanh toán')] | //button[contains(normalize-space(.),'Thanh toán')]")
        );
        assertNotNull("Không tìm được nút thanh toán ở trang giỏ hàng", button);
        click(button);
        wait.until(d -> d.getCurrentUrl().contains("/checkout"));
    }
    protected void addAvailableFixture() {
        String url = fixture("availableProductUrl");
        product(url);
        // Chọn rõ một phiên bản trước khi kiểm tra nút mua.
        if (!options().isEmpty()) {
            for (int i = 0; i < options().size(); i++) {
                chooseOption(i);
                if (availableToAdd() && !body().toLowerCase(Locale.ROOT).contains("vui lòng chọn phiên bản")) break;
            }
        }
        // Đừng chuyển thành SKIPPED: đây là dữ liệu nền cho toàn bộ luồng thanh toán.
        // Nếu mất hàng, phải báo FAIL rõ ràng để đổi URL fixture.
        assertTrue("Fixture availableProductUrl không mua được: " + url + ". Chọn sản phẩm còn hàng và chọn đúng phiên bản.", availableToAdd());
        addToCart();
    }
    protected void navigateToCheckoutAfterBuy() {
        // KICAP có thể dùng nút "Mua ngay" để đi thẳng tới checkout.
        // Chỉ đi qua /cart nếu nút vừa bấm không chuyển thẳng tới checkout.
        if (driver.getCurrentUrl().contains("/checkout")) return;
        goCart();
        goCheckout();
    }
    protected void checkoutWithCart() {
        addAvailableFixture();
        navigateToCheckoutAfterBuy();
    }
    protected WebElement checkoutField(String field) {
        switch (field) {
            case "name": return required(By.cssSelector("input[name='name'], input[name='billingName'], input[name='full_name'], input[name*='full_name'], input[id*='name']"),
                    By.xpath("//input[contains(@placeholder,'Họ tên') or contains(@placeholder,'họ tên') or contains(@placeholder,'Người nhận')]") );
            case "phone": return required(By.cssSelector("input[type='tel'], input[name='phone'], input[name='billingPhone'], input[name*='phone'], input[id*='phone']"));
            case "email": return required(By.cssSelector("input[type='email'], input[name='email'], input[name='billingEmail'], input[name*='email']"));
            case "address": return required(By.cssSelector("input[name='address'], input[name='billingAddress'], input[name='address1'], input[name*='address1'], input[id*='address']"),
                    By.xpath("//input[contains(@placeholder,'Địa chỉ') or contains(@placeholder,'địa chỉ')]") );
            case "note": return required(By.cssSelector("textarea[name='note'], textarea[name='orderNote'], textarea, input[name='note']"));
            default: throw new IllegalArgumentException("Trường chưa hỗ trợ: " + field);
        }
    }
    protected WebElement region(String type) {
        String key = type.equals("province") ? "province" : type.equals("district") ? "district" : "ward";
        String alt = key.equals("province") ? "city" : key;
        WebElement x = maybe(By.cssSelector("select[name*='"+key+"'], select[name*='"+alt+"'], select[id*='"+key+"'], [name*='"+key+"'][role='combobox'], [id*='"+key+"'][role='combobox']"));
        if (x == null) {
            String label = type.equals("province") ? "Tỉnh" : type.equals("district") ? "Quận" : "Phường";
            x = maybe(By.xpath("//*[contains(text(),'" + label + "')]/following::select[1]"),
                    By.xpath("//*[contains(text(),'" + label + "')]/following::*[@role='combobox'][1]"));
        }
        assertNotNull("Không tìm thấy ô chọn " + type + ". Kiểm tra lại locator.", x);
        return x;
    }
    protected void selectRegion(String type, String value) {
        WebElement el = region(type);
        if (el.getTagName().equalsIgnoreCase("select")) {
            wait.until(d -> new Select(region(type)).getOptions().stream().anyMatch(
                    o -> o.getText().trim().equalsIgnoreCase(value.trim())));
            new Select(region(type)).selectByVisibleText(value);
            pauseAfterAction(); // thao tác Select không đi qua click() của BaseTest
        } else {
            click(el);
            WebElement item = required(By.xpath("//*[@role='option' and contains(normalize-space(.),'" + value + "')] | //li[contains(normalize-space(.),'" + value + "')]"));
            click(item);
        }
    }
    protected String selectFirstRegionOption(String type) {
        WebElement el = region(type);
        assertEquals("Chức năng chọn ngẫu nhiên giá trị chỉ hỗ trợ <select>", "select", el.getTagName().toLowerCase(Locale.ROOT));
        // Sau chọn tỉnh, các quận/huyện có thể được tải bằng AJAX.
        wait.until(d -> new Select(region(type)).getOptions().stream().anyMatch(o ->
                !o.getText().trim().isEmpty() && !o.getText().matches("(?iu).*(chọn|select|--).*" ) && o.isEnabled() && !attr(o, "value").trim().isEmpty()));
        Select box = new Select(region(type));
        for (WebElement option : box.getOptions()) {
            String label = option.getText().trim();
            if (!label.isEmpty() && !label.matches("(?iu).*(chọn|select|--).*") && option.isEnabled()
                    && !attr(option, "value").trim().isEmpty()) {
                box.selectByVisibleText(label);
                pauseAfterAction();
                return label;
            }
        }
        throw new AssertionError("Danh sách " + type + " không có lựa chọn hợp lệ.");
    }

    protected boolean invalid(WebElement field) {
        try {
            Object result = ((JavascriptExecutor) driver).executeScript("return !arguments[0].checkValidity();", field);
            return Boolean.TRUE.equals(result) || attr(field,"aria-invalid").equals("true") ||
                    !attr(field,"validationMessage").isEmpty() || attr(field,"class").toLowerCase(Locale.ROOT).contains("error");
        } catch (WebDriverException e) { return false; }
    }
    protected WebElement placeOrderButton() {
        return required(By.xpath("//button[contains(normalize-space(.),'Đặt hàng') or contains(normalize-space(.),'Hoàn tất đơn hàng')] | //input[@type='submit' and contains(@value,'Đặt hàng')]"));
    }
    protected void ensureNoOrderSubmitted(String oldUrl) {
        assertFalse("Không được chuyển đến trang đặt hàng thành công", driver.getCurrentUrl().matches("(?i).*success.*|.*thank.?you.*"));
        assertFalse("Không được hiển thị xác nhận đặt hàng", body().matches("(?is).*đặt hàng thành công.*"));
    }
    protected void safeInvalidSubmit(WebElement invalidField) {
        // Bảo vệ: chỉ nhấn Đặt hàng khi đã xác minh trường không hợp lệ theo HTML5.
        requireScenario(invalid(invalidField), "Trường thiếu/sai không được HTML5 đánh dấu invalid; cần kiểm thử trên môi trường test");
        String before = driver.getCurrentUrl();
        click(placeOrderButton());
        ensureNoOrderSubmitted(before);
    }
    protected void selectPayment(String keyword) {
        String expected = foldVietnamese(keyword);
        WebElement candidate = null;
        // Không chỉ tìm <label>: theme checkout có thể dùng button/div có role radio.
        for (WebElement node : visible(By.cssSelector(
                "label, button, [role='radio'], [role='button'], .payment-method, .payment-option"))) {
            String caption = node.getText() + " " + attr(node,"title") + " " + attr(node,"aria-label");
            String normalized = foldVietnamese(caption);
            boolean match = normalized.contains(expected)
                    || (expected.contains("chuyen khoan") && (normalized.contains("bank transfer") || normalized.contains("chuyen tien")));
            if (match) { candidate = node; break; }
        }
        assertNotNull("Không tìm thấy phương thức thanh toán: " + keyword
                + ". Kiểm tra liệu checkout có hỗ trợ phương thức này và DOM thực tế.", candidate);
        click(candidate);
    }

    private String foldVietnamese(String text) {
        String normalized = java.text.Normalizer.normalize(text.toLowerCase(Locale.ROOT), java.text.Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "").replace('đ','d').replaceAll("\\s+", " ").trim();
    }
    protected void requireLoginFixture() {
        String email = config("testEmail", ""), password = config("testPassword", "");
        requireScenario(!email.isEmpty() && !password.isEmpty(), "Cần -DtestEmail và -DtestPassword là tài khoản TEST");
        open(BASE + "/account/login");
        fill(required(By.cssSelector("input[type='email'], input[name='email']")), email);
        fill(required(By.cssSelector("input[type='password']")), password);
        click(required(By.cssSelector("button[type='submit'], input[type='submit']")));
        wait.until(d -> !d.getCurrentUrl().contains("login"));
    }
    protected void validCustomer() {
        fill(checkoutField("name"), "Nguyen Van An Test");
        fill(checkoutField("phone"), "0987654321");
        fill(checkoutField("email"), "kicap.test@example.com");
        fill(checkoutField("address"), "123 Duong Kiem Thu");
        String p=config("testProvince", ""), d=config("testDistrict", ""), w=config("testWard", "");
        requireScenario(!p.isEmpty() && !d.isEmpty() && !w.isEmpty(), "Cần -DtestProvince/-DtestDistrict/-DtestWard");
        selectRegion("province", p);
        selectRegion("district", d);
        selectRegion("ward", w);
    }
}
