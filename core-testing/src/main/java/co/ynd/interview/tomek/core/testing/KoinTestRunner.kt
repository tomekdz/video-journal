package co.ynd.interview.tomek.core.testing

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

class KoinTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, Application::class.java.name, context)
    }
}
