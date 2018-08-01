package net.grandcentrix.thirtyinch.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.Issue
import net.grandcentrix.thirtyinch.lint.detector.MissingViewInCompositeDetector
import net.grandcentrix.thirtyinch.lint.detector.MissingViewInThirtyInchDetector

class TiLintRegistry : IssueRegistry() {
    override val issues: List<Issue>
        get() = listOf(
                MissingViewInThirtyInchDetector.ISSUE.apply {
                    setEnabledByDefault(true)
                },
                MissingViewInCompositeDetector.ISSUE.apply {
                    setEnabledByDefault(true)
                }
        )
}
