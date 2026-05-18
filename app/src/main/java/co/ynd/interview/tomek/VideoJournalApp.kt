package co.ynd.interview.tomek

import android.app.Application
import co.ynd.interview.tomek.core.data.OrphanFileCleaner
import co.ynd.interview.tomek.core.data.di.dataModule
import co.ynd.interview.tomek.core.database.di.databaseModule
import co.ynd.interview.tomek.core.domain.di.domainModule
import co.ynd.interview.tomek.feature.camera.di.cameraModule
import co.ynd.interview.tomek.feature.feed.di.feedModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class VideoJournalApp : Application(), KoinComponent {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@VideoJournalApp)
            modules(
                databaseModule,
                dataModule,
                domainModule,
                feedModule,
                cameraModule
            )
        }
        val cleaner: OrphanFileCleaner by inject()
        appScope.launch { cleaner.clean() }
    }
}
