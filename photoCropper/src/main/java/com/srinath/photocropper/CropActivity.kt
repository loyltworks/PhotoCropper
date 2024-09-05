package com.srinath.photocropper

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.srinath.photocropper.databinding.ActivityCropBinding
import java.io.File
import java.io.FileOutputStream

class CropActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCropBinding

    companion object {
        val ORIGINAL_IMAGE_URI = "ORIGINAL_IMAGE_URI"
        val CROPPED_IMAGE_URI = "CROPPED_IMAGE_URI"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCropBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cropImageView.setImageURI(Uri.parse(intent.getStringExtra(ORIGINAL_IMAGE_URI)))

        binding.cropPhotoButton.setOnClickListener {
            saveCroppedBitmap(binding.cropImageView.getCroppedBitmap())
        }

    }

    private fun saveCroppedBitmap(bitmap: Bitmap?) {
        // Save the cropped bitmap to a file or pass it back to the calling activity
        val file = File(externalCacheDir, "cropped_image.jpg")
        val outputStream = FileOutputStream(file)
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        val resultIntent = Intent()
        resultIntent.putExtra(CROPPED_IMAGE_URI, Uri.fromFile(file).toString())
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}