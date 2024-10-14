import kotlinx.serialization.Serializable

@Serializable
data class JsonResponse(val latest_version:String,val changelog:String,val apk_url:String)
