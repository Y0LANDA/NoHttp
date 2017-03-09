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

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.yanzhenjie.nohttp.Logger;
import com.yanzhenjie.nohttp.HandlerDelivery;
import com.yanzhenjie.nohttp.NoHttp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created in Nov 4, 2015 3:07:29 PM.
 *
 * @author Yan Zhenjie.
 */
public class ImageLocalLoader {
    /**
     * Single module.
     */
    private static ImageLocalLoader mInstance;
    /**
     * Default gray image.
     */
    private Drawable mDefaultDrawable;
    /**
     * Image cache.
     */
    private LruCache<String, Bitmap> mLruCache;
    /**
     * Thread pool.
     */
    private ExecutorService mExecutorService;

    /**
     * Get single object.
     *
     * @return {@link ImageLocalLoader}.
     */
    public static ImageLocalLoader getInstance() {
        synchronized (ImageLocalLoader.class) {
            if (mInstance == null) {
                mInstance = new ImageLocalLoader();
            }
        }
        return mInstance;
    }

    private ImageLocalLoader() {
        mDefaultDrawable = new ColorDrawable(Color.GRAY);
        mExecutorService = Executors.newSingleThreadExecutor();

        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 8);
        mLruCache = new LruCache<String, Bitmap>(maxMemory) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
    }

    /**
     * Deposit in the province read images, width is high, the greater the picture clearer, but also the memory.
     *
     * @param imagePath pictures in the path of the memory card.
     * @param maxWidth  the highest limit value target width.
     * @param maxHeight the highest limit value target height.
     * @return Bitmap
     */
    public Bitmap readImage(String imagePath, int maxWidth, int maxHeight) {
        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            InputStream inputStream = null;
            try {
                inputStream = new BufferedInputStream(new FileInputStream(imageFile));
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, null, options);
                inputStream.close();
                int i = 0;
                while (true) {
                    if ((options.outWidth >> i <= maxWidth) && (options.outHeight >> i <= maxHeight)) {
                        inputStream = new BufferedInputStream(new FileInputStream(new File(imagePath)));
                        options.inSampleSize = (int) Math.pow(2.0, i);
                        options.inJustDecodeBounds = false;
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                        inputStream.close();
                        return bitmap;
                    }
                    i += 1;
                }
            } catch (IOException e) {
                Logger.e(e, "This path does not exist" + imagePath + ".");
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }
        return null;
    }

    /**
     * According to the ImageView obtains appropriate width and height of compression.
     *
     * @param imageView {@link ImageView}.
     * @param viewSizes ViewSize.
     */
    public void measureSize(ImageView imageView, int[] viewSizes) {
        final DisplayMetrics displayMetrics = NoHttp.getContext().getResources().getDisplayMetrics();
        final LayoutParams params = imageView.getLayoutParams();
        if (params == null) {
            viewSizes[0] = displayMetrics.widthPixels;
            viewSizes[1] = displayMetrics.heightPixels;
        } else {
            viewSizes[0] = params.width == LayoutParams.WRAP_CONTENT ? 0 : imageView.getWidth(); // Get actual image
            // width
            viewSizes[1] = params.height == LayoutParams.WRAP_CONTENT ? 0 : imageView.getHeight(); // Get actual
            // image height

            if (viewSizes[0] <= 0)
                viewSizes[0] = displayMetrics.widthPixels; // Get layout width parameter
            if (viewSizes[1] <= 0)
                viewSizes[1] = displayMetrics.heightPixels; // Get layout height parameter
        }
    }

    /**
     * Set the default image, resId from drawable. Is displayed when loading or loading failure.
     *
     * @param resId res id.
     */
    @SuppressLint("NewApi")
    public void setDefaultImage(int resId) {
        mDefaultDrawable = ResCompat.getDrawable(resId);
    }

    /**
     * Set the default image, resId from drawable. Is displayed when loading or loading failure.
     *
     * @param color color.
     */
    public void setDefaultImageColor(int color) {
        mDefaultDrawable = new ColorDrawable(color);
    }

    /**
     * Load image from local SDCard.
     *
     * @param imageView {@link ImageView}.
     * @param imagePath path.
     */
    public void loadImage(ImageView imageView, String imagePath) {
        loadImage(imageView, imagePath, 0, 0, null);
    }

    /**
     * Load image from local SDCard.
     *
     * @param imageView         {@link ImageView}.
     * @param imagePath         path.
     * @param imageLoadListener {@link ImageLoadListener}.
     */
    public void loadImage(ImageView imageView, String imagePath, ImageLoadListener imageLoadListener) {
        loadImage(imageView, imagePath, 0, 0, imageLoadListener);
    }

    /**
     * Load image from local SDCard.
     *
     * @param imageView {@link ImageView}.
     * @param imagePath path.
     * @param width     width.
     * @param height    height.
     */
    public void loadImage(ImageView imageView, String imagePath, int width, int height) {
        loadImage(imageView, imagePath, width, height, null);
    }

    /**
     * According to the specified width high loading pictures, wide high, the greater the picture clearer, more memory.
     *
     * @param imageView         {@link ImageView}.
     * @param imagePath         path from local SDCard.
     * @param width             target width.
     * @param height            target height.
     * @param imageLoadListener {@link ImageLoadListener}.
     */
    public void loadImage(ImageView imageView, String imagePath, int width, int height, ImageLoadListener
            imageLoadListener) {
        if (imageLoadListener == null)
            imageView.setTag(imagePath);
        Bitmap bitmap = getImageFromCache(imagePath + width + height);
        if (bitmap == null) {
            imageView.setImageDrawable(mDefaultDrawable);
            mExecutorService.execute(new TaskThread(imageView, imagePath, width, height, imageLoadListener));
        } else {
            ImgBeanHolder holder = new ImgBeanHolder();
            holder.imageView = imageView;
            holder.imagePath = imagePath;
            holder.bitmap = bitmap;
            holder.imageLoadListener = imageLoadListener;
            HandlerDelivery.getInstance().post(holder);
        }
    }

    private Bitmap getImageFromCache(String key) {
        return mLruCache.get(key);
    }

    private void addImageToCache(String key, Bitmap bitmap) {
        if (getImageFromCache(key) == null && bitmap != null)
            mLruCache.put(key, bitmap);
    }

    private class TaskThread implements Runnable {
        private ImageView mImageView;
        private String mImagePath;
        private int width;
        private int height;
        private ImageLoadListener imageLoadListener;

        TaskThread(ImageView imageView, String imagePath, int width, int height, ImageLoadListener imageLoadListener) {
            this.mImagePath = imagePath;
            this.mImageView = imageView;
            this.width = width;
            this.height = height;
            this.imageLoadListener = imageLoadListener;
        }

        @Override
        public void run() {
            if (TextUtils.isEmpty(mImagePath))
                Logger.e("The image path is null");
            else {
                Bitmap bitmap;
                if (width != 0 && height != 0)
                    bitmap = readImage(mImagePath, width, height);
                else {
                    int[] viewSizes = new int[2];
                    measureSize(mImageView, viewSizes);
                    bitmap = readImage(mImagePath, viewSizes[0], viewSizes[1]);
                }
                addImageToCache(mImagePath + width + height, bitmap);
                ImgBeanHolder holder = new ImgBeanHolder();
                holder.bitmap = getImageFromCache(mImagePath + width + height);
                holder.imageView = mImageView;
                holder.imagePath = mImagePath;
                holder.imageLoadListener = imageLoadListener;
                HandlerDelivery.getInstance().post(holder);
            }
        }
    }

    private class ImgBeanHolder implements Runnable {
        Bitmap bitmap;
        ImageView imageView;
        String imagePath;
        ImageLoadListener imageLoadListener;

        @Override
        public void run() {
            if (imagePath.equals(imageView.getTag())) {
                if (bitmap == null)
                    imageView.setImageDrawable(mDefaultDrawable);
                else
                    imageView.setImageBitmap(bitmap);
            }
            if (imageLoadListener != null) {
                if (bitmap != null)
                    imageLoadListener.onLoadSucceed(imageView, bitmap, imagePath);
                else
                    imageLoadListener.onLoadFailed(imageView, imagePath);
            }
        }
    }

    public interface ImageLoadListener {
        void onLoadSucceed(ImageView imageView, Bitmap bitmap, String imagePath);

        void onLoadFailed(ImageView imageView, String imagePath);
    }
}
