package net.grandcentrix.thirtyinch;

import com.android.tools.lint.checks.infrastructure.LintDetectorTest;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;

import java.util.Collections;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class MissingTiViewImplementationDetectorTest extends LintDetectorTest {

    private static final String NO_WARNINGS = "No warnings.";

    /* Stubbed-out source files */

    private final TestFile tiActivityStub = java("" +
            "package net.grandcentrix.thirtyinch;" +
            "public abstract class TiActivity<P extends TiPresenter<V>, V extends TiView> {" +
            "}");

    private final TestFile tiFragmentStub = java("" +
            "package net.grandcentrix.thirtyinch;" +
            "public abstract class TiFragment<P extends TiPresenter<V>, V extends TiView> {" +
            "}");

    private final TestFile tiPresenterStub = java("" +
            "package net.grandcentrix.thirtyinch;" +
            "public abstract class TiPresenter<V extends TiView> {" +
            "}");

    private final TestFile tiViewStub = java("" +
            "package net.grandcentrix.thirtyinch;" +
            "public interface TiView {" +
            "}");

    private final TestFile view = java("" +
            "package foo;" +
            "import net.grandcentrix.thirtyinch.*;" +
            "interface MyView extends TiView {" +
            "}");

    private final TestFile presenter = java("" +
            "package foo;" +
            "import net.grandcentrix.thirtyinch.*;" +
            "final class MyPresenter extends TiPresenter<MyView> {" +
            "}");

    /* Test Cases */

    public void testTiActivity_dontTriggerOnAbstractClass() throws Exception {
        TestFile activity = java("" +
                "package foo;" +
                "import net.grandcentrix.thirtyinch.*;" +
                "public abstract class MyActivity extends TiActivity<MyPresenter, MyView> {" +
                "}");

        assertThat(lintProject(
                tiActivityStub, tiPresenterStub, tiViewStub,
                presenter, view, activity))
                .isEqualTo(NO_WARNINGS);
    }

    public void testTiActivity_andViewIsImplementedCorrectly_noWarnings() throws Exception {
        TestFile activity = java("" +
                "package foo;" +
                "import net.grandcentrix.thirtyinch.*;" +
                "public class MyActivity extends TiActivity<MyPresenter, MyView> implements MyView {" +
                "}");

        assertThat(lintProject(
                tiActivityStub, tiPresenterStub, tiViewStub,
                presenter, view, activity))
                .isEqualTo(NO_WARNINGS);
    }

    public void testTiActivity_doesntImplementInterface_hasWarning() throws Exception {
        TestFile activity = java("" +
                "package foo;" +
                "import net.grandcentrix.thirtyinch.*;" +
                "public class MyActivity extends TiActivity<MyPresenter, MyView> {" +
                "}");

        assertThat(lintProject(
                tiActivityStub, tiPresenterStub, tiViewStub,
                presenter, view, activity))
                .containsOnlyOnce(MissingTiViewImplementationDetector.ISSUE.getId());
    }

    public void testTiActivity_doesntImplementInterface_butOverridesProvideView_noWarnings() throws Exception {
        TestFile activity = java("" +
                "package foo;" +
                "import net.grandcentrix.thirtyinch.*;" +
                "public class MyActivity extends TiActivity<MyPresenter, MyView> {" +
                "   public MyView provideView() {" +
                "       return null;" +
                "   }" +
                "}");

        assertThat(lintProject(
                tiActivityStub, tiPresenterStub, tiViewStub,
                presenter, view, activity))
                .isEqualTo(NO_WARNINGS);
    }

    // TODO Ignored: For some reason, Lint doesn't resolve the base class
    public void _testTiActivity_throughTransitiveBaseClass_hasWarning() throws Exception {
        TestFile baseActivity = java("" +
                "package foo;" +
                "import net.grandcentrix.thirtyinch.*;" +
                "public abstract class BaseActivity<P extends TiPresenter<V>, V extends TiView> extends TiActivity<P, V> {" +
                "}");

        TestFile activity = java("" +
                "package foo;" +
                "import net.grandcentrix.thirtyinch.*;" +
                "public class MyActivity extends BaseActivity<MyPresenter, MyView> {" +
                "}");

        assertThat(lintProject(
                tiActivityStub, tiPresenterStub, tiViewStub,
                presenter, view, baseActivity, activity))
                .containsOnlyOnce(MissingTiViewImplementationDetector.ISSUE.getId());
    }

    public void testTiFragment_dontTriggerOnAbstractClass() throws Exception {
        TestFile fragment = java("" +
                "package foo;" +
                "import net.grandcentrix.thirtyinch.*;" +
                "public abstract class MyFragment extends TiFragment<MyPresenter, MyView> {" +
                "}");

        assertThat(lintProject(
                tiFragmentStub, tiPresenterStub, tiViewStub,
                presenter, view, fragment))
                .isEqualTo(NO_WARNINGS);
    }

    public void testTiFragment_andViewIsImplementedCorrectly_noWarnings() throws Exception {
        TestFile fragment = java("" +
                "package foo;" +
                "import net.grandcentrix.thirtyinch.*;" +
                "public class MyFragment extends TiFragment<MyPresenter, MyView> implements MyView {" +
                "}");

        assertThat(lintProject(
                tiFragmentStub, tiPresenterStub, tiViewStub,
                presenter, view, fragment))
                .isEqualTo(NO_WARNINGS);
    }

    public void testTiFragment_doesntImplementInterface_hasWarning() throws Exception {
        TestFile fragment = java("" +
                "package foo;" +
                "import net.grandcentrix.thirtyinch.*;" +
                "public class MyFragment extends TiFragment<MyPresenter, MyView> {" +
                "}");

        assertThat(lintProject(
                tiFragmentStub, tiPresenterStub, tiViewStub,
                presenter, view, fragment))
                .containsOnlyOnce(MissingTiViewImplementationDetector.ISSUE.getId());
    }

    public void testTiFragment_doesntImplementInterface_butOverridesProvideView_noWarnings() throws Exception {
        TestFile fragment = java("" +
                "package foo;" +
                "import net.grandcentrix.thirtyinch.*;" +
                "public class MyFragment extends TiFragment<MyPresenter, MyView> {" +
                "   public MyView provideView() {" +
                "       return null;" +
                "   }" +
                "}");

        assertThat(lintProject(
                tiFragmentStub, tiPresenterStub, tiViewStub,
                presenter, view, fragment))
                .isEqualTo(NO_WARNINGS);
    }

    // TODO Ignored: For some reason, Lint doesn't resolve the base class
    public void _testTiFragment_throughTransitiveBaseClass_hasWarning() throws Exception {
        TestFile baseFragment = java("" +
                "package foo;" +
                "import net.grandcentrix.thirtyinch.*;" +
                "public abstract class BaseFragment<P extends TiPresenter<V>, V extends TiView> extends TiFragment<P, V> {" +
                "}");

        TestFile fragment = java("" +
                "package foo;" +
                "import net.grandcentrix.thirtyinch.*;" +
                "public class MyFragment extends BaseFragment<MyPresenter, MyView> {" +
                "}");

        assertThat(lintProject(
                tiFragmentStub, tiPresenterStub, tiViewStub,
                presenter, view, baseFragment, fragment))
                .containsOnlyOnce(MissingTiViewImplementationDetector.ISSUE.getId());
    }

    /* Overrides */

    @Override
    protected Detector getDetector() {
        return new MissingTiViewImplementationDetector();
    }

    @Override
    protected List<Issue> getIssues() {
        return Collections.singletonList(MissingTiViewImplementationDetector.ISSUE);
    }
}
