package com.example.stressmeter.ui.home

import android.content.Context
import android.content.Intent
import android.content.res.TypedArray
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.stressmeter.ImageActivity
import com.example.stressmeter.R
import com.example.stressmeter.databinding.FragmentHomeBinding
import kotlinx.coroutines.*
import java.util.*


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var homeViewModel: HomeViewModel

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var active = false
    private lateinit var alertLooper: Job

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        // Adapted from https://stackoverflow.com/questions/6945678/storing-r-drawable-ids-in-xml-array
        val grid: GridView = binding.gridView
        // Update grid when grid value changes (when "More Images" button is pressed)
        homeViewModel.grid.observe(viewLifecycleOwner) {
            var images: ArrayList<ImageViewModel> = ArrayList()
            when (it) {
                0 -> images = getImages(resources.obtainTypedArray(R.array.grid1))
                1 -> images = getImages(resources.obtainTypedArray(R.array.grid2))
                2 -> images = getImages(resources.obtainTypedArray(R.array.grid3))
            }

            val gridAdapter = GridAdapter(requireContext(), images)
            grid.adapter = gridAdapter
        }

        // Open image when clicked in GridView
        grid.setOnItemClickListener { _, _, position, _ ->
            active = false
            val intent = Intent(context, ImageActivity::class.java)
            intent.putExtra(getString(R.string.grid_value_key), homeViewModel.grid.value)
            intent.putExtra(getString(R.string.image_value_key), position)
            startActivity(intent)
        }

        // Go to next grid of images
        val button: Button = binding.moreImagesButton
        button.setOnClickListener {
            homeViewModel.incrementGridValue()
        }

        return root
    }

    // Get images from XML resources file and put them into array for adapter to display in grid
    fun getImages(imageArray: TypedArray): ArrayList<ImageViewModel> {
        var images: ArrayList<ImageViewModel> = ArrayList()
        val stressValues = resources.obtainTypedArray(R.array.stress_values)

        for (i in 0 until imageArray.length()) {
            images.add(ImageViewModel(imageArray.getResourceId(i, -1), stressValues.getInt(i, -1)))
        }

        stressValues.recycle()
        return images
    }

    fun vibrate() {
        if (Build.VERSION.SDK_INT >= 31) {
            val vibratorManager = context?.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
        }
        else {
            val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(1000)
        }
    }

    fun ring() {
        // Adapted from https://stackoverflow.com/questions/2618182/how-to-play-ringtone-alarm-sound-in-android
        val ringtone: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val r = RingtoneManager.getRingtone(context, ringtone)
        r.play()
    }

    // When fragment is in sight, start alert
    override fun onResume() {
        super.onStart()
        active = true
        alertLooper = lifecycleScope.launch {
            while(active) {
                if (active) {
                    vibrate()
                    ring()
                }
                delay(2500)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        active = true
    }

    // When fragment is not in sight, stop alert
    override fun onPause() {
        super.onPause()
        active = false
        alertLooper.cancel()
    }

    override fun onStop() {
        super.onStop()
        active = false
        alertLooper.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        active = false
    }
}