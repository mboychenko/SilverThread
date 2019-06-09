package com.allat.mboychenko.silverthread.presentation.presenters

import androidx.annotation.CallSuper
import io.reactivex.disposables.CompositeDisposable

abstract class BasePresenter<T> {

    protected var subscriptions = CompositeDisposable()

    protected var view: T? = null
        private set


    @CallSuper
    open fun attachView(view: T) {
        this.view = view
        subscriptions = CompositeDisposable()
    }

    @CallSuper
    open fun detachView() {
        subscriptions.dispose()
        this.view = null
    }
}