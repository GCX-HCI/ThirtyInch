/*
 * Copyright (C) 2017 grandcentrix GmbH
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.grandcentrix.thirtyinch.logginginterceptor

import android.util.Log
import net.grandcentrix.thirtyinch.BindViewInterceptor
import net.grandcentrix.thirtyinch.TiLog
import net.grandcentrix.thirtyinch.TiView
import net.grandcentrix.thirtyinch.util.AbstractInvocationHandler
import net.grandcentrix.thirtyinch.util.AnnotationUtil.getInterfaceOfClassExtendingGivenInterface
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Proxy

private val TAG = LoggingInterceptor::class.java.simpleName

/**
 * limit each argument instead of the complete string. This should limit the overall output to a reasonable length
 * while showing all params
 */
private const val MAX_LENGTH_OF_PARAM = 240

/**
 * Logs all methods calls and parameters to the bound view interface.
 *
 * The no argument constructor creates an instance which logs all view interface method invocations to [TiLog]. You
 * may have to enable logging from [TiLog].
 *
 * You can also use the constructor with one [TiLog.Logger] argument. It will create an instance which logs all view
 * interface method invocations to the [logger] you provided.
 *
 * @param logger custom logger, [TiLog.LOGCAT] or [TiLog.NOOP] to disable logging.
 */
class LoggingInterceptor @JvmOverloads constructor(logger: TiLog.Logger? = TiLog.TI_LOG) : BindViewInterceptor {

    private val logger: TiLog.Logger = logger ?: TiLog.NOOP

    private class MethodLoggingInvocationHandler<V>(
            private val view: V,
            private val logger: TiLog.Logger
    ) : AbstractInvocationHandler() {

        override fun toString(): String = "MethodLoggingProxy@${Integer.toHexString(hashCode())}-$view"

        @Throws(Throwable::class)
        override fun handleInvocation(proxy: Any, method: Method, args: Array<Any?>): Any? =
                try {
                    logger.log(Log.VERBOSE, TAG, toString(method, args))
                    method.invoke(view, *args)
                } catch (e: InvocationTargetException) {
                    throw e.cause ?: Exception("Invoked method exception cause is null")
                }
    }

    override fun <V : TiView> intercept(view: V): V =
            if (logger !== TiLog.NOOP) {
                val wrapped = wrap(view)
                TiLog.v(TAG, "wrapping View $view in $wrapped")
                wrapped
            } else {
                view
            }

    private fun <V : TiView> wrap(view: V): V {
        val foundInterfaceClass = getInterfaceOfClassExtendingGivenInterface(view.javaClass, TiView::class.java)
                ?: throw IllegalStateException("the interface extending TiView could not be found")

        @Suppress("UNCHECKED_CAST")
        return Proxy.newProxyInstance(
                foundInterfaceClass.classLoader,
                arrayOf(foundInterfaceClass),
                MethodLoggingInvocationHandler(view, logger)
        ) as V
    }
}

private fun toString(method: Method, args: Array<Any?>): String =
        with(StringBuilder(method.name)) {
            append("(")
            if (args.isNotEmpty()) append(parseParams(args, MAX_LENGTH_OF_PARAM))
            append(")")
            toString()
        }

private fun parseParams(methodParams: Array<Any?>, maxLenOfParam: Int): String =
        methodParams.joinToString { param ->

            val paramString: String = when (param) {
                is List<*> -> {
                    val clazz = param.javaClass.simpleName
                    val size = param.size
                    val hash = Integer.toHexString(param.hashCode())

                    "{$clazz[$size]@$hash} $param"
                }
                is Array<*> -> {
                    val clazz = param.javaClass.simpleName
                    val size = param.size
                    val hash = Integer.toHexString(param.hashCode())
                    // we have to list all array elements ourselves
                    val elements = param.joinToString(prefix = "[", postfix = "]")

                    "{$clazz[$size]@$hash} $elements"
                }
                else -> param.toString()
            }

            if (paramString.length <= maxLenOfParam) {
                paramString
            } else {
                // trim remaining whitespace at the end before appending ellipsis
                val shortParam = paramString.take(maxLenOfParam).trimEnd()
                "$shortParam…"
            }
        }