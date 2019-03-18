package net.grandcentrix.thirtyinch.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.Issue
import net.grandcentrix.thirtyinch.lint.detector.DistinctUntilChangedUsageDetector
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
                },
                DistinctUntilChangedUsageDetector.ISSUE_NO_PARAMETER.apply {
                    setEnabledByDefault(true)
                },
                DistinctUntilChangedUsageDetector.ISSUE_NON_VOID_RETURN_TYPE.apply {
                    setEnabledByDefault(true)
                }
        )

    /**
     * Lint API Version for which the Checks are build.
     *
     * See [com.android.tools.lint.detector.api.describeApi] for possible options
     */
    override val api: Int = 1
}