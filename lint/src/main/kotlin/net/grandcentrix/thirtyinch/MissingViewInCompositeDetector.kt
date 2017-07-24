package net.grandcentrix.thirtyinch

import com.android.annotations.VisibleForTesting
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.intellij.psi.*
import org.jetbrains.uast.*

private val ADD_PLUGIN_METHOD = "addPlugin"
private val TI_ACTIVITY_PLUGIN_NAME = "TiActivityPlugin"
private val TI_FRAGMENT_PLUGIN_NAME = "TiFragmentPlugin"
private val CA_CLASS_NAMES = listOf(
        "com.pascalwelsch.compositeandroid.activity.CompositeActivity",
        "com.pascalwelsch.compositeandroid.fragment.CompositeFragment")

class MissingViewInCompositeDetector : BaseMissingViewDetector() {

    companion object {
        @VisibleForTesting
        val ISSUE: Issue = Issues.MISSING_VIEW.create(
                MissingViewInCompositeDetector::class.java,
                "When using ThirtyInch, a class extending CompositeActivity or CompositeFragment " +
                        "has to implement the TiView interface associated with it in its signature, " +
                        "if it applies the respective plugin as well.")
    }

    override fun applicableSuperClasses() = CA_CLASS_NAMES

    override val issue: Issue
        get() = ISSUE

    override fun tryFindViewInterface(context: JavaContext, declaration: UClass, extendedType: PsiClassType, resolvedType: PsiClass): PsiType? {
        // Expect TiPlugin to be applied in the extended CA class
        var defaultConstructor: PsiMethod? = null
        for (constructor in declaration.constructors) {
            if (constructor.typeParameters.size == 0) {
                // Found default constructor
                defaultConstructor = constructor
                break
            }
        }

        if (defaultConstructor == null) {
            return null
        }

        val uastContext = declaration.getUastContext()
        val body = uastContext.getMethodBody(defaultConstructor)
        return tryFindViewFromCompositeConstructor(context, declaration, body)
    }

    private fun tryFindViewFromCompositeConstructor(context: JavaContext, declaration: UClass, expression: UExpression?): PsiType? {
        if (expression == null) {
            return null
        }

        if (expression is UBlockExpression) {
            // Unwrap block statements
            for (child in expression.expressions) {
                val resolved = tryFindViewFromCompositeConstructor(context, declaration, child)
                if (resolved != null) {
                    return resolved
                }
            }

        } else if (expression is UCallExpression) {
            // Inspect call sites
            val call = expression as UCallExpression?
            if (ADD_PLUGIN_METHOD == call!!.methodName && call.valueArgumentCount == 1) {
                // Expect a plugin to be used as the only argument to this method
                val argument = call.valueArguments[0]
                if (argument is UCallExpression) {
                    val argReference = argument.classReference ?: return null

                    val resolvedName = argReference.resolvedName
                    if (TI_ACTIVITY_PLUGIN_NAME == resolvedName || TI_FRAGMENT_PLUGIN_NAME == resolvedName) {
                        // Matching names. Finally, find the type parameters passed to the plugin
                        val psiReference = argReference.psi as PsiJavaCodeReferenceElement? ?: return null

                        val parameterTypes = psiReference.typeParameters
                        if (parameterTypes.size != 2) {
                            return null
                        }

                        return parameterTypes[1]
                    }
                }
            }
        }

        return null
    }

    override fun allowMissingViewInterface(context: JavaContext, declaration: UClass, viewInterface: PsiType) = false
}
