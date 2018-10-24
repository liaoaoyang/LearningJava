package co.iay.learn.learningjava.spring.springstudy.aspect;

import org.springframework.stereotype.Component;

@Component
public class AspectTestClazz {
    public String testAspectMethod1() {
        return getClass().getCanonicalName();
    }
}
