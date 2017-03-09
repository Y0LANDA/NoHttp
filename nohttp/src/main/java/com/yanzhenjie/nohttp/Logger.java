/*
 * Copyright 2015 Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yanzhenjie.nohttp;

import java.lang.reflect.Method;

/**
 * Created in Jul 28, 2015 7:32:05 PM.
 *
 * @author Yan Zhenjie.
 */
public class Logger {

    private static final String V = "v";
    private static final String D = "d";
    private static final String I = "i";
    private static final String W = "w";
    private static final String E = "e";
    private static final String WTF = "wtf";
    private static final int MAX_LENGTH = 3000;
    /**
     * Library debug tag.
     */
    private static String STag = "NoHttp";
    /**
     * Library debug sign.
     */
    private static boolean SDebug = false;

    /**
     * Set tag of log.
     *
     * @param tag tag.
     */
    public static void setTag(String tag) {
        STag = tag;
    }

    /**
     * Open debug mode of {@code NoHttp}.
     *
     * @param debug true open, false close.
     */
    public static void setDebug(boolean debug) {
        SDebug = debug;
    }

    public static void i(Object msg) {
        print(I, msg);
    }

    public static void i(Throwable e) {
        i(e, "");
    }

    public static void i(Throwable e, Object msg) {
        print(I, msg, e);
    }

    public static void v(Object msg) {
        print(V, msg);
    }

    public static void v(Throwable e) {
        v(e, "");
    }

    public static void v(Throwable e, Object msg) {
        print(V, msg, e);
    }

    public static void d(Object msg) {
        print(D, msg);
    }

    public static void d(Throwable e) {
        d(e, "");
    }

    public static void d(Throwable e, Object msg) {
        print(D, msg, e);
    }

    public static void e(Object msg) {
        print(E, msg);
    }

    public static void e(Throwable e) {
        e(e, "");
    }

    public static void e(Throwable e, String msg) {
        print(E, msg, e);
    }

    public static void w(String msg) {
        print(W, msg);
    }

    public static void w(Throwable e) {
        w(e, "");
    }

    public static void w(Throwable e, String msg) {
        print(W, msg, e);
    }

    public static void wtf(String msg) {
        print(WTF, msg);
    }

    public static void wtf(Throwable e) {
        wtf(e, "");
    }

    public static void wtf(Throwable e, String msg) {
        print(WTF, msg, e);
    }

    private static void print(String method, Object o) {
        print(method, STag, toString(o));
    }

    private static String toString(Object o) {
        return o == null ? "null" : o.toString();
    }

    /**
     * Print log for define method. When information is too long, the Logger can also complete printing. The
     * equivalent of "{@code android.util.Log.i("Tag", "Message")}" "{@code Logger.print("i",
     * "Tag", "Message")}".
     *
     * @param method  such as "{@code v, i, d, w, e, wtf}".
     * @param tag     tag.
     * @param message message.
     */
    private static void print(String method, String tag, String message) {
        if (SDebug) {
            int strLength = message.length();
            if (strLength == 0)
                invokePrint(method, tag, message);
            else {
                for (int i = 0; i < strLength / MAX_LENGTH + (strLength % MAX_LENGTH > 0 ? 1 : 0); i++) {
                    int end = (i + 1) * MAX_LENGTH;
                    if (strLength >= end) {
                        invokePrint(method, tag, message.substring(end - MAX_LENGTH, end));
                    } else {
                        invokePrint(method, tag, message.substring(end - MAX_LENGTH));
                    }
                }
            }
        }
    }

    /**
     * Through the reflection to call the print method.
     *
     * @param method  such as "{@code v, i, d, w, e, wtf}".
     * @param tag     tag.
     * @param message message.
     */
    private static void invokePrint(String method, String tag, String message) {
        try {
            Class<android.util.Log> logClass = android.util.Log.class;
            Method logMethod = logClass.getMethod(method, String.class, String.class);
            logMethod.setAccessible(true);
            logMethod.invoke(null, tag, message);
        } catch (Exception e) {
            System.out.println(tag + ": " + message);
        }
    }

    /**
     * Print log for define method. When information is too long, the Logger can also complete printing. The
     * equivalent of "{@code android.util.Log.i("Tag", "Message")}" "{@code Logger.print("i",
     * "Tag", "Message")}".
     *
     * @param method such as "{@code v, i, d, w, e, wtf}".
     * @param o      message.
     * @param e      error.
     */
    private static void print(String method, Object o, Throwable e) {
        print(method, STag, toString(o), e);
    }

    /**
     * Print log for define method. When information is too long, the Logger can also complete printing. The
     * equivalent of "{@code android.util.Log.i("Tag", "Message")}" "{@code Logger.print("i",
     * "Tag", "Message")}".
     *
     * @param method  such as "{@code v, i, d, w, e, wtf}".
     * @param tag     tag.
     * @param message message.
     * @param e       error.
     */
    private static void print(String method, String tag, String message, Throwable e) {
        invokePrint(method, tag, message, e);
    }

    /**
     * Through the reflection to call the print method.
     *
     * @param method  such as "{@code v, i, d, w, e, wtf}".
     * @param tag     tag.
     * @param message message.
     * @param e       error.
     */
    private static void invokePrint(String method, String tag, String message, Throwable e) {
        if (SDebug)
            try {
                Class<android.util.Log> logClass = android.util.Log.class;
                Method logMethod = logClass.getMethod(method, String.class, String.class, Throwable.class);
                logMethod.setAccessible(true);
                logMethod.invoke(null, tag, message, e);
            } catch (Exception e1) {
                System.out.println(tag + ": " + message);
            }
    }

}