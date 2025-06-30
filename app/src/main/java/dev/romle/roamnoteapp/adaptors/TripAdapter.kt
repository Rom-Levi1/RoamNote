package dev.romle.roamnoteapp.adaptors

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.romle.roamnoteapp.ImageLoader
import dev.romle.roamnoteapp.R
import dev.romle.roamnoteapp.databinding.TripItemBinding
import dev.romle.roamnoteapp.model.Trip
import java.text.SimpleDateFormat
import java.util.*

class TripAdapter(private val context: Context,private val trips: MutableList<Trip>) :
    RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    var onTripClicked: ((View, Trip) -> Unit)? = null

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

        Log.d("TripAdapter", "Binding trip: ${trip.name}, image: ${trip.photoUrl}")


        if (!trip.photoUrl.isNullOrEmpty()) {
            ImageLoader.getInstance().loadImage(trip.photoUrl, b.tripItemIMG)
        } else {
            b.tripItemIMG.setImageResource(R.drawable.no_photo_img)
        }

        holder.itemView.setOnClickListener { view ->
            onTripClicked?.invoke(view, trips[position])
        }

    }




    override fun getItemCount(): Int = trips.size

    fun removeTrip(trip: Trip) {
        val index = trips.indexOf(trip)
        if (index != -1) {
            trips.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun updateTrip(updatedTrip: Trip,originalName: String) {

        val index = trips.indexOfFirst { it.name == originalName }
        if (index != -1) {
            trips[index] = updatedTrip
            notifyItemChanged(index)
        }
    }

    fun addTrip(trip: Trip) {
        if (trips.any { it.name == trip.name }){
            return
        }

        trips.add(trip)
        notifyItemInserted(trips.size - 1)
    }
}
