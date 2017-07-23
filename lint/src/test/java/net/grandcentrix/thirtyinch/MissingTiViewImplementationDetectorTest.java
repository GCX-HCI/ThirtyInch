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

    private final TestFile caActivityStub = java("" +
            "package com.pascalwelsch.compositeandroid.activity;\n" +
            "import net.grandcentrix.thirtyinch.plugin.*;\n" +
            "public class CompositeActivity {\n" +
            "   public void addPlugin(TiActivityPlugin plugin) {\n" +
            "   }\n" +
            "}");

    private final TestFile caActivityPluginStub = java("" +
            "package net.grandcentrix.thirtyinch.plugin;\n" +
            "import net.grandcentrix.thirtyinch.*;\n" +
            "public class TiActivityPlugin<P extends TiPresenter<V>, V extends TiView> {\n" +
            "   public TiActivityPlugin(Runnable action) {\n" +
            "   }\n" +
            "}");

    private final TestFile caFragmentStub = java("" +
            "package com.pascalwelsch.compositeandroid.fragment;\n" +
            "import net.grandcentrix.thirtyinch.plugin.*;\n" +
            "public class CompositeFragment {\n" +
            "   public void addPlugin(TiFragmentPlugin plugin) {\n" +
            "   }\n" +
            "}");

    private final TestFile caFragmentPluginStub = java("" +
            "package net.grandcentrix.thirtyinch.plugin;\n" +
            "import net.grandcentrix.thirtyinch.*;\n" +
            "public class TiFragmentPlugin<P extends TiPresenter<V>, V extends TiView> {\n" +
            "   public TiFragmentPlugin(Runnable action) {\n" +
            "   }\n" +
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

    public void testTiActivity_dontTriggerOnAbstractClass() throws Exception {
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

    public void testTiActivity_andViewIsImplementedCorrectly_noWarnings() throws Exception {
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

    public void testTiActivity_doesntImplementInterface_hasWarning() throws Exception {
        TestFile activity = java("" +
                "package foo;\n" +
                "import net.grandcentrix.thirtyinch.*;\n" +
                "public class MyActivity extends TiActivity<MyPresenter, MyView> {\n" +
                "}");

        assertThat(lintProject(
                tiActivityStub, tiPresenterStub, tiViewStub,
                presenter, view, activity))
                .containsOnlyOnce(MissingTiViewImplementationDetector.ISSUE.getId());
    }

    public void testTiActivity_doesntImplementInterface_butOverridesProvideView_noWarnings() throws Exception {
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

    public void testTiActivity_throughTransitiveBaseClass_hasWarning() throws Exception {
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
                .containsOnlyOnce(MissingTiViewImplementationDetector.ISSUE.getId());
    }
    /*
     * --------------------------------------------------------------------------------
     * TiFragment
     * --------------------------------------------------------------------------------
     */

    public void testTiFragment_dontTriggerOnAbstractClass() throws Exception {
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

    public void testTiFragment_andViewIsImplementedCorrectly_noWarnings() throws Exception {
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

    public void testTiFragment_doesntImplementInterface_hasWarning() throws Exception {
        TestFile fragment = java("" +
                "package foo;\n" +
                "import net.grandcentrix.thirtyinch.*;\n" +
                "public class MyFragment extends TiFragment<MyPresenter, MyView> {\n" +
                "}");

        assertThat(lintProject(
                tiFragmentStub, tiPresenterStub, tiViewStub,
                presenter, view, fragment))
                .containsOnlyOnce(MissingTiViewImplementationDetector.ISSUE.getId());
    }

    public void testTiFragment_doesntImplementInterface_butOverridesProvideView_noWarnings() throws Exception {
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

    public void testTiFragment_throughTransitiveBaseClass_hasWarning() throws Exception {
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
                .containsOnlyOnce(MissingTiViewImplementationDetector.ISSUE.getId());
    }

    /*
     * --------------------------------------------------------------------------------
     * CompositeActivity
     * --------------------------------------------------------------------------------
     */

    public void testCompositeActivity_dontTriggerOnAbstractClass() throws Exception {
        TestFile activity = java("" +
                "package foo;\n" +
                "import net.grandcentrix.thirtyinch.plugin.*;\n" +
                "import com.pascalwelsch.compositeandroid.activity.*;\n" +
                "public abstract class MyActivity extends CompositeActivity {\n" +
                "}");

        assertThat(lintProject(
                caActivityStub, caActivityPluginStub, tiPresenterStub, tiViewStub,
                presenter, view, activity))
                .isEqualTo(NO_WARNINGS);
    }

    public void testCompositeActivity_andViewIsImplementedCorrectly_noWarnings() throws Exception {
        TestFile activity = java("" +
                "package foo;\n" +
                "import net.grandcentrix.thirtyinch.plugin.*;\n" +
                "import com.pascalwelsch.compositeandroid.activity.*;\n" +
                "public class MyActivity extends CompositeActivity implements MyView {\n" +
                "   public MyActivity() {\n" +
                "       addPlugin(new TiActivityPlugin<MyPresenter, MyView>(\n" +
                "           () -> new MyPresenter()));\n" +
                "   }\n" +
                "}");

        assertThat(lintProject(
                caActivityStub, caActivityPluginStub, tiPresenterStub, tiViewStub,
                presenter, view, activity))
                .isEqualTo(NO_WARNINGS);
    }

    public void testCompositeActivity_doesntImplementInterface_hasWarning() throws Exception {
        TestFile activity = java("" +
                "package foo;\n" +
                "import net.grandcentrix.thirtyinch.plugin.*;\n" +
                "import com.pascalwelsch.compositeandroid.activity.*;\n" +
                "public class MyActivity extends CompositeActivity {\n" +
                "   public MyActivity() {\n" +
                "       addPlugin(new TiActivityPlugin<MyPresenter, MyView>(\n" +
                "           () -> new MyPresenter()));\n" +
                "   }\n" +
                "}");

        assertThat(lintProject(
                caActivityStub, caActivityPluginStub, tiPresenterStub, tiViewStub,
                presenter, view, activity))
                .containsOnlyOnce(MissingTiViewImplementationDetector.ISSUE.getId());
    }

    public void testCompositeActivity_doesntImplementInterface_butDoesntHavePluginAppliedEither_noWarnings() throws Exception {
        TestFile activity = java("" +
                "package foo;\n" +
                "import net.grandcentrix.thirtyinch.plugin.*;\n" +
                "import com.pascalwelsch.compositeandroid.activity.*;\n" +
                "public class MyActivity extends CompositeActivity {\n" +
                "}");

        assertThat(lintProject(
                caActivityStub, caActivityPluginStub, tiPresenterStub, tiViewStub,
                presenter, view, activity))
                .isEqualTo(NO_WARNINGS);
    }

    /*
     * --------------------------------------------------------------------------------
     * CompositeFragment
     * --------------------------------------------------------------------------------
     */

    public void testCompositeFragment_dontTriggerOnAbstractClass() throws Exception {
        TestFile fragment = java("" +
                "package foo;\n" +
                "import net.grandcentrix.thirtyinch.plugin.*;\n" +
                "import com.pascalwelsch.compositeandroid.activity.*;\n" +
                "public abstract class MyActivity extends CompositeActivity {\n" +
                "}");

        assertThat(lintProject(
                caActivityStub, caActivityPluginStub, tiPresenterStub, tiViewStub,
                presenter, view, fragment))
                .isEqualTo(NO_WARNINGS);
    }

    public void testCompositeFragment_andViewIsImplementedCorrectly_noWarnings() throws Exception {
        TestFile fragment = java("" +
                "package foo;" +
                "import net.grandcentrix.thirtyinch.plugin.*;\n" +
                "import com.pascalwelsch.compositeandroid.fragment.*;\n" +
                "public class MyFragment extends CompositeFragment implements MyView {\n" +
                "   public MyFragment() {\n" +
                "       addPlugin(new TiFragmentPlugin<MyPresenter, MyView>(\n" +
                "           () -> new MyPresenter()));\n" +
                "   }\n" +
                "}");

        assertThat(lintProject(
                caFragmentStub, caFragmentPluginStub, tiPresenterStub, tiViewStub,
                presenter, view, fragment))
                .isEqualTo(NO_WARNINGS);
    }

    public void testCompositeFragment_doesntImplementInterface_hasWarning() throws Exception {
        TestFile fragment = java("" +
                "package foo;" +
                "import net.grandcentrix.thirtyinch.plugin.*;\n" +
                "import com.pascalwelsch.compositeandroid.fragment.*;\n" +
                "public class MyFragment extends CompositeFragment {\n" +
                "   public MyFragment() {\n" +
                "       addPlugin(new TiFragmentPlugin<MyPresenter, MyView>(\n" +
                "           () -> new MyPresenter()));\n" +
                "   }\n" +
                "}");

        assertThat(lintProject(
                caFragmentStub, caFragmentPluginStub, tiPresenterStub, tiViewStub,
                presenter, view, fragment))
                .containsOnlyOnce(MissingTiViewImplementationDetector.ISSUE.getId());
    }

    public void testCompositeFragment_doesntImplementInterface_butDoesntHavePluginAppliedEither_noWarnings() throws Exception {
        TestFile fragment = java("" +
                "package foo;" +
                "import net.grandcentrix.thirtyinch.plugin.*;\n" +
                "import com.pascalwelsch.compositeandroid.fragment.*;\n" +
                "public class MyFragment extends CompositeFragment {\n" +
                "}");

        assertThat(lintProject(
                caFragmentStub, caFragmentPluginStub, tiPresenterStub, tiViewStub,
                presenter, view, fragment))
                .isEqualTo(NO_WARNINGS);
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
