package com.allat.mboychenko.silverthread.presentation.presenters

import androidx.annotation.CallSuper
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

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

    fun dispose() {
        subscriptions.dispose()
    }

    protected fun manageAddToSubscription(disposable: Disposable) {
        if (subscriptions.isDisposed) {
            subscriptions = CompositeDisposable()
        }

        subscriptions.add(disposable)
    }
}