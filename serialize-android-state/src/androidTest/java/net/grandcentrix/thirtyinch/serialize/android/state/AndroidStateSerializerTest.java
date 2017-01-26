package net.grandcentrix.thirtyinch.serialize.android.state;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import net.grandcentrix.thirtyinch.TiConfiguration;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AndroidStateSerializerTest {

    @Test
    public void testSerializeAndroidState() throws Exception {
        TestPresenter presenter = new TestPresenter(InstrumentationRegistry.getContext());
        final String id = presenter.getId();

        presenter.mValue = 5;
        presenter.persist();
        ((AndroidStatePresenterSerializer) presenter.getConfig().getPresenterSerializer()).waitForPendingTasks();

        AndroidStatePresenterSerializer serializer = new AndroidStatePresenterSerializer(InstrumentationRegistry.getContext());

        presenter = new TestPresenter(InstrumentationRegistry.getContext());
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
