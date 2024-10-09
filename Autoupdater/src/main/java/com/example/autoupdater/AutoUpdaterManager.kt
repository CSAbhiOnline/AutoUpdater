import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.example.autoupdater.UpdateFeatures
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.util.InternalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json


class AutoUpdaterManager(private val context:Context) {
    @OptIn(InternalAPI::class)
    suspend fun checkForUpdate(currentversion: String =getCurrentAppVersionName(), JSONfileURL: String):UpdateFeatures?{
        val client= HttpClient(CIO)
try {

    val response=client.get(JSONfileURL)
    val body=response.body<String>()
    client.close()

    val versionresponse= Json.decodeFromString(JsonResponse.serializer(),body)
    val latestversion=versionresponse.latest_version
    val changelog=versionresponse.changelog
    val apk_url=versionresponse.apk_url
    Log.d("latestversion","latest version: $latestversion")
    val currentversion=getCurrentAppVersionName()
    val isupdateavailable=compareVersions(currentversion,latestversion)
    if(isupdateavailable){
        val updateFeatures=UpdateFeatures(changelog,apk_url,latestversion)
        return updateFeatures
    }
    else return null
} catch (e: Exception){
    return null
}
    }


   fun getCurrentAppVersionName():String{
        val packageInfo=context.packageManager.getPackageInfo(context.packageName,0)
        return packageInfo.versionName
    }
    private fun compareVersions(currentVersion: String, latestVersion: String): Boolean {
       /* val currentParts = currentVersion.split(".").map { it.toInt() }
        val latestParts = latestVersion.split(".").map { it.toInt() }

        for (i in currentParts.indices) {
            if (i >= latestParts.size || currentParts[i] < latestParts[i]) return true // Update needed
            if (currentParts[i] > latestParts[i]) return false  // No update needed
        }
        return if(currentParts.size >= latestParts.size) false else true// No update needed if versions are the same*/
        return if(currentVersion.equals(latestVersion,false)){
            false
        } else true
    }
    @SuppressLint("Range")
    suspend fun downloadapk(context: Context,
                            apkUrl: String,
                            apkName: String,
                            onProgressUpdate: (Int) -> Unit){
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        // Create a request for the APK
        val request = DownloadManager.Request(Uri.parse(apkUrl))
        request.setTitle("Downloading APK")
        request.setDescription("Downloading $apkName...")
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "$apkName.apk")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        // Enqueue the request and get the download ID
        val downloadId = downloadManager.enqueue(request)

        // Monitor the download progress in a coroutine
        GlobalScope.launch(Dispatchers.IO) {
            var downloading = true
            var lastProgress = 0

            while (downloading) {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor: Cursor? = downloadManager.query(query)

                cursor?.use {
                    if (it.moveToFirst()) {
                        // Get bytes downloaded and total size
                        val bytesDownloaded = it.getInt(it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        val totalBytes = it.getInt(it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                        if (totalBytes > 0) {
                            // Calculate the progress as a percentage
                            val progress = ((bytesDownloaded * 100L) / totalBytes).toInt()

                            // Update progress if it has changed
                            if (progress != lastProgress) {
                                lastProgress = progress
                                withContext(Dispatchers.Main) {
                                    onProgressUpdate(progress)  // Update the UI with the new progress
                                }
                            }
                        }

                        // Check if download is completed
                        val status = it.getInt(it.getColumnIndex(DownloadManager.COLUMN_STATUS))
                        if (status == DownloadManager.STATUS_SUCCESSFUL || status == DownloadManager.STATUS_FAILED) {
                            downloading = false
                        }
                    }
                }

                delay(50)
            }
        }

    }
    

}