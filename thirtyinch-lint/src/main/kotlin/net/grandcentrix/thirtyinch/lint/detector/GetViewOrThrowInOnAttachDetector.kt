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
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.UReturnExpression
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.getUastContext
import org.jetbrains.uast.toUElement

private const val TI_CLASS_PRESENTER = "net.grandcentrix.thirtyinch.TiPresenter"
private const val TI_METHOD_ONATTACHVIEW = "onAttachView"
private const val TI_METHOD_GETVIEWORTHROW = "getViewOrThrow"
private const val TI_REFERENCE_VIEWORTHROW = "viewOrThrow"

private const val MAX_TRANSITIVE_CHECK_DEPTH = 5

class GetViewOrThrowInOnAttachDetector : Detector(), Detector.UastScanner {

    companion object {
        val ISSUE = GetViewOrThrowInOnAttach.asLintIssue(
                GetViewOrThrowInOnAttachDetector::class.java,
                "When using getViewOrThrow() in TiPresenter.onAttachView() the view might still be null." +
                        " So getViewOrThrow() might throw an exception during runtime." +
                        " Consider using the view parameter of TiPresenter.onAttachView() directly."
        )
    }

    override fun applicableSuperClasses(): List<String> = listOf(TI_CLASS_PRESENTER)

    override fun visitClass(context: JavaContext, declaration: UClass) {
        val methods = declaration.findMethodsByName(TI_METHOD_ONATTACHVIEW, true)

        methods
                .mapNotNull { method -> method.toUElement()?.getUastContext()?.getMethodBody(method) }
                .forEach { methodBody -> checkForTransitiveUsage(context, methodBody) }
    }

    private fun checkForTransitiveUsage(
            context: JavaContext,
            uElement: UElement,
            depth: Int = 0,
            reportElement: UElement = uElement
    ) {
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
            is USimpleNameReferenceExpression -> {
                if (uElement.identifier == TI_REFERENCE_VIEWORTHROW) report(context, reportElement)
            }
            is UQualifiedReferenceExpression -> {
                checkForTransitiveUsage(context, uElement.receiver)
                checkForTransitiveUsage(context, uElement.selector, reportElement = uElement)
            }
            is UCallExpression -> {
                if (shouldWarn(context, uElement)) report(context, uElement.methodIdentifier ?: uElement)
                else {
                    uElement.resolve()
                            ?.let { uElement.getUastContext().getMethodBody(it) }
                            ?.run { checkForTransitiveUsage(context, uElement = this, depth = depth + 1) }
                }
            }
        }
    }

    private fun shouldWarn(context: JavaContext, call: UCallExpression): Boolean {
        return call.valueArgumentCount == 0
                && TI_METHOD_GETVIEWORTHROW == call.methodName
                && call.resolve()?.let { context.evaluator.isMemberInClass(it, TI_CLASS_PRESENTER) } == true
    }

    private fun report(context: JavaContext, element: UElement) {
        context.report(
                ISSUE,
                context.getLocation(element),
                ISSUE.getBriefDescription(TextFormat.TEXT)
        )
    }
}