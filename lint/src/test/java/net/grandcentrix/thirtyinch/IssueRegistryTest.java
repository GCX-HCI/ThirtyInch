package net.grandcentrix.thirtyinch;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class IssueRegistryTest {

    @Test public void testIssueList() {
        assertThat(new IssueRegistry().getIssues()).containsExactly(
                MissingTiViewImplementationDetector.ISSUE
        );
    }
}
