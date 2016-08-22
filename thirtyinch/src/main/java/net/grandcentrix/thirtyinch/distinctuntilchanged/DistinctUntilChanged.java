package net.grandcentrix.thirtyinch.distinctuntilchanged;

import net.grandcentrix.thirtyinch.TiView;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When added to a {@code void} method with at least one parameter inside a {@link TiView}, the
 * method implementation will only be called when the parameters change. The {@link
 * Object#hashCode()} method is used to detect changes.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistinctUntilChanged {

    boolean logDropped() default false;

}
