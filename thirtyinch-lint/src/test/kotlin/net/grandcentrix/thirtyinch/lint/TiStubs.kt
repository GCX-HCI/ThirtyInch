package net.grandcentrix.thirtyinch.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest

internal object TiStubs {
    val TiView = LintDetectorTest.java(
            "package net.grandcentrix.thirtyinch;\n" +
                    "public interface TiView {}"
    )!!

    val DistinctUntilChanged = LintDetectorTest.java(
            "package net.grandcentrix.thirtyinch.distinctuntilchanged;\n" +
                    "public @interface DistinctUntilChanged {\n" +
                    "    Class<? extends DistinctComparator> comparator() default HashComparator.class;\n" +
                    "    boolean logDropped() default false;\n" +
                    "}"
    )!!

    val CallOnMainThread = LintDetectorTest.java(
            "package net.grandcentrix.thirtyinch.callonmainthread;\n" +
                    "public @interface CallOnMainThread {}"
    )!!

    val TiActivity = LintDetectorTest.java(
            "package net.grandcentrix.thirtyinch;\n" +
                    "public abstract class TiActivity<P extends TiPresenter<V>, V extends TiView> {\n" +
                    "}"
    )!!

    val TiFragment = LintDetectorTest.java(
            "package net.grandcentrix.thirtyinch;\n" +
                    "public abstract class TiFragment<P extends TiPresenter<V>, V extends TiView> {\n" +
                    "}"
    )!!

    val TiPresenter = LintDetectorTest.java(
            "package net.grandcentrix.thirtyinch;\n" +
                    "public abstract class TiPresenter<V extends TiView> {}"
    )!!
}