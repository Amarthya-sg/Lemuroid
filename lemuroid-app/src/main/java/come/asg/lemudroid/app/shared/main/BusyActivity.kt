package come.asg.lemudroid.app.shared.main

import android.app.Activity

interface BusyActivity {
    fun activity(): Activity

    fun isBusy(): Boolean
}
