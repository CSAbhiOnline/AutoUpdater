package com.example.mylibrary

import AutoUpdaterManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.ProgressBar
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.autoupdater.UpdateFeatures
import com.example.mylibrary.ui.theme.MyLibraryTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyLibraryTheme {

                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center){
                    /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (!this@MainActivity.packageManager.canRequestPackageInstalls()) {
                            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                            intent.data = Uri.parse("package:${this@MainActivity.packageName}")
                            this@MainActivity.startActivity(intent)
                        }
                    }*/
                    var update:UpdateFeatures? by remember {
                        mutableStateOf(null)
                    }
                    var progress by remember {
                        mutableStateOf(0)
                    }
                    val autoUpdaterManager=AutoUpdaterManager(LocalContext.current)
                    val coroutineScope= rememberCoroutineScope()
                    LaunchedEffect(Unit) {
                        coroutineScope.launch {
                            withContext(Dispatchers.IO){
                               update= autoUpdaterManager.checkForUpdate(JSONfileURL = "https://raw.githubusercontent.com/CSAbhiOnline/Autoupdater/refs/heads/master/Autoupdater/jsonstructure.json")

                            }
                        }
                    }
                    if(update==null){
                        Text("No updates available!")
                    }
                    else {
                        Text("Latest version: ${update!!.latestversion}")
                        Text("Changelog: ${update!!.changelog}")
                        Text("Apk URL: ${update!!.apk_url}")
                        Text(text = "Progress= $progress")


                        Button(onClick = {

                            coroutineScope.launch {
                                withContext(Dispatchers.IO){
                                    autoUpdaterManager.downloadapk(this@MainActivity, update!!.apk_url,"version/*:${update!!.latestversion}*/"){
                                        progress=it
                                    }
                                }
                            }
                        }) {
                            Text("Install app")
                        }
                        LinearProgressIndicator(progress = {
                            progress.toFloat()
                        },Modifier.fillMaxWidth())

                    }



                }



            }
        }
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyLibraryTheme {
        Greeting("Android")
    }
}
