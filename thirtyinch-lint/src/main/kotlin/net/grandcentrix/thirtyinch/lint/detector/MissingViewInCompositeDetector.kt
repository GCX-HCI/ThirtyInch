package net.grandcentrix.thirtyinch.lint.detector

import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.intellij.psi.PsiJavaCodeReferenceElement
import com.intellij.psi.PsiType
import net.grandcentrix.thirtyinch.lint.TiIssue.MissingView
import org.jetbrains.uast.UBlockExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.getUastContext

private const val ADD_PLUGIN_METHOD = "addPlugin"
private const val TI_ACTIVITY_PLUGIN_NAME = "TiActivityPlugin"
private const val TI_FRAGMENT_PLUGIN_NAME = "TiFragmentPlugin"
private val CA_CLASS_NAMES = listOf(
        "com.pascalwelsch.compositeandroid.activity.CompositeActivity",
        "com.pascalwelsch.compositeandroid.fragment.CompositeFragment"
)

class MissingViewInCompositeDetector : BaseMissingViewDetector() {
    companion object {
        val ISSUE = MissingView.asLintIssue(
                MissingViewInCompositeDetector::class.java,
                "When using ThirtyInch, a class extending CompositeActivity or CompositeFragment " +
                        "has to implement the TiView interface associated with it in its signature, " +
                        "if it applies the respective plugin as well."
        )
    }

    override fun applicableSuperClasses() = CA_CLASS_NAMES

    override val issue: Issue = MissingViewInThirtyInchDetector.ISSUE

    override fun findViewInterface(context: JavaContext, declaration: UClass): PsiType? {
        // Expect TiPlugin to be applied in the extended CA class
        // Found default constructor
        val defaultConstructor = declaration.constructors.firstOrNull { it.typeParameters.isEmpty() }

        defaultConstructor?.let {
            val uastContext = declaration.getUastContext()
            val body = uastContext.getMethodBody(defaultConstructor)
            return tryFindViewFromCompositeConstructor(context, declaration, body)
        }
        return null
    }

    private fun tryFindViewFromCompositeConstructor(context: JavaContext, declaration: UClass,
            expression: UExpression?): PsiType? {
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