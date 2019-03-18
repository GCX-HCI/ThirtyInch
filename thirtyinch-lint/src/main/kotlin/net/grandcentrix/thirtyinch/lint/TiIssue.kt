package net.grandcentrix.thirtyinch.lint

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity

private val CATEGORY_TI = Category.create("ThirtyInch", 90)

sealed class TiIssue(
        val id: String,
        val briefDescription: String,
        val longDescription: String = briefDescription,
        val category: Category,
        val priority: Int,
        val severity: Severity
) {

    object MissingView : TiIssue(
            id = "MissingTiViewImplementation",
            briefDescription = "TiView Implementation missing in class",
            category = CATEGORY_TI,
            priority = 8,
            severity = Severity.ERROR
    )

    object DistinctUntilChangedWithoutParameter : TiIssue(
            id = "DistinctUntilChangedWithoutParameter",
            briefDescription = "@DistinctUntilChanged Annotation on a method without Parameter is useless",
            longDescription = "When using the @DistinctUntilChanged annotation on a method without parameter the method call will be executed each time. @DistinctUntilChanged needs at least one parameter to check if it changed compared to the last method invocation.",
            category = CATEGORY_TI,
            priority = 5,
            severity = Severity.WARNING
    )

    object AnnotationOnNonVoidMethod : TiIssue(
            id = "TiAnnotationOnNonVoidMethod",
            briefDescription = "Annotation of a non Void method is not supported in ThirtyInch",
            longDescription = "When using a ThirtyInch annotation on a method without parameter the method call will be executed each time. Return types are not supported.",
            category = CATEGORY_TI,
            priority = 5,
            severity = Severity.WARNING
    )

    fun asLintIssue(detectorCls: Class<out Detector>, description: String? = null): Issue =
            Issue.create(
                    id,
                    briefDescription,
                    description ?: longDescription,
                    category,
                    priority,
                    severity,
                    Implementation(
                            detectorCls,
                            Scope.JAVA_FILE_SCOPE
                    )
            )
}