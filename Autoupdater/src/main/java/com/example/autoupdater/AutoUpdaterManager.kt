import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.example.autoupdater.UpdateFeatures
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.util.InternalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File


class AutoUpdaterManager(private val context: Context) {
    @OptIn(InternalAPI::class)
    suspend fun checkForUpdate(
        JSONfileURL: String
    ): UpdateFeatures? {
        val client = HttpClient(CIO)
        try {

            val response = client.get(JSONfileURL)
            val body = response.body<String>()
            client.close()

            val versionresponse = Json.decodeFromString(JsonResponse.serializer(), body)
            val latestversion = versionresponse.latest_version
            val changelog = versionresponse.changelog
            val apk_url = versionresponse.apk_url
            Log.d("latestversion", "latest version: $latestversion")
            val currentversion = getCurrentAppVersionName()
            val isupdateavailable = compareVersions(currentversion, latestversion)
            if (isupdateavailable) {
                val updateFeatures = UpdateFeatures(changelog, apk_url, latestversion)
                return updateFeatures
            } else return null
        } catch (e: Exception) {
            return null
        }
    }


    fun getCurrentAppVersionName(): String {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return packageInfo.versionName
    }

    private fun compareVersions(currentVersion: String, latestVersion: String): Boolean {
        return if (currentVersion.equals(latestVersion, false)) {
            false
        } else true
    }

    @SuppressLint("Range")
    suspend fun downloadapk(
        context: Context,
        apkUrl: String,
        apkName: String,
        onProgressUpdate: (Int) -> Unit
    ) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        try {
            val request = DownloadManager.Request(Uri.parse(apkUrl))
            request.setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_DOWNLOADS,
                "$apkName.apk"
            )
            val downloadId = downloadManager.enqueue(request)

            GlobalScope.launch(Dispatchers.IO) {
                var downloading = true
                var lastProgress = 0

                while (downloading) {
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor: Cursor? = downloadManager.query(query)

                    cursor?.use {
                        if (it.moveToFirst()) {
                            val bytesDownloaded =
                                it.getInt(it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            val totalBytes =
                                it.getInt(it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                            if (totalBytes > 0) {
                                val progress = ((bytesDownloaded * 100L) / totalBytes).toInt()


                                if (progress != lastProgress) {
                                    lastProgress = progress
                                    withContext(Dispatchers.Main) {
                                        onProgressUpdate(progress)
                                    }
                                }
                            }


                            val status = it.getInt(it.getColumnIndex(DownloadManager.COLUMN_STATUS))
                            if (status == DownloadManager.STATUS_SUCCESSFUL || status == DownloadManager.STATUS_FAILED) {
                                downloading = false
                            }
                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                installApk(context, apkName)


                            }
                        }
                    }

                }
            }
        } catch (e: Exception) {
            onProgressUpdate(0)
        }


    }

    fun installApk(context: Context, apkName: String) {
        val file =
            File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "$apkName.apk")
        val uri: Uri =
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)


        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }


}