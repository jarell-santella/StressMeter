package com.example.stressmeter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.File

class ImageActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView

    private var gridValue = -1
    private var imageValue = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        imageView = findViewById(R.id.imageView)

        gridValue = intent.getIntExtra(getString(R.string.grid_value_key), -1)
        imageValue = intent.getIntExtra(getString(R.string.image_value_key), -1)

        // Find which grid and then get image by index
        var image: Int = -1
        when (gridValue) {
            0 -> image = resources.obtainTypedArray(R.array.grid1).getResourceId(imageValue, -1)
            1 -> image = resources.obtainTypedArray(R.array.grid2).getResourceId(imageValue, -1)
            2 -> image = resources.obtainTypedArray(R.array.grid3).getResourceId(imageValue, -1)
        }

        imageView.setImageResource(image)
    }

    fun onClickCancel(view: View) {
        finish()
    }

    fun onClickSubmit(view: View) {
        // Adapted from https://stackoverflow.com/questions/16516888/how-to-get-current-date-time-in-milliseconds-in-android
        val timestamp = System.currentTimeMillis()

        val stressValues = resources.obtainTypedArray(R.array.stress_values)
        val stressValue = stressValues.getInt(imageValue, -1)

        // Use IO thread to write file
        CoroutineScope(IO).launch {
            val csvFile = File(getExternalFilesDir(null), getString(R.string.csv_file))
            // If .csv file is not already there, create one with the appropriate columns (headers)
            if (!csvFile.exists() || csvFile.isDirectory) {
                csvFile.createNewFile()
                csvFile.appendText(getString(R.string.csv_headers))
            }

            csvFile.appendText("\n$timestamp,$stressValue")
        }

        stressValues.recycle()
        finishAffinity()
    }
}