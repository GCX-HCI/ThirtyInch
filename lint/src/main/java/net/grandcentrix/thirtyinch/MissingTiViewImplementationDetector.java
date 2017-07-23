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
        private static final String TI_ACTIVITY_FQ = "net.grandcentrix.thirtyinch.TiActivity";
        private static final String TI_FRAGMENT_FQ = "net.grandcentrix.thirtyinch.TiFragment";
        private static final String PROVIDE_VIEW_METHOD = "provideView";

        private final JavaContext context;

        ClassVisitor(JavaContext context) {
            this.context = context;
        }

        @Override
        public void visitClass(PsiClass declaration) {
            super.visitClass(declaration);

            // Don't trigger on abstract classes
            if (PsiUtil.isAbstractClass(declaration)) {
                return;
            }

            // Extract the MVP View interface type from the class
            PsiType resolvedViewInterface = resolveRelevantViewInterface(declaration);
            if (resolvedViewInterface == null) {
                return;
            }

            // Check if the Activity implements that interface
            boolean detectedView = false;
            for (PsiClassType implementedType : declaration.getImplementsListTypes()) {
                if (implementedType.equals(resolvedViewInterface)) {
                    detectedView = true;
                    break;
                }
            }

            if (!detectedView) {
                // Check if provideView() is overridden instead
                boolean detectedOverride = false;
                for (PsiMethod method : declaration.findMethodsByName(PROVIDE_VIEW_METHOD, true)) {
                    if (resolvedViewInterface.equals(method.getReturnType())) {
                        detectedOverride = true;
                        break;
                    }
                }

                if (!detectedOverride) {
                    // Report issue for this class
                    context.report(ISSUE, context.getLocation(declaration.getNameIdentifier()), ISSUE.getBriefDescription(TextFormat.TEXT));
                }
            }
        }

        @Nullable
        private PsiType resolveRelevantViewInterface(@NotNull PsiClass declaration) {
            for (PsiClassType extendedType : declaration.getExtendsListTypes()) {
                PsiClass resolvedType = PsiUtil.resolveGenericsClassInType(extendedType).getElement();
                if (resolvedType == null) {
                    logInternalError("Unable to resolve type '" + extendedType.getClassName() + "', extended by class '" + declaration.getName() + "'");
                    return null;
                }

                String qualifiedName = resolvedType.getQualifiedName();
                if (TI_ACTIVITY_FQ.equals(qualifiedName) || TI_FRAGMENT_FQ.equals(qualifiedName)) {
                    return resolveViewFromParameters(resolvedType.getTypeParameters(), extendedType.getParameters());
                }
            }

            return null;
        }

        @Nullable
        private PsiType resolveViewFromParameters(PsiTypeParameter[] parameterTypes, PsiType[] parameters) {
            // Expect <P extends TiPresenter, V extends TiView>
            if (parameters.length != 2 || parameterTypes.length != 2) {
                return null;
            }

            // Check that the second type parameter is actually a TiView
            PsiTypeParameter parameterType = parameterTypes[1];
            PsiType parameter = parameters[1];
            for (PsiClassType extendedType : parameterType.getExtendsListTypes()) {
                PsiClass resolvedType = PsiUtil.resolveGenericsClassInType(extendedType).getElement();
                if (resolvedType == null) {
                    logInternalError("Unable to resolve type '" + extendedType.getClassName() + "', used as parameter");
                    return null;
                }

                if (TI_VIEW_FQ.equals(resolvedType.getQualifiedName())) {
                    return parameter;
                }
            }

            return null;
        }

        private void logInternalError(String message) {
            context.log(new IllegalStateException(message), "");
        }
    }
}
