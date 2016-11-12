package com.github.albertosh.magic.injection_2.compiler;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Generated;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes({
        "com.github.albertosh.magic.injection_2.compiler.Builder"
})
public class Compiler extends AbstractProcessor {

    private Trees trees;
    private TreeMaker make;
    private Names names;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        trees = Trees.instance(processingEnv);
        Context context = ((JavacProcessingEnvironment)
                processingEnv).getContext();
        make = TreeMaker.instance(context);
        names = Names.instance(context);
    }

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

            injectConstructor(builderClass, klass);
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

    private void injectConstructor(ClassName builderClassName, TypeElement klass) {
        JCTree tree = (JCTree) trees.getTree(klass);
        InjectConstructorVisitor visitor = new InjectConstructorVisitor(builderClassName, klass);
        tree.accept(visitor);
    }

    private class InjectConstructorVisitor extends TreeTranslator {

        private final ClassName builderClassName;
        private final TypeElement klass;

        public InjectConstructorVisitor(ClassName builderClassName, TypeElement klass) {
            this.builderClassName = builderClassName;
            this.klass = klass;
        }

        @Override
        public void visitClassDef(JCTree.JCClassDecl classNode) {
            super.visitClassDef(classNode);

            List<JCTree> members = new ArrayList<>();
            classNode.getMembers().forEach(members::add);

            JCTree.JCMethodDecl constructor = buildConstructorNode();
            members.add(constructor);

            JCTree[] asArray = members.toArray(new JCTree[members.size()]);
            classNode.defs = com.sun.tools.javac.util.List.from(asArray);

            result = classNode;
        }

        private JCTree.JCMethodDecl buildConstructorNode() {
            JCTree.JCModifiers modifiers = make.Modifiers(0L);

            Name methodName = names.init;

            JCTree.JCExpression returnType = null;

            com.sun.tools.javac.util.List<JCTree.JCTypeParameter> typeParameters = com.sun.tools.javac.util.List.nil();

            JCTree.JCVariableDecl builderArgument = buildBuilderArgument();
            com.sun.tools.javac.util.List<JCTree.JCVariableDecl> arguments = com.sun.tools.javac.util.List.of(builderArgument);

            com.sun.tools.javac.util.List<JCTree.JCExpression> exceptionsThrown = com.sun.tools.javac.util.List.nil();

            JCTree.JCBlock body = buildBody();

            JCTree.JCExpression defaultValue = null;

            return make.MethodDef(
                    modifiers,
                    methodName,
                    returnType,
                    typeParameters,
                    arguments,
                    exceptionsThrown,
                    body,
                    defaultValue);
        }

        private JCTree.JCVariableDecl buildBuilderArgument() {
            long flags = Flags.PARAMETER;
            JCTree.JCModifiers modifiers = make.Modifiers(flags);

            Name name = names.fromString("builder");

            JCTree.JCExpression paramClass = null;
            String[] split = builderClassName.toString().split("\\.");
            for (String segment : split) {
                if (paramClass == null)
                    paramClass = make.Ident(names.fromString(segment));
                else
                    paramClass = make.Select(paramClass, names.fromString(segment));
            }

            JCTree.JCExpression init = null;

            return make.VarDef(
                    modifiers,
                    name,
                    paramClass,
                    init);
        }

        private JCTree.JCBlock buildBody() {
            JCTree.JCExpression builderExpression = make.Ident(names.fromString("builder"));

            ListBuffer<JCTree.JCStatement> statementsBuffer = new ListBuffer<JCTree.JCStatement>();

            klass.getEnclosedElements().stream()
                    .filter(element -> element.getKind().isField())
                    .map(element -> (VariableElement) element)
                    .map(field -> {
                        String fieldNameAsString = field.getSimpleName().toString();
                        Name fieldName = names.fromString(fieldNameAsString);
                        JCTree.JCIdent thisExpression = make.Ident(names._this);
                        JCTree.JCFieldAccess thisX = make.Select(thisExpression, fieldName);

                        String methodNameAsString = "get"
                                + fieldNameAsString.substring(0, 1).toUpperCase()
                                + fieldNameAsString.substring(1);
                        Name methodName = names.fromString(methodNameAsString);
                        JCTree.JCExpression methodExpression = make.Select(builderExpression, methodName);

                        com.sun.tools.javac.util.List<JCTree.JCExpression> typeArgs = com.sun.tools.javac.util.List.nil();
                        com.sun.tools.javac.util.List<JCTree.JCExpression> args = com.sun.tools.javac.util.List.nil();
                        JCTree.JCMethodInvocation invocation = make.Apply(
                                typeArgs,
                                methodExpression,
                                args);
                        JCTree.JCExpressionStatement execInvocation = make.Exec(invocation);

                        JCTree.JCAssign assign = make.Assign(thisX, execInvocation.getExpression());
                        return make.Exec(assign);
                    }).forEach(statementsBuffer::add);

            com.sun.tools.javac.util.List<JCTree.JCStatement> statements = statementsBuffer.toList();
            return make.Block(0L, statements);
        }

    }



}
