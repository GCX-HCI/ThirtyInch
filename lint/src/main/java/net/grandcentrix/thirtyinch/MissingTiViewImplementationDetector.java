package net.grandcentrix.thirtyinch;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.TextFormat;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeParameter;
import com.intellij.psi.util.PsiUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UBlockExpression;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UExpression;
import org.jetbrains.uast.UReferenceExpression;
import org.jetbrains.uast.UastContext;
import org.jetbrains.uast.UastUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

@SuppressWarnings("WeakerAccess")
public final class MissingTiViewImplementationDetector extends Detector implements Detector.UastScanner {

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
    public List<String> applicableSuperClasses() {
        List<String> classes = new ArrayList<>();
        classes.addAll(TI_CLASS_NAMES);
        classes.addAll(CA_CLASS_NAMES);
        return classes;
    }

    @Override
    public void visitClass(JavaContext context, UClass declaration) {
        super.visitClass(context, declaration);

        if (!context.isEnabled(ISSUE)) {
            return;
        }

        // Don't trigger on abstract classes
        if (PsiUtil.isAbstractClass(declaration.getPsi())) {
            return;
        }

        // Extract the MVP View type from the declaration
        PsiType viewInterface = tryFindViewInterface(context, declaration);
        if (viewInterface == null) {
            return;
        }

        // Check if the class implements that interface as well
        if (!tryFindViewImplementation(context, declaration, viewInterface)) {
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
    private PsiType tryFindViewInterface(JavaContext context, UClass declaration) {
        for (PsiClassType extendedType : declaration.getExtendsListTypes()) {
            PsiClass resolvedType = extendedType.resolveGenerics().getElement();
            if (resolvedType == null) {
                context.log(new IllegalStateException("Unable to resolve type '" + extendedType.getClassName() + "', extended by class '" + declaration.getName() + "'"), "");
                return null;
            }

            String qualifiedName = resolvedType.getQualifiedName();
            if (TI_CLASS_NAMES.contains(qualifiedName)) {
                // ThirtyInch-based inheritance
                return tryFindViewFromTiClass(context, declaration, extendedType, resolvedType);
            }

            if (CA_CLASS_NAMES.contains(qualifiedName)) {
                // CompositeAndroid-based inheritance
                return tryFindViewFromCompositeClass(context, declaration, extendedType, resolvedType);
            }

            // Crawl up the type hierarchy to catch declarations in super classes
            UastContext uastContext = UastUtils.getUastContext(declaration);
            return tryFindViewInterface(context, uastContext.getClass(resolvedType));
        }

        return null;
    }

    private boolean tryFindViewImplementation(JavaContext context, @NotNull UClass declaration, @NotNull PsiType viewInterface) {
        for (PsiClassType implementedType : declaration.getImplementsListTypes()) {
            if (implementedType.equals(viewInterface)) {
                return true;
            }

            PsiClass resolvedType = implementedType.resolve();
            if (resolvedType == null) {
                context.log(new IllegalStateException("Unable to resolve implemented type '" + implementedType.getClassName() + "', extended by class '" + declaration.getName() + "'"), "");
                continue;
            }

            UastContext uastContext = UastUtils.getUastContext(declaration);
            return tryFindViewImplementation(context, uastContext.getClass(resolvedType), viewInterface);
        }

        return false;
    }

    @Nullable
    private PsiType tryFindViewFromTiClass(JavaContext context, UClass declaration, PsiClassType superType, PsiClass resolvedSuperType) {
        // Expect <P extends TiPresenter, V extends TiView> signature in the extended Ti class
        PsiType[] parameters = superType.getParameters();
        PsiTypeParameter[] parameterTypes = resolvedSuperType.getTypeParameters();
        if (parameters.length != 2 || parameterTypes.length != 2) {
            return null;
        }

        // Check that the second type parameter is actually a TiView
        PsiTypeParameter parameterType = parameterTypes[1];
        PsiType parameter = parameters[1];
        for (PsiClassType extendedParamType : parameterType.getExtendsListTypes()) {
            PsiClass resolvedParamType = extendedParamType.resolveGenerics().getElement();
            if (resolvedParamType == null) {
                context.log(new IllegalStateException("Unable to resolve type '" + extendedParamType.getClassName() + "', used as parameter"), "");
                return null;
            }

            if (TI_VIEW_FQ.equals(resolvedParamType.getQualifiedName())) {
                return parameter;
            }
        }

        return null;
    }

    @Nullable
    private PsiType tryFindViewFromCompositeClass(JavaContext context, UClass declaration, PsiClassType superType, PsiClass resolvedSuperType) {
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

        UastContext uastContext = UastUtils.getUastContext(declaration);
        UExpression body = uastContext.getMethodBody(defaultConstructor);
        return tryFindViewFromCompositeConstructor(context, declaration, body);
    }

    @Nullable
    private PsiType tryFindViewFromCompositeConstructor(JavaContext context, UClass declaration, @Nullable UExpression expression) {
        if (expression == null) {
            return null;
        }

        if (expression instanceof UBlockExpression) {
            // Unwrap block statements
            for (UExpression child : ((UBlockExpression) expression).getExpressions()) {
                PsiType resolved = tryFindViewFromCompositeConstructor(context, declaration, child);
                if (resolved != null) {
                    return resolved;
                }
            }

        } else if (expression instanceof UCallExpression) {
            // Inspect call sites
            UCallExpression call = (UCallExpression) expression;
            if (ADD_PLUGIN_METHOD.equals(call.getMethodName()) && call.getValueArgumentCount() == 1) {
                // Expect a plugin to be used as the only argument to this method
                UExpression argument = call.getValueArguments().get(0);
                if (argument instanceof UCallExpression) {
                    UReferenceExpression argReference = ((UCallExpression) argument).getClassReference();
                    if (argReference == null) {
                        return null;
                    }

                    String resolvedName = argReference.getResolvedName();
                    if (TI_ACTIVITY_PLUGIN_NAME.equals(resolvedName) || TI_FRAGMENT_PLUGIN_NAME.equals(resolvedName)) {
                        // Matching names. Finally, find the type parameters passed to the plugin
                        PsiJavaCodeReferenceElement psiReference = (PsiJavaCodeReferenceElement) argReference.getPsi();
                        if (psiReference == null) {
                            return null;
                        }

                        PsiType[] parameterTypes = psiReference.getTypeParameters();
                        if (parameterTypes.length != 2) {
                            return null;
                        }

                        return parameterTypes[1];
                    }
                }
            }
        }

        return null;
    }
}
