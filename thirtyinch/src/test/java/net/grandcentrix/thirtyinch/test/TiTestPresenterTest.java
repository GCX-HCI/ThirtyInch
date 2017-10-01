package net.grandcentrix.thirtyinch.test;

import static junit.framework.Assert.fail;
import static org.assertj.core.api.Assertions.*;

import android.support.annotation.NonNull;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.ViewAction;
import org.junit.*;

public class TiTestPresenterTest {

    interface MockTiView extends TiView {

        void helloWorld();

    }

    private TiPresenter<MockTiView> mockTiPresenter;

    private MockTiView mockTiView;

    @Before
    public void setUp() throws Exception {
        mockTiPresenter = new TiPresenter<MockTiView>() {

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
        mockTiPresenter.create();
        mockTiView = new MockTiView() {
            @Override
            public void helloWorld() {
                System.out.println("Hello World");
            }
        };
    }

    @Test
    public void test_attachViewInUnitTest_ShouldThrow() throws Exception {
        try {
            mockTiPresenter.attachView(mockTiView);
            fail("No exception");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).contains("no ui thread executor available");
        }
    }

    @Test
    public void test_attachViewInUnitTestWithTiTestPresenter_ShouldNotThrow() throws Exception {
        final TiTestPresenter<MockTiView> testPresenter = new TiTestPresenter<>(mockTiPresenter);
        try {
            testPresenter.attachView(mockTiView);
        } catch (IllegalStateException e) {
            fail("We throw an exception but don't expect one");
        }
    }
}