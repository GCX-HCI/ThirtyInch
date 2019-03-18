package net.grandcentrix.thirtyinch.lint.detector

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Detector.UastScanner
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.TextFormat
import com.intellij.psi.PsiType
import net.grandcentrix.thirtyinch.lint.TiIssue
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UMethod

private const val FQN_ANNOTATION_DISTINCTUNTILCHANGED = "net.grandcentrix.thirtyinch.distinctuntilchanged.DistinctUntilChanged"

class DistinctUntilChangedUsageDetector : Detector(), UastScanner {
    companion object {
        val ISSUE_NO_PARAMETER = TiIssue.DistinctUntilChangedWithoutParameter.asLintIssue(
                detectorCls = DistinctUntilChangedUsageDetector::class.java
        )
        val ISSUE_NON_VOID_RETURN_TYPE = TiIssue.AnnotationOnNonVoidMethod.asLintIssue(
                detectorCls = DistinctUntilChangedUsageDetector::class.java
        )
    }

    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(
            UAnnotation::class.java
    )

    override fun createUastHandler(context: JavaContext): UElementHandler = object : UElementHandler() {
        override fun visitAnnotation(node: UAnnotation) {
            if (node.qualifiedName != FQN_ANNOTATION_DISTINCTUNTILCHANGED) return

            val method = node.uastParent as? UMethod ?: return

            if (!method.hasParameters()) {
                context.report(
                        ISSUE_NO_PARAMETER,
                        context.getLocation(node),
                        ISSUE_NO_PARAMETER.getBriefDescription(TextFormat.TEXT)
                )
            }
            
            if (method.returnType != PsiType.VOID) {
                context.report(
                        ISSUE_NON_VOID_RETURN_TYPE,
                        context.getLocation(node),
                        ISSUE_NON_VOID_RETURN_TYPE.getBriefDescription(TextFormat.TEXT)
                )
            }
        }
    }
}