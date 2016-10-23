package com.github.albertosh.magic.analysis_1.compiler;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes({
        "com.github.albertosh.magic.analysis_1.compiler.Analysis1"
})
public class Compiler extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        roundEnv.getElementsAnnotatedWith(Analysis1.class)
                .stream()
                .map(element -> (TypeElement) element)
                .forEach(this::processClass);

        return true;
    }

    private void processClass(TypeElement klass) {
        note("Processing class " + klass.getSimpleName(), klass);
        klass.getEnclosedElements()
                .stream()
                .filter(element -> element.getKind() == ElementKind.FIELD)
                .map(field -> (VariableElement) field)
                .forEach(this::processField);
    }

    private void processField(VariableElement field) {
        note("It has a field named " + field.getSimpleName() +
                " of type " + field.asType(), field);
    }


    private void note(String message, Element e) {
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                message,
                e);
    }
}
