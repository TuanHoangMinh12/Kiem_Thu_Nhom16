import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.*;
import java.util.List;
import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HoangMinhTuan_ProductDetails_21130600_ASM8 extends HoangMinhTuan_KicapBaseTest_21130600_ASM8 {
    @Test public void TC_CTSP_01_MoTrangChiTiet() {
        open(BASE + "/ban-phim-co");
        WebElement link = required(By.xpath("//a[contains(@href,'/cidoo-qk75')]"));
        click(link);
        assertTrue(driver.getCurrentUrl().contains("cidoo-qk75"));
        assertFalse(title().isEmpty());
    }
    @Test public void TC_CTSP_02_TenSkuThuongHieu() {
        product(PRODUCT);
        assertTrue(title().toLowerCase().contains("cidoo"));
        assertFalse(sku().isEmpty());
        assertTrue("Phải hiển thị nhãn thương hiệu", java.util.regex.Pattern.compile("(?iu)thương\\s*hiệu\\s*:").matcher(body()).find());
    }
    @Test public void TC_CTSP_03_GiaGocGiaBanMucGiam() {
        product(fixture("saleProductUrl"));
        long discounted = money(price().getText());
        WebElement original = required(By.cssSelector(".compare-price, .old-price, .price-old, del, s"));
        long normal = money(original.getText());
        assertTrue("Giá sau giảm phải thấp hơn giá gốc", discounted < normal);
        assertTrue("Phải hiển thị giá trị giảm hoặc tiết kiệm", body().matches("(?s).*(Tiết kiệm|Giảm|%|-[0-9]+).*"));
    }
    @Test public void TC_CTSP_04_AnhDaiDien() {
        product(PRODUCT);
        WebElement img = mainImage();
        assertTrue("Ảnh chưa tải", (Boolean)((JavascriptExecutor)driver).executeScript("return arguments[0].complete && arguments[0].naturalWidth > 0", img));
        assertFalse("Ảnh không có src", attr(img,"src").isEmpty());
    }
    @Test public void TC_CTSP_05_ChuyenAnhThumbnail() {
        product(PRODUCT);
        List<WebElement> list=thumbs();
        requireScenario(list.size() >= 2, "Cần sản phẩm có ít nhất hai ảnh thu nhỏ");
        String before=attr(mainImage(),"src");
        // Thử một ảnh thu nhỏ KHÁC với ảnh chính; chọn phần tử thứ 2 chưa chắc đã khác.
        WebElement target = list.get(1);
        if (list.size() > 2 && attr(target, "src").equals(before)) target = list.get(2);
        click(target);
        try {
            wait.until(d -> !attr(mainImage(), "src").equals(before));
        } catch (org.openqa.selenium.TimeoutException e) {
            fail("Ảnh chính không thay đổi sau khi nhấn thumbnail. Cần kiểm tra locator/gallery hoặc lỗi website.");
        }
        WebElement img=mainImage();
        assertTrue("Ảnh chính phải tải được sau khi chọn thumbnail", (Boolean)((JavascriptExecutor)driver).executeScript("return arguments[0].complete && arguments[0].naturalWidth > 0", img));
        assertNotEquals("Nhấn thumbnail khác phải cập nhật ảnh chính",before,attr(img,"src"));
    }
    @Test public void TC_CTSP_06_PhongToAnh() {
        product(PRODUCT);
        click(mainImage());
        assertTrue("Không có lightbox hoặc ảnh phóng to", displayed(By.cssSelector(".fancybox-container, .pswp, .lightbox, [role='dialog'], .modal.show")));
        WebElement close=required(By.cssSelector(".fancybox-button--close, .pswp__button--close, .lightbox .close, [aria-label='Close']"));
        click(close);
        assertTrue("Trang chi tiết cần còn tồn tại", displayed(By.cssSelector("h1")));
    }
    @Test public void TC_CTSP_07_DanhSachPhienBan() {
        product(VARIANT);
        assertTrue("Sản phẩm phải có các lựa chọn phiên bản", options().size() >= 2);
    }
    @Test public void TC_CTSP_08_ChonPhienBanConHang() {
        product(fixture("availableVariantUrl"));
        String choice=chooseOption(0);
        assertTrue("Phải có lựa chọn hợp lệ", !choice.isEmpty() || !selectedOption().isEmpty());
        assertTrue("Phải có thể thêm bản còn hàng", availableToAdd());
    }
    @Test public void TC_CTSP_09_ThieuPhienBanBatBuoc() {
        product(fixture("requiredVariantUrl"));
        WebElement add=addButton();
        requireScenario(!options().isEmpty(), "Cần sản phẩm có thuộc tính bắt buộc và chưa mặc định chọn");
        requireScenario(!add.isEnabled() || body().contains("Chọn") || body().contains("chọn"), "Trang phải xác định thiếu phiên bản");
        if (add.isEnabled()) click(add);
        assertFalse("Không được báo thêm thành công khi thiếu phiên bản", body().contains("Thêm vào giỏ hàng thành công"));
    }
    @Test public void TC_CTSP_10_ChonBanHetHang() {
        product(fixture("soldOutVariantUrl"));
        requireScenario(!options().isEmpty(), "Cần biến thể hết hàng");
        chooseOption(options().size()-1);
        assertTrue("Không được cho mua khi hết hàng", body().contains("Hết hàng") || !availableToAdd());
    }
    @Test public void TC_CTSP_11_SoLuongMacDinh() {
        product(PRODUCT);
        assertEquals(1, qty());
        assertNotNull(plus()); assertNotNull(minus());
    }
    @Test public void TC_CTSP_12_TangSoLuong() {
        product(PRODUCT);
        int before=qty(); click(plus()); assertEquals(before+1,qty());
        click(plus()); assertEquals(before+2,qty());
    }
    @Test public void TC_CTSP_13_GiamSoLuong() {
        product(PRODUCT);
        fill(quantity(),"3"); assertEquals(3,qty());
        click(minus()); assertEquals(2,qty());
        click(minus()); assertEquals(1,qty());
    }
    @Test public void TC_CTSP_14_SoLuongKhongDuong() {
        product(PRODUCT);
        for(String value:new String[]{"0","-1"}) {
            fill(quantity(),value);
            String v=attr(quantity(),"value");
            assertTrue("Không được giữ giá trị sai "+value,
                    invalid(quantity()) || v.isEmpty() || (v.matches("\\d+") && Integer.parseInt(v)>=1));
        }
    }
    @Test public void TC_CTSP_15_SoLuongKyTuSai() {
        product(PRODUCT);
        for(String value:new String[]{"abc","@#","1.5"}) {
            fill(quantity(),value);
            String v=attr(quantity(),"value");
            assertTrue("Chỉ số nguyên dương hợp lệ: "+value,
                    invalid(quantity()) || v.isEmpty() || (v.matches("\\d+") && Integer.parseInt(v)>=1));
        }
    }
    @Test public void TC_CTSP_16_SoLuongVuotTonKho() {
        product(fixture("limitedStockUrl"));
        int stock=Integer.parseInt(config("limitedStockCount","-1"));
        requireScenario(stock>=1,"Cần -DlimitedStockCount là tồn kho đã đối chiếu");
        fill(quantity(),String.valueOf(stock+1));
        assertTrue("Phải chặn số lượng vượt tồn kho",invalid(quantity()) || qty()<=stock || !availableToAdd());
    }
    @Test public void TC_CTSP_17_ThemHangVaoGio() {
        addAvailableFixture();
        assertTrue("Phải hiển thị xác nhận thêm vào giỏ hoặc giỏ được cập nhật",
                body().contains("giỏ hàng") || driver.getCurrentUrl().contains("cart"));
    }
    @Test public void TC_CTSP_18_DoiChieuDuLieuGio() {
        product(fixture("availableProductUrl"));
        String name=title(), variant="";
        if(!options().isEmpty()) variant=chooseOption(0);
        requireScenario(availableToAdd(),"Sản phẩm phải có sẵn");
        String priceText=price().getText();
        addToCart(); goCart();
        assertTrue("Tên sản phẩm không có trong giỏ",body().contains(name));
        if(!variant.isEmpty()) assertTrue("Phiên bản trong giỏ không khớp",body().contains(variant));
        // Số lượng có thể nằm trong input, không nhất thiết có chữ "Số lượng".
        WebElement cartQty = maybe(By.cssSelector(".cart-item input[name*='quantity'], .cart-item input[type='number'], .cart-product input[type='number'], input[name='quantity'], input[name='updates[]']"));
        assertNotNull("Không định vị được ô số lượng trong giỏ; cần đối chiếu DOM giỏ hàng.",cartQty);
        assertEquals("Số lượng trong giỏ phải bằng 1", "1",attr(cartQty,"value"));
        assertTrue("Đơn giá phải được giữ",body().contains(priceText.trim()));
    }
    @Test public void TC_CTSP_19_DatTruoc() {
        product(fixture("preorderUrl"));
        WebElement preorder=required(By.xpath("//button[contains(.,'Đặt trước')]"));
        assertTrue("Nút Đặt trước phải khả dụng",preorder.isEnabled());
        assertTrue("Phải nêu tình trạng giao hàng",body().contains("dự kiến") || body().contains("Pre-order"));
        click(preorder);
        assertTrue("Đặt trước phải cập nhật giỏ/thông báo",body().contains("Thêm vào giỏ hàng thành công") ||
                body().contains("Bạn đã thêm") || driver.getCurrentUrl().contains("cart"));
    }
    @Test public void TC_CTSP_20_KhongMuaHangHet() {
        product(fixture("soldOutProductUrl"));
        assertTrue("Trạng thái phải hiển thị hết hàng",body().contains("Hết hàng"));
        assertFalse("Không được bật nút mua cho hàng hết",availableToAdd());
    }
    @Test public void TC_CTSP_21_MoTaThongSo() {
        product(PRODUCT);
        assertTrue("Thiếu mô tả sản phẩm",body().contains("Thông số") || body().contains("Thông tin sản phẩm"));
        assertTrue("Mô tả phải có nội dung",body().length()>500);
    }
    @Test public void TC_CTSP_22_LienKetChinhSach() {
        product(PRODUCT);
        WebElement policy=required(By.xpath("//a[contains(.,'Chính sách bảo hành')]"));
        click(policy);
        if(driver.getWindowHandles().size()>1) {
            for(String tab:driver.getWindowHandles()) driver.switchTo().window(tab);
        }
        assertTrue("Trang bảo hành phải mở đúng",driver.getCurrentUrl().contains("bao-hanh") || body().contains("bảo hành"));
        product(PRODUCT);
        WebElement delivery=required(By.xpath("//a[contains(.,'vận chuyển') or contains(.,'FREESHIP')]") );
        click(delivery);
        assertTrue("Phải đến trang hướng dẫn vận chuyển",driver.getCurrentUrl().contains("van-chuyen") || body().toLowerCase().contains("vận chuyển"));
    }
    @Test public void TC_CTSP_23_SoundProfile() {
        product(fixture("soundProfileProductUrl"));
        WebElement play=maybe(By.cssSelector("button[aria-label*='Play'], button[aria-label*='Phát'], .sound-profile button"));
        requireScenario(play!=null,"Sản phẩm cần bản Sound Profile hoạt động");
        click(play);
        List<WebElement> audios=driver.findElements(By.cssSelector("audio"));
        WebElement audio=audios.isEmpty()?null:audios.get(0);
        requireScenario(audio!=null,"Cần thẻ audio để đo trạng thái phát/dừng bằng Selenium");
        Object paused=((JavascriptExecutor)driver).executeScript("return arguments[0].paused",audio);
        assertEquals("Phải phát được âm thanh",Boolean.FALSE,paused);
        ((JavascriptExecutor)driver).executeScript("arguments[0].pause()",audio);
        assertEquals(Boolean.TRUE,((JavascriptExecutor)driver).executeScript("return arguments[0].paused",audio));
    }
    @Test public void TC_CTSP_24_VietDanhGia() {
        product(PRODUCT);
        // Nội dung trang có thể là "Viết đánh giá của bạn" trong một thẻ không phải button/a.
        WebElement trigger = required(
                By.xpath("//button[contains(.,'Viết đánh giá')] | //a[contains(.,'Viết đánh giá')] | //*[@role='button' and contains(.,'Viết đánh giá')]"),
                By.xpath("//*[contains(text(),'Viết đánh giá của bạn')]"));
        click(trigger);
        wait.until(d -> displayed(By.cssSelector("textarea, .review-form input, [class*='review'] textarea, [role='dialog'] input")));
        assertTrue("Form đánh giá phải có ô nhập",displayed(By.cssSelector("textarea, .review-form input, [role='dialog'] input")));
    }
    @Test public void TC_CTSP_25_GiaoDienMobile() {
        driver.manage().window().setSize(new Dimension(375,667));
        product(PRODUCT);
        Number scrollWidth=(Number)((JavascriptExecutor)driver).executeScript("return document.documentElement.scrollWidth");
        Number clientWidth=(Number)((JavascriptExecutor)driver).executeScript("return document.documentElement.clientWidth");
        assertTrue("Trang bị tràn ngang ở mobile",scrollWidth.intValue()<=clientWidth.intValue()+2);
        assertTrue("Ảnh sản phẩm phải hiển thị",mainImage().isDisplayed());
        assertNotNull("Phải có giá bán",price());
        assertTrue("Phải thấy công cụ mua",displayed(By.xpath("//button[contains(.,'Đặt trước') or contains(.,'Thêm vào giỏ')]")));
    }
}
