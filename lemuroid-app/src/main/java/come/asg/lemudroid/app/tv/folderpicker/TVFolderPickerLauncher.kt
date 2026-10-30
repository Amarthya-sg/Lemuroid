package come.asg.lemudroid.app.tv.folderpicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import come.asg.lemudroid.R
import come.asg.lemudroid.app.shared.ImmersiveActivity
import come.asg.lemudroid.app.shared.library.LibraryIndexScheduler
import come.asg.lemudroid.lib.preferences.SharedPreferencesHelper

class TVFolderPickerLauncher : ImmersiveActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            startActivityForResult(Intent(this, TVFolderPickerActivity::class.java), REQUEST_CODE_PICK_FOLDER)
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        resultData: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (requestCode == REQUEST_CODE_PICK_FOLDER && resultCode == Activity.RESULT_OK) {
            val sharedPreferences = SharedPreferencesHelper.getLegacySharedPreferences(this)
            val preferenceKey = getString(come.asg.lemudroid.lib.R.string.pref_key_legacy_external_folder)

            val currentValue: String? = sharedPreferences.getString(preferenceKey, null)
            val newValue = resultData?.extras?.getString(TVFolderPickerActivity.RESULT_DIRECTORY_PATH)

            if (newValue.toString() != currentValue) {
                sharedPreferences.edit().apply {
                    this.putString(preferenceKey, newValue.toString())
                    this.commit()
                }
            }

            startLibraryIndexWork()
        }
        finish()
    }

    private fun startLibraryIndexWork() {
        LibraryIndexScheduler.scheduleLibrarySync(applicationContext)
    }

    companion object {
        private const val REQUEST_CODE_PICK_FOLDER = 1

        fun pickFolder(context: Context) {
            context.startActivity(Intent(context, TVFolderPickerLauncher::class.java))
        }
    }
}
