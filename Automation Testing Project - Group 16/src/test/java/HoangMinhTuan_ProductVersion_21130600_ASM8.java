import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.*;
import java.util.List;
import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HoangMinhTuan_ProductVersion_21130600_ASM8 extends HoangMinhTuan_KicapBaseTest_21130600_ASM8 {
    private void openVariant() { product(VARIANT); }
    private String choose(String key, int fallback) {
        return chooseOption(Integer.parseInt(config(key, String.valueOf(fallback))));
    }
    @Test public void TC_PBSP_01_HienThiCacNhom() {
        product(fixture("multiGroupProductUrl"));
        List<WebElement> groups=visible(By.cssSelector(".swatch, fieldset, .variant-group, [class*='variant-group'], [data-option-index]"));
        assertTrue("Cần hiển thị ít nhất hai nhóm thuộc tính",groups.size()>=2);
    }
    @Test public void TC_PBSP_02_HienThiCacGiaTri() {
        openVariant();
        assertTrue("Phải hiển thị nhiều lựa chọn",options().size()>=2);
        for(WebElement option:options())
            assertTrue("Mỗi lựa chọn phải có tên/nhãn",!option.getText().trim().isEmpty() || !attr(option,"title").isEmpty());
    }
    @Test public void TC_PBSP_03_TrangThaiMacDinh() {
        openVariant();
        assertTrue("Phải hiện lựa chọn mặc định hoặc nhắc chọn phiên bản",
                !selectedOption().isEmpty() || body().contains("Phân loại") || body().contains("Chọn"));
    }
    @Test public void TC_PBSP_04_NhanThuocTinhBatBuoc() {
        product(fixture("requiredVariantUrl"));
        assertTrue("Không có hướng dẫn/nhãn lựa chọn",body().contains("Chọn") || body().contains("chọn") || body().contains("Phân loại:"));
        assertTrue(options().size()>0);
    }
    @Test public void TC_PBSP_05_ChonMauConHang() {
        product(fixture("colorAvailableUrl"));
        String chosen=choose("colorOptionIndex",0);
        assertTrue(!chosen.isEmpty() || !selectedOption().isEmpty());
        assertFalse(driver.getTitle().contains("500"));
    }
    @Test public void TC_PBSP_06_ChonSwitchHoacCauHinh() {
        openVariant();
        String value=chooseOption(1);
        assertTrue("Lựa chọn cấu hình không có tên",!value.isEmpty() || !selectedOption().isEmpty());
    }
    @Test public void TC_PBSP_07_ToHopThuocTinhHopLe() {
        product(fixture("multiGroupAvailableUrl"));
        List<WebElement> groups=visible(By.cssSelector(".swatch, .variant-group, fieldset"));
        requireScenario(groups.size()>=2,"Cần hai nhóm thuộc tính độc lập");
        for(WebElement g:groups) {
            List<WebElement> x=g.findElements(By.cssSelector("label"));
            requireScenario(!x.isEmpty(),"Mỗi nhóm cần có lựa chọn");
            click(x.get(0));
        }
        assertTrue("Phải sẵn sàng thêm vào giỏ",availableToAdd());
        assertFalse(sku().isEmpty());
    }
    @Test public void TC_PBSP_08_DoiABALienTuc() {
        openVariant();
        String a=chooseOption(0);
        String stateA=selectedOption();
        String b=chooseOption(1);
        String stateB=selectedOption();
        String aAgain=chooseOption(0);
        assertEquals("Quay lại A phải cùng nhãn",a,aAgain);
        assertNotEquals("A và B phải là hai phiên bản khác nhau",a,b);
        if(!stateA.isEmpty() && !stateB.isEmpty()) {
            assertNotEquals("Trạng thái lựa chọn phải đổi A-B",stateA,stateB);
            assertEquals("Quay lại phải khôi phục lựa chọn A",stateA,selectedOption());
        }
    }
    @Test public void TC_PBSP_09_DanhDauLuaChon() {
        openVariant();
        chooseOption(1);
        assertTrue("Phải có trạng thái được chọn trong DOM (radio checked hoặc active/selected)", hasSelectedOptionInDom());
    }
    @Test public void TC_PBSP_10_DoiGiaTheoPhienBan() {
        product(fixture("differentPriceVariantsUrl"));
        choose("priceVariantA",0); long a=money(price().getText());
        choose("priceVariantB",1);
        wait.until(d -> money(price().getText()) != a);
        long b=money(price().getText());
        assertNotEquals("Giá không thay đổi theo hai phiên bản có mức giá khác nhau",a,b);
    }
    @Test public void TC_PBSP_11_DoiSkuTheoPhienBan() {
        product(fixture("differentSkuVariantsUrl"));
        choose("skuVariantA",0); String a=sku();
        choose("skuVariantB",1);
        wait.until(d -> !sku().equals(a));
        String b=sku();
        assertNotEquals("SKU không đổi theo hai biến thể có SKU khác nhau",a,b);
    }
    @Test public void TC_PBSP_12_DoiAnhTheoPhienBan() {
        product(fixture("differentImageVariantsUrl"));
        choose("imageVariantA",0); String a=attr(mainImage(),"src");
        choose("imageVariantB",1);
        wait.until(d -> !attr(mainImage(),"src").equals(a));
        String b=attr(mainImage(),"src");
        assertNotEquals("Ảnh phải thay đổi theo phiên bản màu",a,b);
        assertTrue((Boolean)((JavascriptExecutor)driver).executeScript("return arguments[0].naturalWidth>0",mainImage()));
    }
    @Test public void TC_PBSP_13_DoiTonKhoTheoPhienBan() {
        product(fixture("mixedStockVariantUrl"));
        choose("stockAvailableIndex",0); boolean available=availableToAdd();
        choose("stockUnavailableIndex",1); boolean unavailable=availableToAdd();
        assertTrue("Phiên bản còn hàng phải mua được",available);
        assertFalse("Phiên bản hết hàng không được cho mua",unavailable);
    }
    @Test public void TC_PBSP_14_ThemBanConHang() {
        product(fixture("availableVariantUrl"));
        choose("availableVariantIndex",0);
        assertTrue(availableToAdd()); addToCart();
        assertTrue(body().contains("giỏ") || driver.getCurrentUrl().contains("cart"));
    }
    @Test public void TC_PBSP_15_BanHetHang() {
        product(fixture("soldOutVariantUrl"));
        choose("soldOutVariantIndex",0);
        assertTrue("Phải hiện hết hàng",body().contains("Hết hàng"));
        assertFalse("Không được bật mua",availableToAdd());
    }
    @Test public void TC_PBSP_16_BanPreorder() {
        product(fixture("preorderUrl"));
        chooseOption(0);
        WebElement b=required(By.xpath("//button[contains(.,'Đặt trước')]"));
        assertTrue(b.isEnabled());
        assertTrue("Cần thông tin dự kiến",body().contains("dự kiến") || body().contains("Sắp về"));
        click(b);
        assertFalse(driver.getTitle().contains("500"));
    }
    @Test public void TC_PBSP_17_ToHopKhongTonTai() {
        product(fixture("invalidCombinationUrl"));
        choose("invalidCombinationFirstIndex",0);
        int second=Integer.parseInt(config("invalidCombinationSecondIndex","1"));
        List<WebElement> x=options();
        requireScenario(x.size()>second,"Thiếu lựa chọn tạo tổ hợp không tồn tại");
        WebElement target=x.get(second);
        assertTrue("Giá trị không tồn tại phải vô hiệu hóa hoặc đánh dấu không khả dụng",
                !target.isEnabled() || attr(target,"class").matches("(?i).*(disabled|soldout|unavailable).*"));
    }
    @Test public void TC_PBSP_18_ChuaChonDuThuocTinh() {
        product(fixture("requiredVariantUrl"));
        requireScenario(options().size()>0,"Cần phiên bản bắt buộc chưa chọn đủ");
        WebElement button=addButton();
        if(button.isEnabled()) click(button);
        assertFalse("Không được thêm khi thiếu lựa chọn",body().contains("Thêm vào giỏ hàng thành công"));
    }
    @Test public void TC_PBSP_19_SoLuongMacDinh() {
        openVariant(); chooseOption(0);
        assertEquals(1,qty());
        assertNotNull(plus()); assertNotNull(minus());
    }
    @Test public void TC_PBSP_20_TangSoLuong() {
        product(fixture("stockAtLeast3VariantUrl"));
        chooseOption(0);
        int initial=qty();
        click(plus()); assertEquals(initial+1,qty());
        click(plus()); assertEquals(initial+2,qty());
    }
    @Test public void TC_PBSP_21_GiamTaiGioiHanDuoi() {
        openVariant(); chooseOption(0);
        fill(quantity(),"2"); click(minus()); assertEquals(1,qty());
        click(minus()); assertEquals("Không được giảm xuống dưới 1",1,qty());
    }
    @Test public void TC_PBSP_22_NhapZeroAm() {
        openVariant(); chooseOption(0);
        for(String value:new String[]{"0","-1"}) {
            fill(quantity(),value);
            String actual=attr(quantity(),"value");
            assertTrue("Số lượng không hợp lệ phải bị loại hoặc cảnh báo",
                    invalid(quantity()) || actual.isEmpty() || (actual.matches("\\d+") && Integer.parseInt(actual)>0));
        }
    }
    @Test public void TC_PBSP_23_NhapChuKyTuThapPhan() {
        openVariant(); chooseOption(0);
        for(String value:new String[]{"abc","@#","1.5"}) {
            fill(quantity(),value);
            String actual=attr(quantity(),"value");
            assertTrue("Chỉ chấp nhận số nguyên dương",invalid(quantity()) || actual.isEmpty() || (actual.matches("\\d+") && Integer.parseInt(actual)>0));
        }
    }
    @Test public void TC_PBSP_24_VuotTonKhoPhienBan() {
        product(fixture("limitedStockVariantUrl"));
        chooseOption(0);
        int stock=Integer.parseInt(config("limitedVariantStockCount","-1"));
        requireScenario(stock>=1,"Cần -DlimitedVariantStockCount theo dữ liệu tồn kho thực");
        fill(quantity(),String.valueOf(stock+1));
        assertTrue("Không được vượt tồn kho",invalid(quantity()) || qty()<=stock || !availableToAdd());
    }
    @Test public void TC_PBSP_25_ThemDungPhienBanVaSoLuong() {
        product(fixture("stockAtLeast3VariantUrl"));
        String name=title(), option=chooseOption(0);
        fill(quantity(),"2"); assertEquals(2,qty());
        assertTrue(availableToAdd()); addToCart(); goCart();
        assertTrue("Tên sản phẩm phải có trong giỏ",body().contains(name));
        if(!option.isEmpty()) assertTrue("Phải đối chiếu tên phiên bản trong giỏ",body().contains(option));
        WebElement cartQty=required(By.cssSelector(".cart-item input[name*='quantity'], .cart-item input[type='number'], .cart-product input[type='number'], .cart-quantity input"));
        assertEquals("Giỏ phải lưu đúng số lượng 2","2",attr(cartQty,"value"));
    }
}
