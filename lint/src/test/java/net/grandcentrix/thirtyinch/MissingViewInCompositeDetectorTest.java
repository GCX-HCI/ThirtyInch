package net.grandcentrix.thirtyinch;

import com.android.tools.lint.checks.infrastructure.LintDetectorTest;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MissingViewInCompositeDetectorTest extends LintDetectorTest {

    private static final String NO_WARNINGS = "No warnings.";

    /* Stubbed-out source files */

    private final TestFile tiPresenterStub = java("" +
            "package net.grandcentrix.thirtyinch;\n" +
            "public abstract class TiPresenter<V extends TiView> {\n" +
            "}");

    private final TestFile tiViewStub = java("" +
            "package net.grandcentrix.thirtyinch;\n" +
            "public interface TiView {\n" +
            "}");

    private final TestFile caBasePluginStub = java("" +
            "package com.pascalwelsch.compositeandroid;\n" +
            "public interface Plugin {\n" +
            "}");

    private final TestFile caActivityStub = java("" +
            "package com.pascalwelsch.compositeandroid.activity;\n" +
            "import net.grandcentrix.thirtyinch.plugin.*;\n" +
            "import com.pascalwelsch.compositeandroid.*;\n" +
            "public class CompositeActivity {\n" +
            "   public void addPlugin(Plugin plugin) {\n" +
            "   }\n" +
            "}");

    private final TestFile caActivityPluginStub = java("" +
            "package net.grandcentrix.thirtyinch.plugin;\n" +
            "import net.grandcentrix.thirtyinch.*;\n" +
            "import com.pascalwelsch.compositeandroid.*;\n" +
            "public class TiActivityPlugin<P extends TiPresenter<V>, V extends TiView> implements Plugin {\n" +
            "   public TiActivityPlugin(Runnable action) {\n" +
            "   }\n" +
            "}");

    private final TestFile caFragmentStub = java("" +
            "package com.pascalwelsch.compositeandroid.fragment;\n" +
            "import net.grandcentrix.thirtyinch.plugin.*;\n" +
            "import com.pascalwelsch.compositeandroid.*;\n" +
            "public class CompositeFragment {\n" +
            "   public void addPlugin(Plugin plugin) {\n" +
            "   }\n" +
            "}");

    private final TestFile caFragmentPluginStub = java("" +
            "package net.grandcentrix.thirtyinch.plugin;\n" +
            "import net.grandcentrix.thirtyinch.*;\n" +
            "import com.pascalwelsch.compositeandroid.*;\n" +
            "public class TiFragmentPlugin<P extends TiPresenter<V>, V extends TiView> implements Plugin {\n" +
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
     * CompositeActivity
     * --------------------------------------------------------------------------------
     */

    public void testActivity_dontTriggerOnAbstractClass() throws Exception {
        TestFile activity = java("" +
                "package foo;\n" +
                "import net.grandcentrix.thirtyinch.plugin.*;\n" +
                "import com.pascalwelsch.compositeandroid.activity.*;\n" +
                "public abstract class MyActivity extends CompositeActivity {\n" +
                "}");

        assertThat(lintProject(
                caActivityStub, caBasePluginStub, caActivityPluginStub, tiPresenterStub, tiViewStub,
                presenter, view, activity))
                .isEqualTo(NO_WARNINGS);
    }

    public void testActivity_andViewIsImplementedCorrectly_noWarnings() throws Exception {
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
                caActivityStub, caBasePluginStub, caActivityPluginStub, tiPresenterStub, tiViewStub,
                presenter, view, activity))
                .isEqualTo(NO_WARNINGS);
    }

    public void testActivity_doesntImplementInterface_hasWarning() throws Exception {
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
                caActivityStub, caBasePluginStub, caActivityPluginStub, tiPresenterStub, tiViewStub,
                presenter, view, activity))
                .containsOnlyOnce(Issues.MISSING_VIEW.getId());
    }

    public void testActivity_doesntImplementInterface_butDoesntHavePluginAppliedEither_noWarnings() throws Exception {
        TestFile activity = java("" +
                "package foo;\n" +
                "import net.grandcentrix.thirtyinch.plugin.*;\n" +
                "import com.pascalwelsch.compositeandroid.activity.*;\n" +
                "public class MyActivity extends CompositeActivity {\n" +
                "}");

        assertThat(lintProject(
                caActivityStub, caBasePluginStub, caActivityPluginStub, tiPresenterStub, tiViewStub,
                presenter, view, activity))
                .isEqualTo(NO_WARNINGS);
    }

    /*
     * --------------------------------------------------------------------------------
     * CompositeFragment
     * --------------------------------------------------------------------------------
     */

    public void testFragment_dontTriggerOnAbstractClass() throws Exception {
        TestFile fragment = java("" +
                "package foo;\n" +
                "import net.grandcentrix.thirtyinch.plugin.*;\n" +
                "import com.pascalwelsch.compositeandroid.activity.*;\n" +
                "public abstract class MyActivity extends CompositeActivity {\n" +
                "}");

        assertThat(lintProject(
                caActivityStub, caBasePluginStub, caActivityPluginStub, tiPresenterStub, tiViewStub,
                presenter, view, fragment))
                .isEqualTo(NO_WARNINGS);
    }

    public void testFragment_andViewIsImplementedCorrectly_noWarnings() throws Exception {
        TestFile fragment = java("" +
                "package foo;\n" +
                "import net.grandcentrix.thirtyinch.plugin.*;\n" +
                "import com.pascalwelsch.compositeandroid.fragment.*;\n" +
                "public class MyFragment extends CompositeFragment implements MyView {\n" +
                "   public MyFragment() {\n" +
                "       addPlugin(new TiFragmentPlugin<MyPresenter, MyView>(\n" +
                "           () -> new MyPresenter()));\n" +
                "   }\n" +
                "}");

        assertThat(lintProject(
                caFragmentStub, caBasePluginStub, caFragmentPluginStub, tiPresenterStub, tiViewStub,
                presenter, view, fragment))
                .isEqualTo(NO_WARNINGS);
    }

    @SuppressWarnings("Convert2Lambda")
    public void testFragment_doesntImplementInterface_hasWarning_java7() throws Exception {
        TestFile fragment = java("" +
                "package foo;\n" +
                "import net.grandcentrix.thirtyinch.plugin.*;\n" +
                "import com.pascalwelsch.compositeandroid.fragment.*;\n" +
                "public class MyFragment extends CompositeFragment {\n" +
                "   public MyFragment() {\n" +
                "       addPlugin(new TiFragmentPlugin<>(\n" +
                "                new Runnable() {\n" +
                "                    @Override\n" +
                "                    public void run() {\n" +
                "                        new MyPresenter();\n" +
                "                    }\n" +
                "                }));" +
                "   }\n" +
                "}");

        assertThat(lintProject(
                caFragmentStub, caBasePluginStub, caFragmentPluginStub, tiPresenterStub, tiViewStub,
                presenter, view, fragment))
                .containsOnlyOnce(Issues.MISSING_VIEW.getId());
    }

    public void testFragment_doesntImplementInterface_hasWarning_java8() throws Exception {
        TestFile fragment = java("" +
                "package foo;\n" +
                "import net.grandcentrix.thirtyinch.plugin.*;\n" +
                "import com.pascalwelsch.compositeandroid.fragment.*;\n" +
                "public class MyFragment extends CompositeFragment {\n" +
                "   public MyFragment() {\n" +
                "       addPlugin(new TiFragmentPlugin<MyPresenter, MyView>(\n" +
                "           () -> new MyPresenter()));\n" +
                "   }\n" +
                "}");

        assertThat(lintProject(
                caFragmentStub, caBasePluginStub, caFragmentPluginStub, tiPresenterStub, tiViewStub,
                presenter, view, fragment))
                .containsOnlyOnce(Issues.MISSING_VIEW.getId());
    }

    public void testFragment_doesntImplementInterface_butDoesntHavePluginAppliedEither_noWarnings() throws Exception {
        TestFile fragment = java("" +
                "package foo;\n" +
                "import net.grandcentrix.thirtyinch.plugin.*;\n" +
                "import com.pascalwelsch.compositeandroid.fragment.*;\n" +
                "public class MyFragment extends CompositeFragment {\n" +
                "}");

        assertThat(lintProject(
                caFragmentStub, caBasePluginStub, caFragmentPluginStub, tiPresenterStub, tiViewStub,
                presenter, view, fragment))
                .isEqualTo(NO_WARNINGS);
    }

    public void testFragment_appliesUnrelatedPlugin_noWarnings() throws Exception {
        TestFile otherPlugin = java("" +
                "package foo;\n" +
                "import com.pascalwelsch.compositeandroid.*;\n" +
                "public class OtherPlugin implements Plugin {\n" +
                "}");

        TestFile fragment = java("" +
                "package foo;\n" +
                "import net.grandcentrix.thirtyinch.plugin.*;\n" +
                "import com.pascalwelsch.compositeandroid.fragment.*;\n" +
                "public class MyFragment extends CompositeFragment {\n" +
                "   public MyFragment() {\n" +
                "       addPlugin(new OtherPlugin());\n" +
                "   }\n" +
                "}");

        assertThat(lintProject(
                caFragmentStub, caBasePluginStub, caFragmentPluginStub, tiPresenterStub, tiViewStub,
                presenter, view, otherPlugin, fragment))
                .isEqualTo(NO_WARNINGS);
    }

    /* Overrides */

    @Override
    protected Detector getDetector() {
        return new MissingViewInCompositeDetector();
    }

    @Override
    protected List<Issue> getIssues() {
        return Collections.singletonList(MissingViewInCompositeDetector.Companion.getISSUE());
    }
}
