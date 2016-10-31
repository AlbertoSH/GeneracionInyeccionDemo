package com.github.albertosh.magic.generation_1.sample;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.lang.model.element.Modifier;

public class HelloWorldGenerator {

    public static void main(String[] args) {
        /*
         * We want to achieve this:
         *
         * package com.example.helloworld;
         *
         * public final class HelloWorld {
         *     public static void main(String[] args) {
         *         System.out.println("Hello, JavaPoet!");
         *     }
         * }
         *
         */

        MethodSpec mainSpec = buildMainSpec();

        TypeSpec helloWorld = buildTypeSpec(mainSpec);

        JavaFile javaFile = JavaFile.builder("com.example.helloworld", helloWorld)
                .build();

        try {
            javaFile.writeTo(System.out);
        } catch (IOException e) {
            // This shouldn't happen...
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    private static MethodSpec buildMainSpec() {
        return MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(String[].class, "args")
                .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
                .build();
    }

    private static TypeSpec buildTypeSpec(MethodSpec mainSpec) {
        return TypeSpec.classBuilder("HelloWorld")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(mainSpec)
                .build();
    }
}
