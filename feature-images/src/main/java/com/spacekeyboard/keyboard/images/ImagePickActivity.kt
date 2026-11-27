package com.spacekeyboard.keyboard.images

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

class ImagePickActivity : ComponentActivity() {
    private val picker = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        receive(uris)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) return
        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun receive(uris: List<Uri>) {
        if (uris.isEmpty()) {
            finish()
            return
        }
        val app = applicationContext
        ImageStore(app).addAsync(uris) { result ->
            ImageMessages.show(app, result)
            if (!isFinishing && !isDestroyed) finish()
        }
    }
}

object ImagePicker {
    fun launch(context: Context) {
        val intent = Intent(context, ImagePickActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

object ImageMessages {
    fun show(context: Context, result: ImportResult) {
        val text = when {
            result.full -> context.getString(R.string.images_full, ImagePolicy.LIMIT)
            result.added == 0 -> context.getString(R.string.images_failed)
            result.failed > 0 -> context.getString(R.string.images_added_some_failed, result.added, result.failed)
            else -> context.getString(R.string.images_added, result.added)
        }
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }
}
