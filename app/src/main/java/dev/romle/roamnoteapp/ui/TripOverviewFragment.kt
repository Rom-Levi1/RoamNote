package dev.romle.roamnoteapp.ui

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import dev.romle.roamnoteapp.ImageLoader
import dev.romle.roamnoteapp.R
import dev.romle.roamnoteapp.databinding.FragmentTripOverviewBinding
import dev.romle.roamnoteapp.model.DayLog
import dev.romle.roamnoteapp.model.Trip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.toColorInt
import dev.romle.roamnoteapp.model.SessionManager


class TripOverviewFragment : Fragment() {

    private var _binding: FragmentTripOverviewBinding? = null

    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentTripOverviewBinding.inflate(layoutInflater)
        val root: View = binding.root



        return root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindTripData()

    }

    private fun bindTripData() {
        val tripId = TripOverviewFragmentArgs.fromBundle(requireArguments()).tripId
        val trip = SessionManager.currentUser?.trips?.find { it.id == tripId }

        trip?.let { trip ->
            if (!trip.photoUrl.isNullOrEmpty()) {
                ImageLoader.getInstance().loadImage(trip.photoUrl, binding.tripOverviewIMG)
            } else {
                binding.tripOverviewIMG.setImageResource(R.drawable.no_photo_img)
            }

            binding.tripOverviewTripName.text = trip.name

            val startDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(trip.startDate))
            val lastDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(trip.lastDate))
            binding.tripOverviewTripDates.text = "From $startDate to $lastDate"

            binding.tripOverviewTripCost.text = String.format("%.2f$", trip.cost)

            lifecycleScope.launch {
                val percentages = withContext(Dispatchers.Default) {
                    val costMap = calculatePieChartMap(trip)
                    val total = costMap.values.sum()
                    costMap.mapValues { (_, value) ->
                        if (total == 0.0) 0f else ((value / total) * 100).toFloat()
                    }
                }
                setupPieChart(binding.overviewPieChart, percentages)
            }

            displayDayLogPhotos(trip.dayLogs)
        }
    }

    private fun calculatePieChartMap(trip: Trip): Map<String, Double> {
        val tagSums = mutableMapOf<String, Double>()
        var hotelTotal = 0.0
        var activitiesTotal = 0.0

        for (log in trip.dayLogs.values) {

            log.hotel?.let {
                hotelTotal += it.cost
            }

            for (activity in log.activities){
                activitiesTotal += activity.value.cost
            }
            for (expense in log.expenses.values) {
                for (tag in expense.tags) {
                    tagSums[tag.name] = tagSums.getOrDefault(tag.name, 0.0) + expense.cost
                }
            }
        }

        val result = mutableMapOf<String, Double>()
        result["Hotels"] = hotelTotal
        result["Activities"] = activitiesTotal
        result.putAll(tagSums)

        return result
    }


    private fun displayDayLogPhotos(dayLogs: Map<String, DayLog>) {
        val gridLayout = binding.photosGrid
        gridLayout.removeAllViews()

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val totalPadding = resources.getDimensionPixelSize(R.dimen.login_linew_margin) * 2
        val spacingBetween = 16
        val imageWidth = (screenWidth - totalPadding - spacingBetween) / 2

        for (log in dayLogs.values) {
            val photoUrl = log.photoUrl
            if (!photoUrl.isNullOrBlank()) {
                val imageView = ImageView(requireContext()).apply {
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = imageWidth
                        height = ViewGroup.LayoutParams.WRAP_CONTENT
                        setMargins(8, 8, 8, 8)
                    }
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    adjustViewBounds = true
                    background = ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_box)
                    setPadding(16, 16, 16, 16)
                }

                ImageLoader.getInstance().loadImage(photoUrl, imageView)

                gridLayout.addView(imageView)
            }
        }
    }

    private fun setupPieChart(pieChart: PieChart, tagPercentages: Map<String, Float>) {
        val entries = tagPercentages
            .filterValues { it > 0f }
            .map { (tag, percent) -> PieEntry(percent, tag) }

        val tagColorMap = mapOf(
            "Hotels" to "#2196F3".toColorInt(),     // Blue
            "Activities" to "#F44336".toColorInt(),      // Red
            "FOOD" to "#4CAF50".toColorInt(),   // Green
            "TRANSPORT" to "#FF9800".toColorInt(),      // Orange
            "SHOPPING" to "#E91E63".toColorInt(),       // Pink
            "ESSENTIALS" to "#9C27B0".toColorInt(),     // Purple
            "TICKETS & Entry" to "#03A9F4".toColorInt(),// Light Blue
            "OTHER" to "#795548".toColorInt()           // Brown
        )

        val dataSet = PieDataSet(entries, "Expense Distribution").apply {
            setDrawValues(true)
            valueTextSize = 12f
            valueTextColor = Color.BLACK
            sliceSpace = 2f

            colors = entries.map { entry ->
                tagColorMap[entry.label] ?: Color.LTGRAY
            }
            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            valueLinePart1Length = 0.6f
            valueLinePart2Length = 0.4f
            valueLineWidth = 2f
            valueLineColor = Color.DKGRAY

            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return  String.format("%.1f%%", value)
                }
            }
        }

        pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            isDrawHoleEnabled = true
            setUsePercentValues(true)
            setDrawEntryLabels(true)
            setEntryLabelTextSize(12f)
            setEntryLabelColor(Color.BLACK)
            holeRadius = 50f
            transparentCircleRadius = 55f
            setExtraOffsets(10f, 10f, 10f, 10f)
            legend.isEnabled = true
            animateY(1000, Easing.EaseInOutQuad)
            invalidate()
        }
    }

    override fun onResume() {
        super.onResume()
        bindTripData()
    }
}

