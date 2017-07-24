package net.grandcentrix.thirtyinch

import com.android.annotations.VisibleForTesting
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiJavaCodeReferenceElement
import com.intellij.psi.PsiType
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
        // Found default constructor
        val defaultConstructor = declaration.constructors
                .filter { it.typeParameters.isEmpty() }
                .firstOrNull()

        defaultConstructor?.let {
            val uastContext = declaration.getUastContext()
            val body = uastContext.getMethodBody(defaultConstructor)
            return tryFindViewFromCompositeConstructor(context, declaration, body)
        }

        return null
    }

    private fun tryFindViewFromCompositeConstructor(context: JavaContext, declaration: UClass, expression: UExpression?): PsiType? {
        if (expression == null) {
            return null
        }

        when (expression) {
            is UBlockExpression -> {
                // Unwrap block statements; the first resolvable result is returned
                expression.expressions
                        .mapNotNull { tryFindViewFromCompositeConstructor(context, declaration, it) }
                        .forEach { return it }
            }

            is UCallExpression -> {
                // Inspect call sites
                if (ADD_PLUGIN_METHOD == expression.methodName && expression.valueArgumentCount == 1) {
                    // Expect a plugin to be used as the only argument to this method
                    val argument = expression.valueArguments[0]

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
        }

        return null
    }

    override fun allowMissingViewInterface(context: JavaContext, declaration: UClass, viewInterface: PsiType) = false
}
