package dev.romle.roamnoteapp.ui

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dev.romle.roamnoteapp.databinding.FragmentDailyLogBinding

class DailyLogFragment : Fragment() {

    private var _binding: FragmentDailyLogBinding? = null


    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentDailyLogBinding.inflate(inflater, container, false)
        val root: View = binding.root

        initViews()

        return root
    }

    private fun initViews() {
        binding.logIMGCalendar.setOnClickListener{
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(requireContext(), { _, selYear, selMonth, selDay ->
                val selectedDate = "${selDay}/${selMonth + 1}/$selYear"
                binding.dateSelectorDate.text = selectedDate
            }, year, month, day)

            datePicker.show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}