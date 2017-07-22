package net.grandcentrix.thirtyinch;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

import org.jetbrains.uast.UClass;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public final class MissingTiViewImplementationDetector extends Detector implements Detector.UastScanner {

    static final Issue ISSUE = Issue.create(
            "MissingTiViewImplementation",
            "TiView Implementation missing in class",
            "With ThirtyInch, an Activity extending TiActivity or CompositeActivity has to implement the View interface in its signature.",
            Category.CORRECTNESS,
            8,
            Severity.ERROR,
            new Implementation(MissingTiViewImplementationDetector.class, EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES)));

    @Override
    public List<String> applicableSuperClasses() {
        List<String> superClasses = new ArrayList<>();
        superClasses.add("net.grandcentrix.thirtyinch.TiActivity");
        superClasses.add("com.pascalwelsch.compositeandroid.activity.CompositeActivity");
        return superClasses;
    }

    @Override
    public void visitClass(JavaContext context, UClass declaration) {
        super.visitClass(context, declaration);

        context.log(null, "Visit Declaration: " + declaration.asLogString());

        // TODO Check for Fragments
        // TODO Check type parameters
        // TODO Check if provideView() overridden
    }
}
