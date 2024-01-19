package com.darkness.sparkwomen;

import android.content.Context;

import java.io.File;

public class DirectoryUtils {

    public static void createDirectoryInInternalStorage(Context context, String directoryName) {
        // Get the internal storage directory for Pictures
        File picturesDirectory = new File(context.getExternalFilesDir(null), "Pictures");

        // Create a File object representing the desired directory within Pictures
        File directory = new File(picturesDirectory, directoryName);

        // Check if the directory exists
        if (!directory.exists()) {
            // If the directory doesn't exist, create it
            directory.mkdirs();
        }
    }
}
