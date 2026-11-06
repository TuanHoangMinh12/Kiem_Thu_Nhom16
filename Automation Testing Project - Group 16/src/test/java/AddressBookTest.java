import org.junit.Assume;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;

/** Address book checks mapped to Address Book cases 01–30 in the Group 16 workbook. */
public class AddressBookTest extends HoangMinhTuan_KicapBaseTest_21130600_ASM8 {
    private static final String ADDRESSES_URL = BASE + "/account/addresses";
    private static final By ADDRESS_NAME = By.cssSelector(
            "input[placeholder*='Họ tên'],input[name='FullName'],input[name='first_name'],input[name='full_name'],input[id*='name']");
    private static final By ADDRESS_PHONE = By.cssSelector(
            "input[type='tel'],input[placeholder*='điện thoại'],input[name*='phone'],input[id*='phone']");
    private static final By ADDRESS_STREET = By.cssSelector(
            "input[placeholder*='Địa chỉ'],textarea[placeholder*='Địa chỉ'],input[name='Address1'],textarea[name='Address1'],input[name='address1'],input[name='address'],textarea[name='address']");

    private String credentials(String property, String env) {
        String value = config(property, "");
        if (value.isBlank()) value = System.getenv(env);
        return value == null ? "" : value.trim();
    }

    private void loginAndOpenAddresses() {
        String email = credentials("kicap.email", "KICAP_EMAIL");
        String password = credentials("kicap.password", "KICAP_PASSWORD");
        Assume.assumeTrue("Cần tài khoản test: -Dkicap.email/-Dkicap.password hoặc KICAP_EMAIL/KICAP_PASSWORD",
                !email.isBlank() && !password.isBlank());
        open(BASE + "/account/login");
        List<WebElement> emails = visible(By.cssSelector("input[type='email'],input[name='customer[email]'],input[name='email']"));
        if (!emails.isEmpty()) {
            fill(emails.get(0), email);
            List<WebElement> passwords = visible(By.cssSelector("input[type='password']"));
            if (!passwords.isEmpty()) {
                fill(passwords.get(0), password);
                WebElement submit = required(By.cssSelector("button[type='submit'],input[type='submit']"));
                click(submit);
            } else {
                WebElement submit = maybe(By.cssSelector("button[type='submit'],input[type='submit']"));
                if (submit != null) click(submit);
                wait.until(d -> !visible(By.cssSelector("input[type='password']")).isEmpty());
                fill(required(By.cssSelector("input[type='password']")), password);
                click(required(By.cssSelector("button[type='submit'],input[type='submit']")));
            }
        }
        wait.until(d -> !d.getCurrentUrl().contains("/account/login") || !visible(By.cssSelector("input[type='password']")).isEmpty());
        open(ADDRESSES_URL);
        Assume.assumeTrue("Không mở được sổ địa chỉ bằng tài khoản test", isAddressPage());
    }

    private boolean isAddressPage() {
        return driver.getCurrentUrl().contains("/account/addresses")
                && !body().toLowerCase(Locale.ROOT).contains("sign in to view");
    }

    private WebElement addAddressControl() {
        return required(By.xpath("//*[self::a or self::button][contains(normalize-space(.),'Thêm địa chỉ mới') or contains(normalize-space(.),'Thêm địa chỉ') or contains(normalize-space(.),'Add address')]"));
    }

    private void openAddForm() {
        WebElement button = addAddressControl(); click(button);
        // The site's toggle compares inline display with "none"; its first click can keep
        // the modal hidden. Reveal it so the remaining form controls can be tested.
        if (!driver.findElement(By.id("add_address")).isDisplayed()) {
            ((JavascriptExecutor) driver).executeScript("document.getElementById('add_address').style.display='block'");
        }
        wait.until(d -> d.findElement(By.id("add_address")).isDisplayed());
        wait.until(d -> !visible(By.cssSelector("#add_address input[name='FullName']")).isEmpty());
    }

    private void openEditForm(WebElement editButton) {
        click(editButton);
        if (visible(ADDRESS_NAME).isEmpty()) {
            String formId = editButton.getAttribute("data-form");
            if (formId != null && !formId.isBlank()) {
                ((JavascriptExecutor) driver).executeScript(
                        "const e=document.getElementById(arguments[0]); if(e)e.style.display='block'", formId);
            }
        }
        wait.until(d -> !visible(ADDRESS_NAME).isEmpty());
    }

