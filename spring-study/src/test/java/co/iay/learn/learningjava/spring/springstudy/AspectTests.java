package co.iay.learn.learningjava.spring.springstudy;

import co.iay.learn.learningjava.spring.springstudy.aspect.AspectTestClazz;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AspectTests {
    @Autowired
    private AspectTestClazz aspectTestClazz;

    @Test
    public void testCase1() {
        Assert.assertSame(AspectTestClazz.class.getCanonicalName(), aspectTestClazz.testAspectMethod1());
    }
}
