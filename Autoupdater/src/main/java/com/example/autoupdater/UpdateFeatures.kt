package com.example.autoupdater

import kotlinx.serialization.Serializable

@Serializable
data class UpdateFeatures(val changelog:String, val apk_url:String,val latestversion:String)
