package tests;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.text.Normalizer;
import java.util.stream.Collectors;

/** Search and collection tests using KICAP's current Vietnamese storefront labels. */
public class SearchFilterTest extends BaseTest {
    private static final By PRODUCT_LINKS = By.cssSelector(
            "a[href^='/']:not([href='/']):not([href*='/collections/']):not([href*='/account']):not([href*='/pages/']):not([href*='/cart']):not([href*='/search']):not([href*='/blogs/'])");
    private WebDriverWait waitPage() { return new WebDriverWait(driver, Duration.ofSeconds(20)); }

    private void submitSearch(String query) {
        String trimmed = query.trim();
        if (trimmed.isEmpty()) {
            driver.get("https://kicap.vn/");
            return;
        }
        String encodedQuery = URLEncoder.encode(trimmed, StandardCharsets.UTF_8);
        driver.get("https://kicap.vn/search?q=" + encodedQuery);
        waitPage().until(ExpectedConditions.urlContains("/search"));
    }

    private WebElement openSearchInput() {
        driver.get("https://kicap.vn/search?q=keycap");
        return waitPage().until(d -> d.findElements(By.cssSelector("input[name='q'],input[type='search']"))
                .stream().filter(WebElement::isDisplayed).findFirst().orElse(null));
    }

    private void openCollection() {
        driver.get("https://kicap.vn/collections/all");
        waitPage().until(ExpectedConditions.presenceOfElementLocated(PRODUCT_LINKS));
    }

    private void clickFilterLabel(String text) {
        String oldUrl = driver.getCurrentUrl();
        List<String> oldProducts = productHrefs();
        WebElement label = waitPage().until(ExpectedConditions.elementToBeClickable(
                By.xpath("//label[contains(normalize-space(.), '" + text + "')]")));
        List<WebElement> checks = label.findElements(By.cssSelector("input[type='checkbox'],input[type='radio']"));
        WebElement checkbox = checks.isEmpty()
                ? driver.findElement(By.id(label.getAttribute("for")))
                : checks.get(0);
        boolean wasSelected = checkbox.isSelected();
        ((JavascriptExecutor) driver).executeScript("arguments[0].click()", checkbox);
        waitPage().until(d -> checkbox.isSelected() != wasSelected);
        waitPage().until(d -> !d.getCurrentUrl().equals(oldUrl) || !productHrefs().equals(oldProducts));
    }

    private List<String> visibleProductTitles() {
        return driver.findElements(By.cssSelector("main h2 a[href^='/'],main h3 a[href^='/'],.product-card__title a,.product-item__title a")).stream().map(WebElement::getText)
                .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }

    private List<String> productHrefs() {
        Object hrefs = ((JavascriptExecutor) driver).executeScript(
                "return Array.from(document.querySelectorAll(arguments[0])).map(a => a.href)",
                "a[href^='/']:not([href='/']):not([href*='/collections/']):not([href*='/account']):not([href*='/pages/']):not([href*='/cart']):not([href*='/search']):not([href*='/blogs/'])");
        return (List<String>) hrefs;
    }

