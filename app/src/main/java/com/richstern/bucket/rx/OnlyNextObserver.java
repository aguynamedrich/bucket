package com.richstern.bucket.rx;

import android.util.Log;

import rx.Observer;
import rx.functions.Action1;

public class OnlyNextObserver<T> implements Observer<T> {

    private static final String TAG = OnlyNextObserver.class.getSimpleName();

    private final Observer<T> mActual;

    public OnlyNextObserver(Observer<T> actual) {
        mActual = actual;
    }

    @Override
    public void onCompleted() {
        Log.d(TAG, "onCompleted");
    }

    @Override
    public void onError(Throwable e) {
        Log.d(TAG, String.format("onError [%s] %s", e.getClass().getSimpleName(), e.getMessage()));
    }

    @Override
    public void onNext(T t) {
        mActual.onNext(t);
    }

    public static <T> OnlyNextObserver<T> forObserver(Observer<T> observer) {
        return new OnlyNextObserver<>(observer);
    }

    public static <T> OnlyNextObserver<T> forAction(final Action1<T> action) {
        return new OnlyNextObserver<>(new Observer<T>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(T t) {
                action.call(t);
            }
        });
    }
}