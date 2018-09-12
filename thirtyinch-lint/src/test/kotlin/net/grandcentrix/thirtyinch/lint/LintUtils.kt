package net.grandcentrix.thirtyinch.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest

internal const val NO_WARNINGS = "No warnings."

internal val tiActivityStub = LintDetectorTest.java(
        "package net.grandcentrix.thirtyinch;\n" +
                "public abstract class TiActivity<P extends TiPresenter<V>, V extends TiView> {\n" +
                "}"
)

internal val tiFragmentStub = LintDetectorTest.java(
        "package net.grandcentrix.thirtyinch;\n" +
                "public abstract class TiFragment<P extends TiPresenter<V>, V extends TiView> {\n" +
                "}"
)

internal val tiViewStub = LintDetectorTest.java(
        "package net.grandcentrix.thirtyinch;\n" +
                "public interface TiView {\n" +
                "}"
)

internal val tiPresenterStub = LintDetectorTest.java(
        "package net.grandcentrix.thirtyinch;\n" +
                "public abstract class TiPresenter<V extends TiView> {\n" +
                "  public V getView() {\n" +
                "        return null;\n" +
                "  }\n" +
                "  public V getViewOrThrow() {\n" +
                "        return null;\n" +
                "  }\n" +
                "}"
)