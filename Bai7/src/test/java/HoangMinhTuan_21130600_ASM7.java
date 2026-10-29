import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HoangMinhTuan_21130600_ASM7 {
    private static final String BASE_URL = "https://kicap.vn";
    private static final String PRODUCT_PATH = "/cidoo-qk75";
    private static final int DEMO_DELAY_MS = 1500;
    private static final By TITLE = By.cssSelector("h1.title-head");

    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;
    @Before
    public void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        js = (JavascriptExecutor) driver;
    }
    @After
    public void tearDown() {
        if (driver != null) {
            pause();
            driver.quit();
        }
    }

    private void pause() {
        try {
            Thread.sleep(DEMO_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void openProduct() {
        driver.get(BASE_URL);
        pause();

        WebElement product = wait.until(d -> d.findElements(By.cssSelector("a[href*='" + PRODUCT_PATH + "']")).stream().filter(WebElement::isDisplayed).findFirst().orElse(null));

        js.executeScript("arguments[0].scrollIntoView({block:'center'});", product);
        pause();
        product.click();

        wait.until(ExpectedConditions.urlContains(PRODUCT_PATH));
        wait.until(ExpectedConditions.visibilityOfElementLocated(TITLE));
        pause();
    }

    /*
     * Trả về ID của radio button tương ứng với màu sản phẩm.
     */
    private String colorId(String color) {
        return "Trắng".equals(color) ? "swatch-0-trang" : "swatch-0-den";
    }

    /*
     * Tìm label có thể click của phiên bản màu.
     */
    private WebElement colorLabel(String color) {
        By locator = By.cssSelector("label[for='" + colorId(color) + "']");
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /*
     * Kiểm tra radio button của phiên bản màu đã được chọn hay chưa.
     */
    private boolean isColorSelected(String color) {
        return driver.findElement(By.id(colorId(color))).isSelected();
    }

    private void selectColor(String color) {
        WebElement label = colorLabel(color);
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", label);
        pause();

        label.click();
        wait.until(d -> isColorSelected(color));
        pause();
    }

    private WebElement firstDisplayed(By locator) {
        return wait.until(d -> d.findElements(locator).stream().filter(element -> element.isDisplayed() && element.isEnabled()).findFirst().orElse(null));
    }

    /*
     * Tìm và lấy nội dung thông báo lỗi trên trang thanh toán.
     */
    private String getError(String message) {
        By locator = By.xpath("//p[contains(@class,'field__message--error') and normalize-space(.)='" + message + "']");
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText().trim();
    }

    /*
     * Mở trang thanh toán theo luồng:
     * Trang chủ → Chi tiết sản phẩm → Chọn màu
     * → Mua ngay → Giỏ hàng → Thanh toán.
     */
    private void openCheckout() {
        // Mở trang sản phẩm và chọn phiên bản màu Trắng.
        openProduct();
        selectColor("Trắng");

        // Tìm nút Mua ngay trên trang chi tiết sản phẩm.
        By buyLocator = By.xpath("//button[contains(normalize-space(.),'Mua ngay')] | //a[contains(normalize-space(.),'Mua ngay')]");
        WebElement buyButton = wait.until(ExpectedConditions.elementToBeClickable(buyLocator));

        // Cuộn đến và click nút Mua ngay.
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", buyButton);
        pause();
        buyButton.click();

        // Chờ website chuyển đến trang giỏ hàng.
        wait.until(ExpectedConditions.urlContains("/cart"));
        pause();

        System.out.println("Đã vào giỏ hàng: " + driver.getCurrentUrl());

        // Tìm nút Tiến hành thanh toán đang hiển thị.
        By checkoutLocator = By.xpath("//button[contains(normalize-space(.),'Tiến hành thanh toán')] | //a[contains(normalize-space(.),'Tiến hành thanh toán')]");
        WebElement checkoutButton = firstDisplayed(checkoutLocator);

        // Cuộn đến và click nút Tiến hành thanh toán.
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", checkoutButton);
        pause();
        checkoutButton.click();

        // Chờ website chuyển đến trang thanh toán.
        wait.until(ExpectedConditions.urlContains("/checkout/"));
        pause();

        System.out.println("Đã vào trang thanh toán: " + driver.getCurrentUrl());
    }

    /*
     * TC01 - CHI TIẾT SẢN PHẨM
     */
    @Test
    public void TC01_openProductDetail() {
        // Thực hiện mở trang chi tiết sản phẩm.
        openProduct();

        // Kiểm tra URL và tên sản phẩm.
        assertTrue("URL trang chi tiết sản phẩm không đúng", driver.getCurrentUrl().contains(PRODUCT_PATH));
        assertTrue("Tên sản phẩm không đúng", driver.findElement(TITLE).getText().contains("Cidoo QK75"));
    }

    /*
     * TC02 - CHI TIẾT SẢN PHẨM
     */
    @Test
    public void TC02_productInformation() {
        // Mở trang chi tiết sản phẩm.
        openProduct();

        // Kiểm tra tên, giá và hình ảnh sản phẩm.
        assertTrue("Tên sản phẩm không đúng", driver.findElement(TITLE).getText().contains("Cidoo QK75"));
        assertTrue("Không thấy giá 999.000đ", wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "999.000")));
        assertFalse("Không tìm thấy hình ảnh sản phẩm", driver.findElements(By.tagName("img")).isEmpty());
    }

    /*
     * TC03 - PHIÊN BẢN SẢN PHẨM
     */
    @Test
    public void TC03_availableVariants() {
        // Mở trang chi tiết sản phẩm.
        openProduct();

        // Kiểm tra hai phiên bản màu.
        assertTrue("Không tìm thấy phiên bản màu Trắng", colorLabel("Trắng").isDisplayed());
        assertTrue("Không tìm thấy phiên bản màu Đen", colorLabel("Đen").isDisplayed());
    }

    /*
     * TC04 - PHIÊN BẢN SẢN PHẨM
     */
    @Test
    public void TC04_changeVariant() {
        // Mở trang chi tiết sản phẩm.
        openProduct();

        // Chọn và kiểm tra màu Trắng.
        selectColor("Trắng");
        assertTrue("Không chọn được phiên bản màu Trắng", isColorSelected("Trắng"));

        // Chuyển sang và kiểm tra màu Đen.
        selectColor("Đen");
        assertTrue("Không chọn được phiên bản màu Đen", isColorSelected("Đen"));
        assertFalse("Phiên bản màu Trắng vẫn còn được chọn", isColorSelected("Trắng"));
    }

    /*
     * TC05 - THANH TOÁN
     */
    @Test
    public void TC05_openCheckout() {
        // Thực hiện luồng mở trang thanh toán.
        openCheckout();

        // Kiểm tra URL trang thanh toán.
        assertTrue("Không vào được trang thanh toán", driver.getCurrentUrl().contains("/checkout/"));
    }

    /*
     * TC06 - THANH TOÁN
     */
    @Test
    public void TC06_checkoutRequiredFields() {
        // Mở trang thanh toán.
        openCheckout();

        // Kiểm tra đang ở đúng trang thanh toán.
        assertTrue("Không vào được trang thanh toán", driver.getCurrentUrl().contains("/checkout/"));

        // Danh sách ID của các trường bắt buộc.
        String[] requiredFields = {"billingName", "billingPhone", "billingAddress"};

        // Kiểm tra các trường bắt buộc đang để trống.
        for (String id : requiredFields) {
            String value = driver.findElement(By.id(id)).getAttribute("value");
            assertEquals("Trường " + id + " phải đang trống", "", value);
        }

        // Tìm nút ĐẶT HÀNG đang hiển thị.
        WebElement orderButton = firstDisplayed(By.cssSelector("button.btn-checkout"));
        assertNotNull("Không tìm thấy nút ĐẶT HÀNG", orderButton);

        // Click ĐẶT HÀNG khi các trường bắt buộc đang trống.
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", orderButton);
        pause();
        orderButton.click();
        pause();

        // Danh sách thông báo lỗi mong đợi.
        String[] messages = {
                "Vui lòng nhập họ tên",
                "Vui lòng nhập số điện thoại",
                "Vui lòng nhập địa chỉ"
        };

        // Kiểm tra từng thông báo lỗi.
        for (String message : messages) {
            assertEquals(message, getError(message));
            System.out.println("PASS: " + message);
        }

        // Kiểm tra website không chuyển sang trang hoàn tất đơn hàng.
        assertTrue("Website đã rời khỏi trang thanh toán", driver.getCurrentUrl().contains("/checkout/"));

        System.out.println("TC06 PASS: Các trường bắt buộc hoạt động đúng.");
    }
}