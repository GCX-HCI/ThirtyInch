package net.grandcentrix.thirtyinch.lint.detector

import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.TextFormat
import net.grandcentrix.thirtyinch.lint.TiIssue.GetViewOrThrowInOnAttach
import org.jetbrains.uast.UBlockExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UDeclarationsExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.ULocalVariable
import org.jetbrains.uast.UReturnExpression
import org.jetbrains.uast.getUastContext
import org.jetbrains.uast.toUElement

private const val TI_METHOD_ONATTACHVIEW = "onAttachView"
private const val TI_METHOD_GETVIEWORTHROW = "getViewOrThrow"

private const val MAX_TRANSITIVE_CHECK_DEPTH = 5

private val TI_CLASS_NAMES = listOf(
        "net.grandcentrix.thirtyinch.TiPresenter"
)

class GetViewOrThrowInOnAttachDetector : Detector(), Detector.UastScanner {

    companion object {
        val ISSUE = GetViewOrThrowInOnAttach.asLintIssue(
                GetViewOrThrowInOnAttachDetector::class.java,
                "When using getViewOrThrow() in TiPresenter.onAttachView() the view might still be null." +
                        " So getViewOrThrow() might throw an exception during runtime." +
                        " Consider using the view parameter of TiPresenter.onAttachView() directly."
        )
    }

    override fun applicableSuperClasses(): List<String> = TI_CLASS_NAMES

    override fun visitClass(context: JavaContext, declaration: UClass) {
        val methods = declaration.findMethodsByName(TI_METHOD_ONATTACHVIEW, true)

        methods
                .mapNotNull { method -> method.toUElement()?.getUastContext()?.getMethodBody(method) }
                .forEach { methodBody -> checkForTransitiveUsage(context, methodBody) }
    }

    private fun checkForTransitiveUsage(context: JavaContext, uElement: UElement, depth: Int = 0) {
        if (depth > MAX_TRANSITIVE_CHECK_DEPTH) return // limit check of call cascades to reduce lint check speed

        when (uElement) {
            is UBlockExpression -> {
                uElement.expressions.forEach { checkForTransitiveUsage(context, it) }
            }
            is UDeclarationsExpression -> {
                uElement.declarations.forEach { checkForTransitiveUsage(context, it) }
            }
            is ULocalVariable -> {
                uElement.uastInitializer?.run { checkForTransitiveUsage(context, uElement = this) }
            }
            is UReturnExpression -> {
                uElement.returnExpression?.run { checkForTransitiveUsage(context, uElement = this) }
            }
            is UCallExpression -> {
                if (shouldWarn(uElement)) report(context, uElement)
                else {
                    uElement.resolve()
                            ?.let { uElement.getUastContext().getMethodBody(it) }
                            ?.run { checkForTransitiveUsage(context, uElement = this, depth = depth + 1) }
                }
            }
        }
    }

    private fun shouldWarn(call: UCallExpression): Boolean {
        return call.valueArgumentCount == 0 && TI_METHOD_GETVIEWORTHROW == call.methodName
        // TODO do a check if the method is from the right class
    }

    private fun report(context: JavaContext, element: UCallExpression) {
        context.report(
                ISSUE,
                context.getLocation(element.methodIdentifier ?: element),
                ISSUE.getBriefDescription(TextFormat.TEXT)
        )
    }
}