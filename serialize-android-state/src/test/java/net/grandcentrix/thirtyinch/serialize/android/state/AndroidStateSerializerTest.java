package net.grandcentrix.thirtyinch.serialize.android.state;

import android.content.Context;

import net.grandcentrix.thirtyinch.TiConfiguration;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AndroidStateSerializerTest {

    @Test
    public void testSerializeAndroidState() throws Exception {
        TestPresenter presenter = new TestPresenter(RuntimeEnvironment.application);
        final String id = presenter.getId();

        presenter.mValue = 5;
        presenter.persist();
        ((AndroidStatePresenterSerializer) presenter.getConfig().getPresenterSerializer()).waitForPendingTasks();

        AndroidStatePresenterSerializer serializer = new AndroidStatePresenterSerializer(RuntimeEnvironment.application);

        presenter = new TestPresenter(RuntimeEnvironment.application);
        assertEquals(0, presenter.mValue);

        presenter = serializer.deserialize(presenter, id);
        assertEquals(5, presenter.mValue);
    }

    public interface TestView extends TiView {
    }

    public static final class TestPresenter extends TiPresenter<TestView> {

        @com.evernote.android.state.State
        int mValue = 0;

        public TestPresenter(Context context) {
            super(new TiConfiguration.Builder().setPresenterSerializer(new AndroidStatePresenterSerializer(context)).build());
        }
    }
}
