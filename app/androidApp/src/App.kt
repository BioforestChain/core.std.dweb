package dweb.subsoil

import android.app.Application
import android.content.Context
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.dweb_browser.core.std.dns.DnsNMM
import org.dweb_browser.helper.androidAppContextDeferred

class App : Application() {
    companion object {
        lateinit var appContext: Context
        private val mutex = Mutex()
        private var dnsNMMPo = CompletableDeferred<DnsNMM>()
        suspend fun startMicroModuleProcess(): CompletableDeferred<DnsNMM> = mutex.withLock {
            MainScope().launch {
                if (dnsNMMPo.isCompleted) {
                    dnsNMMPo.await().bootstrap()
                } else {
                    try {
                        val dnsNMM = startApplication()
                        dnsNMMPo.complete(dnsNMM)
                    } catch (e: Throwable) {
                        dnsNMMPo.completeExceptionally(e)
                    }
                }
            }
            return dnsNMMPo
        }
    }

    override fun onCreate() {
        appContext = this
        androidAppContextDeferred.complete(this)
        super.onCreate()
    }

    override fun onTerminate() {
        super.onTerminate()
    }
}

