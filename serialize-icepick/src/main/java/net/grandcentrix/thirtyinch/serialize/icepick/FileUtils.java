package net.grandcentrix.thirtyinch.serialize.icepick;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Created by rwondratschek on 12/14/16.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
/*package*/ final class FileUtils {

    private FileUtils() {
        // no op
    }

    public static byte[] readFile(File file) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            byte[] buffer = new byte[(int) file.length()];

            int read;
            int offset = 0;

            while ((read = fis.read(buffer, offset, buffer.length - offset)) >= 0 && offset < buffer.length) {
                offset += read;
            }

            if (offset != buffer.length) {
                return Arrays.copyOf(buffer, offset);
            } else {
                return buffer;
            }

        } finally {
            close(fis);
        }
    }

    public static void writeFile(File file, byte[] data) throws IOException {
        if (file == null || data == null) {
            throw new IllegalArgumentException();
        }

        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            throw new IOException("Could not load parent directory for " + file.getAbsolutePath());
        }

        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("Could not load file for " + file.getAbsolutePath());
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(data);

        } finally {
            close(fos);
        }
    }

    public static void delete(File file) throws IOException {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File file1 : files) {
                delete(file1);
            }
        }
        if (!file.delete()) {
            throw new IOException("could not delete file " + file);
        }
    }

    public static void close(Closeable closeable) {
        if (closeable != null) {
            if (closeable instanceof OutputStream) {
                try {
                    ((OutputStream) closeable).flush();
                } catch (IOException ignored) {
                }
            }

            if (closeable instanceof FileOutputStream) {
                try {
                    ((FileOutputStream) closeable).getFD().sync();
                } catch (IOException ignored) {
                }
            }

            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }
}
