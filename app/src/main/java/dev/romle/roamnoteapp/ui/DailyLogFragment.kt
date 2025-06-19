package dev.romle.roamnoteapp.ui

import android.app.DatePickerDialog
import android.graphics.Typeface
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.textview.MaterialTextView
import dev.romle.roamnoteapp.R
import dev.romle.roamnoteapp.databinding.FragmentDailyLogBinding
import dev.romle.roamnoteapp.model.Expense
import dev.romle.roamnoteapp.model.Hotel
import dev.romle.roamnoteapp.ui.dialogfragments.AddExpenseFragment
import dev.romle.roamnoteapp.ui.dialogfragments.AddHotelFragment

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

        return root
    }

    private fun initViews() {

        binding.logIMGCalendar.setOnClickListener {
            hotelDateHandle { day, month, year ->
                val selectedDate = "$day/$month/$year"
                binding.dateSelectorDate.text = selectedDate
            }
        }

        hotelDialogHandle { hotel ->
            binding.hotelSelectorName.text = hotel.name
        }

        expenseDialogHandle { expense ->

            val container = binding.expenseListContainer

            val expenseView = MaterialTextView(requireContext()).apply {
                text = "${expense.name}: ₪${expense.cost}"
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.log_button_size))
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 8, 0, 8)
            }

            container.addView(expenseView)
        }

    }

    fun hotelDateHandle(onDatePicked: (day: Int, month: Int, year: Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(requireContext(), { _, selYear, selMonth, selDay ->
            onDatePicked(selDay, selMonth + 1, selYear)
        }, year, month, day)

        datePicker.show()
    }

    fun hotelDialogHandle(onHotelPicked: (Hotel) -> Unit) {
        parentFragmentManager.setFragmentResultListener("HOTEL_RESULT", viewLifecycleOwner) { _, result ->
            val hotel = result.getSerializable("HOTEL") as Hotel
            Log.d("HotelReceived", hotel.toString())
            onHotelPicked(hotel)
        }

        binding.logAddHotelSelectorText.setOnClickListener {
            val dialog = AddHotelFragment()
            dialog.show(parentFragmentManager, "AddHotelDialog")
        }
    }

    fun expenseDialogHandle(onExpensePicked: (Expense) -> Unit){
        parentFragmentManager.setFragmentResultListener("EXPENSE_RESULT",viewLifecycleOwner){_,result ->
            val expense = result.getSerializable("EXPENSE") as Expense
            Log.d("ExpenseReceived", expense.toString())
            onExpensePicked(expense)
        }

        binding.logExpenseBTNAdd.setOnClickListener{
            val dialog = AddExpenseFragment()
            dialog.show(parentFragmentManager,"AddExpenseDialog")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}