package Tests;

import Customer.Customer;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestFlight {
    @Test
    public void test() {
        System.out.println("testing mc test face");
        String name = "Raphi";
        Customer c = new Customer();
        c.setName(name);
        assertEquals(name, c.getName());
    }
}
