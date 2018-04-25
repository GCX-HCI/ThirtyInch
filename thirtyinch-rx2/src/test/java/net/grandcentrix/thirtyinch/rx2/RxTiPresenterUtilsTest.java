package net.grandcentrix.thirtyinch.rx2;

import static org.mockito.Mockito.*;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;
import org.junit.*;

public class RxTiPresenterUtilsTest {

    private TiPresenter mPresenter;

    private TiView mView;

    @Before
    public void setUp() throws Exception {
        mView = mock(TiView.class);
        mPresenter = new TiPresenter() {
        };
    }

    @After
    public void tearDown() throws Exception {
        mPresenter = null;
        mView = null;
    }

    @Test
    public void testDeliverLatestToView_Empty() throws Exception {
        mPresenter.create();

        TestObserver<Integer> testObserver = new TestObserver<>();
        Observable.<Integer>empty()
                .compose(RxTiPresenterUtils.<Integer>deliverLatestToView(mPresenter))
                .subscribe(testObserver);

        mPresenter.attachView(mView);

        testObserver.assertNotComplete();
        testObserver.assertNoErrors();
        testObserver.assertEmpty();
    }

    @Test
    public void testDeliverLatestToView_SingleItemViewComesAndGoes() throws Exception {
        mPresenter.create();

        PublishSubject<Integer> source = PublishSubject.create();
        TestObserver<Integer> testObserver = new TestObserver<>();

        source
                .compose(RxTiPresenterUtils.<Integer>deliverLatestToView(mPresenter))
                .subscribe(testObserver);

        source.onNext(1);
        source.onNext(2);
        mPresenter.attachView(mView);
        mPresenter.detachView();
        mPresenter.attachView(mView);
        mPresenter.detachView();
        mPresenter.attachView(mView);

        testObserver.assertNotComplete();
        testObserver.assertNoErrors();
        testObserver.assertValuesOnly(2, 2, 2);
    }

    @Test
    public void testDeliverLatestToView_ViewComesAndGoes() throws Exception {
        mPresenter.create();

        PublishSubject<Integer> source = PublishSubject.create();
        TestObserver<Integer> testObserver = new TestObserver<>();

        source
                .compose(RxTiPresenterUtils.<Integer>deliverLatestToView(mPresenter))
                .subscribe(testObserver);

        source.onNext(1);
        source.onNext(2);
        mPresenter.attachView(mView);
        source.onNext(3);
        mPresenter.detachView();
        source.onNext(4);
        source.onNext(5);
        mPresenter.attachView(mView);
        source.onNext(6);

        testObserver.assertNotComplete();
        testObserver.assertNoErrors();
        testObserver.assertValuesOnly(2, 3, 5, 6);
    }

    @Test
    public void testDeliverLatestToView_ViewNeverReady() throws Exception {
        mPresenter.create();

        TestObserver<Integer> testObserver = new TestObserver<>();
        Observable.just(1, 2, 3)
                .compose(RxTiPresenterUtils.<Integer>deliverLatestToView(mPresenter))
                .subscribe(testObserver);

        testObserver.assertNotComplete();
        testObserver.assertNoErrors();
        testObserver.assertEmpty();
    }

    @Test
    public void testDeliverLatestToView_ViewNotReady() throws Exception {
        mPresenter.create();

        TestObserver<Integer> testObserver = new TestObserver<>();
        Observable.just(1, 2, 3)
                .compose(RxTiPresenterUtils.<Integer>deliverLatestToView(mPresenter))
                .subscribe(testObserver);

        mPresenter.attachView(mView);

        testObserver.assertNotComplete();
        testObserver.assertNoErrors();
        testObserver.assertValuesOnly(3);
    }

    @Test
    public void testDeliverLatestToView_ViewReady() throws Exception {
        mPresenter.create();
        mPresenter.attachView(mView);

        TestObserver<Integer> testObserver = new TestObserver<>();
        Observable.just(1, 2, 3)
                .compose(RxTiPresenterUtils.<Integer>deliverLatestToView(mPresenter))
                .subscribe(testObserver);

        testObserver.assertNotComplete();
        testObserver.assertNoErrors();
        testObserver.assertValuesOnly(1, 2, 3);
    }

    @Test
    public void testIsViewReady_AttachView_ShouldCallValueFalseTrue() throws Exception {
        mPresenter.create();

        final TestObserver<Boolean> test = RxTiPresenterUtils.isViewReady(mPresenter).test();

        mPresenter.attachView(mView);
        test.assertValues(false, true);
    }

    @Test
    public void testIsViewReady_BeforeAttachView_ShouldCallValueFalse() throws Exception {
        mPresenter.create();

        final TestObserver<Boolean> test = RxTiPresenterUtils.isViewReady(mPresenter).test();

        test.assertValue(false);
    }

    @Test
    public void testIsViewReady_DisposeBeforeAttachView_ShouldRemoveCallback() throws Exception {
        mPresenter.create();

        final TestObserver<Boolean> test = RxTiPresenterUtils.isViewReady(mPresenter).test();

        test.assertValue(false);
        test.dispose();
        test.isDisposed();
        mPresenter.attachView(mView);
        test.assertValue(false);
    }

}
