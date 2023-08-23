package com.example.androidspeechrecognizer.service;

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognitionService
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log


interface ASRResultListener {
    fun onPartialResult(result: String)

    fun onFinalResult(result: String)
}


class RecognitionHelper(val context: Context) : RecognitionListener {
    private val TAG = "RecognitionHelper"

    private lateinit var mResultListener: ASRResultListener

    private lateinit var recognizer: SpeechRecognizer

    fun prepareRecognition(resultListener: ASRResultListener): Boolean {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.e("RecognitionHelper", "System has no recognition service yet.")
            return false
        }


        val serviceComponent = Settings.Secure.getString(
            context.contentResolver,
            "voice_recognition_service"
        )
        // 当前系统内置语音识别服务
        // 当前系统内置语音识别服务
        val component = ComponentName.unflattenFromString(serviceComponent)

        // 内置语音识别服务是否可用
        var isRecognizerServiceValid = false
        var currentRecognitionCmp: ComponentName? = null
        // 查找得到的 "可用的" 语音识别服务
        val intentServices = context.packageManager.queryIntentServices(
            Intent(RecognitionService.SERVICE_INTERFACE),
            PackageManager.MATCH_ALL
        )

        if (intentServices != null && intentServices.size != 0) {
            for (info in intentServices) {
                Log.d(
                    TAG, "\t" + info.loadLabel(context.packageManager) + ": "
                            + info.serviceInfo.packageName + "/" + info.serviceInfo.name
                );

                // 这里拿系统使用的语音识别服务和内置的语音识别比较，如果相同，OK我们直接直接使用
                // 如果相同就可以直接使用mSpeechRecognizer = 			   SpeechRecognizer.createSpeechRecognizer(context);来创建实例，因为内置的可以使用
                if (info.serviceInfo.packageName.equals(component?.packageName)) {
                    isRecognizerServiceValid = true
                    break;
                } else {
                    // 如果服务不同，说明 内置服务 和 系统使用 不是同一个，那么我们需要使用系统使用的
                    // 因为内置的系统不用，我们用了也没有用
                    currentRecognitionCmp = ComponentName (info.serviceInfo.packageName, info.serviceInfo.name)
                }

            }
        } else {
            // 这里既是查不到可用的语音识别服务，可以歇菜了
            Log.d(TAG, "No recognition services installed");
        }


        // 当前系统内置语音识别服务可用
        recognizer = if (isRecognizerServiceValid) {
            SpeechRecognizer.createSpeechRecognizer(context);
        } else {
            // 内置不可用，需要我们使用查找到的可用的
            SpeechRecognizer.createSpeechRecognizer(context, currentRecognitionCmp);
        }

        mResultListener = resultListener
        return true

    }

    fun startRecognition() {
        val intent = createRecognitionIntent()
        recognizer.setRecognitionListener(this)
        recognizer.startListening(intent)
    }

    fun stopRecognition() {
        recognizer.stopListening()
    }

    private fun createRecognitionIntent() = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
    }


    override fun onReadyForSpeech(params: Bundle?) {
        Log.d("SpeechRecognition", "onReadyForSpeech")
    }

    override fun onBeginningOfSpeech() {
        Log.d("SpeechRecognition", "onBeginningOfSpeech")
    }

    override fun onRmsChanged(rmsdB: Float) {
        Log.d("SpeechRecognition", "onRmsChanged")
    }

    override fun onBufferReceived(buffer: ByteArray?) {
        Log.d("SpeechRecognition", "onBufferReceived")
    }

    override fun onEndOfSpeech() {
        Log.d("SpeechRecognition", "onEndOfSpeech")
    }

    override fun onError(error: Int) {
        Log.d("SpeechRecognition", "onError$error")
    }

    override fun onResults(bundle: Bundle?) {
        bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let {
            Log.d(
                "RecognitionHelper", "onResults() with:$bundle" +
                        " results:$it"
            )

            mResultListener.onFinalResult(it[0])
        }

    }

    override fun onPartialResults(bundle: Bundle?) {
        bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let {
            Log.d(
                "RecognitionHelper", "onPartialResults() with:$bundle" +
                        " results:$it"
            )

            mResultListener.onPartialResult(it[0])
        }


    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        Log.d("SpeechRecognition", "onEvent")
    }

    fun release() {
        recognizer.destroy()

    }
}
