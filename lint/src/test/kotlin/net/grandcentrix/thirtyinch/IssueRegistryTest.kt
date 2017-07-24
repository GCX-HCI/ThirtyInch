package net.grandcentrix.thirtyinch

import com.android.tools.lint.detector.api.Issue
import org.fest.assertions.api.Assertions.assertThat
import org.junit.Test

class IssueRegistryTest {

    @Test fun testIssueList() {
        assertThat<Issue>(IssueRegistry().issues)
                .containsExactly(
                        MissingViewInThirtyInchDetector.ISSUE,
                        MissingViewInCompositeDetector.ISSUE
                )
    }
}
