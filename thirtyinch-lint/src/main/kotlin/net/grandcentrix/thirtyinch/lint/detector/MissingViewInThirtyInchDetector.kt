package net.grandcentrix.thirtyinch.lint.detector

import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiType
import net.grandcentrix.thirtyinch.lint.TiIssue.MissingView
import org.jetbrains.kotlin.utils.addToStdlib.firstNotNullResult
import org.jetbrains.uast.UClass

private const val TI_VIEW_FQ = "net.grandcentrix.thirtyinch.TiView"
private const val PROVIDE_VIEW_METHOD = "provideView"
private val TI_CLASS_NAMES = listOf(
        "net.grandcentrix.thirtyinch.TiActivity",
        "net.grandcentrix.thirtyinch.TiFragment",
        "net.grandcentrix.thirtyinch.TiDialogFragment"
)

class MissingViewInThirtyInchDetector : BaseMissingViewDetector() {
    companion object {
        val ISSUE = MissingView.asLintIssue(
                MissingViewInThirtyInchDetector::class.java,
                "When using ThirtyInch, a class extending TiActivity or TiFragment " +
                        "has to implement the TiView interface associated with it in its signature, " +
                        "or implement `provideView()` instead to override this default behaviour."
        )
    }

    override fun applicableSuperClasses() = TI_CLASS_NAMES

    override val issue: Issue = ISSUE

    override fun findViewInterface(context: JavaContext, declaration: UClass): PsiType? {
        return declaration.extendsListTypes
                .firstNotNullResult { extendedType -> tryFindViewInterface(extendedType) }
    }

    private fun tryFindViewInterface(extendedType: PsiClassType): PsiType? {
        val resolvedType = extendedType.resolveGenerics().element ?: return null

        val parameters = extendedType.parameters
        val parameterTypes = resolvedType.typeParameters

        check(parameters.size == parameterTypes.size) { "Got different Array Sizes" }

        return parameters
                .mapIndexed { i, psiType -> Pair(psiType, parameterTypes[i]) }
                .firstNotNullResult { (type, typeParameter) ->
                    typeParameter.extendsListTypes
                            .map { it.resolveGenerics().element }
                            .filter { TI_VIEW_FQ == it?.qualifiedName }
                            .map { type }
                            .firstOrNull()
                            ?: (type as? PsiClassType)?.let { tryFindViewInterface(it) }
                }
    }

    override fun allowMissingViewInterface(context: JavaContext, declaration: UClass,
            viewInterface: PsiType): Boolean {
        // Interface not implemented; check if provideView() is overridden instead
        return declaration.findMethodsByName(PROVIDE_VIEW_METHOD, true)
                .any { viewInterface == it.returnType }
    }
}