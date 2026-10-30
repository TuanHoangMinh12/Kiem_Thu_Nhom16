package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.AddressBookPage;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;

public class AddressBookTest extends BaseTest {
    private AddressBookPage openAuthenticatedAddressBook() {
        AddressBookPage page = new AddressBookPage(driver);
        page.loginPrecondition(credential("KICAP_EMAIL", "kicap.email"), credential("KICAP_PASSWORD", "kicap.password"));
        page.openAddressBook();
        Assert.assertTrue(page.isAddressBookDisplayed(), "Could not open the authenticated address book.");
        return page;
    }

    @Test(description = "Customer can open their KICAP address book")
    public void customerCanOpenAddressBook() {
        String email = credential("KICAP_EMAIL", "kicap.email");
        String password = credential("KICAP_PASSWORD", "kicap.password");
        AddressBookPage addressBook = new AddressBookPage(driver);
        addressBook.loginPrecondition(email, password);
        addressBook.openAddressBook();

        Assert.assertFalse(addressBook.isLoginPageDisplayed(),
                "KICAP stayed on the sign-in page. Check the credentials or whether the site requires an additional verification step.");
        Assert.assertTrue(addressBook.isAddressBookDisplayed(),
                "Expected the authenticated account address page. Actual URL: " + driver.getCurrentUrl());
    }

    @Test(description = "AddressBook-02: Add address form opens")
    public void addAddressFormOpens() {
        AddressBookPage page = openAuthenticatedAddressBook();
        Assert.assertTrue(page.isAddAddressBtnDisplayed(), "Add address control is missing.");
        page.clickAddNewAddress();
        Assert.assertTrue(page.isAddressFormDisplayed(), "Address form did not open.");
    }

    @Test(description = "AddressBook-03: Address form contains editable fields")
    public void addressFormHasFields() {
        AddressBookPage page = openAuthenticatedAddressBook();
        page.clickAddNewAddress();
        Assert.assertTrue(page.isAddressFormDisplayed(), "Name and phone inputs were not found.");
        Assert.assertTrue(page.hasBasicAddressFields(), "Expected name, phone, company and street fields.");
        Assert.assertTrue(page.visibleAddressFormFieldCount() >= 4);
    }

    @Test(description = "AddressBook: Form values enter the matching visible fields")
    public void addressFormValuesUseCorrectFields() {
        AddressBookPage page = openAuthenticatedAddressBook();
        page.clickAddNewAddress();
        page.fillBasicAddressFields("Automation Test", "0900000000", "KICAP QA", "123 Test Street");
        Assert.assertEquals(driver.findElement(By.cssSelector("input[placeholder='H\u1ecd t\u00ean']")).getAttribute("value"), "Automation Test");
        Assert.assertEquals(driver.findElement(By.cssSelector("input[placeholder='S\u1ed1 \u0111i\u1ec7n tho\u1ea1i']")).getAttribute("value"), "0900000000");
        Assert.assertEquals(driver.findElement(By.cssSelector("input[placeholder='C\u00f4ng ty']")).getAttribute("value"), "KICAP QA");
        Assert.assertEquals(driver.findElement(By.cssSelector("input[placeholder='\u0110\u1ecba ch\u1ec9'], textarea[placeholder='\u0110\u1ecba ch\u1ec9']")).getAttribute("value"), "123 Test Street");
    }

    @Test(description = "AddressBook-03: Add a valid address and verify it appears in the address list")
    public void addValidAddress() {
        AddressBookPage page = openAuthenticatedAddressBook();
        String marker = page.addUniqueTestAddress();
        Assert.assertTrue(driver.findElement(By.tagName("body")).getText().contains(marker),
                "The newly saved test address was not shown in the address list.");
    }

    @Test(description = "AddressBook-04: Address form exposes location selectors")
    public void addressFormLocationSelectors() {
        AddressBookPage page = openAuthenticatedAddressBook();
        page.clickAddNewAddress();
        Assert.assertTrue(page.hasLocationLabels(), "Expected province, district and ward selectors in the form.");
    }

