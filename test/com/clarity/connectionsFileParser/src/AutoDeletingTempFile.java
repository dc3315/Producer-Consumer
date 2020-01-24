// Copyright 2020, David Cattle, All rights reserved.

package com.clarity.connectionsFileParser.src;

import java.io.File;
import java.io.IOException;

public class AutoDeletingTempFile implements AutoCloseable {

    private File file;

    public AutoDeletingTempFile(String prefix, String suffix,
                                String pathname) throws IOException {
        this.file = File.createTempFile(prefix, suffix, new File(pathname));
    }

    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }

    @Override
    public void close() {
        if (file != null && file.exists()) {
            file.delete();
        }
    }
}