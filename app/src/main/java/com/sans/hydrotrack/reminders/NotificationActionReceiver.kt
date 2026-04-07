package com.sans.hydrotrack.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.sans.hydrotrack.HydroTrackApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val amountMl = intent.getIntExtra(EXTRA_AMOUNT_ML, 0)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)

        if (amountMl > 0) {
            val pendingResult = goAsync()
            val app = context.applicationContext as HydroTrackApp
            val repository = app.container.hydrationRepository
            
            // Use a background dispatcher for DB operations
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    repository.addEntry(amountMl, "Notification")
                    if (notificationId != -1) {
                        NotificationManagerCompat.from(context).cancel(notificationId)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        const val EXTRA_AMOUNT_ML = "com.sans.hydrotrack.extra.AMOUNT_ML"
        const val EXTRA_NOTIFICATION_ID = "com.sans.hydrotrack.extra.NOTIFICATION_ID"
    }
}
