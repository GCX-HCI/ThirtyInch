package net.grandcentrix.thirtyinch.lint.detector

import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.TextFormat
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiType
import com.intellij.psi.util.PsiUtil
import org.jetbrains.uast.UClass
import org.jetbrains.uast.getUastContext

// Base class for Lint checks centered around the notion of "TiView not implemented"
abstract class BaseMissingViewDetector : Detector(), Detector.UastScanner {

    /**
     * The Issue that the detector is connected to,
     * reported on illegal state detection
     */
    abstract val issue: Issue

    /**
     * The list of super-classes to detect.
     * We're forcing sub-classed Detectors to implement this by means of redeclaration
     */
    abstract override fun applicableSuperClasses(): List<String>

    /**
     * Tries to extract the PsiType of the TiView sub-class
     * that is relevant for the given declaration. The relevant
     * super-class (from applicableSuperClasses()) & its resolved variant
     * are given as well.
     */
    abstract fun tryFindViewInterface(context: JavaContext, declaration: UClass, extendedType: PsiClassType,
            resolvedType: PsiClass): PsiType?

    /**
     * Whether or not to allow the absence of an "implements TiView" clause
     * on the given declaration. The View interface is given as well to allow
     * for further introspection into the setup of the class at hand.
     * When false is returned here, Lint will report the Issue connected to this Detector
     * on the given declaration.
     */
    abstract fun allowMissingViewInterface(context: JavaContext, declaration: UClass, viewInterface: PsiType): Boolean

    final override fun visitClass(context: JavaContext, declaration: UClass) {
        if (!context.isEnabled(issue)) {
            return
        }
        // Don't trigger on abstract classes
        if (PsiUtil.isAbstractClass(declaration.psi)) {
            return
        }
        // Extract the MVP View type from the declaration
        tryFindViewInterface(context, declaration)?.let { viewInterface ->
            // Check if the class implements that interface as well
            if (!tryFindViewImplementation(context, declaration, viewInterface)) {
                // Interface not implemented; check if alternate condition applies
                if (!allowMissingViewInterface(context, declaration, viewInterface)) {
                    // Invalid state: Report issue for this class
                    declaration.nameIdentifier?.run {
                        context.report(
                                issue,
                                context.getLocation(this.originalElement),
                                issue.getBriefDescription(TextFormat.TEXT))
                    }
                }
            }
        }
    }

    private fun tryFindViewInterface(context: JavaContext, declaration: UClass): PsiType? {
        for (extendedType in declaration.extendsListTypes) {
            extendedType.resolveGenerics().element?.let { resolvedType ->
                val qualifiedName = resolvedType.qualifiedName
                if (applicableSuperClasses().contains(qualifiedName)) {
                    // This detector is interested in this class; delegate to it
                    return tryFindViewInterface(context, declaration, extendedType, resolvedType)
                }
                // Crawl up the type hierarchy to catch declarations in super classes
                val uastContext = declaration.getUastContext()
                return tryFindViewInterface(context, uastContext.getClass(resolvedType))
            }
        }
        return null
    }

    private fun tryFindViewImplementation(context: JavaContext, declaration: UClass,
            viewInterface: PsiType): Boolean {
        for (implementedType in declaration.implementsListTypes) {
            if (implementedType == viewInterface) {
                return true
            }
            implementedType.resolve()?.let { resolvedType ->
                val uastContext = declaration.getUastContext()
                return tryFindViewImplementation(context, uastContext.getClass(resolvedType), viewInterface)
            }
        }
        return false
    }
}