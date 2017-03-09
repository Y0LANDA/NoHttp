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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>
 * A default implementation of Binary.
 * All the methods are called in Son thread.
 * </p>
 * Created in Oct 17, 2015 12:40:54 PM.
 *
 * @author Yan Zhenjie.
 */
public class FileBinary extends BasicBinary {

    private File mFile;

    /**
     * File binary.
     *
     * @param file a file.
     */
    public FileBinary(File file) {
        this(file, file.getName(), null);
    }

    /**
     * File binary.
     *
     * @param file     a file.
     * @param fileName file name.
     */
    public FileBinary(File file, String fileName) {
        this(file, fileName, null);
    }

    /**
     * File binary.
     *
     * @param file     a file.
     * @param fileName file name.
     * @param mimeType content type.
     */
    public FileBinary(File file, String fileName, String mimeType) {
        super(fileName, mimeType);
        if (file == null)
            throw new IllegalArgumentException("File is null: " + fileName);
        if (!file.exists())
            throw new IllegalArgumentException("File not found: " + fileName);
        mFile = file;
    }

    @Override
    public long getBinaryLength() {
        return mFile.length();
    }

    @Override
    protected InputStream getInputStream() throws IOException {
        return new FileInputStream(mFile);
    }
}
