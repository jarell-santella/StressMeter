package com.example.stressmeter.ui.home

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.ImageView

class GridAdapter(private val activity: Context, private val images: ArrayList<ImageViewModel>) : BaseAdapter() {
    // Adapted from https://www.w3adda.com/kotlin-android-tutorial/kotlin-gridview

    private lateinit var imageView: ImageView

    override fun getCount(): Int {
        return images.size
    }

    override fun getItem(index: Int): Any {
        return images[index]
    }

    override fun getItemId(index: Int): Long {
        // Returns drawable ID in resources integer array
        return images[index].image.toLong()
    }

    override fun getView(index: Int, view: View?, viewGroup: ViewGroup?): View {
        imageView = ImageView(activity)
        imageView.setImageResource(images[index].image)

        // Adapted from https://developer.android.com/reference/android/widget/ImageView.ScaleType
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP

        // Adapted from https://stackoverflow.com/questions/22440238/android-gridview-making-items-fit-on-every-screen-size
        // 4 columns, fill entire screen with square images therefore height and width of images must be width of screen
        val imageDimensions = activity.resources.displayMetrics.widthPixels / 4
        imageView.layoutParams = AbsListView.LayoutParams(imageDimensions, imageDimensions)

        return imageView
    }

}
