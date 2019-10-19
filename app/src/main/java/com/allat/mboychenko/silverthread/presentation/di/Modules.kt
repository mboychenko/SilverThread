package com.allat.mboychenko.silverthread.presentation.di

import com.allat.mboychenko.silverthread.data.storage.cache.provideExoPlayerCache
import com.allat.mboychenko.silverthread.presentation.presenters.QuotesNotificationSettingsPresenter
import com.allat.mboychenko.silverthread.domain.interactor.QuotesDetailsStorage
import com.allat.mboychenko.silverthread.data.storage.StorageImplementation
import com.allat.mboychenko.silverthread.domain.helper.BooksHelper
import com.allat.mboychenko.silverthread.domain.interactor.*
import com.allat.mboychenko.silverthread.data.storage.Storage
import com.allat.mboychenko.silverthread.presentation.presenters.*
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val presentersModule = module {
    factory {
        BooksPresenter(androidContext(), get(), get(), get())
    }

    factory {
        DownloadsPresenter(androidContext())
    }

    factory {
        AllatPresenter(androidContext(), get(), get())
    }

    factory {
        QuotesPresenter(androidContext(), get())
    }

    factory {
        PracticesPresenter(androidContext())
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
        AppSettingsStorageInteractor(get()) as AppSettingsStorage
    }

    factory {
        AllatNotificationsInteractor(get<StorageImplementation>()) as AllatNotificationsSettingsStorage
    }

    factory {
        BooksLoaderDeatilsInteractor(get()) as BooksLoaderDetailsStorage
    }

    factory {
        FileLoaderDetailsInteractor(get()) as FileLoadingDetailsStorage
    }

    factory {
        QuotesInteractor(get<StorageImplementation>()) as QuotesDetailsStorage
    }

}

val exoPlayerStorageModule = module {

    single { provideExoPlayerCache(androidContext()) }

}