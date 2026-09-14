package com.gamedeck.app.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.app.Activity

class ScreenRecorderService : Service() {

    private var recorder: MediaRecorder? = null
    private var projection: MediaProjection? = null
    private var vdisplay: VirtualDisplay? = null

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel("rec", "Screen Record", NotificationManager.IMPORTANCE_LOW)
        )
        val stopIntent = Intent(this, ScreenRecorderService::class.java).setAction("STOP")
        val stopPi = android.app.PendingIntent.getService(
            this, 0, stopIntent,
            android.app.PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(this, "rec")
            .setContentTitle("GameDeck Recorder")
            .setContentText("Recording gameplay...")
            .setSmallIcon(android.R.drawable.presence_video_online)
            .addAction(0, "STOP", stopPi)
            .build()

        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(2, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        } else {
            startForeground(2, notif)
        }

        if (intent?.action == "STOP") {
            stopRecording()
            return START_NOT_STICKY
        }

        val code = intent?.getIntExtra("code", Activity.RESULT_CANCELED) ?: Activity.RESULT_CANCELED
        val data: Intent? = if (Build.VERSION.SDK_INT >= 33) {
            intent?.getParcelableExtra("data", Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra("data")
        }
        if (code != Activity.RESULT_OK || data == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        startRecording(code, data)
        return START_STICKY
    }

    private fun startRecording(code: Int, data: Intent) {
        val mpm = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val mp = mpm.getMediaProjection(code, data)
        projection = mp

        if (Build.VERSION.SDK_INT >= 34) {
            mp.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() { stopRecording() }
            }, Handler(Looper.getMainLooper()))
        }

        val dm = resources.displayMetrics
        val w = dm.widthPixels
        val h = dm.heightPixels
        val dpi = dm.densityDpi

        val dir = getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: filesDir
        dir.mkdirs()
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(dir, "gamedeck_$stamp.mp4")

        val hasAudio = ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        val mr = MediaRecorder()
        if (hasAudio) mr.setAudioSource(MediaRecorder.AudioSource.MIC)
        mr.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mr.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mr.setOutputFile(file.absolutePath)
        mr.setVideoEncodingBitRate(16_000_000)
        mr.setVideoFrameRate(60)
        mr.setVideoSize(w, h)
        if (hasAudio) {
            mr.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mr.setAudioChannels(1)
            mr.setAudioSamplingRate(44100)
        }
        mr.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        mr.prepare()

        vdisplay = mp.createVirtualDisplay(
            "gamedeck", w, h, dpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mr.surface, null, null
        )
        mr.start()
        recorder = mr
    }

    private fun stopRecording() {
        try { recorder?.stop() } catch (e: Exception) {}
        try { recorder?.release() } catch (e: Exception) {}
        recorder = null
        try { vdisplay?.release() } catch (e: Exception) {}
        vdisplay = null
        try { projection?.stop() } catch (e: Exception) {}
        projection = null
        try { stopForeground(STOP_FOREGROUND_REMOVE) } catch (e: Exception) {}
        stopSelf()
    }
}
