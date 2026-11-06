import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/** JUnit 4 suite for the Shopping Cart, Search & Filter, and Address Book workbook cases. */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        ShoppingCartTest.class,
        SearchFilterTest.class,
        AddressBookTest.class
})
public class PhongTestSuite {
}