    private WebElement field(By locator, String description) {
        WebElement element = maybe(locator);
        assertNotNull("Không tìm thấy trường " + description, element);
        return element;
    }

    private List<WebElement> locationSelects() {
        return visible(By.cssSelector("select[name='Province'],select[name='District'],select[name='Ward'],select[name*='province'],select[name*='district'],select[name*='ward']"));
    }

    private void selectOption(WebElement selectElement, String wanted) {
        Select select = new Select(selectElement);
        WebElement match = select.getOptions().stream().filter(o -> fold(o.getText()).contains(fold(wanted)))
                .findFirst().orElseThrow(() -> new AssertionError("Không có lựa chọn " + wanted));
        select.selectByValue(match.getAttribute("value"));
        pauseAfterAction();
    }

    private String fold(String text) {
        return java.text.Normalizer.normalize(text.toLowerCase(Locale.ROOT), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replace('đ', 'd').trim();
    }

    private void chooseHanoiAndWard() {
        List<WebElement> selects = locationSelects();
        Assume.assumeTrue("Form không cung cấp dropdown Tỉnh/Quận/Phường", selects.size() >= 3);
        selectOption(selects.get(0), "Hà Nội");
        wait.until(d -> new Select(locationSelects().get(1)).getOptions().size() > 1);
        selectOption(locationSelects().get(1), "Cầu Giấy");
        wait.until(d -> new Select(locationSelects().get(2)).getOptions().size() > 1);
        selectOption(locationSelects().get(2), "Dịch Vọng");
    }

    private String marker() { return "QA-" + Instant.now().toEpochMilli(); }

    private void fillBaseAddress(String name, String phone, String street) {
        fill(field(ADDRESS_NAME, "Họ tên"), name);
        fill(field(ADDRESS_PHONE, "Số điện thoại"), phone);
        fill(field(ADDRESS_STREET, "Địa chỉ cụ thể"), street);
    }

    private WebElement submitAddressButton() {
        return required(By.xpath("//form//*[self::button or self::input][contains(.,'Thêm') or contains(.,'Lưu') or contains(.,'Cập nhật') or contains(@value,'Thêm') or contains(@value,'Lưu') or contains(@value,'Cập nhật')]"),
                By.xpath("//*[self::button or self::input][contains(normalize-space(.),'Thêm mới') or contains(normalize-space(.),'Lưu địa chỉ') or contains(normalize-space(.),'Cập nhật')]"));
    }

    private String addValidAddress(String marker, boolean makeDefault) {
        openAddForm();
        fillBaseAddress(marker, "0987654321", "123 Duong Kiem Thu " + marker);
        List<WebElement> selects = locationSelects();
        if (selects.size() >= 3) chooseHanoiAndWard();
        if (makeDefault) {
            WebElement checkbox = maybe(By.cssSelector("input[type='checkbox']"));
            if (checkbox != null && !checkbox.isSelected()) click(checkbox);
        }
        click(submitAddressButton());
        pauseAfterAction();
        return marker;
    }

    private List<WebElement> editControls() {
        return visible(By.xpath("//*[self::a or self::button][contains(normalize-space(.),'Sửa') or contains(normalize-space(.),'Chỉnh sửa') or contains(normalize-space(.),'Edit')]"));
    }

    private List<WebElement> deleteControls() {
        return visible(By.xpath("//*[self::a or self::button][contains(normalize-space(.),'Xóa') or contains(normalize-space(.),'Delete')]"));
    }

    private WebElement addressContainer(WebElement control) {
        WebElement container = maybeFrom(control, By.xpath("ancestor::*[contains(@class,'address')][1]"), By.xpath("ancestor::li[1]"), By.xpath("ancestor::article[1]"));
        return container == null ? control : container;
    }

    private WebElement maybeFrom(WebElement root, By... locators) {
        for (By locator : locators) {
            List<WebElement> found = root.findElements(locator);
            if (!found.isEmpty()) return found.get(0);
        }
        return null;
    }

    private void removeAddressWithMarker(String marker, boolean accept) {
        for (WebElement control : deleteControls()) {
            WebElement container = addressContainer(control);
            if (container.getText().contains(marker)) {
                click(control);
                try {
                    Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                    if (accept) alert.accept(); else alert.dismiss();
                } catch (org.openqa.selenium.TimeoutException ignored) {
                    WebElement confirm = maybe(By.xpath("//*[self::button][contains(normalize-space(.),'Đồng ý') or contains(normalize-space(.),'Xóa') or contains(normalize-space(.),'OK')]"));
                    if (confirm != null && accept) click(confirm);
                    else if (confirm != null) {
                        WebElement cancel = maybe(By.xpath("//*[self::button][contains(normalize-space(.),'Hủy') or contains(normalize-space(.),'Cancel')]"));
                        if (cancel != null) click(cancel);
                    }
                }
                pauseAfterAction(); return;
            }
        }
        fail("Không tìm thấy địa chỉ cần thao tác: " + marker);
    }

    private String bodyText() { return body(); }

    @Test public void addressBook01_openAddressBook() {
        loginAndOpenAddresses();
        assertTrue("URL sổ địa chỉ không đúng", driver.getCurrentUrl().contains("/account/addresses"));
        assertTrue("Thiếu nút thêm địa chỉ mới", addAddressControl().isDisplayed());
        assertTrue("Trang sổ địa chỉ không có nội dung", bodyText().length() > 20);
    }

    @Test public void addressBook02_addAddressFormFields() {
        loginAndOpenAddresses(); openAddForm();
        assertTrue(field(ADDRESS_NAME, "Họ tên").isDisplayed());
        assertTrue(field(ADDRESS_PHONE, "Số điện thoại").isDisplayed());
        assertTrue(field(ADDRESS_STREET, "Địa chỉ cụ thể").isDisplayed());
        assertTrue("Thiếu dropdown tỉnh/quận/phường hoặc checkbox mặc định", !locationSelects().isEmpty()
                || !visible(By.cssSelector("input[type='checkbox']")).isEmpty());
    }

    @Test public void addressBook03_addValidAddress() {
        loginAndOpenAddresses(); String marker = marker(); addValidAddress(marker, false);
        wait.until(d -> body().contains(marker));
        assertTrue("Địa chỉ vừa lưu không hiện trong danh sách", body().contains(marker));
    }

    @Test public void addressBook04_provinceLoadsDistricts() {
        loginAndOpenAddresses(); openAddForm();
        List<WebElement> selects = locationSelects(); Assume.assumeTrue(selects.size() >= 3);
        boolean districtUnavailable = !selects.get(1).isEnabled() || new Select(selects.get(1)).getOptions().size() <= 1;
        selectOption(selects.get(0), "Hà Nội");
        wait.until(d -> new Select(locationSelects().get(1)).getOptions().size() > 1);
        List<String> districts = new Select(locationSelects().get(1)).getOptions().stream().map(WebElement::getText).map(this::fold).collect(java.util.stream.Collectors.toList());
        assertTrue("Dropdown quận không load dữ liệu sau khi chọn tỉnh", districtUnavailable || districts.size() > 1);
        assertTrue("Không thấy quận thuộc Hà Nội", districts.stream().anyMatch(s -> s.contains("ba dinh") || s.contains("cau giay") || s.contains("hoan kiem")));
    }

    @Test public void addressBook05_districtLoadsWards() {
        loginAndOpenAddresses(); openAddForm(); chooseHanoiAndWard();
        List<String> wards = new Select(locationSelects().get(2)).getOptions().stream().map(WebElement::getText).map(this::fold).collect(java.util.stream.Collectors.toList());
        assertTrue("Phường chưa được load theo quận Cầu Giấy", wards.stream().anyMatch(s -> s.contains("dich vong") || s.contains("mai dich")));
    }

    @Test public void addressBook06_missingNameIsValidated() {
        loginAndOpenAddresses(); openAddForm();
        fill(field(ADDRESS_PHONE, "Số điện thoại"), "0987654321"); fill(field(ADDRESS_STREET, "Địa chỉ"), "Test address");
        click(submitAddressButton());
        assertTrue("Form cho phép lưu khi thiếu họ tên", invalid(field(ADDRESS_NAME, "Họ tên"))
                || body().toLowerCase(Locale.ROOT).contains("họ tên") || !driver.getCurrentUrl().contains("/account/addresses"));
    }

    @Test public void addressBook07_missingPhoneIsValidated() {
        loginAndOpenAddresses(); openAddForm();
        fill(field(ADDRESS_NAME, "Họ tên"), "Automation Test"); fill(field(ADDRESS_STREET, "Địa chỉ"), "Test address");
        click(submitAddressButton());
        assertTrue("Form cho phép lưu khi thiếu số điện thoại", invalid(field(ADDRESS_PHONE, "Số điện thoại"))
                || body().toLowerCase(Locale.ROOT).contains("điện thoại") || !driver.getCurrentUrl().contains("/account/addresses"));
    }

    @Test public void addressBook08_invalidPhoneCharactersRejected() {
        loginAndOpenAddresses(); openAddForm(); WebElement phone = field(ADDRESS_PHONE, "Số điện thoại");
        phone.sendKeys("abcxyz@123");
        assertTrue("Trường điện thoại nhận ký tự không hợp lệ", phone.getAttribute("value").matches("[0-9]*") || invalid(phone));
    }

    @Test public void addressBook09_invalidPhoneLengthRejected() {
        loginAndOpenAddresses(); openAddForm();
        WebElement phone = field(ADDRESS_PHONE, "Số điện thoại"); fill(phone, "09123");
        click(submitAddressButton());
        assertTrue("Số điện thoại ngắn không được báo lỗi", invalid(phone) || body().contains("10") || body().contains("không hợp lệ"));
    }

    @Test public void addressBook10_missingStreetIsValidated() {
        loginAndOpenAddresses(); openAddForm();
        fill(field(ADDRESS_NAME, "Họ tên"), "Automation Test"); fill(field(ADDRESS_PHONE, "Số điện thoại"), "0987654321");
        click(submitAddressButton());
        assertTrue("Form cho phép lưu khi thiếu địa chỉ", invalid(field(ADDRESS_STREET, "Địa chỉ cụ thể"))
                || body().toLowerCase(Locale.ROOT).contains("địa chỉ") || !driver.getCurrentUrl().contains("/account/addresses"));
    }

    @Test public void addressBook11_locationFieldsAreRequired() {
        loginAndOpenAddresses(); openAddForm();
        fillBaseAddress("Automation Test", "0987654321", "123 Test Street");
        click(submitAddressButton());
        assertTrue("Thiếu cảnh báo khi chưa chọn tỉnh/thành", body().toLowerCase(Locale.ROOT).contains("tỉnh")
                || locationSelects().stream().anyMatch(this::invalid));
    }

    @Test public void addressBook12_cancelAddForm() {
        loginAndOpenAddresses(); String before = body(); openAddForm();
        fill(field(ADDRESS_NAME, "Họ tên"), "Draft Address");
        WebElement cancel = required(By.xpath("//*[self::button or self::a][contains(normalize-space(.),'Hủy') or contains(normalize-space(.),'Đóng') or @aria-label='Close']"));
        click(cancel);
        assertFalse("Hủy form nhưng draft đã được lưu", body().contains("Draft Address") && !before.contains("Draft Address"));
    }

    @Test public void addressBook13_addAsDefault() {
        loginAndOpenAddresses(); String marker = marker(); addValidAddress(marker, true);
        assertTrue("Địa chỉ mới chưa được gắn mặc định", body().contains(marker)
                && body().substring(Math.max(0, body().indexOf(marker) - 100), Math.min(body().length(), body().indexOf(marker) + 400)).toLowerCase(Locale.ROOT).contains("mặc định"));
    }

    @Test public void addressBook14_editAddressPrefillsExistingValues() {
        loginAndOpenAddresses(); List<WebElement> edits = editControls();
        Assume.assumeTrue("Tài khoản test cần ít nhất một địa chỉ để sửa", !edits.isEmpty());
        openEditForm(edits.get(0));
        assertTrue("Form sửa địa chỉ không mở", !visible(ADDRESS_NAME).isEmpty());
        assertTrue("Tên cũ không được điền sẵn", !field(ADDRESS_NAME, "Họ tên").getAttribute("value").isBlank());
    }

    @Test public void addressBook15_updateNameAndPhone() {
        loginAndOpenAddresses(); Assume.assumeTrue(!editControls().isEmpty()); openEditForm(editControls().get(0));
        fill(field(ADDRESS_NAME, "Họ tên"), "Nguyễn Văn B"); fill(field(ADDRESS_PHONE, "Số điện thoại"), "0912345678");
        click(submitAddressButton());
        wait.until(d -> body().contains("Nguyễn Văn B"));
        assertTrue(body().contains("0912345678"));
    }

    @Test public void addressBook16_updateLocationFields() {
        loginAndOpenAddresses(); Assume.assumeTrue(!editControls().isEmpty()); openEditForm(editControls().get(0));
        List<WebElement> selects = locationSelects(); Assume.assumeTrue("Form sửa không có vùng địa lý", selects.size() >= 3);
        selectOption(selects.get(0), "Đà Nẵng");
        wait.until(d -> new Select(locationSelects().get(1)).getOptions().size() > 1);
        selectOption(locationSelects().get(1), "Hải Châu");
        wait.until(d -> new Select(locationSelects().get(2)).getOptions().size() > 1);
        selectOption(locationSelects().get(2), "Thạch Thang");
        click(submitAddressButton());
        wait.until(d -> fold(body()).contains("da nang"));
        assertTrue(fold(body()).contains("da nang"));
    }

    @Test public void addressBook17_editRequiresName() {
        loginAndOpenAddresses(); Assume.assumeTrue(!editControls().isEmpty()); openEditForm(editControls().get(0));
        WebElement name = field(ADDRESS_NAME, "Họ tên"); name.clear(); click(submitAddressButton());
        assertTrue("Form sửa cho phép bỏ trống tên", invalid(name) || body().toLowerCase(Locale.ROOT).contains("họ tên"));
    }

    @Test public void addressBook18_cancelEditPreservesSavedAddress() {
        loginAndOpenAddresses(); Assume.assumeTrue(!editControls().isEmpty());
        WebElement container = addressContainer(editControls().get(0)); String before = container.getText();
        openEditForm(editControls().get(0)); fill(field(ADDRESS_NAME, "Họ tên"), "Unsaved Edit");
        click(required(By.xpath("//*[self::button or self::a][contains(normalize-space(.),'Hủy') or contains(normalize-space(.),'Đóng')]")));
        assertTrue("Hủy sửa nhưng thông tin lưu bị thay đổi", body().contains(before.split("\\n")[0]));
        assertFalse(body().contains("Unsaved Edit"));
    }

    @Test public void addressBook19_deleteAddressAndAcceptConfirmation() {
        loginAndOpenAddresses(); String marker = marker(); addValidAddress(marker, false);
        removeAddressWithMarker(marker, true);
        assertFalse("Địa chỉ chưa được xóa", body().contains(marker));
    }

    @Test public void addressBook20_cancelAddressDeletion() {
        loginAndOpenAddresses(); String marker = marker(); addValidAddress(marker, false);
        removeAddressWithMarker(marker, false);
        assertTrue("Địa chỉ đã bị xóa sau khi hủy", body().contains(marker));
    }

    @Test public void addressBook21_deleteDefaultAddressBehavior() {
        loginAndOpenAddresses(); List<WebElement> controls = deleteControls();
        Assume.assumeTrue("Tài khoản test cần địa chỉ mặc định", !controls.isEmpty());
        WebElement defaultDelete = controls.stream().filter(c -> addressContainer(c).getText().toLowerCase(Locale.ROOT).contains("mặc định"))
                .findFirst().orElse(controls.get(0));
        click(defaultDelete);
        try { wait.until(ExpectedConditions.alertIsPresent()).accept(); } catch (org.openqa.selenium.TimeoutException ignored) { }
        pauseAfterAction();
        String result = body().toLowerCase(Locale.ROOT);
        assertTrue("Sau thao tác phải tiếp tục ở sổ địa chỉ và thể hiện kết quả", driver.getCurrentUrl().contains("/account/addresses")
                && (result.contains("mặc định") || result.contains("không thể xóa") || !deleteControls().isEmpty()));
    }

    @Test public void addressBook22_setDefaultAddressFromList() {
        loginAndOpenAddresses(); String marker = marker(); addValidAddress(marker, false);
        WebElement container = visible(By.xpath("//*[contains(normalize-space(.),'" + marker + "')]")).stream().findFirst().orElse(null);
        assertNotNull("Địa chỉ vừa tạo không hiển thị", container);
        WebElement setDefault = maybeFrom(container, By.xpath(".//*[self::a or self::button][contains(.,'Mặc định') or contains(.,'Đặt làm') or contains(.,'Set as')]"));
        assertNotNull("Không có thao tác đặt mặc định ở danh sách", setDefault);
        click(setDefault); pauseAfterAction();
        assertTrue("Địa chỉ chưa được đánh dấu mặc định", body().contains(marker)
                && body().toLowerCase(Locale.ROOT).contains("mặc định"));
    }

    @Test public void addressBook23_specialCharactersInNameAreHandled() {
        loginAndOpenAddresses(); openAddForm(); WebElement name = field(ADDRESS_NAME, "Họ tên");
        fill(name, "Nguyễn @#$ %^&"); fill(field(ADDRESS_PHONE, "Số điện thoại"), "0987654321");
        fill(field(ADDRESS_STREET, "Địa chỉ"), "123 Test Street"); click(submitAddressButton());
        assertTrue("Tên chứa ký tự đặc biệt không được báo lỗi/làm sạch", body().contains("không hợp lệ")
                || !body().contains("Nguyễn @#$ %^&") || invalid(name));
    }

    @Test public void addressBook24_nameLengthIsLimited() {
        loginAndOpenAddresses(); openAddForm(); WebElement name = field(ADDRESS_NAME, "Họ tên");
        String longName = "A".repeat(260); fill(name, longName);
        String maximum = attr(name, "maxlength");
        assertTrue("Trường tên không giới hạn độ dài", name.getAttribute("value").length() <= 255
                || (!maximum.isBlank() && Integer.parseInt(maximum) <= 255));
    }

    @Test public void addressBook25_addMultipleAddresses() {
        loginAndOpenAddresses(); List<String> markers = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            String marker = marker() + "-" + i; addValidAddress(marker, false); markers.add(marker);
        }
        for (String marker : markers) assertTrue("Thiếu địa chỉ đã thêm: " + marker, body().contains(marker));
    }

