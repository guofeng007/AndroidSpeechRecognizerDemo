package com.example.androidspeechrecognizer

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.example.androidspeechrecognizer.service.ASRResultListener
import com.example.androidspeechrecognizer.service.RecognitionHelper
import com.example.androidspeechrecognizer.ui.theme.AndroidSpeechRecognizerTheme

class MainActivity : ComponentActivity(), ASRResultListener {
    public val recognitionHelper: RecognitionHelper by lazy {
        RecognitionHelper(this)
    }

    private var asrResult = mutableStateOf<String>("")

    private var updatingTextTimeDelayed = 0L
    private val mainHandler = Handler(Looper.getMainLooper())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidSpeechRecognizerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android",asrResult)
                }
            }
        }

        if (!recognitionHelper.prepareRecognition(this)) {
            Toast.makeText(this, "Recognition not available", Toast.LENGTH_SHORT).show()
            return
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        recognitionHelper.release()
    }

    override fun onPartialResult(result: String) {
        asrResult.value = result
    }

    override fun onFinalResult(result: String) {
        asrResult.value = result
    }
}

@Composable
fun Greeting(name: String, asrResult: MutableState<String>?, modifier: Modifier = Modifier) {
    val activity = LocalContext.current as MainActivity
    Column(modifier = modifier
        .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hello $name! ${asrResult?.value}",
            modifier = modifier
        )
        Row() {
           Button(onClick = {
               val hasAudioPermission = ActivityCompat.checkSelfPermission(
                   activity,
                   android.Manifest.permission.RECORD_AUDIO
               ) == PackageManager.PERMISSION_GRANTED
               if(hasAudioPermission){
                   activity.recognitionHelper.startRecognition()
               } else {
                   ActivityCompat.requestPermissions(
                       activity,
                       arrayOf(android.Manifest.permission.RECORD_AUDIO),
                       1
                   )
               }
           }) {
               Text(text = "StartRecognition")
           }
            Button(onClick = {
                activity.recognitionHelper.stopRecognition()
            }) {
                Text(text = "stopRecognition")
            }
        }


    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroidSpeechRecognizerTheme {
        Greeting("Android", null)
    }
}