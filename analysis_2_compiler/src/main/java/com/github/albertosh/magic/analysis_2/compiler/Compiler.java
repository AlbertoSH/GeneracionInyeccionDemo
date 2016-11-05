package com.github.albertosh.magic.analysis_2.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import static javafx.scene.input.KeyCode.M;

@SupportedAnnotationTypes({
        "com.github.albertosh.magic.analysis_2.compiler.Toothpick"
})
public class Compiler extends AbstractProcessor {

    private Map<TypeMirror, List<TypeMirror>> graph = new HashMap<>();

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        roundEnv.getElementsAnnotatedWith(Toothpick.class)
                .stream()
                .map(cons -> (ExecutableElement) cons)
                .forEach(this::processConstructor);

        if (roundEnv.processingOver()) {
            graph.entrySet()
                    .forEach(entry -> {
                        String message
                                = "In order to build a " +
                                entry.getKey() +
                                " we need to build first: " +
                                String.join(", ", entry.getValue()
                                        .stream()
                                        .map(TypeMirror::toString)
                                        .collect(Collectors.toList()));

                        note(message);
                    });
        }

        return true;
    }

    private void processConstructor(ExecutableElement constructor) {
        TypeElement klass = (TypeElement) constructor.getEnclosingElement();
        note("Processing constructor of class " + klass.getSimpleName(), constructor);
        if (classHasAtMost1AnnotatedConstructor(klass)) {
            analyzeDependencies(klass, constructor);
        } else {
            error("Class " + klass.getSimpleName() + " has more than one constructor annotated", klass);
        }
    }

    private boolean classHasAtMost1AnnotatedConstructor(TypeElement klass) {
        long constructorCount = klass.getEnclosedElements()
                .stream()
                .filter(element -> element.getAnnotation(Toothpick.class) != null)
                .count();
        return constructorCount < 2;
    }

    private void analyzeDependencies(TypeElement klass, ExecutableElement constructor) {
        graph.put(klass.asType(),
                constructor.getParameters()
                        .stream()
                        .map(Element::asType)
                        .collect(Collectors.toList()));
    }

    private void note(String message) {
        note(message, null);
    }

    private void note(String message, Element e) {
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                message,
                e);
    }

    private void error(String message, Element e) {
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                message,
                e);
    }
}
