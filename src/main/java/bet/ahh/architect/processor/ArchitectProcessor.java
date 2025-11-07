package bet.ahh.architect.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import bet.ahh.architect.Architect;

@SupportedAnnotationTypes("bet.ahh.architect.Architect")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class ArchitectProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Architect.class)) {
            if (!(element instanceof ExecutableElement constructor)) {
                continue;
            }

            try {
                generateBuilder(constructor);
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "Failed to generate builder: " + e.getMessage(),
                        element);
            }
        }
        return true;
    }

    private void generateBuilder(ExecutableElement constructor) throws IOException {
        TypeElement typeElement = (TypeElement) constructor.getEnclosingElement();
        List<? extends VariableElement> parameters = constructor.getParameters();

        List<ParameterInfo> mandatoryParams = new ArrayList<>();
        List<ParameterInfo> optionalParams = new ArrayList<>();

        for (VariableElement param : parameters) {
            ParameterInfo paramInfo = new ParameterInfo(param);
            if (paramInfo.isOptional()) {
                optionalParams.add(paramInfo);
            } else {
                mandatoryParams.add(paramInfo);
            }
        }

        String packageName = processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
        String className = typeElement.getSimpleName().toString();
        String builderClassName = className + "Builder";

        List<TypeSpec> stageInterfaces = generateStageInterfaces(mandatoryParams, optionalParams, className,
                builderClassName);
        TypeSpec builderClass = generateBuilderClass(
                builderClassName,
                className,
                mandatoryParams,
                optionalParams,
                stageInterfaces);

        JavaFile javaFile = JavaFile.builder(packageName, builderClass)
                .build();

        javaFile.writeTo(processingEnv.getFiler());
    }

    private List<TypeSpec> generateStageInterfaces(
            List<ParameterInfo> mandatoryParams,
            List<ParameterInfo> optionalParams,
            String className,
            String builderClassName) {
        List<TypeSpec> interfaces = new ArrayList<>();

        for (int i = 0; i < mandatoryParams.size(); i++) {
            ParameterInfo param = mandatoryParams.get(i);
            String nextStage = (i == mandatoryParams.size() - 1)
                    ? "BuildStage"
                    : mandatoryParams.get(i + 1).stageName();

            TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(param.stageName())
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

            ParameterSpec.Builder paramBuilder = ParameterSpec.builder(param.parameterType(), param.name());
            if (!param.parameterType().isPrimitive()) {
                paramBuilder.addAnnotation(
                        AnnotationSpec.builder(ClassName.get("org.jetbrains.annotations", "NotNull")).build());
            }
            ParameterSpec paramSpec = paramBuilder.build();

            MethodSpec method = MethodSpec.methodBuilder(param.name())
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addParameter(paramSpec)
                    .returns(ClassName.bestGuess(nextStage))
                    .build();

            interfaceBuilder.addMethod(method);
            interfaces.add(interfaceBuilder.build());
        }

        TypeSpec.Builder buildStageBuilder = TypeSpec.interfaceBuilder("BuildStage")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        for (ParameterInfo param : optionalParams) {
            ParameterSpec.Builder paramBuilder = ParameterSpec.builder(param.unwrappedType(), param.name());
            if (param.isNullable() && !param.unwrappedType().isPrimitive()) {
                paramBuilder.addAnnotation(
                        AnnotationSpec.builder(ClassName.get("org.jetbrains.annotations", "Nullable")).build());
            } else if (!param.unwrappedType().isPrimitive()) {
                paramBuilder.addAnnotation(
                        AnnotationSpec.builder(ClassName.get("org.jetbrains.annotations", "NotNull")).build());
            }
            ParameterSpec paramSpec = paramBuilder.build();

            MethodSpec method = MethodSpec.methodBuilder(param.name())
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addParameter(paramSpec)
                    .returns(ClassName.bestGuess("BuildStage"))
                    .build();
            buildStageBuilder.addMethod(method);
        }

        MethodSpec buildMethod = MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(ClassName.bestGuess(className))
                .build();
        buildStageBuilder.addMethod(buildMethod);

        interfaces.add(buildStageBuilder.build());
        return interfaces;
    }

    private TypeSpec generateBuilderClass(
            String builderClassName,
            String className,
            List<ParameterInfo> mandatoryParams,
            List<ParameterInfo> optionalParams,
            List<TypeSpec> stageInterfaces) {
        TypeSpec.Builder outerBuilder = TypeSpec.classBuilder(builderClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        for (TypeSpec stage : stageInterfaces) {
            outerBuilder.addType(stage);
        }

        TypeSpec.Builder implBuilder = TypeSpec.classBuilder("BuilderImpl")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);

        for (TypeSpec stage : stageInterfaces) {
            implBuilder.addSuperinterface(ClassName.bestGuess(stage.name));
        }

        for (ParameterInfo param : mandatoryParams) {
            implBuilder.addField(param.type(), param.name(), Modifier.PRIVATE);
        }

        for (ParameterInfo param : optionalParams) {
            implBuilder.addField(param.unwrappedType(), param.name(), Modifier.PRIVATE);
        }

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .build();
        implBuilder.addMethod(constructor);

        String firstStage = mandatoryParams.isEmpty()
                ? "BuildStage"
                : mandatoryParams.get(0).stageName();

        MethodSpec builderMethod = MethodSpec.methodBuilder("builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ClassName.bestGuess(firstStage))
                .addStatement("return new BuilderImpl()")
                .build();
        outerBuilder.addMethod(builderMethod);

        for (int i = 0; i < mandatoryParams.size(); i++) {
            ParameterInfo param = mandatoryParams.get(i);
            String nextStage = (i == mandatoryParams.size() - 1)
                    ? "BuildStage"
                    : mandatoryParams.get(i + 1).stageName();

            ParameterSpec.Builder paramBuilder = ParameterSpec.builder(param.parameterType(), param.name());
            if (!param.parameterType().isPrimitive()) {
                paramBuilder.addAnnotation(
                        AnnotationSpec.builder(ClassName.get("org.jetbrains.annotations", "NotNull")).build());
            }
            ParameterSpec paramSpec = paramBuilder.build();

            MethodSpec setter = MethodSpec.methodBuilder(param.name())
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(paramSpec)
                    .returns(ClassName.bestGuess(nextStage))
                    .addStatement("this.$L = $L", param.name(), param.name())
                    .addStatement("return this")
                    .build();
            implBuilder.addMethod(setter);
        }

        for (ParameterInfo param : optionalParams) {
            ParameterSpec.Builder paramBuilder = ParameterSpec.builder(param.unwrappedType(), param.name());
            if (param.isNullable() && !param.unwrappedType().isPrimitive()) {
                paramBuilder.addAnnotation(
                        AnnotationSpec.builder(ClassName.get("org.jetbrains.annotations", "Nullable")).build());
            } else if (!param.unwrappedType().isPrimitive()) {
                paramBuilder.addAnnotation(
                        AnnotationSpec.builder(ClassName.get("org.jetbrains.annotations", "NotNull")).build());
            }
            ParameterSpec paramSpec = paramBuilder.build();

            MethodSpec setter = MethodSpec.methodBuilder(param.name())
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(paramSpec)
                    .returns(ClassName.bestGuess("BuildStage"))
                    .addStatement("this.$L = $L", param.name(), param.name())
                    .addStatement("return this")
                    .build();
            implBuilder.addMethod(setter);
        }

        CodeBlock.Builder constructorCall = CodeBlock.builder()
                .add("return new $L(\n", className)
                .indent();

        List<ParameterInfo> allParams = new ArrayList<>();
        allParams.addAll(mandatoryParams);
        allParams.addAll(optionalParams);

        for (int i = 0; i < allParams.size(); i++) {
            ParameterInfo param = allParams.get(i);
            if (param.isOptionalType()) {
                constructorCall.add("$T.ofNullable(this.$L)", Optional.class, param.name());
            } else {
                constructorCall.add("this.$L", param.name());
            }
            if (i < allParams.size() - 1) {
                constructorCall.add(",\n");
            }
        }

        constructorCall.unindent()
                .add("\n);");

        MethodSpec buildMethod = MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.bestGuess(className))
                .addCode(constructorCall.build())
                .build();
        implBuilder.addMethod(buildMethod);

        outerBuilder.addType(implBuilder.build());
        return outerBuilder.build();
    }

    private class ParameterInfo {
        private final VariableElement element;
        private final boolean         isOptionalType;
        private final boolean         isNullable;

        ParameterInfo(VariableElement element) {
            this.element = element;
            this.isOptionalType = isOptionalType(element.asType());
            this.isNullable = hasNullableAnnotation(element);
        }

        boolean isOptional() {
            return isOptionalType || isNullable;
        }

        boolean isOptionalType() {
            return isOptionalType;
        }

        boolean isNullable() {
            return isNullable;
        }

        String name() {
            return element.getSimpleName().toString();
        }

        String stageName() {
            String name = name();
            return Character.toUpperCase(name.charAt(0)) + name.substring(1) + "Stage";
        }

        TypeName type() {
            return TypeName.get(element.asType());
        }

        TypeName parameterType() {
            return isOptionalType ? unwrappedType() : type();
        }

        TypeName unwrappedType() {
            if (!isOptionalType) {
                return type();
            }
            DeclaredType declaredType = (DeclaredType) element.asType();
            TypeMirror typeArgument = declaredType.getTypeArguments().get(0);
            return TypeName.get(typeArgument);
        }

        private boolean isOptionalType(TypeMirror type) {
            if (!(type instanceof DeclaredType declaredType)) {
                return false;
            }
            Element element = declaredType.asElement();
            if (!(element instanceof TypeElement typeElement)) {
                return false;
            }
            return typeElement.getQualifiedName().toString().equals("java.util.Optional");
        }

        private boolean hasNullableAnnotation(VariableElement element) {
            return element.getAnnotationMirrors().stream()
                    .anyMatch(am -> {
                        String annotationName = am.getAnnotationType().asElement().getSimpleName().toString();
                        return annotationName.equals("Nullable");
                    });
        }
    }
}
