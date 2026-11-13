package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.R
import com.example.model.StudySession
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

private const val TAG = "StudyNotification"
const val CHANNEL_ID = "study_reminders_channel"
const val CHANNEL_NAME = "Lembretes de Estudo / Study Reminders"

const val KEY_SESSION_TITLE = "key_session_title"
const val KEY_TECH_NAME = "key_tech_name"
const val KEY_SESSION_TIME = "key_session_time"
const val KEY_SESSION_TOPIC = "key_session_topic"

class StudyReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val title = inputData.getString(KEY_SESSION_TITLE) ?: "Hora do seu Estudo de Programação!"
            val techName = inputData.getString(KEY_TECH_NAME) ?: "Código"
            val time = inputData.getString(KEY_SESSION_TIME) ?: ""
            val topic = inputData.getString(KEY_SESSION_TOPIC) ?: "Conceitos práticos e exercícios no DevRoutine"

            createNotificationChannel(applicationContext)

            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val contentText = if (time.isNotBlank()) "Às $time • $topic" else topic

            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("⏰ $techName: $title")
                .setContentText(contentText)
                .setStyle(NotificationCompat.BigTextStyle().bigText("Seu estudo de $techName está agendado agora!\n$contentText\nAbra o DevRoutine para praticar e acumular pontos de XP!"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            val notificationManager = NotificationManagerCompat.from(applicationContext)
            notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), notification)

            Log.d(TAG, "Notification delivered successfully: $title")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error delivering study reminder notification", e)
            Result.failure()
        }
    }

    companion object {
        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                    description = "Notificações e lembretes push das rotinas de estudo agendadas no calendário"
                    enableVibration(true)
                }
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                notificationManager?.createNotificationChannel(channel)
            }
        }
    }
}

object NotificationScheduler {

    /**
     * Agenda um lembrete com antecedência para a sessão de estudo via WorkManager.
     */
    fun scheduleSessionReminder(context: Context, session: StudySession) {
        try {
            val workManager = WorkManager.getInstance(context)
            val sessionDateTime = parseDateTime(session.dateIso, session.timeFormatted)
            val now = LocalDateTime.now()

            // Dispara 15 minutos antes da sessão (ou imediatamente se faltar menos tempo mas ainda for hoje)
            val reminderTime = sessionDateTime.minusMinutes(15)
            val delaySeconds = if (reminderTime.isAfter(now)) {
                Duration.between(now, reminderTime).seconds
            } else if (sessionDateTime.isAfter(now)) {
                Duration.between(now, sessionDateTime).seconds
            } else {
                // Horário já passou
                return
            }

            val inputData = Data.Builder()
                .putString(KEY_SESSION_TITLE, session.titlePt)
                .putString(KEY_TECH_NAME, session.tech.displayName)
                .putString(KEY_SESSION_TIME, session.timeFormatted)
                .putString(KEY_SESSION_TOPIC, session.topicPt.ifBlank { session.notesPt })
                .build()

            val reminderRequest = OneTimeWorkRequestBuilder<StudyReminderWorker>()
                .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
                .setInputData(inputData)
                .addTag("session_${session.id}")
                .build()

            workManager.enqueueUniqueWork(
                "reminder_session_${session.id}_${session.dateIso}",
                ExistingWorkPolicy.REPLACE,
                reminderRequest
            )
            Log.d(TAG, "Scheduled reminder for session ${session.id} with delay: ${delaySeconds}s")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule session reminder", e)
        }
    }

    /**
     * Agenda verificação periódica diária para manter o usuário ativo e consistente.
     */
    fun scheduleDailyStudyCheck(context: Context) {
        try {
            val workManager = WorkManager.getInstance(context)
            val dailyRequest = PeriodicWorkRequestBuilder<StudyReminderWorker>(24, TimeUnit.HOURS)
                .setInputData(
                    Data.Builder()
                        .putString(KEY_SESSION_TITLE, "Consistência Diária de Código")
                        .putString(KEY_TECH_NAME, "DevRoutine AI")
                        .putString(KEY_SESSION_TOPIC, "Não perca sua sequência de estudos hoje! Resolva atividades e garanta seus pontos de XP.")
                        .build()
                )
                .build()

            workManager.enqueueUniquePeriodicWork(
                "daily_study_routine_check",
                ExistingPeriodicWorkPolicy.KEEP,
                dailyRequest
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule daily check", e)
        }
    }

    fun cancelDailyStudyCheck(context: Context) {
        try {
            WorkManager.getInstance(context).cancelUniqueWork("daily_study_routine_check")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel daily check", e)
        }
    }

    /**
     * Dispara notificação imediata de teste para o usuário confirmar o funcionamento.
     */
    fun sendImmediateTestNotification(context: Context, isPt: Boolean) {
        try {
            StudyReminderWorker.createNotificationChannel(context)
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                9999,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val title = if (isPt) "🔔 Lembrete de Estudo Ativo!" else "🔔 Study Reminder Active!"
            val body = if (isPt) "O sistema de notificações push locais com WorkManager está funcionando perfeitamente! Seus estudos agendados serão avisados aqui."
            else "Local push notification system with WorkManager is working perfectly! Your scheduled sessions will be reminded here."

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(1001, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending test notification", e)
        }
    }

    private fun parseDateTime(dateIso: String, timeFormatted: String): LocalDateTime {
        return try {
            val timeParts = timeFormatted.split(":")
            val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 19
            val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
            val date = java.time.LocalDate.parse(dateIso, DateTimeFormatter.ISO_LOCAL_DATE)
            date.atTime(hour, minute)
        } catch (e: Exception) {
            LocalDateTime.now().plusHours(1)
        }
    }
}
