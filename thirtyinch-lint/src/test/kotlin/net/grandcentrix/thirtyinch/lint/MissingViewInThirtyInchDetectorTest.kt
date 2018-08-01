package net.grandcentrix.thirtyinch.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import net.grandcentrix.thirtyinch.lint.detector.MissingViewInThirtyInchDetector
import org.assertj.core.api.Assertions.*

private const val NO_WARNINGS = "No warnings."

class MissingViewInThirtyInchDetectorTest : LintDetectorTest() {

    /* Stubbed-out source files */

    private val tiActivityStub = java(
            "package net.grandcentrix.thirtyinch;\n" +
                    "public abstract class TiActivity<P extends TiPresenter<V>, V extends TiView> {\n" +
                    "}"
    )

    private val tiFragmentStub = java(
            "package net.grandcentrix.thirtyinch;\n" +
                    "public abstract class TiFragment<P extends TiPresenter<V>, V extends TiView> {\n" +
                    "}"
    )

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

    override fun getDetector(): Detector = MissingViewInThirtyInchDetector()

    override fun getIssues(): MutableList<Issue> = mutableListOf(
            MissingViewInThirtyInchDetector.ISSUE)

    /*
     * --------------------------------------------------------------------------------
     * TiActivity
     * --------------------------------------------------------------------------------
     */

    fun testActivity_dontTriggerOnAbstractClass() {
        val activity = java(
                "package foo;\n" +
                        "import net.grandcentrix.thirtyinch.*;\n" +
                        "public abstract class MyActivity extends TiActivity<MyPresenter, MyView> {\n" +
                        "}"
        )

        assertThat(
                lintProject(
                        tiActivityStub,
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
                        "import net.grandcentrix.thirtyinch.*;\n" +
                        "public class MyActivity extends TiActivity<MyPresenter, MyView> implements MyView {\n" +
                        "}"
        )

        assertThat(
                lintProject(
                        tiActivityStub,
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
                        "import net.grandcentrix.thirtyinch.*;\n" +
                        "public class MyActivity extends TiActivity<MyPresenter, MyView> {\n" +
                        "}"
        )

        assertThat(
                lintProject(
                        tiActivityStub,
                        tiPresenterStub,
                        tiViewStub,
                        presenter,
                        view,
                        activity
                )
        ).containsOnlyOnce(TiIssue.MissingView.id)
    }

    fun testActivity_doesntImplementInterface_butOverridesProvideView_noWarnings() {
        val activity = java(
                "package foo;\n" +
                        "import net.grandcentrix.thirtyinch.*;\n" +
                        "public class MyActivity extends TiActivity<MyPresenter, MyView> {\n" +
                        "   public MyView provideView() {\n" +
                        "       return null;\n" +
                        "   }\n" +
                        "}"
        )

        assertThat(
                lintProject(
                        tiActivityStub,
                        tiPresenterStub,
                        tiViewStub,
                        presenter,
                        view,
                        activity
                )
        ).isEqualTo(NO_WARNINGS)
    }

    fun testActivity_throughTransitiveBaseClass_hasWarning() {
        val baseActivity = java(
                "package foo;\n" +
                        "import net.grandcentrix.thirtyinch.*;\n" +
                        "public abstract class BaseActivity<P extends TiPresenter<V>, V extends TiView> extends TiActivity<P, V> {\n" +
                        "}"
        )

        val activity = java(
                "package foo;\n" +
                        "import net.grandcentrix.thirtyinch.*;\n" +
                        "public class MyActivity extends BaseActivity<MyPresenter, MyView> {\n" +
                        "}"
        )

        assertThat(
                lintProject(
                        tiActivityStub,
                        tiPresenterStub,
                        tiViewStub,
                        presenter,
                        view,
                        baseActivity,
                        activity
                )
        ).containsOnlyOnce(TiIssue.MissingView.id)
    }
    /*
     * --------------------------------------------------------------------------------
     * TiFragment
     * --------------------------------------------------------------------------------
     */

    fun testFragment_dontTriggerOnAbstractClass() {
        val fragment = java(
                "package foo;\n" +
                        "import net.grandcentrix.thirtyinch.*;\n" +
                        "public abstract class MyFragment extends TiFragment<MyPresenter, MyView> {\n" +
                        "}"
        )

        assertThat(
                lintProject(
                        tiFragmentStub,
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
                        "import net.grandcentrix.thirtyinch.*;\n" +
                        "public class MyFragment extends TiFragment<MyPresenter, MyView> implements MyView {\n" +
                        "}"
        )

        assertThat(lintProject(
                tiFragmentStub, tiPresenterStub, tiViewStub,
                presenter, view, fragment))
                .isEqualTo(NO_WARNINGS)
    }

    fun testFragment_doesntImplementInterface_hasWarning() {
        val fragment = java(
                "package foo;\n" +
                        "import net.grandcentrix.thirtyinch.*;\n" +
                        "public class MyFragment extends TiFragment<MyPresenter, MyView> {\n" +
                        "}"
        )

        assertThat(
                lintProject(
                        tiFragmentStub,
                        tiPresenterStub,
                        tiViewStub,
                        presenter,
                        view,
                        fragment
                )
        ).containsOnlyOnce(TiIssue.MissingView.id)
    }

    fun testFragment_doesntImplementInterface_butOverridesProvideView_noWarnings() {
        val fragment = java(
                "package foo;\n" +
                        "import net.grandcentrix.thirtyinch.*;\n" +
                        "public class MyFragment extends TiFragment<MyPresenter, MyView> {\n" +
                        "   public MyView provideView() {\n" +
                        "       return null;\n" +
                        "   }\n" +
                        "}"
        )

        assertThat(
                lintProject(
                        tiFragmentStub,
                        tiPresenterStub,
                        tiViewStub,
                        presenter,
                        view,
                        fragment
                )
        ).isEqualTo(NO_WARNINGS)
    }

    fun testFragment_throughTransitiveBaseClass_hasWarning() {
        val baseFragment = java(
                "package foo;\n" +
                        "import net.grandcentrix.thirtyinch.*;\n" +
                        "public abstract class BaseFragment<P extends TiPresenter<V>, V extends TiView> extends TiFragment<P, V> {\n" +
                        "}"
        )

        val fragment = java(
                "package foo;\n" +
                        "import net.grandcentrix.thirtyinch.*;\n" +
                        "public class MyFragment extends BaseFragment<MyPresenter, MyView> {\n" +
                        "}"
        )

        assertThat(
                lintProject(
                        tiFragmentStub,
                        tiPresenterStub,
                        tiViewStub,
                        presenter,
                        view,
                        baseFragment,
                        fragment
                )
        ).containsOnlyOnce(TiIssue.MissingView.id)
    }
}