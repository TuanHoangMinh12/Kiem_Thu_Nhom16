import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/*
 * Test Suite chạy toàn bộ bài ASM8 theo thứ tự:
 * 1. Chi tiết sản phẩm
 * 2. Phiên bản sản phẩm
 * 3. Thanh toán
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        HoangMinhTuan_ProductDetails_21130600_ASM8.class,
        HoangMinhTuan_ProductVersion_21130600_ASM8.class,
        HoangMinhTuan_Payment_21130600_ASM8.class
})
public class ASM8TestSuite {

}