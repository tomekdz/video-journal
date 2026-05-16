package co.ynd.interview.tomek

import android.app.Application
import co.ynd.interview.tomek.core.data.di.dataModule
import co.ynd.interview.tomek.core.database.di.databaseModule
import co.ynd.interview.tomek.core.domain.di.domainModule
import co.ynd.interview.tomek.feature.camera.di.cameraModule
import co.ynd.interview.tomek.feature.feed.di.feedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class VideoJournalApp : Application() {
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
    }
}
