package com.allat.mboychenko.silverthread.presentation.di

import androidx.work.WorkManager
import com.allat.mboychenko.silverthread.data.repositories.DiaryNotesRepository
import com.allat.mboychenko.silverthread.data.repositories.DiaryPracticesRepository
import com.allat.mboychenko.silverthread.data.storage.preferences.SensitiveStorage
import com.allat.mboychenko.silverthread.data.storage.preferences.SensitiveStorageImpl
import com.allat.mboychenko.silverthread.presentation.cache.provideExoPlayerCache
import com.allat.mboychenko.silverthread.presentation.presenters.QuotesNotificationSettingsPresenter
import com.allat.mboychenko.silverthread.domain.interactor.QuotesDetailsStorage
import com.allat.mboychenko.silverthread.data.storage.preferences.StorageImplementation
import com.allat.mboychenko.silverthread.domain.helper.BooksHelper
import com.allat.mboychenko.silverthread.domain.interactor.*
import com.allat.mboychenko.silverthread.data.storage.preferences.Storage
import com.allat.mboychenko.silverthread.data.storage.db.AllatDatabase
import com.allat.mboychenko.silverthread.domain.helper.BackupHelper
import com.allat.mboychenko.silverthread.presentation.presenters.*
import com.allat.mboychenko.silverthread.presentation.viewmodels.BackupViewModel
import com.allat.mboychenko.silverthread.presentation.viewmodels.DiaryNotesViewModel
import com.allat.mboychenko.silverthread.presentation.viewmodels.PracticesDiaryViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val viewModelsModule = module {
    viewModel { DiaryNotesViewModel(get()) }
    viewModel { PracticesDiaryViewModel(get()) }
    viewModel { BackupViewModel(androidContext(), get(), get(), get()) }
}

val androidModule = module {
    single { WorkManager.getInstance(androidContext()) }
}

val dbModule = module {
    single { AllatDatabase.getDatabase(get()) }
    single { get<AllatDatabase>().diaryNotesDao() }
    single { get<AllatDatabase>().practicesDiaryDao() }
}

val repositoryModule = module {
    single { DiaryNotesRepository(get()) }
    single { DiaryPracticesRepository(get()) }
}

val helpers = module {
    single { BackupHelper(get(), get(), get(), get()) }
}

val useCaseModule = module {
    factory { DiaryNotesUseCase(get()) }
    factory { DiaryPracticesUseCase(get()) }
}

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

    scope(named(QUOTES_FRAGMENT_SCOPE_NAME)) {
        scoped { QuotesPresenter(androidContext(), get()) }
    }

    factory {
        ParablesPresenter(androidContext(), get())
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
        PracticeTimerPresenter(androidContext(), get())
    }

    factory {
        BooksHelper(androidContext())
    }

}

val storageModule = module {

    single {
        StorageImplementation(
            androidContext()
        )
    }

    single {
        SensitiveStorageImpl(
            androidContext()
        ) as SensitiveStorage
    }

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

    factory {
        ParablesInteractor(get<StorageImplementation>()) as ParablesDetailsStorage
    }

    factory {
        PracticeStorageInteractor(get<StorageImplementation>()) as PracticeStorage
    }

}

val exoPlayerStorageModule = module {

    single { provideExoPlayerCache(androidContext()) }

}

const val QUOTES_FRAGMENT_SCOPE_NAME = "QUOTES_FRAGMENT_SCOPE"