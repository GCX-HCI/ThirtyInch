package net.grandcentrix.thirtyinch.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.LintDetectorTest.java
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import net.grandcentrix.thirtyinch.lint.detector.DistinctUntilChangedUsageDetector
import org.assertj.core.api.Assertions

private const val NO_WARNINGS = "No warnings."

private val CLASS_DISTINCTUNTILCHANGED = java(
        "package net.grandcentrix.thirtyinch.distinctuntilchanged;\n" +
                "\n" +
                "import java.lang.annotation.Documented;\n" +
                "import java.lang.annotation.ElementType;\n" +
                "import java.lang.annotation.Retention;\n" +
                "import java.lang.annotation.RetentionPolicy;\n" +
                "import java.lang.annotation.Target;\n" +
                "\n" +
                "@Documented\n" +
                "@Target(ElementType.METHOD)\n" +
                "@Retention(RetentionPolicy.RUNTIME)\n" +
                "public @interface DistinctUntilChanged {\n" +
                "\n" +
                "    Class<? extends DistinctComparator> comparator() default HashComparator.class;\n" +
                "\n" +
                "    boolean logDropped() default false;\n" +
                "\n" +
                "}"
)

class DistinctUntilChangedUsageDetectorTest : LintDetectorTest() {

    override fun getDetector(): Detector = DistinctUntilChangedUsageDetector()

    override fun getIssues(): MutableList<Issue> = mutableListOf(
            DistinctUntilChangedUsageDetector.ISSUE_NO_PARAMETER,
            DistinctUntilChangedUsageDetector.ISSUE_NON_VOID_RETURN_TYPE
    )

    fun testJava_annotation_used_on_method_without_parameter_should_have_warning() {
        val testInterface = java(
                "package foo;\n" +
                        "import net.grandcentrix.thirtyinch.distinctuntilchanged.DistinctUntilChanged;\n" +
                        "import net.grandcentrix.thirtyinch.TiView;\n" +
                        "public interface MyInterface extends TiView {\n" +
                        "   @DistinctUntilChanged\n" +
                        "   public void test();\n" +
                        "}"
        )

        Assertions.assertThat(lintProject(CLASS_DISTINCTUNTILCHANGED, testInterface))
                .containsOnlyOnce(TiIssue.DistinctUntilChangedWithoutParameter.id)
    }

    fun testKotlin_annotation_used_on_method_without_parameter_should_have_warning() {
        val testInterface = kotlin(
                "package foo\n" +
                        "import net.grandcentrix.thirtyinch.distinctuntilchanged.DistinctUntilChanged\n" +
                        "import net.grandcentrix.thirtyinch.TiView\n" +
                        "interface MyInterface : TiView {\n" +
                        "   @DistinctUntilChanged\n" +
                        "   fun test()\n" +
                        "}"
        )

        Assertions.assertThat(lintProject(CLASS_DISTINCTUNTILCHANGED, testInterface))
                .containsOnlyOnce(TiIssue.DistinctUntilChangedWithoutParameter.id)
    }

    fun testJava_annotation_used_on_method_with_parameter_should_have_no_warning() {
        val testInterface = java(
                "package foo;\n" +
                        "import net.grandcentrix.thirtyinch.distinctuntilchanged.DistinctUntilChanged;\n" +
                        "import net.grandcentrix.thirtyinch.TiView;\n" +
                        "public interface MyInterface extends TiView {\n" +
                        "   @DistinctUntilChanged\n" +
                        "   public void test(String id);\n" +
                        "}"
        )

        Assertions.assertThat(lintProject(CLASS_DISTINCTUNTILCHANGED, testInterface)).isEqualTo(NO_WARNINGS)
    }

    fun testKotlin_annotation_used_on_method_with_parameter_should_have_no_warning() {
        val testInterface = kotlin(
                "package foo\n" +
                        "import net.grandcentrix.thirtyinch.distinctuntilchanged.DistinctUntilChanged\n" +
                        "import net.grandcentrix.thirtyinch.TiView\n" +
                        "interface MyInterface : TiView {\n" +
                        "   @DistinctUntilChanged\n" +
                        "   fun test(id: String)\n" +
                        "}"
        )

        Assertions.assertThat(lintProject(CLASS_DISTINCTUNTILCHANGED, testInterface)).isEqualTo(NO_WARNINGS)
    }

    fun testJava_annotation_used_on_method_with_non_void_return_type_should_have_warning() {
        val testInterface = java(
                "package foo;\n" +
                        "import net.grandcentrix.thirtyinch.distinctuntilchanged.DistinctUntilChanged;\n" +
                        "import net.grandcentrix.thirtyinch.TiView;\n" +
                        "public interface MyInterface extends TiView {\n" +
                        "   @DistinctUntilChanged\n" +
                        "   public int test(String id);\n" +
                        "}"
        )

        Assertions.assertThat(lintProject(CLASS_DISTINCTUNTILCHANGED, testInterface))
                .containsOnlyOnce(TiIssue.AnnotationOnNonVoidMethod.id)
    }

    fun testKotlin_annotation_used_on_method_with_non_void_return_type_should_have_warning() {
        val testInterface = kotlin(
                "package foo\n" +
                        "import net.grandcentrix.thirtyinch.distinctuntilchanged.DistinctUntilChanged\n" +
                        "import net.grandcentrix.thirtyinch.TiView\n" +
                        "interface MyInterface : TiView {\n" +
                        "   @DistinctUntilChanged\n" +
                        "   fun test(id: String): Int\n" +
                        "}"
        )

        Assertions.assertThat(lintProject(CLASS_DISTINCTUNTILCHANGED, testInterface))
                .containsOnlyOnce(TiIssue.AnnotationOnNonVoidMethod.id)
    }

    fun testJava_annotation_used_on_method_with_void_return_type_should_have_no_warning() {
        val testInterface = java(
                "package foo;\n" +
                        "import net.grandcentrix.thirtyinch.distinctuntilchanged.DistinctUntilChanged;\n" +
                        "import net.grandcentrix.thirtyinch.TiView;\n" +
                        "public interface MyInterface extends TiView {\n" +
                        "   @DistinctUntilChanged\n" +
                        "   public void test(String id);\n" +
                        "}"
        )

        Assertions.assertThat(lintProject(CLASS_DISTINCTUNTILCHANGED, testInterface)).isEqualTo(NO_WARNINGS)
    }

    fun testKotlin_annotation_used_on_method_with_void_return_type_should_have_no_warning() {
        val testInterface = kotlin(
                "package foo\n" +
                        "import net.grandcentrix.thirtyinch.distinctuntilchanged.DistinctUntilChanged\n" +
                        "import net.grandcentrix.thirtyinch.TiView\n" +
                        "interface MyInterface : TiView {\n" +
                        "   @DistinctUntilChanged\n" +
                        "   fun test(id: String)\n" +
                        "}"
        )

        Assertions.assertThat(lintProject(CLASS_DISTINCTUNTILCHANGED, testInterface)).isEqualTo(NO_WARNINGS)
    }
}