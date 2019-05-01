package com.allat.mboychenko.silverthread.presentation.presenters

import androidx.annotation.CallSuper

abstract class BasePresenter<T> {

    protected var view: T? = null
        private set


    @CallSuper
    open fun attachView(view: T) {
        this.view = view
    }

    @CallSuper
    open fun detachView() {
        this.view = null
    }
}