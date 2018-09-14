package net.grandcentrix.thirtyinch.lint

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity

private val CATEGORY_TI = Category.create("ThirtyInch", 5)

sealed class TiIssue(
        val id: String,
        val briefDescription: String,
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

    object GetViewOrThrowInOnAttach : TiIssue(
            id = "GetViewOrThrowInOnAttach",
            briefDescription = "TiPresenter.getViewOrThrow() might throw an exception",
            category = CATEGORY_TI,
            priority = 6,
            severity = Severity.WARNING
    )

    fun asLintIssue(detectorCls: Class<out Detector>, description: String = briefDescription): Issue =
            Issue.create(
                    id,
                    briefDescription,
                    description,
                    category,
                    priority,
                    severity,
                    Implementation(
                            detectorCls,
                            Scope.JAVA_FILE_SCOPE
                    )
            )
}