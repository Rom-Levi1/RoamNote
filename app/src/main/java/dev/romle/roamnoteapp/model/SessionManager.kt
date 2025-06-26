package dev.romle.roamnoteapp.model

object SessionManager {
    var currentUser: User? = null

    fun updateTrip(updatedTrip: Trip) {
        val trips = currentUser?.trips ?: return

        val newList = trips.map {
            if (it.id == updatedTrip.id) updatedTrip else it
        }

        trips.clear()
        trips.addAll(newList)
    }
}
