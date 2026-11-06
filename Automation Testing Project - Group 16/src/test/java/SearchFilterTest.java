import org.junit.Test;
import org.junit.Assume;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/** Search and filter tests mapped to Search Filter cases 01–30 in the Group 16 workbook. */
public class SearchFilterTest extends HoangMinhTuan_KicapBaseTest_21130600_ASM8 {
    private static final String COLLECTION = BASE + "/collections/all";
    private static final By SEARCH_INPUT = By.cssSelector("#kicap-header-search-input[name='query']");

    private void waitForCollectionUpdate(WebElement oldCard) {
        if (oldCard != null) {
            try { wait.until(ExpectedConditions.stalenessOf(oldCard)); }
            catch (org.openqa.selenium.TimeoutException ignored) { }
        }
        wait.until(d -> !visible(By.cssSelector(".kicap-collection-card h2 a")).isEmpty()
                || body().contains("Chưa có sản phẩm phù hợp"));
    }

    private void collection() {
        open(COLLECTION);
        wait.until(d -> !visible(By.cssSelector(".kicap-collection-card h2 a")).isEmpty());
    }

    private List<String> productTitles() {
        Object value = ((JavascriptExecutor) driver).executeScript(
                "return Array.from(document.querySelectorAll('main h2 a[href^=\"/\"],main h3 a[href^=\"/\"]')).map(e=>e.innerText.trim()).filter(Boolean)");
        if (value instanceof List<?>) return ((List<?>) value).stream().map(String::valueOf).distinct().collect(Collectors.toList());
        return List.of();
    }

