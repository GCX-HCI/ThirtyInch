package net.grandcentrix.thirtyinch.test;

import static junit.framework.Assert.fail;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import android.support.annotation.NonNull;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.ViewAction;
import org.junit.*;

public class TiTestPresenterTest {

    interface MockTiView extends TiView {

        void helloWorld();

    }

    private TiPresenter<MockTiView> mMockTiPresenter;

    private MockTiView mMockTiView;

    @Before
    public void setUp() throws Exception {
        mMockTiPresenter = new TiPresenter<MockTiView>() {

            @Override
            protected void onAttachView(@NonNull MockTiView view) {
                super.onAttachView(view);
                sendToView(new ViewAction<MockTiView>() {
                    @Override
                    public void call(MockTiView tiView) {
                        tiView.helloWorld();
                    }
                });
            }
        };
        mMockTiPresenter.create();
        mMockTiView = mock(MockTiView.class);
    }

    @Test
    public void test_attachViewInUnitTest_ShouldThrow() throws Exception {
        try {
            mMockTiPresenter.attachView(mMockTiView);
            fail("No exception");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).contains("no ui thread executor available");
        }
    }

    @Test
    public void test_attachViewInUnitTestWithTiTestPresenter_ShouldNotThrow() throws Exception {
        final TiTestPresenter<MockTiView> testPresenter = new TiTestPresenter<>(mMockTiPresenter);
        testPresenter.attachView(mMockTiView);

        verify(mMockTiView).helloWorld();
    }
}