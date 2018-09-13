package net.grandcentrix.thirtyinch.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import net.grandcentrix.thirtyinch.lint.detector.GetViewOrThrowInOnAttachDetector
import org.assertj.core.api.Assertions

private val view = LintDetectorTest.java(
        "package foo;\n" +
                "import net.grandcentrix.thirtyinch.*;\n" +
                "interface MyView extends TiView {\n" +
                "}"
)

class GetViewOrThrowInOnAttachDetectorTest : LintDetectorTest() {

    override fun getIssues(): MutableList<Issue> = mutableListOf(GetViewOrThrowInOnAttachDetector.ISSUE)

    override fun getDetector(): Detector {
        return GetViewOrThrowInOnAttachDetector()
    }

    fun test_noDirectUsage_noWarning() {
        val presenter = java(
                "package foo;\n" +
                        "import net.grandcentrix.thirtyinch.*;\n" +
                        "public class MyPresenter extends TiPresenter<MyView> {\n" +
                        "  protected void onAttachView(MyView view) {\n" +
                        "    final int test = 42;\n" +
                        "    final int test2 = test();\n" +
                        "    test();\n" +
                        "  }\n" +
                        "  private int test() {" +
                        "    return 42;\n" +
                        "  }\n" +
                        "}"
        )

        Assertions.assertThat(
                lintProject(
                        tiPresenterStub,
                        tiViewStub,
                        view,
                        presenter
                )
        ).isEqualTo(NO_WARNINGS)
    }

    fun test_getViewOrThrow_asMethod_hasWarning() {
        val presenter = java(
                "package foo;\n" +
                        "import net.grandcentrix.thirtyinch.*;\n" +
                        "public class MyPresenter extends TiPresenter<MyView> {\n" +
                        "  protected void onAttachView(MyView view) {" +
                        "    getViewOrThrow();" +
                        "  }" +
                        "}"
        )

        Assertions.assertThat(
                lintProject(
                        tiPresenterStub,
                        tiViewStub,
                        view,
                        presenter
                )
        ).containsOnlyOnce(TiIssue.GetViewOrThrowInOnAttach.id)
    }

    fun test_getViewOrThrow_asAssignment_hasWarning() {
        val presenter = java(
                "package foo;\n" +
                        "import net.grandcentrix.thirtyinch.*;\n" +
                        "public class MyPresenter extends TiPresenter<MyView> {\n" +
                        "  protected void onAttachView(MyView view) {\n" +
                        "    MyView view = getViewOrThrow();\n" +
                        "  }\n" +
                        "}"
        )

        Assertions.assertThat(
                lintProject(
                        tiPresenterStub,
                        tiViewStub,
                        view,
                        presenter
                )
        ).containsOnlyOnce(TiIssue.GetViewOrThrowInOnAttach.id)
    }

    fun test_getViewOrThrow_asReturn_hasWarning() {
        val presenter = java(
                "package foo;\n" +
                        "import net.grandcentrix.thirtyinch.*;\n" +
                        "public class MyPresenter extends TiPresenter<MyView> {\n" +
                        "  protected void onAttachView(MyView view) {\n" +
                        "    MyView view = test();\n" +
                        "  }\n" +
                        "  private MyView test() {" +
                        "    return getViewOrThrow();\n" +
                        "  }\n" +
                        "}"
        )

        Assertions.assertThat(
                lintProject(
                        tiPresenterStub,
                        tiViewStub,
                        view,
                        presenter
                )
        ).containsOnlyOnce(TiIssue.GetViewOrThrowInOnAttach.id)
    }

    fun test_getViewOrThrow_transitiveUsage_hasWarning() {
        val presenter = java(
                "package foo;\n" +
                        "import net.grandcentrix.thirtyinch.*;\n" +
                        "public class MyPresenter extends TiPresenter<MyView> {\n" +
                        "  protected void onAttachView(MyView view) {\n" +
                        "    test();\n" +
                        "  }\n" +
                        "  private void test() {" +
                        "    test2();\n" +
                        "  }\n" +
                        "  private void test2() {" +
                        "    test3();\n" +
                        "  }\n" +
                        "  private void test3() {" +
                        "    test4();\n" +
                        "  }\n" +
                        "  private void test4() {" +
                        "    getViewOrThrow();\n" +
                        "  }\n" +
                        "}"
        )

        Assertions.assertThat(
                lintProject(
                        tiPresenterStub,
                        tiViewStub,
                        view,
                        presenter
                )
        ).containsOnlyOnce(TiIssue.GetViewOrThrowInOnAttach.id)
    }

    fun test_noTransitiveUsage_noWarning() {
        val presenter = java(
                "package foo;\n" +
                        "import net.grandcentrix.thirtyinch.*;\n" +
                        "public class MyPresenter extends TiPresenter<MyView> {\n" +
                        "  protected void onAttachView(MyView view) {\n" +
                        "    test();\n" +
                        "  }\n" +
                        "  private void test() {" +
                        "    test2();\n" +
                        "  }\n" +
                        "  private void test2() {" +
                        "    test3();\n" +
                        "  }\n" +
                        "  private void test3() {" +
                        "    test4();\n" +
                        "  }\n" +
                        "  private void test4() {" +
                        "  }\n" +
                        "}"
        )

        Assertions.assertThat(
                lintProject(
                        tiPresenterStub,
                        tiViewStub,
                        view,
                        presenter
                )
        ).isEqualTo(NO_WARNINGS)
    }
}