package net.grandcentrix.thirtyinch

import com.android.tools.lint.detector.api.*
import java.util.*

enum class Issues(
        val id: String,
        val briefDescription: String,
        val category: Category,
        val priority: Int,
        val severity: Severity) {

    MISSING_VIEW(
            id = "MissingTiViewImplementation",
            briefDescription = "TiView Implementation missing in class",
            category = Category.CORRECTNESS,
            priority = 8,
            severity = Severity.ERROR);

    fun create(detectorCls: Class<out Detector>, description: String = briefDescription): Issue =
            Issue.create(
                    id,
                    briefDescription,
                    description,
                    category,
                    priority,
                    severity,
                    Implementation(
                            detectorCls,
                            EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES)))
}
