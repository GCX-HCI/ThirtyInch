package net.grandcentrix.thirtyinch.lint

import net.grandcentrix.thirtyinch.lint.detector.DistinctUntilChangedUsageDetector
import net.grandcentrix.thirtyinch.lint.detector.MissingViewInCompositeDetector
import net.grandcentrix.thirtyinch.lint.detector.MissingViewInThirtyInchDetector
import org.assertj.core.api.Assertions.*
import org.junit.*

class IssueRegistryTest {

    @Test
    fun testIssueList() {
        assertThat(TiLintRegistry().issues)
                .containsExactly(
                        MissingViewInThirtyInchDetector.ISSUE,
                        MissingViewInCompositeDetector.ISSUE,
                        DistinctUntilChangedUsageDetector.ISSUE_NO_PARAMETER,
                        DistinctUntilChangedUsageDetector.ISSUE_NON_VOID_RETURN_TYPE
                )
    }
}