    @Test(description = "AddressBook-05: Address book remains usable at mobile viewport")
    public void addressBookMobileViewport() {
        AddressBookPage page = openAuthenticatedAddressBook();
        driver.manage().window().setSize(new Dimension(390, 844));
        long viewportWidth = (Long) ((JavascriptExecutor) driver).executeScript("return document.documentElement.clientWidth");
        long contentWidth = (Long) ((JavascriptExecutor) driver).executeScript("return document.documentElement.scrollWidth");
        Assert.assertTrue(page.isAddressBookDisplayed());
        Assert.assertTrue(contentWidth <= viewportWidth + 20,
                "Address page overflows mobile viewport: content=" + contentWidth + ", viewport=" + viewportWidth);
    }

    @Test(description = "AddressBook-30: Guest cannot access another customer's address book")
    public void guestAddressBookAccessIsBlocked() {
        driver.get("https://kicap.vn/account/addresses");
        Assert.assertTrue(driver.getCurrentUrl().contains("/account/login") ||
                        !driver.findElements(By.cssSelector("input[type='password'],input[name='customer[email]']")).isEmpty(),
                "Unauthenticated visitor should be redirected to sign in.");
    }

    @Test(description = "AddressBook-06: Add-address form shows a required name field")
    public void addressFormNameField() {
        AddressBookPage page = openAuthenticatedAddressBook(); page.clickAddNewAddress();
        Assert.assertFalse(driver.findElements(By.cssSelector("input[name*='full_name'],input[name*='name']")).isEmpty());
    }

    @Test(description = "AddressBook-07: Add-address form shows a phone field")
    public void addressFormPhoneField() {
        AddressBookPage page = openAuthenticatedAddressBook(); page.clickAddNewAddress();
        Assert.assertFalse(driver.findElements(By.cssSelector("input[name*='phone'],input[type='tel']")).isEmpty());
    }

    @Test(description = "AddressBook-08: Add-address form shows a street-address field")
    public void addressFormStreetField() {
        AddressBookPage page = openAuthenticatedAddressBook(); page.clickAddNewAddress();
        Assert.assertTrue(page.hasBasicAddressFields(), "Street address field was not found by its visible placeholder.");
    }

    @Test(description = "AddressBook-09: Add-address form exposes dependent location selects")
    public void addressFormLocationSelects() {
        AddressBookPage page = openAuthenticatedAddressBook(); page.clickAddNewAddress();
        Assert.assertTrue(driver.findElements(By.cssSelector("select")).size() >= 1,
                "Expected province or city selector in address form.");
    }

    @Test(description = "AddressBook-10: Add-address form has a default-address option")
    public void addressFormDefaultCheckbox() {
        AddressBookPage page = openAuthenticatedAddressBook(); page.clickAddNewAddress();
        Assert.assertFalse(driver.findElements(By.cssSelector("input[type='checkbox']")).isEmpty(),
                "Default-address checkbox was not found.");
    }

    @Test(description = "AddressBook-11: Existing addresses expose edit controls")
    public void existingAddressEditControls() {
        openAuthenticatedAddressBook();
        Assert.assertFalse(driver.findElements(By.xpath("//*[self::a or self::button][contains(.,'Sửa') or contains(.,'Chỉnh sửa')]")).isEmpty(),
                "No existing address edit control found; account needs at least one saved address.");
    }

    @Test(description = "AddressBook-12: Existing addresses expose remove controls")
    public void existingAddressRemoveControls() {
        openAuthenticatedAddressBook();
        Assert.assertFalse(driver.findElements(By.xpath("//*[self::a or self::button][contains(.,'Xóa')]")).isEmpty(),
                "No existing address remove control found; account needs a saved address.");
    }

    @Test(description = "AddressBook-13: Address page contains the account address list")
    public void addressListVisible() {
        AddressBookPage page = openAuthenticatedAddressBook();
        Assert.assertTrue(page.isAddressBookDisplayed());
        Assert.assertTrue(driver.findElement(By.tagName("body")).getText().length() > 20);
    }

    private String credential(String environmentName, String propertyName) {
        String value = System.getProperty(propertyName);
        if (value == null || value.isBlank()) value = System.getenv(environmentName);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Set " + environmentName + " (or -D" + propertyName + ") before running this account test.");
        }
        return value;
    }
}
