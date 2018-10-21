package co.iay.learn.learningjava.basic;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

public class LearnAnnotationTest {
    public static class LearnAnnotationTestObj {
        @LearnAnnotation(filedAnnotationValue = LEARN_ANNOTATION_TEST_VALUE)
        public void methodWithAnnotation() {
            System.out.println("R U OK?");
        }
    }

    final static private String                 LEARN_ANNOTATION_TEST_VALUE   = "LEARN_ANNOTATION_TEST_VALUE";
    final static private String                 LEARN_ANNOTATION_TEST_VALUE_1 = "LEARN_ANNOTATION_TEST_VALUE_1";
    @LearnAnnotation(filedAnnotationValue = LEARN_ANNOTATION_TEST_VALUE)
    private              LearnAnnotationTestObj testObj                       = new LearnAnnotationTestObj();
    @LearnAnnotation(filedAnnotationValue = LEARN_ANNOTATION_TEST_VALUE)
    private              int                    testInt                       = 0;
    @LearnAnnotation(filedAnnotationValue = LEARN_ANNOTATION_TEST_VALUE)
    @LearnAnnotation(value = LEARN_ANNOTATION_TEST_VALUE_1)
    private              int                    testRepeatInt                 = 0;

    @Test
    public void testCase1() {
        try {
            Method method = testObj.getClass().getMethod("methodWithAnnotation");
            Assert.assertEquals(
                    LEARN_ANNOTATION_TEST_VALUE,
                    method.getAnnotation(LearnAnnotation.class).filedAnnotationValue()

            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCase2() {
        try {
            LearnAnnotation annotation = this.getClass().getDeclaredField("testObj").getAnnotation(LearnAnnotation.class);
            Assert.assertNotNull(annotation);
            Assert.assertEquals(
                    LEARN_ANNOTATION_TEST_VALUE,
                    annotation.filedAnnotationValue()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCase3() {
        try {
            LearnAnnotation annotation = this.getClass().getDeclaredField("testInt").getAnnotation(LearnAnnotation.class);
            Assert.assertNotNull(annotation);
            Assert.assertEquals(
                    LEARN_ANNOTATION_TEST_VALUE,
                    annotation.filedAnnotationValue()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCase4() {
        try {
            LearnRepeatableAnnotation annotation = this.getClass().getDeclaredField("testRepeatInt").getAnnotation(LearnRepeatableAnnotation.class);
            Assert.assertNotNull(annotation);
            Assert.assertEquals(2, annotation.value().length);
            Assert.assertEquals("", annotation.value()[0].value());
            Assert.assertEquals(
                    LEARN_ANNOTATION_TEST_VALUE,
                    annotation.value()[0].filedAnnotationValue()
            );
            Assert.assertEquals(
                    LEARN_ANNOTATION_TEST_VALUE_1,
                    annotation.value()[1].value()
            );
            Assert.assertEquals("", annotation.value()[1].filedAnnotationValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
