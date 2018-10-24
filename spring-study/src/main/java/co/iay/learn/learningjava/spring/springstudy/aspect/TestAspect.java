package co.iay.learn.learningjava.spring.springstudy.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TestAspect {
    @Pointcut("execution(public * co.iay.learn.learningjava.spring.springstudy.aspect.AspectTestClazz.*(..))")
    public void testAspect() {
    }

    @Before("testAspect()")
    public int doBeforeB() {
        System.out.println("doBeforeB");
        return 3;
    }

    @Before("testAspect()")
    public int doBeforeA() {
        System.out.println("doBeforeA");
        return 1;
    }

    @Before("testAspect()")
    public int doBeforeC() {
        System.out.println("doBeforeC");
        return 2;
    }
}