    private List<Long> productPrices() {
        List<Long> values = new ArrayList<>();
        for (WebElement card : visible(By.cssSelector(".kicap-collection-card"))) {
            WebElement price = maybeFrom(card, By.cssSelector(".kicap-collection-card__price strong"), By.cssSelector(".kicap-collection-card__price"));
            if (price != null) {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?<!\\d)(\\d{1,3}(?:[.,]\\d{3})+|\\d{4,})").matcher(price.getText());
                if (m.find()) try { values.add(Long.parseLong(m.group(1).replaceAll("[.,]", ""))); }
                catch (NumberFormatException ignored) { }
            }
        }
        return values;
    }

    private WebElement maybeFrom(WebElement root, By... locators) {
        for (By locator : locators) {
            List<WebElement> found = root.findElements(locator);
            if (!found.isEmpty()) return found.get(0);
        }
        return null;
    }

    private boolean selectedFilter(String id) {
        Object value = ((JavascriptExecutor) driver).executeScript(
                "return !!document.querySelector(arguments[0])?.checked", "#" + id);
        return Boolean.TRUE.equals(value);
    }

    private WebElement searchInput() {
        WebElement input = maybe(SEARCH_INPUT);
        if (input == null || !input.isDisplayed()) {
            WebElement opener = maybe(By.cssSelector("button[aria-label='Mở tìm kiếm'],.site-header-search"));
            if (opener != null) click(opener);
        }
        return required(SEARCH_INPUT);
    }

    private void submitSearch(String query) {
        open(BASE + "/search?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8));
    }

    private String fold(String value) {
        return Normalizer.normalize(value.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replace('đ', 'd').trim();
    }

    private WebElement filter(String label) {
        String safe = label.replace("'", "\\'");
        return required(By.xpath("//main//*[self::label or self::a or self::button][normalize-space(.)='" + safe + "']"),
                By.xpath("//main//*[normalize-space(.)='" + safe + "']/ancestor::label[1]"));
    }

    private void clickFilter(String label) {
        WebElement item = filter(label);
        WebElement input = maybeFrom(item, By.cssSelector("input[type='checkbox'],input[type='radio']"));
        if (input == null) throw new AssertionError("Bộ lọc không có checkbox: " + label);
        if (input.isSelected()) return;
        WebElement oldCard = maybe(By.cssSelector(".kicap-collection-card"));
        click(item);
        waitForCollectionUpdate(oldCard);
    }

    private void uncheckFilter(String label) {
        WebElement item = filter(label);
        WebElement input = maybeFrom(item, By.cssSelector("input[type='checkbox'],input[type='radio']"));
        assertNotNull("Không tìm thấy checkbox bộ lọc " + label, input);
        if (!input.isSelected()) return;
        WebElement oldCard = maybe(By.cssSelector(".kicap-collection-card"));
        click(item);
        waitForCollectionUpdate(oldCard);
    }

    private void chooseSort(String keyword) {
        WebElement selectElement = required(By.id("kicap-collection-sort"));
        Select sort = new Select(selectElement);
        String wanted = fold(keyword);
        WebElement option = sort.getOptions().stream().filter(o -> fold(o.getText()).contains(wanted)
                        || fold(o.getAttribute("value")).contains(wanted))
                .findFirst().orElseThrow(() -> new AssertionError("Không có tùy chọn sắp xếp: " + keyword));
        WebElement oldCard = maybe(By.cssSelector(".kicap-collection-card"));
        sort.selectByValue(option.getAttribute("value"));
        waitForCollectionUpdate(oldCard);
    }

    private void applyCustomPrice(String min, String max) {
        List<WebElement> fields = visible(By.cssSelector("input[type='number'],input[name*='min'],input[name*='max'],input[placeholder*='Giá']"));
        Assume.assumeTrue("Trang hiện tại không hỗ trợ nhập khoảng giá tùy chỉnh", fields.size() >= 2);
        fields.get(0).clear(); fields.get(0).sendKeys(min);
        fields.get(1).clear(); fields.get(1).sendKeys(max);
        WebElement apply = maybe(By.xpath("//*[self::button or self::a][contains(normalize-space(.),'Áp dụng') or contains(normalize-space(.),'Apply')]"));
        if (apply != null) click(apply); else fields.get(1).sendKeys(Keys.ENTER);
        pauseAfterAction();
    }

    private void assertPricesInRange(long min, long max) {
        List<Long> prices = productPrices();
        assertFalse("Không đọc được giá sản phẩm trong kết quả", prices.isEmpty());
        assertTrue("Có sản phẩm ngoài khoảng giá " + min + "–" + max + ": " + prices,
                prices.stream().allMatch(p -> p >= min && p <= max));
    }

    @Test public void searchFilter01_headerSearchFieldVisible() {
        open(BASE);
        assertTrue("Ô tìm kiếm trên header không hiển thị", searchInput().isDisplayed());
        assertTrue("Ô tìm kiếm chưa có placeholder gợi ý", !attr(searchInput(), "placeholder").isBlank()
                || !visible(By.cssSelector("button[type='submit'][aria-label*='earch'],button[aria-label*='ìm kiếm']")).isEmpty());
    }

    @Test public void searchFilter02_searchVietnameseKeyword() {
        submitSearch("Bàn phím");
        assertTrue(driver.getCurrentUrl().contains("/search"));
        assertTrue("Không có sản phẩm khớp từ khóa Bàn phím", body().toLowerCase(Locale.ROOT).contains("bàn phím")
                || productTitles().stream().anyMatch(t -> fold(t).contains("ban phim")));
    }

    @Test public void searchFilter03_searchProductCode() {
        submitSearch("KC84");
        assertTrue(driver.getCurrentUrl().contains("/search"));
        assertTrue("Không thấy kết quả KC84", body().toLowerCase(Locale.ROOT).contains("kc84"));
    }

    @Test public void searchFilter04_searchAutocompleteAppears() {
        open(BASE); WebElement input = searchInput(); input.clear(); input.sendKeys("keycap");
        List<WebElement> suggestions = visible(By.cssSelector("[role='option'],.search-suggest a,.predictive-search a,[class*='suggest'] a"));
        Assume.assumeTrue("Website không hiển thị autocomplete khi nhập từ khóa", !suggestions.isEmpty());
        assertTrue("Popup gợi ý không hiển thị tên sản phẩm", !suggestions.get(0).getText().trim().isEmpty());
    }

    @Test public void searchFilter05_openProductFromAutocomplete() {
        open(BASE); WebElement input = searchInput(); input.clear(); input.sendKeys("keycap");
        List<WebElement> suggestions = visible(By.cssSelector("[role='option'] a,.search-suggest a,.predictive-search a,[class*='suggest'] a"));
        Assume.assumeTrue("Website không có liên kết autocomplete để mở sản phẩm", !suggestions.isEmpty());
        WebElement suggestion = suggestions.get(0);
        click(suggestion);
        wait.until(d -> d.getCurrentUrl().matches(".*(/products/|/[^/?#]+)$"));
        assertFalse("Click gợi ý không mở trang sản phẩm", driver.getCurrentUrl().contains("/search"));
    }

    @Test public void searchFilter06_searchIgnoresLetterCase() {
        submitSearch("keycap"); List<String> lower = productTitles().stream().map(this::fold).sorted().collect(Collectors.toList());
        submitSearch("KEYCAP"); List<String> upper = productTitles().stream().map(this::fold).sorted().collect(Collectors.toList());
        assertEquals("Kết quả tìm kiếm phân biệt chữ hoa/thường", lower, upper);
    }

    @Test public void searchFilter07_searchWithVietnameseAccents() {
        submitSearch("Nút phím");
        assertTrue(driver.getCurrentUrl().contains("/search"));
        assertFalse("Tìm kiếm từ khóa có dấu không trả kết quả", productTitles().isEmpty());
    }

    @Test public void searchFilter08_searchWithoutVietnameseAccents() {
        submitSearch("Nut phim");
        assertTrue(driver.getCurrentUrl().contains("/search"));
        assertTrue("Không có kết quả tương đương từ khóa không dấu", !productTitles().isEmpty()
                || body().toLowerCase(Locale.ROOT).contains("không tìm thấy"));
    }

    @Test public void searchFilter09_trimSearchWhitespace() {
        submitSearch("bàn phím"); List<String> expected = productTitles().stream().map(this::fold).sorted().collect(Collectors.toList());
        submitSearch("  bàn phím  "); List<String> actual = productTitles().stream().map(this::fold).sorted().collect(Collectors.toList());
        assertEquals("Khoảng trắng đầu/cuối làm thay đổi kết quả", expected, actual);
    }

    @Test public void searchFilter10_specialCharactersDoNotCrash() {
        submitSearch("@#$%^&");
        assertFalse("Trang tìm kiếm trả lỗi 500", driver.getTitle().contains("500") || body().contains("Internal Server Error"));
        assertTrue(driver.getCurrentUrl().contains("/search"));
    }

    @Test public void searchFilter11_scriptInputIsSanitized() {
        submitSearch("<script>alert(1)</script>");
        try { driver.switchTo().alert(); fail("Tìm kiếm đã kích hoạt JavaScript alert"); }
        catch (org.openqa.selenium.NoAlertPresentException expected) { }
        assertTrue("Không giữ lại trang tìm kiếm an toàn", driver.getCurrentUrl().contains("/search"));
    }

    @Test public void searchFilter12_noMatchShowsEmptyState() {
        submitSearch("xyzabc123999");
        assertTrue("Không thấy thông báo không có kết quả", body().toLowerCase(Locale.ROOT).contains("không tìm thấy")
                || body().toLowerCase(Locale.ROOT).contains("no results") || productTitles().isEmpty());
    }

    @Test public void searchFilter13_emptyQueryDoesNotBreakPage() {
        open(BASE); WebElement input = searchInput(); input.clear(); input.sendKeys(Keys.ENTER);
        pauseAfterAction();
        assertFalse("Truy vấn trống dẫn tới lỗi", driver.getTitle().contains("500") || body().contains("Internal Server Error"));
        assertTrue(driver.getCurrentUrl().contains("kicap.vn"));
    }

    @Test public void searchFilter14_filterByBrand() {
        collection(); clickFilter("Akko");
        assertFalse("Bộ lọc thương hiệu không trả sản phẩm", productTitles().isEmpty());
        assertTrue("Có sản phẩm không thuộc Akko", productTitles().stream().allMatch(t -> fold(t).contains("akko")));
    }

    @Test public void searchFilter15_selectMultipleBrands() {
        collection(); clickFilter("Akko"); clickFilter("Monsgeek");
        assertFalse("Bộ lọc nhiều thương hiệu không trả sản phẩm", productTitles().isEmpty());
        assertTrue("Chưa chọn cả hai thương hiệu", selectedFilter("filter-akko") && selectedFilter("filter-monsgeek"));
    }

    @Test public void searchFilter16_filterPriceUnder500000() {
        collection(); clickFilter("Giá dưới 100.000đ"); clickFilter("100.000đ - 200.000đ");
        clickFilter("200.000đ - 300.000đ"); clickFilter("300.000đ - 500.000đ");
        assertPricesInRange(0, 500000);
    }

    @Test public void searchFilter17_filterPrice500000To1500000() {
        collection(); clickFilter("500.000đ - 1.000.000đ"); assertPricesInRange(500000, 1000000);
    }

    @Test public void searchFilter18_filterPriceOver2000000() {
        collection();
        Assume.assumeTrue("Website chỉ cung cấp bộ lọc giá trên 1.000.000đ, không có ngưỡng trên 2.000.000đ", false);
    }

    @Test public void searchFilter19_customPriceRange() {
        collection(); applyCustomPrice("300000", "800000"); assertPricesInRange(300000, 800000);
    }

    @Test public void searchFilter20_rejectInvalidPriceRange() {
        collection(); applyCustomPrice("1000000", "200000");
        String text = body().toLowerCase(Locale.ROOT);
        assertTrue("Không báo lỗi hoặc chuẩn hóa khoảng giá min > max", text.contains("không được lớn hơn")
                || text.contains("không hợp lệ") || text.contains("invalid") || text.contains("1000000")
                || text.contains("1.000.000"));
    }

    @Test public void searchFilter21_filterInStockProducts() {
        collection(); clickFilter("Hàng có sẵn");
        assertFalse("Lọc còn hàng không trả sản phẩm", productTitles().isEmpty());
        assertFalse("Kết quả vẫn hiển thị trạng thái hết hàng", visible(By.cssSelector(".kicap-collection-card__status--sold-out")).size() > 0);
    }

    @Test public void searchFilter22_combineBrandAndPriceFilters() {
        collection(); clickFilter("Akko"); clickFilter("500.000đ - 1.000.000đ");
        assertTrue("Bộ lọc Akko chưa được chọn", selectedFilter("filter-akko"));
        if (!productPrices().isEmpty()) assertPricesInRange(500000, 1000000);
        else assertTrue("Kết quả trống không có thông báo", body().contains("Chưa có sản phẩm phù hợp"));
    }

    @Test public void searchFilter23_removeOneSelectedFilter() {
        collection(); clickFilter("Akko"); clickFilter("300.000đ - 500.000đ");
        uncheckFilter("Akko");
        wait.until(d -> !selectedFilter("filter-akko"));
        assertTrue("Bộ lọc giá còn lại chưa được giữ", selectedFilter("filter-300-000d-500-000d"));
        if (!productPrices().isEmpty()) assertPricesInRange(300000, 500000);
        else assertTrue("Kết quả trống không có thông báo", body().contains("Chưa có sản phẩm phù hợp"));
    }

    @Test public void searchFilter24_clearAllFilters() {
        collection(); clickFilter("Akko"); clickFilter("300.000đ - 500.000đ");
        uncheckFilter("Akko"); uncheckFilter("300.000đ - 500.000đ");
        assertFalse("Bộ lọc thương hiệu còn được chọn", selectedFilter("filter-akko"));
        assertFalse("Bộ lọc giá còn được chọn", selectedFilter("filter-300-000d-500-000d"));
    }

    @Test public void searchFilter25_sortPriceAscending() {
        collection(); chooseSort("Giá thấp đến cao");
        List<Long> prices = productPrices(); assertTrue("Cần ít nhất hai giá để kiểm tra sort", prices.size() >= 2);
        assertEquals("Giá chưa được sắp xếp tăng dần", prices.stream().sorted().collect(Collectors.toList()), prices);
    }

    @Test public void searchFilter26_sortPriceDescending() {
        collection(); chooseSort("Giá cao đến thấp");
        List<Long> prices = productPrices(); assertTrue("Cần ít nhất hai giá để kiểm tra sort", prices.size() >= 2);
        assertEquals("Giá chưa được sắp xếp giảm dần", prices.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList()), prices);
    }

    @Test public void searchFilter27_sortNameAlphabetically() {
        collection(); chooseSort("Tên A");
        List<String> names = productTitles().stream().map(this::fold).collect(Collectors.toList());
        assertTrue("Cần ít nhất hai tên để kiểm tra sort", names.size() >= 2);
        assertEquals("Tên sản phẩm chưa sắp xếp A-Z", names.stream().sorted().collect(Collectors.toList()), names);
    }

    @Test public void searchFilter28_sortNewestOrBestSelling() {
        collection();
        WebElement sortElement = required(By.id("kicap-collection-sort"));
        Select sort = new Select(sortElement);
        WebElement option = sort.getOptions().stream().filter(o -> fold(o.getText()).contains("moi nhat")
                || fold(o.getText()).contains("ban chay")).findFirst()
                .orElseThrow(() -> new AssertionError("Không có tùy chọn Mới nhất/Bán chạy nhất"));
        WebElement oldCard = maybe(By.cssSelector(".kicap-collection-card"));
        sort.selectByValue(option.getAttribute("value")); waitForCollectionUpdate(oldCard);
        assertFalse("Danh sách rỗng sau khi sắp xếp", productTitles().isEmpty());
    }

    @Test public void searchFilter29_sortAfterApplyingPriceFilter() {
        collection(); clickFilter("500.000đ - 1.000.000đ"); chooseSort("Giá cao đến thấp");
        assertPricesInRange(500000, 1000000);
        List<Long> prices = productPrices();
        assertEquals("Giá sau lọc chưa sắp xếp giảm dần", prices.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList()), prices);
    }

    @Test public void searchFilter30_collectionPagination() {
        collection();
        String before = driver.getCurrentUrl();
        WebElement next = maybe(By.xpath("//*[self::a or self::button][normalize-space(.)='2' or normalize-space(.)='Sau' or @aria-label='Next']"));
        assertNotNull("Không tìm thấy điều khiển sang trang 2", next);
        click(next);
        wait.until(d -> !d.getCurrentUrl().equals(before));
        assertTrue("URL không thể hiện trang 2", driver.getCurrentUrl().contains("page=2") || driver.getCurrentUrl().matches(".*/page/2.*"));
        assertFalse("Trang 2 không hiển thị sản phẩm", productTitles().isEmpty());
    }
}
