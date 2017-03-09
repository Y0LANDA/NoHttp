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
package com.yanzhenjie.nohttp.tools;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StrikethroughSpan;
import android.view.View;
import android.widget.TextView;

import com.yanzhenjie.nohttp.NoHttp;

import java.lang.reflect.Method;

/**
 * Created in Nov 27, 2015 6:20:48 PM.
 *
 * @author Yan Zhenjie.
 */
public class ResCompat {

    public static Drawable getDrawable(int drawableId) {
        return getDrawable(drawableId, null);
    }

    public static Drawable getDrawable(int drawableId, Theme theme) {
        Resources resources = NoHttp.getContext().getResources();
        Class<?> resourcesClass = resources.getClass();
        if (Build.VERSION.SDK_INT >= AndroidVersion.LOLLIPOP)
            try {
                Method getDrawableMethod = resourcesClass.getMethod("getDrawable", int.class, Theme.class);
                getDrawableMethod.setAccessible(true);
                return (Drawable) getDrawableMethod.invoke(resources, drawableId, theme);
            } catch (Throwable e) {
            }
        else
            try {
                Method getDrawableMethod = resourcesClass.getMethod("getDrawable", int.class);
                getDrawableMethod.setAccessible(true);
                return (Drawable) getDrawableMethod.invoke(resources, drawableId);
            } catch (Throwable e) {
            }
        return null;
    }

    public static void setLeftDrawable(TextView textView, Drawable leftDrawable) {
        setDrawableBounds(leftDrawable);
        Drawable top = textView.getCompoundDrawables()[1];
        Drawable right = textView.getCompoundDrawables()[2];
        Drawable bottom = textView.getCompoundDrawables()[3];
        textView.setCompoundDrawables(leftDrawable, top, right, bottom);
    }

    public static void setLeftDrawable(TextView textView, int drawableId) {
        setLeftDrawable(textView, getDrawable(drawableId));
    }

    public static void setTopDrawable(TextView textView, Drawable topDrawable) {
        setDrawableBounds(topDrawable);
        Drawable left = textView.getCompoundDrawables()[0];
        Drawable right = textView.getCompoundDrawables()[2];
        Drawable bottom = textView.getCompoundDrawables()[3];
        textView.setCompoundDrawables(left, topDrawable, right, bottom);
    }

    public static void setTopDrawable(TextView textView, int drawableId) {
        setTopDrawable(textView, getDrawable(drawableId));
    }

    public static void setRightDrawable(TextView textView, Drawable rightDrawable) {
        setDrawableBounds(rightDrawable);
        Drawable left = textView.getCompoundDrawables()[0];
        Drawable top = textView.getCompoundDrawables()[1];
        Drawable bottom = textView.getCompoundDrawables()[3];
        textView.setCompoundDrawables(left, top, rightDrawable, bottom);
    }

    public static void setRightDrawable(TextView textView, int drawableId) {
        setRightDrawable(textView, getDrawable(drawableId));
    }

    public static void setBottomDrawable(TextView textView, Drawable bottomDrawable) {
        setDrawableBounds(bottomDrawable);
        Drawable left = textView.getCompoundDrawables()[0];
        Drawable top = textView.getCompoundDrawables()[1];
        Drawable right = textView.getCompoundDrawables()[2];
        textView.setCompoundDrawables(left, top, right, bottomDrawable);
    }

    public static void setBottomDrawable(TextView textView, int drawableId) {
        setBottomDrawable(textView, getDrawable(drawableId));
    }

    public static void setCompoundDrawables(TextView textView, Drawable leftDrawable, Drawable topDrawable,
                                            Drawable rightDrawable, Drawable bottomDrawable) {
        setDrawableBounds(leftDrawable);
        setDrawableBounds(topDrawable);
        setDrawableBounds(rightDrawable);
        setDrawableBounds(bottomDrawable);
        textView.setCompoundDrawables(leftDrawable, topDrawable, rightDrawable, bottomDrawable);
    }

