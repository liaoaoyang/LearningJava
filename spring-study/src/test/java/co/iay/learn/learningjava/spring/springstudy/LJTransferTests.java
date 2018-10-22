package co.iay.learn.learningjava.spring.springstudy;

import co.iay.learn.learningjava.spring.springstudy.objs.transfer.Test1Transfer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LJTransferTests {
    @Test
    public void testWhoAmI() {
        try {
            Class<?> c = Class.forName(Test1Transfer.class.getName() + "Impl");
            Assert.notNull(c.getMethod("whoAmI"), "");
            Object transfer = c.newInstance();
            Assert.isTrue(c.getMethod("whoAmI").invoke(transfer).equals("I am LJTransfer\n"), "");
        } catch (ClassNotFoundException e) {
            System.out.println("You may need to run [mvn compile] first");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
