package net.grandcentrix.thirtyinch;

import com.android.tools.lint.detector.api.Issue;

import java.util.ArrayList;
import java.util.List;

public class IssueRegistry extends com.android.tools.lint.client.api.IssueRegistry {

    @Override
    public List<Issue> getIssues() {
        List<Issue> issues = new ArrayList<>();
        issues.add(MissingTiViewImplementationDetector.ISSUE);
        return issues;
    }
}
