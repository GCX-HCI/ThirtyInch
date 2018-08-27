package net.grandcentrix.thirtyinch.lint

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import java.util.EnumSet

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
            category = Category.CORRECTNESS,
            priority = 8,
            severity = Severity.ERROR
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