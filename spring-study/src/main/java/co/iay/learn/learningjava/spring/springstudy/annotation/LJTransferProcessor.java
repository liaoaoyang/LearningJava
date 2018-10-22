package co.iay.learn.learningjava.spring.springstudy.annotation;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.HashSet;
import java.util.Set;

public class LJTransferProcessor extends AbstractProcessor {
    final static private String      METHOD_WHO_AM_I  = "whoAmI";
    final static private String      NEW_CLASS_SUFFIX = "Impl";
    private              Set<String> supportedAnnotationTypes;

    public LJTransferProcessor() {
        this.supportedAnnotationTypes = new HashSet<>();
        this.supportedAnnotationTypes.add(LJTransfer.class.getCanonicalName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            for (Element element : roundEnv.getElementsAnnotatedWith(LJTransfer.class)) {
                if (element.getKind() != ElementKind.INTERFACE) {
                    continue;
                }

                TypeElement typeElement = (TypeElement) element;
                String packageName = typeElement.getQualifiedName().toString()
                        .substring(0, typeElement.getQualifiedName().toString().lastIndexOf(typeElement.getSimpleName().toString()) - 1);
                String newClassName = typeElement.getSimpleName().toString() + NEW_CLASS_SUFFIX;

                TypeSpec.Builder newLJTransferClassBuilder = TypeSpec.classBuilder(newClassName)
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(buildMethodWhoAmI());
                JavaFile.builder(packageName, newLJTransferClassBuilder.build()).build().writeTo(processingEnv.getFiler());
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return this.supportedAnnotationTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    private MethodSpec buildMethodWhoAmI() {
        return MethodSpec.methodBuilder(METHOD_WHO_AM_I)
                .addModifiers(Modifier.PUBLIC)
                .addCode("return \"I am LJTransfer\\n\";")
                .returns(String.class)
                .build();
    }
}
