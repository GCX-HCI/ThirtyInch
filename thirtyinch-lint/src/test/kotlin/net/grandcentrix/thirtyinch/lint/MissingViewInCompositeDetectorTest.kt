package net.grandcentrix.thirtyinch.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import net.grandcentrix.thirtyinch.lint.detector.MissingViewInCompositeDetector
import org.assertj.core.api.Assertions.*

private const val NO_WARNINGS = "No warnings."

class MissingViewInCompositeDetectorTest : LintDetectorTest() {

    /* Stubbed-out source files */

    private val tiPresenterStub = java(
            "package net.grandcentrix.thirtyinch;\n" +
                    "public abstract class TiPresenter<V extends TiView> {\n" +
                    "}"
    )

    private val tiViewStub = java(
            "package net.grandcentrix.thirtyinch;\n" +
                    "public interface TiView {\n" +
                    "}"
    )

    private val caBasePluginStub = java(
            "package com.pascalwelsch.compositeandroid;\n" +
                    "public interface Plugin {\n" +
                    "}"
    )

    private val caActivityStub = java(
            "package com.pascalwelsch.compositeandroid.activity;\n" +
                    "import net.grandcentrix.thirtyinch.plugin.*;\n" +
                    "import com.pascalwelsch.compositeandroid.*;\n" +
                    "public class CompositeActivity {\n" +
                    "   public void addPlugin(Plugin plugin) {\n" +
                    "   }\n" +
                    "}"
    )

    private val caActivityPluginStub = java(
            "package net.grandcentrix.thirtyinch.plugin;\n" +
                    "import net.grandcentrix.thirtyinch.*;\n" +
                    "import com.pascalwelsch.compositeandroid.*;\n" +
                    "public class TiActivityPlugin<P extends TiPresenter<V>, V extends TiView> implements Plugin {\n" +
                    "   public TiActivityPlugin(Runnable action) {\n" +
                    "   }\n" +
                    "}"
    )

    private val caFragmentStub = java(
            "package com.pascalwelsch.compositeandroid.fragment;\n" +
                    "import net.grandcentrix.thirtyinch.plugin.*;\n" +
                    "import com.pascalwelsch.compositeandroid.*;\n" +
                    "public class CompositeFragment {\n" +
                    "   public void addPlugin(Plugin plugin) {\n" +
                    "   }\n" +
                    "}"
    )

    private val caFragmentPluginStub = java(
            "package net.grandcentrix.thirtyinch.plugin;\n" +
                    "import net.grandcentrix.thirtyinch.*;\n" +
                    "import com.pascalwelsch.compositeandroid.*;\n" +
                    "public class TiFragmentPlugin<P extends TiPresenter<V>, V extends TiView> implements Plugin {\n" +
                    "   public TiFragmentPlugin(Runnable action) {\n" +
                    "   }\n" +
                    "}"
    )

    private val view = java(
            "package foo;\n" +
                    "import net.grandcentrix.thirtyinch.*;\n" +
                    "interface MyView extends TiView {\n" +
                    "}"
    )

    private val presenter = java(
            "package foo;\n" +
                    "import net.grandcentrix.thirtyinch.*;\n" +
                    "final class MyPresenter extends TiPresenter<MyView> {\n" +
                    "}"
    )

    /* Overrides */

    override fun getDetector(): Detector = MissingViewInCompositeDetector()

    override fun getIssues(): MutableList<Issue> = mutableListOf(MissingViewInCompositeDetector.ISSUE)

    override fun allowMissingSdk(): Boolean = true
/*
     * --------------------------------------------------------------------------------
     * CompositeActivity
     * --------------------------------------------------------------------------------
     */

    fun testActivity_dontTriggerOnAbstractClass() {
        val activity = java(
                "package foo;\n" +
                        "import net.grandcentrix.thirtyinch.plugin.*;\n" +
                        "import com.pascalwelsch.compositeandroid.activity.*;\n" +
                        "public abstract class MyActivity extends CompositeActivity {\n" +
                        "}"
        )

        assertThat(
                lintProject(
                        caActivityStub,
                        caBasePluginStub,
                        caActivityPluginStub,
                        tiPresenterStub,
                        tiViewStub,
                        presenter,
                        view,
                        activity
                )
        ).isEqualTo(NO_WARNINGS)
    }

    fun testActivity_andViewIsImplementedCorrectly_noWarnings() {
        val activity = java(
                "package foo;\n" +
                        "import net.grandcentrix.thirtyinch.plugin.*;\n" +
                        "import com.pascalwelsch.compositeandroid.activity.*;\n" +
                        "public class MyActivity extends CompositeActivity implements MyView {\n" +
                        "   public MyActivity() {\n" +
                        "       addPlugin(new TiActivityPlugin<MyPresenter, MyView>(\n" +
                        "           () -> new MyPresenter()));\n" +
                        "   }\n" +
                        "}"
        )

        assertThat(
                lintProject(
                        caActivityStub,
                        caBasePluginStub,
                        caActivityPluginStub,
                        tiPresenterStub,
                        tiViewStub,
                        presenter,
                        view,
                        activity
                )
        ).isEqualTo(NO_WARNINGS)
    }

    fun testActivity_doesntImplementInterface_hasWarning() {
        val activity = java(
                "package foo;\n" +
                        "import net.grandcentrix.thirtyinch.plugin.*;\n" +
                        "import com.pascalwelsch.compositeandroid.activity.*;\n" +
                        "public class MyActivity extends CompositeActivity {\n" +
                        "   public MyActivity() {\n" +
                        "       addPlugin(new TiActivityPlugin<MyPresenter, MyView>(\n" +
                        "           () -> new MyPresenter()));\n" +
                        "   }\n" +
                        "}"
        )

        assertThat(
                lintProject(
                        caActivityStub,
                        caBasePluginStub,
                        caActivityPluginStub,
                        tiPresenterStub,
                        tiViewStub,
                        presenter,
                        view,
                        activity
                )
        ).containsOnlyOnce(TiIssue.MissingView.id)
    }

