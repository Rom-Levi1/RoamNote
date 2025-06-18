package dev.romle.roamnoteapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.romle.roamnoteapp.databinding.TripItemBinding
import dev.romle.roamnoteapp.model.Trip
import java.text.SimpleDateFormat
import java.util.*

class TripAdapter(private val trips: MutableList<Trip>) :
    RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    class TripViewHolder(val binding: TripItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val binding = TripItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TripViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = trips[position]
        val b = holder.binding

        b.tripItemTXTTripTitle.text = trip.name

        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val startDateStr = formatter.format(Date(trip.startDate))
        val endDateStr = formatter.format(Date(trip.lastDate))
        b.tripItemTXTDates.text = "$startDateStr - $endDateStr"

        if (!trip.photoUrl.isNullOrEmpty()) {
            ImageLoader.getInstance().loadImage(trip.photoUrl, b.tripItemIMG)
        } else {
            b.tripItemIMG.setImageResource(R.drawable.no_photo_img)
        }
    }

    override fun getItemCount(): Int = trips.size

    fun addTrip(trip: Trip) {
        trips.add(trip)
        notifyItemInserted(trips.size - 1)
    }
}
