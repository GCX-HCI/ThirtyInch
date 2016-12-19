package net.grandcentrix.thirtyinch.serialize.icepick;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.serialize.TiPresenterSerializer;

import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import icepick.Icepick;

/**
 * @author rwondratschek
 */
public class IcepickPresenterSerializer implements TiPresenterSerializer {

    private static final String DELIMITER = "--";

    private final File mFilesDir;
    private final ExecutorService mExecutorService;

    public IcepickPresenterSerializer(@NonNull Context context) {
        this(new File(context.getCacheDir(), "TiPresenter"));
    }

    public IcepickPresenterSerializer(@NonNull File filesDir) {
        mFilesDir = filesDir;
        mExecutorService = Executors.newSingleThreadExecutor();
        mExecutorService.execute(mCleanupRunnable); // clean up once
    }

    @Override
    public void serialize(@NonNull final TiPresenter presenter, @NonNull final String presenterId) {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle bundle = new Bundle();
                    Icepick.saveInstanceState(presenter, bundle);
                    byte[] data = ParcelableUtil.marshal(bundle);

                    String filename = presenterId + DELIMITER + System.currentTimeMillis();

                    File file = new File(mFilesDir, filename);
                    FileUtils.writeFile(file, data);
                } catch (Exception ignored) {
                }
            }
        });
    }

    @NonNull
    @Override
    public <V extends TiView, P extends TiPresenter<V>> P deserialize(@NonNull P presenter, @NonNull final String presenterId) {
        File file = getFileFromId(presenterId);
        if (file == null) {
            return presenter;
        }

        try {
            byte[] data = FileUtils.readFile(file);
            Bundle bundle = ParcelableUtil.unmarshal(data, Bundle.CREATOR);
            bundle.setClassLoader(getClass().getClassLoader());
            Icepick.restoreInstanceState(presenter, bundle);

        } catch (Exception ignored) {
        }

        return presenter;
    }

    @Override
    public void cleanup(@NonNull final String presenterId) {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                File file = getFileFromId(presenterId);
                if (file != null) {
                    try {
                        FileUtils.delete(file);
                    } catch (Exception ignored) {
                    }
                }
            }
        });
    }

    @VisibleForTesting
    /*package*/ final void waitForPendingTasks() throws Exception {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                // no op
            }
        }).get(1, TimeUnit.MINUTES);
    }

    private File getFileFromId(@NonNull final String presenterId) {
        File[] files = mFilesDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile() && file.exists() && file.getName().startsWith(presenterId);
            }
        });
        return files == null || files.length != 1 ? null : files[0];
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final Runnable mCleanupRunnable = new Runnable() {
        @Override
        public void run() {
            // pause a little bit to not slow down app startup time
            SystemClock.sleep(TimeUnit.SECONDS.toMillis(10));

            File[] files = mFilesDir.listFiles();
            if (files == null || files.length == 0) {
                return;
            }

            final long currentTimeMillis = System.currentTimeMillis();

            for (File file : files) {
                try {
                    if (!file.exists() || !file.isFile()) {
                        continue;
                    }

                    String[] split = file.getName().split(DELIMITER);
                    if (split.length != 2) {
                        continue;
                    }

                    long timestamp = Long.parseLong(split[1]);
                    if (TimeUnit.DAYS.toMillis(14) + timestamp < currentTimeMillis) {
                        // older than 14 days, delete
                        FileUtils.delete(file);
                    }

                } catch (Exception ignored) {
                }
            }
        }
    };
}
