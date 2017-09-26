package net.grandcentrix.thirtyinch;

import com.android.tools.lint.checks.infrastructure.LintDetectorTest;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MissingViewInThirtyInchDetectorTest extends LintDetectorTest {

    private static final String NO_WARNINGS = "No warnings.";

    /* Stubbed-out source files */

    private final TestFile tiActivityStub = java("" +
            "package net.grandcentrix.thirtyinch;\n" +
            "public abstract class TiActivity<P extends TiPresenter<V>, V extends TiView> {\n" +
            "}");

    private final TestFile tiFragmentStub = java("" +
            "package net.grandcentrix.thirtyinch;\n" +
            "public abstract class TiFragment<P extends TiPresenter<V>, V extends TiView> {\n" +
            "}");

    private final TestFile tiPresenterStub = java("" +
            "package net.grandcentrix.thirtyinch;\n" +
            "public abstract class TiPresenter<V extends TiView> {\n" +
            "}");

    private final TestFile tiViewStub = java("" +
            "package net.grandcentrix.thirtyinch;\n" +
            "public interface TiView {\n" +
            "}");

    private final TestFile view = java("" +
            "package foo;\n" +
            "import net.grandcentrix.thirtyinch.*;\n" +
            "interface MyView extends TiView {\n" +
            "}");

    private final TestFile presenter = java("" +
            "package foo;\n" +
            "import net.grandcentrix.thirtyinch.*;\n" +
            "final class MyPresenter extends TiPresenter<MyView> {\n" +
            "}");

    /*
     * --------------------------------------------------------------------------------
     * TiActivity
     * --------------------------------------------------------------------------------
     */

    public void testActivity_dontTriggerOnAbstractClass() throws Exception {
        TestFile activity = java("" +
                "package foo;\n" +
                "import net.grandcentrix.thirtyinch.*;\n" +
                "public abstract class MyActivity extends TiActivity<MyPresenter, MyView> {\n" +
                "}");

        assertThat(lintProject(
                tiActivityStub, tiPresenterStub, tiViewStub,
                presenter, view, activity))
                .isEqualTo(NO_WARNINGS);
    }

    public void testActivity_andViewIsImplementedCorrectly_noWarnings() throws Exception {
        TestFile activity = java("" +
                "package foo;\n" +
                "import net.grandcentrix.thirtyinch.*;\n" +
                "public class MyActivity extends TiActivity<MyPresenter, MyView> implements MyView {\n" +
                "}");

        assertThat(lintProject(
                tiActivityStub, tiPresenterStub, tiViewStub,
                presenter, view, activity))
                .isEqualTo(NO_WARNINGS);
    }

    public void testActivity_doesntImplementInterface_hasWarning() throws Exception {
        TestFile activity = java("" +
                "package foo;\n" +
                "import net.grandcentrix.thirtyinch.*;\n" +
                "public class MyActivity extends TiActivity<MyPresenter, MyView> {\n" +
                "}");

        assertThat(lintProject(
                tiActivityStub, tiPresenterStub, tiViewStub,
                presenter, view, activity))
                .containsOnlyOnce(Issues.MISSING_VIEW.getId());
    }

    public void testActivity_doesntImplementInterface_butOverridesProvideView_noWarnings() throws Exception {
        TestFile activity = java("" +
                "package foo;\n" +
                "import net.grandcentrix.thirtyinch.*;\n" +
                "public class MyActivity extends TiActivity<MyPresenter, MyView> {\n" +
                "   public MyView provideView() {\n" +
                "       return null;\n" +
                "   }\n" +
                "}");

        assertThat(lintProject(
                tiActivityStub, tiPresenterStub, tiViewStub,
                presenter, view, activity))
                .isEqualTo(NO_WARNINGS);
    }

    public void testActivity_throughTransitiveBaseClass_hasWarning() throws Exception {
        TestFile baseActivity = java("" +
                "package foo;\n" +
                "import net.grandcentrix.thirtyinch.*;\n" +
                "public abstract class BaseActivity<P extends TiPresenter<V>, V extends TiView> extends TiActivity<P, V> {\n" +
                "}");

        TestFile activity = java("" +
                "package foo;\n" +
                "import net.grandcentrix.thirtyinch.*;\n" +
                "public class MyActivity extends BaseActivity<MyPresenter, MyView> {\n" +
                "}");

        assertThat(lintProject(
                tiActivityStub, tiPresenterStub, tiViewStub,
                presenter, view, baseActivity, activity))
                .containsOnlyOnce(Issues.MISSING_VIEW.getId());
    }
    /*
     * --------------------------------------------------------------------------------
     * TiFragment
     * --------------------------------------------------------------------------------
     */

    public void testFragment_dontTriggerOnAbstractClass() throws Exception {
        TestFile fragment = java("" +
                "package foo;\n" +
                "import net.grandcentrix.thirtyinch.*;\n" +
                "public abstract class MyFragment extends TiFragment<MyPresenter, MyView> {\n" +
                "}");

        assertThat(lintProject(
                tiFragmentStub, tiPresenterStub, tiViewStub,
                presenter, view, fragment))
                .isEqualTo(NO_WARNINGS);
    }

    public void testFragment_andViewIsImplementedCorrectly_noWarnings() throws Exception {
        TestFile fragment = java("" +
                "package foo;\n" +
                "import net.grandcentrix.thirtyinch.*;\n" +
                "public class MyFragment extends TiFragment<MyPresenter, MyView> implements MyView {\n" +
                "}");

        assertThat(lintProject(
                tiFragmentStub, tiPresenterStub, tiViewStub,
                presenter, view, fragment))
                .isEqualTo(NO_WARNINGS);
    }

    public void testFragment_doesntImplementInterface_hasWarning() throws Exception {
        TestFile fragment = java("" +
                "package foo;\n" +
                "import net.grandcentrix.thirtyinch.*;\n" +
                "public class MyFragment extends TiFragment<MyPresenter, MyView> {\n" +
                "}");

        assertThat(lintProject(
                tiFragmentStub, tiPresenterStub, tiViewStub,
                presenter, view, fragment))
                .containsOnlyOnce(Issues.MISSING_VIEW.getId());
    }

    public void testFragment_doesntImplementInterface_butOverridesProvideView_noWarnings() throws Exception {
        TestFile fragment = java("" +
                "package foo;\n" +
                "import net.grandcentrix.thirtyinch.*;\n" +
                "public class MyFragment extends TiFragment<MyPresenter, MyView> {\n" +
                "   public MyView provideView() {\n" +
                "       return null;\n" +
                "   }\n" +
                "}");

        assertThat(lintProject(
                tiFragmentStub, tiPresenterStub, tiViewStub,
                presenter, view, fragment))
                .isEqualTo(NO_WARNINGS);
    }

    public void testFragment_throughTransitiveBaseClass_hasWarning() throws Exception {
        TestFile baseFragment = java("" +
                "package foo;\n" +
                "import net.grandcentrix.thirtyinch.*;\n" +
                "public abstract class BaseFragment<P extends TiPresenter<V>, V extends TiView> extends TiFragment<P, V> {\n" +
                "}");

        TestFile fragment = java("" +
                "package foo;\n" +
                "import net.grandcentrix.thirtyinch.*;\n" +
                "public class MyFragment extends BaseFragment<MyPresenter, MyView> {\n" +
                "}");

        assertThat(lintProject(
                tiFragmentStub, tiPresenterStub, tiViewStub,
                presenter, view, baseFragment, fragment))
                .containsOnlyOnce(Issues.MISSING_VIEW.getId());
    }

    /* Overrides */

    @Override
    protected Detector getDetector() {
        return new MissingViewInThirtyInchDetector();
    }

    @Override
    protected List<Issue> getIssues() {
        return Collections.singletonList(MissingViewInThirtyInchDetector.Companion.getISSUE());
    }
}