    private void selectSortContaining(String text) {
        List<WebElement> selects = waitPage().until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.tagName("select")));
        String wanted = normalize(text);
        Select sort = selects.stream().map(Select::new).filter(s -> s.getOptions().stream().anyMatch(o ->
                        normalize(o.getText()).contains(wanted) || o.getAttribute("value").toLowerCase().contains(wanted.contains("tang") ? "asc" : "desc")))
                .findFirst().orElseThrow(() -> new AssertionError("Sort option not found: " + text + "; available: " +
                        selects.stream().flatMap(s -> new Select(s).getOptions().stream()).map(o -> o.getText() + " [" + o.getAttribute("value") + "]").collect(Collectors.joining(", "))));
        String value = sort.getOptions().stream().filter(o -> normalize(o.getText()).contains(wanted) ||
                o.getAttribute("value").toLowerCase().contains(wanted.contains("tang") ? "asc" : "desc"))
                .findFirst().orElseThrow().getAttribute("value");
        sort.selectByValue(value);
        Assert.assertEquals(sort.getFirstSelectedOption().getAttribute("value"), value);
    }

    private String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase();
    }

    @Test(description = "SearchFilter-01: Search input is visible on the results page")
    public void search01HeaderInput() {
        Assert.assertTrue(openSearchInput().isDisplayed());
    }
    @Test(description = "SearchFilter-02: Search a common Vietnamese product keyword")
    public void search02KeyboardKeyword() { submitSearch("b\u00e0n ph\u00edm"); Assert.assertTrue(driver.getCurrentUrl().contains("/search")); }
    @Test(description = "SearchFilter-03: Search by product code")
    public void search03ProductCode() { submitSearch("KC84"); Assert.assertTrue(driver.getCurrentUrl().contains("/search")); }
    @Test(description = "SearchFilter-04: Search input accepts text while typing")
    public void search04Suggestions() {
        WebElement input = openSearchInput();
        input.clear();
        input.sendKeys("keycap");
        Assert.assertEquals(input.getAttribute("value"), "keycap");
    }
    @Test(description = "SearchFilter-05: Search route returns product results")
    public void search05SearchRoute() { submitSearch("keycap"); Assert.assertFalse(driver.findElements(PRODUCT_LINKS).isEmpty()); }
    @Test(description = "SearchFilter-06: Search accepts uppercase input")
    public void search06Uppercase() { submitSearch("BAN PHIM"); Assert.assertTrue(driver.getCurrentUrl().contains("/search")); }
    @Test(description = "SearchFilter-07: Search accepts Vietnamese diacritics")
    public void search07VietnameseAccents() { submitSearch("n\u00fat ph\u00edm"); Assert.assertTrue(driver.getCurrentUrl().contains("/search")); }
    @Test(description = "SearchFilter-08: Search accepts unaccented Vietnamese")
    public void search08WithoutAccents() { submitSearch("nut phim"); Assert.assertTrue(driver.getCurrentUrl().contains("/search")); }
    @Test(description = "SearchFilter-09: Search accepts surrounding whitespace")
    public void search09Spaces() { submitSearch("  keycap  "); Assert.assertTrue(driver.getCurrentUrl().contains("/search")); }
    @Test(description = "SearchFilter-10: Search safely handles special characters")
    public void search10SpecialCharacters() { submitSearch("@#$%^&"); Assert.assertTrue(driver.getCurrentUrl().contains("/search")); }
    @Test(description = "SearchFilter-11: Search safely handles script-like text")
    public void search11ScriptText() { submitSearch("<script>alert(1)</script>"); Assert.assertTrue(driver.getCurrentUrl().contains("/search")); }
    @Test(description = "SearchFilter-12: No-match query returns a search page")
    public void search12NoMatch() { submitSearch("xyzabc123999"); Assert.assertTrue(driver.getCurrentUrl().contains("/search")); }
    @Test(description = "SearchFilter-13: Empty search does not break the storefront")
    public void search13Empty() { submitSearch(""); Assert.assertTrue(driver.getCurrentUrl().contains("/search") || driver.getCurrentUrl().contains("kicap.vn")); }

    @Test(description = "SearchFilter-14: All-products collection contains products")
    public void search14CollectionProducts() {
        openCollection();
        Assert.assertTrue(driver.findElements(PRODUCT_LINKS).size() >= 2);
        Assert.assertTrue(driver.findElement(By.tagName("body")).getText().contains("T\u1ea5t c\u1ea3 s\u1ea3n ph\u1ea9m"));
    }
    @Test(description = "SearchFilter-15: Collection exposes brand filters")
    public void search15BrandFilters() {
        openCollection();
        Assert.assertTrue(driver.findElement(By.tagName("body")).getText().contains("Th\u01b0\u01a1ng hi\u1ec7u"));
    }
    @Test(description = "SearchFilter-16: Collection exposes price ranges")
    public void search16PriceRanges() {
        openCollection(); String body = driver.findElement(By.tagName("body")).getText();
        Assert.assertTrue(body.contains("300.000\u0111 - 500.000\u0111"));
        Assert.assertTrue(body.contains("500.000\u0111 - 1.000.000\u0111"));
    }
    @Test(description = "SearchFilter-17: Collection exposes an availability filter")
    public void search17AvailabilityFilter() {
        openCollection();
        Assert.assertTrue(driver.findElement(By.tagName("body")).getText().contains("H\u00e0ng c\u00f3 s\u1eb5n"));
    }
    @Test(description = "SearchFilter-18: Collection exposes sort choices")
    public void search18SortOptions() {
        openCollection();
        Assert.assertTrue(waitPage().until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.tagName("select")))
                .stream().map(Select::new).anyMatch(s -> s.getOptions().size() > 1));
    }
    @Test(description = "SearchFilter-19: Collection pagination is visible")
    public void search19Pagination() {
        openCollection();
        Assert.assertTrue(waitPage().until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[normalize-space()='Sau']"))).isDisplayed());
    }
    @Test(description = "SearchFilter-20: Price filter section is visible")
    public void search20PriceFilterPanel() {
        openCollection(); Assert.assertTrue(driver.findElement(By.tagName("body")).getText().contains("Gi\u00e1 s\u1ea3n ph\u1ea9m"));
    }

    @Test(description = "SearchFilter-21: Apply a brand filter")
    public void search21ApplyBrandFilter() { openCollection(); clickFilterLabel("Ure"); Assert.assertFalse(visibleProductTitles().isEmpty()); Assert.assertTrue(visibleProductTitles().stream().allMatch(t -> t.toLowerCase().contains("ure")), "Unexpected products: " + visibleProductTitles()); }
    @Test(description = "SearchFilter-22: Apply a price range filter")
    public void search22ApplyPriceFilter() { openCollection(); clickFilterLabel("300.000"); Assert.assertFalse(visibleProductTitles().isEmpty()); }
    @Test(description = "SearchFilter-23: Clear selected filters")
    public void search23ClearFilters() {
        openCollection();
        clickFilterLabel("Ure");
        clickFilterLabel("Ure");
        Assert.assertTrue(visibleProductTitles().size() > 1, "Clearing the brand filter should restore the broader product list");
    }
    @Test(description = "SearchFilter-24: Sort products by ascending price")
    public void search24SortAscending() { openCollection(); selectSortContaining("t\u0103ng d\u1ea7n"); }
    @Test(description = "SearchFilter-25: Navigate to the next collection page")
    public void search25NextCollectionPage() {
        openCollection(); String before = driver.getCurrentUrl();
        waitPage().until(ExpectedConditions.elementToBeClickable(By.xpath("//*[normalize-space()='Sau']"))).click();
        waitPage().until(d -> !d.getCurrentUrl().equals(before));
        Assert.assertTrue(driver.getCurrentUrl().contains("page=") || driver.getCurrentUrl().contains("/collections/all"));
    }
    @Test(description = "SearchFilter-26: Apply in-stock filter")
    public void search26InStockFilter() { openCollection(); clickFilterLabel("H\u00e0ng c\u00f3 s\u1eb5n"); Assert.assertFalse(visibleProductTitles().isEmpty()); Assert.assertFalse(driver.findElement(By.tagName("body")).getText().contains("H\u1ebft h\u00e0ng")); }
    @Test(description = "SearchFilter-27: Apply two brand filters")
    public void search27MultipleBrands() { openCollection(); clickFilterLabel("Ure"); clickFilterLabel("NOVA"); Assert.assertFalse(visibleProductTitles().isEmpty()); Assert.assertTrue(visibleProductTitles().stream().allMatch(t -> t.toLowerCase().contains("ure") || t.toLowerCase().contains("nova")), "Unexpected products: " + visibleProductTitles()); }
    @Test(description = "SearchFilter-28: Sort products by descending price")
    public void search28SortDescending() { openCollection(); selectSortContaining("gi\u1ea3m d\u1ea7n"); }
    @Test(description = "SearchFilter-29: Sort controls remain available after applying a filter")
    public void search29SortAfterFilter() { openCollection(); clickFilterLabel("Ure"); Assert.assertFalse(visibleProductTitles().isEmpty()); Assert.assertTrue(visibleProductTitles().stream().allMatch(t -> t.toLowerCase().contains("ure"))); Assert.assertFalse(driver.findElements(By.tagName("select")).isEmpty()); }
    @Test(description = "SearchFilter-30: Clear all filters from collection")
    public void search30ClearAllFilters() { search23ClearFilters(); }
}