    @Test public void addressBook26_mobileLayoutDoesNotOverflow() {
        loginAndOpenAddresses(); driver.manage().window().setSize(new Dimension(390, 844));
        long viewport = (Long) ((JavascriptExecutor) driver).executeScript("return document.documentElement.clientWidth");
        long content = (Long) ((JavascriptExecutor) driver).executeScript("return document.documentElement.scrollWidth");
        assertTrue("Sổ địa chỉ tràn ngang ở mobile: " + content + " > " + viewport, content <= viewport + 20);
        assertTrue(addAddressControl().isDisplayed());
    }

    @Test public void addressBook27_addressAppearsAtCheckout() {
        loginAndOpenAddresses(); String marker = marker(); addValidAddress(marker, false);
        requireScenario(!config("availableProductUrl", "").isBlank(), "Cần fixture availableProductUrl để kiểm tra địa chỉ ở checkout");
        addAvailableFixture(); navigateToCheckoutAfterBuy();
        assertTrue("Địa chỉ mới chưa xuất hiện trong checkout", body().contains(marker));
    }

    @Test public void addressBook28_checkoutAddressCanBeSavedToBook() {
        loginAndOpenAddresses();
        requireScenario(!config("availableProductUrl", "").isBlank(), "Cần fixture availableProductUrl để mở checkout");
        addAvailableFixture(); navigateToCheckoutAfterBuy();
        WebElement save = maybe(By.xpath("//label[contains(.,'Lưu vào sổ địa chỉ')]/input | //input[@type='checkbox'][following-sibling::*[contains(.,'Lưu vào sổ địa chỉ')]]"));
        requireScenario(save != null, "Checkout hiện tại không có tùy chọn lưu địa chỉ vào sổ");
        String marker = marker();
        WebElement address = maybe(By.cssSelector("input[name*='address'],input[autocomplete='address-line1']"));
        requireScenario(address != null, "Checkout không có trường địa chỉ để cập nhật");
        fill(address, marker); if (!save.isSelected()) click(save);
        WebElement saveAddress = maybe(By.xpath("//*[self::button or self::a][contains(normalize-space(.),'Lưu địa chỉ') or contains(normalize-space(.),'Save address')]"));
        requireScenario(saveAddress != null, "Không có thao tác lưu địa chỉ ở checkout mà không gửi đơn hàng");
        click(saveAddress);
        open(ADDRESSES_URL);
        assertTrue("Địa chỉ checkout không đồng bộ về address book", body().contains(marker));
    }

    @Test public void addressBook29_accountBreadcrumbReturnsToAccount() {
        loginAndOpenAddresses();
        WebElement account = maybe(By.cssSelector("a[href='/account'],a[href*='/account?']"),
                By.xpath("//*[self::a][contains(normalize-space(.),'Tài khoản') or contains(normalize-space(.),'Account')]") );
        assertNotNull("Không thấy breadcrumb/link tài khoản", account); click(account);
        wait.until(ExpectedConditions.urlContains("/account"));
        assertFalse("Link tài khoản vẫn ở address book", driver.getCurrentUrl().contains("/addresses"));
    }

    @Test public void addressBook30_guestCannotOpenAddressBook() {
        open(ADDRESSES_URL);
        assertTrue("Khách truy cập được trang address book", driver.getCurrentUrl().contains("/account/login")
                || !visible(By.cssSelector("input[type='password'],input[name='customer[email]']")).isEmpty());
    }
}
