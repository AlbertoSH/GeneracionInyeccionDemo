package com.github.albertosh.magic.generation_2.compiler;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Generated;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import static javafx.scene.input.KeyCode.M;

@SupportedAnnotationTypes({
        "com.github.albertosh.magic.generation_2.compiler.Builder"
})
public class Compiler extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        roundEnv.getElementsAnnotatedWith(Builder.class)
                .stream()
                .map(element -> (TypeElement) element)
                .forEach(this::processClass);

        return true;
    }

    private void processClass(TypeElement klass) {
        String builderClassName = klass.getSimpleName().toString() + "Builder";
        String builderClassPackage = ((PackageElement)klass.getEnclosingElement()).getQualifiedName().toString();
        ClassName builderClass = ClassName.bestGuess(builderClassPackage + "." + builderClassName);

        AnnotationSpec generatedAnnotationSpec = buildGeneratedAnnotation();

        MethodSpec constructorSpec = buildConstructorSpec();

        MethodSpec.Builder fromPrototypeSpecBuilder = buildFromPrototypeSpec(klass, builderClass);

        TypeSpec.Builder builderSpecBuilder = TypeSpec.classBuilder(builderClassName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(generatedAnnotationSpec)
                .addMethod(constructorSpec);

        klass.getEnclosedElements().stream()
                .filter(element -> element.getKind().isField())
                .map(element -> (VariableElement) element)
                .forEach(field -> processField(field, builderSpecBuilder,
                        builderClass, fromPrototypeSpecBuilder, klass));

        fromPrototypeSpecBuilder
                .addStatement("return this");

        MethodSpec buildSpec = buildBuildSpec(klass);

        TypeSpec builderSpec = builderSpecBuilder
                .addMethod(fromPrototypeSpecBuilder.build())
                .addMethod(buildSpec)
                .build();

        JavaFile javaFile = JavaFile.builder(builderClassPackage, builderSpec)
                .build();
        try {
            Filer filer = processingEnv.getFiler();
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private AnnotationSpec buildGeneratedAnnotation() {
        return AnnotationSpec.builder(Generated.class)
                .addMember("value", "\"Builder\"")
                .build();
    }

    private MethodSpec buildConstructorSpec() {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .build();
    }

    private MethodSpec.Builder buildFromPrototypeSpec(TypeElement elementClass, ClassName builderClass) {
        ParameterSpec parameterSpec = ParameterSpec.builder(
                TypeName.get(elementClass.asType()),
                "prototype",
                Modifier.FINAL
        )
                .build();
        return MethodSpec.methodBuilder("fromPrototype")
                .addModifiers(Modifier.PUBLIC)
                .returns(builderClass)
                .addParameter(parameterSpec);
    }

    private void processField(VariableElement field, TypeSpec.Builder builderSpecBuilder,
                              ClassName builderClass, MethodSpec.Builder fromPrototypeSpecBuilder,
                              TypeElement klass) {
        addFieldToBuilder(field, builderSpecBuilder);
        addWithMethodToBuilder(field, builderSpecBuilder, builderClass);
        addGetterToBuilder(field, builderSpecBuilder);
        addFromPrototypeSentence(field, fromPrototypeSpecBuilder, klass);
    }

    private void addFieldToBuilder(VariableElement field, TypeSpec.Builder builderSpecBuilder) {
        FieldSpec fieldSpec = FieldSpec.builder(
                TypeName.get(field.asType()),
                field.getSimpleName().toString(),
                Modifier.PRIVATE)
                .build();
        builderSpecBuilder.addField(fieldSpec);
    }

    private void addWithMethodToBuilder(VariableElement field, TypeSpec.Builder builderSpecBuilder,
                                        ClassName builderClass) {

        String fieldName = field.getSimpleName().toString();
        String upperFieldName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);

        ParameterSpec parameterSpec = ParameterSpec.builder(
                TypeName.get(field.asType()),
                field.getSimpleName().toString(),
                Modifier.FINAL)
                .build();

        MethodSpec withMethod = MethodSpec.methodBuilder("with" + upperFieldName)
                .addModifiers(Modifier.PUBLIC)
                .returns(builderClass)
                .addParameter(parameterSpec)
                .addStatement("this.$N = $N", fieldName, fieldName)
                .addStatement("return this")
                .build();
        builderSpecBuilder.addMethod(withMethod);
    }

    private void addGetterToBuilder(VariableElement field, TypeSpec.Builder builderSpecBuilder) {
        String fieldName = field.getSimpleName().toString();
        String upperFieldName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);

        MethodSpec getter = MethodSpec.methodBuilder("get" + upperFieldName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(ClassName.get(field.asType()))
                .addStatement("return $N", fieldName)
                .build();
        builderSpecBuilder.addMethod(getter);
    }

    private void addFromPrototypeSentence(VariableElement field,
                                          MethodSpec.Builder fromPrototypeSpecBuilder,
                                          TypeElement klass) {

        if (!field.getModifiers().contains(Modifier.PRIVATE)) {
            // can be accessed directly
            fromPrototypeSpecBuilder
                    .addStatement("this.$N = prototype.$N",
                            field.getSimpleName(), field.getSimpleName());
        } else {
            // We need a getter
            Optional<ExecutableElement> optMethod = klass.getEnclosedElements().stream()
                    .filter(element -> element.getKind() == ElementKind.METHOD)
                    .map(e -> (ExecutableElement) e)
                    .filter(method -> !method.getModifiers().contains(Modifier.PRIVATE))
                    .filter(method -> method.getReturnType().equals(field.asType()))
                    .filter(method -> method.getParameters().isEmpty())
                    .filter(method -> method.getSimpleName().toString().equalsIgnoreCase("get"+field.getSimpleName()))
                    .findFirst();
            if (optMethod.isPresent()) {
                ExecutableElement method = optMethod.get();
                fromPrototypeSpecBuilder
                        .addStatement("this.$N = prototype.$N()",
                                field.getSimpleName(), method.getSimpleName());
            } else {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.WARNING,
                        "Unable to find a way to get " + field.getSimpleName() +
                                ". An easy way to fix this is to add a getter to your " +
                                klass.getSimpleName() + " class",
                        field
                );
            }
        }
    }

    private MethodSpec buildBuildSpec(TypeElement klass) {
        return MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(ClassName.get(klass))
                .addStatement("return new $T(this)", klass)
                .build();
    }

}
