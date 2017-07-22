package net.grandcentrix.thirtyinch;

import com.android.tools.lint.checks.infrastructure.LintDetectorTest;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;

import org.intellij.lang.annotations.Language;

import java.util.Collections;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class MissingTiViewImplementationDetectorTest extends LintDetectorTest {

    private static final String NO_WARNINGS = "No warnings.";

    /* Stubbed-out source files */

    private final TestFile tiActivityStub = java("" +
            "package net.grandcentrix.thirtyinch;" +
            "public abstract class TiActivity<P extends TiPresenter<V>, V extends TiView> {" +
            "   public abstract P providePresenter();" +
            "}");

    private final TestFile tiPresenterStub = java("" +
            "package net.grandcentrix.thirtyinch;" +
            "public abstract class TiPresenter<V extends TiView> {" +
            "}");

    private final TestFile tiViewStub = java("" +
            "package net.grandcentrix.thirtyinch;" +
            "public interface TiView {" +
            "}");

    /* Test Cases */

    public void testSomething() throws Exception {
        TestFile view = java("" +
                "package foo;" +
                "import net.grandcentrix.thirtyinch.*;" +
                "interface MyView extends TiView {" +
                "}");

        TestFile presenter = java("" +
                "package foo;" +
                "import net.grandcentrix.thirtyinch.*;" +
                "final class MyPresenter extends TiPresenter<MyView> {" +
                "}");

        TestFile activity = java("" +
                "package foo;" +
                "import net.grandcentrix.thirtyinch.*;" +
                "public class MyActivity extends TiActivity<MyPresenter, MyView> {" +
                "   @Override" +
                "   public MyPresenter providePresenter() {" +
                "       return new MyPresenter();" +
                "   }" +
                "}");

        assertThat(lintProject(
                tiActivityStub, tiPresenterStub, tiViewStub,
                activity, presenter, view)).isEqualTo(NO_WARNINGS);
    }

    @Override
    protected Detector getDetector() {
        return new MissingTiViewImplementationDetector();
    }

    @Override
    protected List<Issue> getIssues() {
        return Collections.singletonList(MissingTiViewImplementationDetector.ISSUE);
    }
}
