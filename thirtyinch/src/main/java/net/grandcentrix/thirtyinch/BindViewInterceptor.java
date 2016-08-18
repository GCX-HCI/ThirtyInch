package net.grandcentrix.thirtyinch;

public interface BindViewInterceptor {

    <V extends TiView> V intercept(final V view);

}
