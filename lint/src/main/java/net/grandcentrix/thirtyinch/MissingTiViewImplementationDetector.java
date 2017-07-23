package net.grandcentrix.thirtyinch;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.TextFormat;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeParameter;
import com.intellij.psi.util.PsiUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

// TODO Check for CompositeActivity
@SuppressWarnings("WeakerAccess")
public final class MissingTiViewImplementationDetector extends Detector implements Detector.JavaPsiScanner {

    static final Issue ISSUE = Issue.create(
            "MissingTiViewImplementation",
            "TiView Implementation missing in class",
            "When using ThirtyInch, a class extending TiActivity, TiFragment or CompositeActivity " +
                    "has to implement the TiView interface associated with it in its signature, " +
                    "or implement `provideView()` instead to override this default behaviour.",
            Category.CORRECTNESS,
            8,
            Severity.ERROR,
            new Implementation(MissingTiViewImplementationDetector.class, EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES)));

    @Override
    public List<Class<? extends PsiElement>> getApplicablePsiTypes() {
        return Collections.<Class<? extends PsiElement>>singletonList(PsiClass.class);
    }

    @Override
    public JavaElementVisitor createPsiVisitor(JavaContext context) {
        if (context.isEnabled(ISSUE)) {
            return new ClassVisitor(context);
        }

        return null;
    }

    private static final class ClassVisitor extends JavaElementVisitor {

        private static final String TI_VIEW_FQ = "net.grandcentrix.thirtyinch.TiView";
        private static final String PROVIDE_VIEW_METHOD = "provideView";
        private static final List<String> TI_CLASS_NAMES = unmodifiableList(asList(
                "net.grandcentrix.thirtyinch.TiActivity",
                "net.grandcentrix.thirtyinch.TiFragment"));
        private static final String ADD_PLUGIN_METHOD = "addPlugin";
        private static final String TI_ACTIVITY_PLUGIN_NAME = "TiActivityPlugin";
        private static final String TI_FRAGMENT_PLUGIN_NAME = "TiFragmentPlugin";
        private static final List<String> CA_CLASS_NAMES = unmodifiableList(asList(
                "com.pascalwelsch.compositeandroid.activity.CompositeActivity",
                "com.pascalwelsch.compositeandroid.fragment.CompositeFragment"));

        private final JavaContext context;

        ClassVisitor(JavaContext context) {
            this.context = context;
        }

        @Override
        public void visitClass(PsiClass declaration) {
            // Don't trigger on abstract classes
            if (PsiUtil.isAbstractClass(declaration)) {
                return;
            }

            // Extract the MVP View interface type from the class
            PsiType viewInterface = resolveRelevantViewInterface(declaration);
            if (viewInterface == null) {
                return;
            }

            // Check if the class implements that interface as well
            if (!hasViewInterfaceImplementation(declaration, viewInterface)) {
                // Interface not implemented; check if provideView() is overridden instead
                boolean detectedOverride = false;
                for (PsiMethod method : declaration.findMethodsByName(PROVIDE_VIEW_METHOD, true)) {
                    if (viewInterface.equals(method.getReturnType())) {
                        detectedOverride = true;
                        break;
                    }
                }

                if (!detectedOverride) {
                    // Invalid state: Report issue for this class
                    context.report(ISSUE, context.getLocation(declaration.getNameIdentifier()), ISSUE.getBriefDescription(TextFormat.TEXT));
                }
            }
        }

        @Nullable
        private PsiType resolveRelevantViewInterface(@NotNull PsiClass declaration) {
            for (PsiClassType extendedType : declaration.getExtendsListTypes()) {
                PsiClass resolvedType = extendedType.resolveGenerics().getElement();
                if (resolvedType == null) {
                    logInternalError("Unable to resolve type '" + extendedType.getClassName() + "', extended by class '" + declaration.getName() + "'");
                    return null;
                }

                String qualifiedName = resolvedType.getQualifiedName();
                if (TI_CLASS_NAMES.contains(qualifiedName)) {
                    // ThirtyInch-based inheritance
                    return resolveFromThirtyInchClass(declaration, extendedType, resolvedType);
                }

                if (CA_CLASS_NAMES.contains(qualifiedName)) {
                    // CompositeAndroid-based inheritance
                    return resolveFromCompositeAndroidClass(declaration, extendedType, resolvedType);
                }

                // Crawl up the type hierarchy to catch declarations in super classes
                return resolveRelevantViewInterface(resolvedType);
            }

            return null;
        }

        private boolean hasViewInterfaceImplementation(@NotNull PsiClass declaration, @NotNull PsiType viewInterface) {
            for (PsiClassType implementedType : declaration.getImplementsListTypes()) {
                if (implementedType.equals(viewInterface)) {
                    return true;
                }

                PsiClass resolvedType = implementedType.resolve();
                if (resolvedType == null) {
                    logInternalError("Unable to resolve implemented type '" + implementedType.getClassName() + "', extended by class '" + declaration.getName() + "'");
                    continue;
                }
                return hasViewInterfaceImplementation(resolvedType, viewInterface);
            }

            return false;
        }

        @Nullable
        private PsiType resolveFromThirtyInchClass(PsiClass declaration, PsiClassType extendedType, PsiClass resolvedType) {
            // Expect <P extends TiPresenter, V extends TiView> signature in the extended Ti class
            PsiType[] parameters = extendedType.getParameters();
            PsiTypeParameter[] parameterTypes = resolvedType.getTypeParameters();
            if (parameters.length != 2 || parameterTypes.length != 2) {
                return null;
            }

            // Check that the second type parameter is actually a TiView
            PsiTypeParameter parameterType = parameterTypes[1];
            PsiType parameter = parameters[1];
            for (PsiClassType extendedParamType : parameterType.getExtendsListTypes()) {
                PsiClass resolvedParamType = extendedParamType.resolveGenerics().getElement();
                if (resolvedParamType == null) {
                    logInternalError("Unable to resolve type '" + extendedParamType.getClassName() + "', used as parameter");
                    return null;
                }

                if (TI_VIEW_FQ.equals(resolvedParamType.getQualifiedName())) {
                    return parameter;
                }
            }

            return null;
        }

        @Nullable
        private PsiType resolveFromCompositeAndroidClass(PsiClass declaration, PsiClassType extendedType, PsiClass resolvedType) {
            // Expect TiPlugin to be applied in the extended CA class
            PsiMethod defaultConstructor = null;
            for (PsiMethod constructor : declaration.getConstructors()) {
                if (constructor.getTypeParameters().length == 0) {
                    // Found default constructor
                    defaultConstructor = constructor;
                    break;
                }
            }

            if (defaultConstructor == null) {
                return null;
            }

//            PsiCodeBlock body = defaultConstructor.getBody();
//            if (body == null) {
//                context.log(null, "Body null");
//                return null;
//            }
//
//            // Search for the registration of a Ti-based plugin inside the constructor
//            for (PsiStatement statement : body.getStatements()) {
//                context.log(null, "\t" + statement.getText());
//
//                PsiType resolved = resolveCompositeAndroidConstructorStatement(statement);
//                if (resolved != null) {
//                    return resolved;
//                }
////                String text = statement.getText();
////                if (text.contains(ADD_PLUGIN_METHOD) &&
////                        (text.contains(TI_ACTIVITY_PLUGIN_NAME) || text.contains(TI_FRAGMENT_PLUGIN_NAME))) {
////
////                }
//            }

            return null;
        }

        @Nullable
        private PsiType resolveCompositeAndroidConstructorStatement(PsiElement element) {
            context.log(null, "\t\t" + element.getClass() + "\t" + element.getText());

            for (PsiElement child : element.getChildren()) {
                PsiType resolved = resolveCompositeAndroidConstructorStatement(child);
                if (resolved != null) {
                    return resolved;
                }
            }

            return null;
        }

        private void logInternalError(String message) {
            context.log(new IllegalStateException(message), "");
        }
    }
}