    public static void setCompoundDrawables(TextView textView, int drawableLeftId, int drawableRightId, int
            drawableTopId, int drawableBottomId) {
        setCompoundDrawables(textView, getDrawable(drawableLeftId), getDrawable(drawableRightId), getDrawable
                (drawableTopId), getDrawable(drawableBottomId));
    }

    public static void setDrawableBounds(Drawable drawable) {
        if (drawable != null)
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
    }

    public static int getColor(int colorId) {
        return getColor(colorId, null);
    }

    public static int getColor(int colorId, Theme theme) {
        Resources resources = NoHttp.getContext().getResources();
        Class<?> resourcesClass = resources.getClass();
        if (Build.VERSION.SDK_INT >= AndroidVersion.M)
            try {
                Method getColorMethod = resourcesClass.getMethod("getColor", int.class, Theme.class);
                getColorMethod.setAccessible(true);
                return (Integer) getColorMethod.invoke(resources, colorId, theme);
            } catch (Throwable e) {
            }
        else
            try {
                Method getColorMethod = resourcesClass.getMethod("getColor", int.class);
                getColorMethod.setAccessible(true);
                return (Integer) getColorMethod.invoke(resources, colorId);
            } catch (Throwable e) {
            }
        return Color.BLACK;
    }

    public static ColorStateList getColorStateList(int colorStateId) {
        return getColorStateList(colorStateId, null);
    }

    public static ColorStateList getColorStateList(int colorStateId, Theme theme) {
        Resources resources = NoHttp.getContext().getResources();
        Class<?> resourcesClass = resources.getClass();
        if (Build.VERSION.SDK_INT >= AndroidVersion.M)
            try {
                Method getColorStateListMethod = resourcesClass.getMethod("getColorStateList", int.class, Theme
                        .class);
                getColorStateListMethod.setAccessible(true);
                return (ColorStateList) getColorStateListMethod.invoke(resources, colorStateId, theme);
            } catch (Throwable e) {
            }
        else
            try {
                Method getColorStateListMethod = resourcesClass.getMethod("getColorStateList", int.class);
                getColorStateListMethod.setAccessible(true);
                return (ColorStateList) getColorStateListMethod.invoke(resources, colorStateId);
            } catch (Throwable e) {
            }
        return null;
    }

    public static void setBackground(View view, int drawableId) {
        setBackground(view, getDrawable(drawableId));
    }

    public static void setBackground(View view, Drawable background) {
        if (Build.VERSION.SDK_INT >= AndroidVersion.JELLY_BEAN)
            setBackground("setBackground", view, background);
        else
            setBackground("setBackgroundDrawable", view, background);
    }

    public static void setBackground(String method, View view, Drawable background) {
        try {
            Method viewMethod = view.getClass().getMethod(method, Drawable.class);
            viewMethod.setAccessible(true);
            viewMethod.invoke(view, background);
        } catch (Throwable e) {
        }
    }

    public static SpannableString getScaleText(CharSequence content, int start, int end, int px) {
        SpannableString stringSpan = new SpannableString(content);
        stringSpan.setSpan(new AbsoluteSizeSpan(px), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return stringSpan;
    }

    public static SpannableString getColorText(CharSequence content, int start, int end, int color) {
        SpannableString stringSpan = new SpannableString(content);
        stringSpan.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return stringSpan;
    }

    public static SpannableString getDeleteText(CharSequence content) {
        return getDeleteText(content, 0, content.length());
    }

    public static SpannableString getDeleteText(CharSequence content, int start, int end) {
        SpannableString stringSpan = new SpannableString(content);
        stringSpan.setSpan(new StrikethroughSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return stringSpan;
    }

    public static SpannableString getImageSpanText(CharSequence content, Drawable drawable, int start, int end) {
        SpannableString stringSpan = new SpannableString(content);
        setDrawableBounds(drawable);
        ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
        stringSpan.setSpan(imageSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return stringSpan;
    }
}
