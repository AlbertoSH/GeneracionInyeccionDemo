package com.github.albertosh.magic.injection_1.compiler;

import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

@SupportedAnnotationTypes({
        "com.github.albertosh.magic.injection_1.compiler.InjectGetter"
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

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        roundEnv.getElementsAnnotatedWith(InjectGetter.class)
                .stream()
                .map(element -> (VariableElement) element)
                .forEach(this::injectGetter);

        return true;
    }

    private void injectGetter(VariableElement field) {
        JCTree tree = (JCTree) trees.getTree(field.getEnclosingElement());
        TreeTranslator visitor = new InjectGetVisitor(field);
        tree.accept(visitor);
    }

    private class InjectGetVisitor extends TreeTranslator {

        private final VariableElement field;

        public InjectGetVisitor(VariableElement field) {
            this.field = field;
        }

        @Override
        public void visitClassDef(JCTree.JCClassDecl classNode) {
            super.visitClassDef(classNode);

            List<JCTree> members = new ArrayList<>();
            classNode.getMembers().forEach(members::add);

            JCTree.JCMethodDecl getter = buildGetterNode();
            members.add(getter);

            JCTree[] asArray = members.toArray(new JCTree[members.size()]);
            classNode.defs = com.sun.tools.javac.util.List.from(asArray);

            result = classNode;
        }

        private JCTree.JCMethodDecl buildGetterNode() {
            JCTree.JCModifiers modifiers = make.Modifiers(Flags.PUBLIC);

            String fieldName = field.getSimpleName().toString();
            String upperFieldName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            Name methodName = names.fromString("get" + upperFieldName);

            JCTree.JCExpression returnType = typeToJCExpression(field.asType());

            com.sun.tools.javac.util.List<JCTree.JCTypeParameter> typeParameters = com.sun.tools.javac.util.List.nil();

            com.sun.tools.javac.util.List<JCTree.JCVariableDecl> arguments = com.sun.tools.javac.util.List.nil();

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

        private JCTree.JCExpression typeToJCExpression(TypeMirror type) {
            JCTree.JCExpression expression = null;
            String[] split = type.toString().split("\\.");
            for (String segment : split) {
                if (expression == null)
                    expression = make.Ident(names.fromString(segment));
                else
                    expression = make.Select(expression, names.fromString(segment));
            }
            return expression;
        }

        private JCTree.JCBlock buildBody() {
            JCTree.JCExpression thisExpression = make.Ident(names._this);
            Name fieldName = names.fromString(field.getSimpleName().toString());
            JCTree.JCFieldAccess thisX = make.Select(thisExpression, fieldName);

            JCTree.JCStatement returnValueStatement = make.Return(thisX);

            long modifiers = 0L;
            com.sun.tools.javac.util.List statementList = com.sun.tools.javac.util.List.of(returnValueStatement);

            return make.Block(modifiers, statementList);
        }

    }
}