    fun testActivity_doesntImplementInterface_butDoesntHavePluginAppliedEither_noWarnings() {
        val activity = java(
                "package foo;\n" +
                        "import net.grandcentrix.thirtyinch.plugin.*;\n" +
                        "import com.pascalwelsch.compositeandroid.activity.*;\n" +
                        "public class MyActivity extends CompositeActivity {\n" +
                        "}"
        )

        assertThat(
                lintProject(
                        caActivityStub,
                        caBasePluginStub,
                        caActivityPluginStub,
                        tiPresenterStub,
                        tiViewStub,
                        presenter,
                        view,
                        activity
                )
        ).isEqualTo(NO_WARNINGS)
    }

    /*
     * --------------------------------------------------------------------------------
     * CompositeFragment
     * --------------------------------------------------------------------------------
     */

    fun testFragment_dontTriggerOnAbstractClass() {
        val fragment = java(
                "package foo;\n" +
                        "import net.grandcentrix.thirtyinch.plugin.*;\n" +
                        "import com.pascalwelsch.compositeandroid.activity.*;\n" +
                        "public abstract class MyActivity extends CompositeActivity {\n" +
                        "}"
        )

        assertThat(
                lintProject(
                        caActivityStub,
                        caBasePluginStub,
                        caActivityPluginStub,
                        tiPresenterStub,
                        tiViewStub,
                        presenter,
                        view,
                        fragment
                )
        ).isEqualTo(NO_WARNINGS)
    }

    fun testFragment_andViewIsImplementedCorrectly_noWarnings() {
        val fragment = java(
                "package foo;\n" +
                        "import net.grandcentrix.thirtyinch.plugin.*;\n" +
                        "import com.pascalwelsch.compositeandroid.fragment.*;\n" +
                        "public class MyFragment extends CompositeFragment implements MyView {\n" +
                        "   public MyFragment() {\n" +
                        "       addPlugin(new TiFragmentPlugin<MyPresenter, MyView>(\n" +
                        "           () -> new MyPresenter()));\n" +
                        "   }\n" +
                        "}"
        )

        assertThat(
                lintProject(
                        caFragmentStub,
                        caBasePluginStub,
                        caFragmentPluginStub,
                        tiPresenterStub,
                        tiViewStub,
                        presenter,
                        view,
                        fragment
                )
        ).isEqualTo(NO_WARNINGS)
    }

    fun testFragment_doesntImplementInterface_hasWarning_java7() {
        val fragment = java(
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
                        "}"
        )

        assertThat(
                lintProject(
                        caFragmentStub,
                        caBasePluginStub,
                        caFragmentPluginStub,
                        tiPresenterStub,
                        tiViewStub,
                        presenter,
                        view,
                        fragment
                )
        ).containsOnlyOnce(TiIssue.MissingView.id)
    }

    fun testFragment_doesntImplementInterface_hasWarning_java8() {
        val fragment = java(
                "package foo;\n" +
                        "import net.grandcentrix.thirtyinch.plugin.*;\n" +
                        "import com.pascalwelsch.compositeandroid.fragment.*;\n" +
                        "public class MyFragment extends CompositeFragment {\n" +
                        "   public MyFragment() {\n" +
                        "       addPlugin(new TiFragmentPlugin<MyPresenter, MyView>(\n" +
                        "           () -> new MyPresenter()));\n" +
                        "   }\n" +
                        "}"
        )

        assertThat(
                lintProject(
                        caFragmentStub,
                        caBasePluginStub,
                        caFragmentPluginStub,
                        tiPresenterStub,
                        tiViewStub,
                        presenter,
                        view,
                        fragment
                )
        ).containsOnlyOnce(TiIssue.MissingView.id)
    }

    fun testFragment_doesntImplementInterface_butDoesntHavePluginAppliedEither_noWarnings() {
        val fragment = java(
                "package foo;\n" +
                        "import net.grandcentrix.thirtyinch.plugin.*;\n" +
                        "import com.pascalwelsch.compositeandroid.fragment.*;\n" +
                        "public class MyFragment extends CompositeFragment {\n" +
                        "}"
        )

        assertThat(
                lintProject(
                        caFragmentStub,
                        caBasePluginStub,
                        caFragmentPluginStub,
                        tiPresenterStub,
                        tiViewStub,
                        presenter,
                        view,
                        fragment
                )
        ).isEqualTo(NO_WARNINGS)
    }

    fun testFragment_appliesUnrelatedPlugin_noWarnings() {
        val otherPlugin = java(
                "package foo;\n" +
                        "import com.pascalwelsch.compositeandroid.*;\n" +
                        "public class OtherPlugin implements Plugin {\n" +
                        "}"
        )

        val fragment = java(
                "package foo;\n" +
                        "import net.grandcentrix.thirtyinch.plugin.*;\n" +
                        "import com.pascalwelsch.compositeandroid.fragment.*;\n" +
                        "public class MyFragment extends CompositeFragment {\n" +
                        "   public MyFragment() {\n" +
                        "       addPlugin(new OtherPlugin());\n" +
                        "   }\n" +
                        "}"
        )

        assertThat(
                lintProject(
                        caFragmentStub,
                        caBasePluginStub,
                        caFragmentPluginStub,
                        tiPresenterStub,
                        tiViewStub,
                        presenter,
                        view,
                        otherPlugin,
                        fragment
                )
        ).isEqualTo(NO_WARNINGS)
    }
}