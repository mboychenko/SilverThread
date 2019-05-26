package com.allat.mboychenko.silverthread.presentation.helpers

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

fun runTaskOnComputation(task: () -> Unit): Disposable =
    Observable.fromCallable(task)
        .subscribeOn(Schedulers.computation())
        .subscribe()

fun runTaskOnComputation(task: () -> Unit, onComplete: () -> Unit): Disposable =
    Observable.fromCallable(task)
        .subscribeOn(Schedulers.computation())
        .subscribe({}, {}, onComplete)
