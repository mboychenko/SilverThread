package com.allat.mboychenko.silverthread.presentation.di

import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.data.storage.cache.provideExoPlayerCache
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.presenters.QuotesNotificationSettingsPresenter
import com.allat.mboychenko.silverthread.domain.interactor.QuotesDetailsStorage
import com.allat.mboychenko.silverthread.presentation.presenters.QuotesPresenter
import com.allat.mboychenko.silverthread.data.storage.StorageImplementation
import com.allat.mboychenko.silverthread.domain.helper.BooksHelper
import com.allat.mboychenko.silverthread.domain.interactor.*
import com.allat.mboychenko.silverthread.data.storage.Storage
import com.allat.mboychenko.silverthread.presentation.presenters.AllatPresenter
import com.allat.mboychenko.silverthread.presentation.presenters.BooksPresenter
import com.allat.mboychenko.silverthread.presentation.presenters.RadioPresenter
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val presentersModule = module {
    factory {
        BooksPresenter(androidContext(), get(), get())
    }

    factory {
        AllatPresenter(androidContext(), get())
    }

    factory {
        QuotesPresenter(androidContext(), get())
    }

    factory {
        QuotesNotificationSettingsPresenter(androidContext(), get())
    }

    factory {
        RadioPresenter(androidContext())
    }

    factory {
        BooksHelper(androidContext())
    }

}

val storageModule = module {

    single { StorageImplementation(androidContext()) }

    factory {
        get<StorageImplementation>() as Storage
    }

    factory {
        AllatTimeZoneInteractor(get()) as AllatTimeZoneStorage
    }

    factory {
        BooksLoaderDeatilsInteractor(get()) as BooksLoaderDetailsStorage
    }

    factory {
        QuotesInteractor(get()) as QuotesDetailsStorage
    }

}

val exoPlayerStorageModule = module {

    single { provideExoPlayerCache(androidContext()) }

}