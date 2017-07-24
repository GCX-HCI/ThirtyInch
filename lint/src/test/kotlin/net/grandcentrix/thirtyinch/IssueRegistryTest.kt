package net.grandcentrix.thirtyinch

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class IssueRegistryTest {

    @Test fun testIssueList() {
        assertThat(IssueRegistry().issues)
                .containsExactly(
                        MissingViewInThirtyInchDetector.ISSUE,
                        MissingViewInCompositeDetector.ISSUE
                )
    }
}
