import android.content.Context
import android.util.Log
import com.example.autoupdater.UpdateFeatures
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.util.InternalAPI
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

}