package com.example.stressmeter.ui.results

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.stressmeter.R
import com.example.stressmeter.databinding.FragmentResultsBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import lecho.lib.hellocharts.gesture.ContainerScrollType
import lecho.lib.hellocharts.gesture.ZoomType
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.view.LineChartView
import java.io.File

class ResultsFragment : Fragment() {
    private lateinit var tableLayout: TableLayout
    private lateinit var lineChartView: LineChartView
    private lateinit var data: LineChartData
    private lateinit var dataPoints: ArrayList<PointValue>
    private lateinit var csvFile: File

    private var _binding: FragmentResultsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val galleryViewModel =
            ViewModelProvider(this).get(ResultsViewModel::class.java)

        _binding = FragmentResultsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textResults
        galleryViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        tableLayout = binding.tableLayout

        // Build UI using main UI thread
        CoroutineScope(Main).launch {
            //Adapted from https://github.com/lecho/hellocharts-android
            lineChartView = binding.lineChartView
            lineChartView.isInteractive = true
            lineChartView.zoomType = ZoomType.HORIZONTAL_AND_VERTICAL
            lineChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL)

            // Set up chart properties
            data = LineChartData()
            val x = Axis()
            val y = Axis()
            x.name = getString(R.string.x_axis)
            y.name = getString(R.string.y_axis)
            data.axisXBottom = x
            data.axisYLeft = y
        }

        // Build data using IO thread
        CoroutineScope(IO).launch {
            dataPoints = ArrayList()
            csvFile = File(requireContext().getExternalFilesDir(null), getString(R.string.csv_file))
            if (csvFile.exists() && !csvFile.isDirectory) {
                loadData()
            }
        }

        return root
    }

    private suspend fun loadData() {
        withContext(IO) {
            var job1 = launch {
                val lines = csvFile.readLines()
                for (i in 1 until lines.size) {
                    val values = lines[i].split(",").toTypedArray()
                    addTableRow(tableLayout, values[0], values[1])
                    dataPoints.add(PointValue(i.toFloat(), values[1].toFloat()))
                }
            }
            // Make sure job2 only executes after job1
            job1.join()
            var job2 = launch {
                // Create line data for chart
                val line = Line(dataPoints).setColor(Color.BLUE).setCubic(true)
                val lines: ArrayList<Line> = ArrayList()
                lines.add(line)
                data.lines = lines
                lineChartView.lineChartData = data
            }
            var job3 = launch {
                // Fix bug where y-axis is cut off
                // Adapted from https://github.com/lecho/hellocharts-android/issues/243)
                val v = Viewport(lineChartView.maximumViewport)
                v.top = v.top
                lineChartView.maximumViewport = v
                lineChartView.currentViewport = v
            }
        }
    }

    // Adds 2-column table rows to a TableLayout
    private suspend fun addTableRow(tableLayout: TableLayout, leftValue: String, rightValue: String) {
        var tableRow: TableRow = TableRow(context)
        var col1: TextView = TextView(context)
        var col2: TextView = TextView(context)
        withContext(Main) {
            var job1 = launch {
                col1.text = leftValue
                col2.text = rightValue
            }
            // Add properties to TextView
            col1.setPadding(10, 10, 10, 10)
            col2.setPadding(10, 10, 10, 10)

            val layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)

            col1.layoutParams = layoutParams
            col2.layoutParams = layoutParams

            col1.setBackgroundResource(R.drawable.cell_border)
            col2.setBackgroundResource(R.drawable.cell_border)

            tableRow.addView(col1)
            tableRow.addView(col2)

            tableLayout.addView(tableRow)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}