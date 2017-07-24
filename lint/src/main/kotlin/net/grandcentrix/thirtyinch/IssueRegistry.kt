package net.grandcentrix.thirtyinch

class IssueRegistry : com.android.tools.lint.client.api.IssueRegistry() {

    override fun getIssues() = listOf(
            MissingViewInThirtyInchDetector.ISSUE,
            MissingViewInCompositeDetector.ISSUE
    )
}
