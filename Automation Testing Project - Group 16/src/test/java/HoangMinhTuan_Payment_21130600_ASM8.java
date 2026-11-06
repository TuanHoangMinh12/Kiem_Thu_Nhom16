import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.*;
import java.util.List;
import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HoangMinhTuan_Payment_21130600_ASM8 extends HoangMinhTuan_KicapBaseTest_21130600_ASM8 {
    private void orderIfAllowed() {
        allowOrderOrSkip();
        click(placeOrderButton());
        wait.until(d -> d.getCurrentUrl().matches("(?i).*(thank|success|order).*" ) ||
                d.findElement(By.tagName("body")).getText().matches("(?is).*(đặt hàng thành công|cảm ơn bạn đã đặt hàng|mã đơn hàng).*"));
        assertTrue("Cần hiển thị xác nhận đơn hoặc mã đơn",
                driver.getCurrentUrl().matches("(?i).*(thank|success|order).*" ) ||
                        body().matches("(?is).*(đặt hàng thành công|cảm ơn bạn đã đặt hàng|mã đơn hàng).*"));
    }
    private WebElement fee() {
        return required(
                By.cssSelector("[data-shipping-fee], .shipping-fee .price, .shipping-price, .shipping-fee, .shipping-line .amount, .order-summary .shipping .price"),
                By.xpath("//*[contains(normalize-space(.),'Phí vận chuyển') and not(*)]/following-sibling::*[contains(.,'₫') or contains(.,'Miễn phí')][1]"));
    }
    private WebElement grandTotal() {
        return required(By.cssSelector(".order-total, .grand-total, [data-total], .total-price, .order-summary .total"),
                By.xpath("//*[contains(text(),'Tổng cộng') or contains(text(),'Tổng thanh toán')]/following::*[contains(text(),'₫') or contains(text(),'đ')][1]"));
    }
    private void requireCheckout() {
        checkoutWithCart();
        assertNotNull(checkoutField("name"));
    }
    private void prepareValidOrder(String paymentKeyword) {
        requireCheckout(); validCustomer(); selectPayment(paymentKeyword);
    }
    @Test public void TC_TT_01_MoThanhToanKhiCoHang() {
        product(fixture("availableProductUrl"));
        String productName=title();
        if(!options().isEmpty()) chooseOption(0);
        requireScenario(availableToAdd(),"Sản phẩm phải có thể thêm vào giỏ");
        addToCart(); navigateToCheckoutAfterBuy();
        assertTrue("Tóm tắt đơn phải có sản phẩm",body().contains(productName));
        assertNotNull(grandTotal());
    }
    @Test public void TC_TT_02_GioTrongKhongDuocThanhToan() {
        open(BASE+"/checkout");
        assertTrue("Không được thanh toán giỏ rỗng",driver.getCurrentUrl().contains("cart") ||
                body().matches("(?is).*(giỏ hàng trống|giỏ hàng của bạn chưa có|không có sản phẩm).*"));
    }
    @Test public void TC_TT_03_KhachKhongDangNhap() {
        requireCheckout();
        assertTrue("Khách phải được nhập tên",checkoutField("name").isEnabled());
        assertTrue("Khách phải được nhập SĐT",checkoutField("phone").isEnabled());
    }
    @Test public void TC_TT_04_TuDienThongTinKhiDangNhap() {
        requireLoginFixture(); requireCheckout();
        assertFalse("Tài khoản đã lưu tên phải tự điền",attr(checkoutField("name"),"value").trim().isEmpty());
        assertTrue("Thông tin phải sửa được",checkoutField("name").isEnabled());
    }
    @Test public void TC_TT_05_TrongTatCaTruongBatBuoc() {
        requireCheckout();
        fill(checkoutField("name"),"");
        fill(checkoutField("phone"),"");
        fill(checkoutField("address"),"");
        safeInvalidSubmit(checkoutField("name"));
        assertTrue("Trường tên cần cảnh báo",invalid(checkoutField("name")));
        assertTrue("Trường SĐT cần cảnh báo",invalid(checkoutField("phone")));
        assertTrue("Trường địa chỉ cần cảnh báo",invalid(checkoutField("address")));
        assertEquals("",attr(checkoutField("name"),"value"));
    }
    @Test public void TC_TT_06_ThieuHoTen() {
        requireCheckout();
        fill(checkoutField("phone"),"0987654321");
        fill(checkoutField("email"),"kicap.test@example.com");
        fill(checkoutField("name"),"");
        safeInvalidSubmit(checkoutField("name"));
        assertTrue("Trường họ tên phải không hợp lệ",invalid(checkoutField("name")));
    }
    @Test public void TC_TT_07_SoDienThoaiHopLe() {
        requireCheckout();
        fill(checkoutField("phone"),"0987654321");
        assertEquals("0987654321",attr(checkoutField("phone"),"value"));
        assertFalse("Không được đánh dấu số hợp lệ thành invalid",invalid(checkoutField("phone")));
    }
    @Test public void TC_TT_08_SoDienThoaiSai() {
        requireCheckout();
        for(String value:new String[]{"098abc","123","0987654321123"}) {
            fill(checkoutField("phone"),value);
            assertTrue("Số điện thoại sai phải bị chặn/cảnh báo: "+value,
                    invalid(checkoutField("phone")) || !attr(checkoutField("phone"),"value").equals(value));
        }
    }
    @Test public void TC_TT_09_EmailHopLe() {
        requireCheckout();
        fill(checkoutField("email"),"kicap.test@example.com");
        assertEquals("kicap.test@example.com",attr(checkoutField("email"),"value"));
        assertFalse(invalid(checkoutField("email")));
    }
    @Test public void TC_TT_10_EmailSai() {
        requireCheckout();
        for(String value:new String[]{"test","test@","@example.com"}) {
            fill(checkoutField("email"),value);
            assertTrue("Email sai cần bị đánh dấu invalid: "+value,invalid(checkoutField("email")));
        }
    }
    @Test public void TC_TT_11_ThieuDiaChiChiTiet() {
        requireCheckout();
        fill(checkoutField("address"),"");
        safeInvalidSubmit(checkoutField("address"));
        assertTrue(invalid(checkoutField("address")));
    }
    @Test public void TC_TT_12_ChonTinhThanh() {
        requireCheckout();
        selectRegion("province",config("testProvince","Hà Nội"));
        WebElement district=region("district");
        assertTrue("Quận/huyện phải được cho phép chọn",district.isEnabled());
        if(district.getTagName().equalsIgnoreCase("select"))
            assertTrue("Quận/huyện phải có danh sách",district.findElements(By.tagName("option")).size()>1);
    }
    @Test public void TC_TT_13_ChonQuanHuyen() {
        requireCheckout();
        selectRegion("province",config("testProvince","Hà Nội"));
        String district = config("testDistrict", "");
        if (district.isEmpty()) selectFirstRegionOption("district");
        else selectRegion("district", district);
        WebElement ward=region("ward");
        assertTrue("Phường/xã cần được cập nhật",ward.isEnabled());
        if(ward.getTagName().equalsIgnoreCase("select"))
            assertTrue("Phường/xã phải có danh sách",ward.findElements(By.tagName("option")).size()>1);
    }
    @Test public void TC_TT_14_ThieuQuanHoacPhuong() {
        requireCheckout();
        selectRegion("province",config("testProvince","Hà Nội"));
        WebElement district=region("district");
        requireScenario(invalid(district),"Quận/huyện không được trình duyệt đánh dấu bắt buộc; cần môi trường test");
        safeInvalidSubmit(district);
    }
    @Test public void TC_TT_15_ChuanHoaKhoangTrang() {
        allowOrderOrSkip();
        requireCheckout(); validCustomer();
        fill(checkoutField("name"),"  Nguyen Van An Test  ");
        fill(checkoutField("address"),"  123 Duong Kiem Thu  ");
        selectPayment("COD");
        orderIfAllowed();
        assertTrue("Thông tin đơn phải hiển thị tên đã chuẩn hóa",body().contains("Nguyen Van An Test"));
        assertFalse("Họ tên không được còn khoảng trắng dư",body().contains("  Nguyen Van An Test  "));
    }
    @Test public void TC_TT_16_PhiShipDuoi800Nghin() {
        product(fixture("under800kAvailableUrl"));
        requireScenario(money(price().getText())<800000L,"Cần sản phẩm dưới 800.000đ");
        if(!options().isEmpty()) chooseOption(0);
        requireScenario(availableToAdd(),"Sản phẩm dưới ngưỡng phải còn hàng");
        addToCart(); navigateToCheckoutAfterBuy();
        validCustomer();
        long ship=moneyOrZero(fee().getText());
        assertEquals("Theo chính sách Kicap: phí cơ bản dưới 800.000đ là 30.000đ (trừ trường hợp đặc biệt)",30000L,ship);
        assertNotNull(grandTotal());
    }
    @Test public void TC_TT_17_FreeshipTu800Nghin() {
        product(fixture("over800kAvailableUrl"));
        long productPrice=money(price().getText());
        requireScenario(productPrice>=800000,"Sản phẩm fixture phải có giá từ 800.000đ");
        if(!options().isEmpty()) chooseOption(0);
        requireScenario(availableToAdd(),"Cần sản phẩm còn hàng");
        addToCart(); navigateToCheckoutAfterBuy();
        validCustomer();
        String displayedFee=fee().getText().toLowerCase();
        assertEquals("Đơn đủ điều kiện miễn phí vận chuyển",0L,moneyOrZero(displayedFee));
    }
    @Test public void TC_TT_18_ChonPhuongThucVanChuyen() {
        requireCheckout();
        List<WebElement> methods=driver.findElements(By.cssSelector("input[name*='shipping'], input[name*='delivery'], [class*='shipping-method'] input[type='radio']"));
        requireScenario(!methods.isEmpty(),"Cần lựa chọn phương thức vận chuyển");
        WebElement choice=methods.get(0);
        if(!choice.isSelected()) ((JavascriptExecutor)driver).executeScript("arguments[0].click();",choice);
        assertTrue("Phương thức phải được chọn",choice.isSelected());
        long selected=methods.stream().filter(WebElement::isSelected).count();
        assertEquals("Chỉ được chọn một phương thức vận chuyển",1L,selected);
        assertNotNull(fee());
    }
    @Test public void TC_TT_19_ChonCOD() {
        // Test CHỌN COD không đồng nghĩa với việc phải ĐẶT ĐƠN thật.
        requireCheckout();
        selectPayment("COD");
        assertTrue("COD phải hiển thị trong phương thức thanh toán",body().contains("COD"));
    }
    @Test public void TC_TT_20_ChonChuyenKhoan() {
        requireCheckout();
        selectPayment("Chuyển khoản");
        assertTrue("Hình thức chuyển khoản phải được hiển thị",body().contains("Chuyển khoản"));
    }
    @Test public void TC_TT_21_GhiChuDonHang() {
        // Chỉ kiểm thử nhập và giữ dữ liệu trên form, KHÔNG xác nhận lưu trên đơn thật.
        requireCheckout();
        String note="Giao gio hanh chinh, goi truoc khi giao";
        fill(checkoutField("note"),note);
        assertEquals("Ghi chú phải giữ đúng nội dung đã nhập",note,attr(checkoutField("note"),"value"));
    }
    @Test public void TC_TT_22_DanhSachNhieuSanPham() {
        product(fixture("availableProductUrl")); String first=title();
        if(!options().isEmpty()) chooseOption(0);
        requireScenario(availableToAdd(),"Cần sản phẩm thứ nhất còn hàng"); addToCart();
        product(fixture("secondAvailableProductUrl")); String second=title();
        if(!options().isEmpty()) chooseOption(0);
        requireScenario(availableToAdd(),"Cần sản phẩm thứ hai còn hàng"); addToCart();
        goCart(); goCheckout();
        assertTrue("Thiếu sản phẩm thứ nhất",body().contains(first));
        assertTrue("Thiếu sản phẩm thứ hai",body().contains(second));
    }
    @Test public void TC_TT_23_TinhTongThanhToan() {
        requireCheckout();
        String raw=grandTotal().getText();
        long total=money(raw);
        long shipping=moneyOrZero(fee().getText());
        WebElement subtotal=required(By.cssSelector(".subtotal, .sub-total, [data-subtotal], .order-subtotal"),
                By.xpath("//*[contains(text(),'Tạm tính')]/following::*[contains(text(),'₫')][1]"));
        long goods=money(subtotal.getText());
        WebElement discount=maybe(By.cssSelector(".discount-amount, [data-discount], .order-discount"));
        long discountValue=discount==null?0:money(discount.getText());
        System.out.println("[ASM8][TT_23] subtotal="+goods+" shipping="+shipping+" discount="+discountValue+" total="+total
                +" | feeText="+fee().getText()+" | subtotalText="+subtotal.getText()+" | totalText="+grandTotal().getText());
        assertEquals("Tổng tiền = Tiền hàng + Phí ship - Ưu đãi (kiểm tra các khoản khác như phí COD/thuế nếu có)",goods+shipping-discountValue,total);
    }
    @Test public void TC_TT_24_ChongTaoDonTrung() {
        allowOrderOrSkip();
        // Cần trang quản trị/lịch sử đơn có thể đếm số đơn thực tế,
        // chỉ dựa vào một màn hình xác nhận không chứng minh được không tạo đơn trùng.
        String auditUrl=fixture("orderAuditUrl");
        String rowSelector=config("orderRowSelector",".order-row");
        requireLoginFixture();
        open(auditUrl);
        int before=driver.findElements(By.cssSelector(rowSelector)).size();
        requireScenario(before>=0,"Cần trang lịch sử đơn đã tải");
        prepareValidOrder("COD");
        WebElement button=placeOrderButton();
        click(button);
        try { if(button.isDisplayed() && button.isEnabled()) click(button); }
        catch(StaleElementReferenceException ignored) { /* Nút bị thay sau khi gửi */ }
        wait.until(d -> d.getCurrentUrl().matches("(?i).*(thank|success|order).*" ) || body().contains("mã đơn"));
        open(auditUrl);
        int after=driver.findElements(By.cssSelector(rowSelector)).size();
        assertEquals("Nhấn hai lần chỉ được tạo một đơn mới",before+1,after);
    }
    @Test public void TC_TT_25_DatHangThanhCong() {
        allowOrderOrSkip();
        prepareValidOrder("COD");
        orderIfAllowed();
    }
}
