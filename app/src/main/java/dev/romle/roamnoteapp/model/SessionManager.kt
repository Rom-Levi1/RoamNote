package dev.romle.roamnoteapp.model

object SessionManager {
    var currentUser: User? = null

    fun updateTrip(updatedTrip: Trip) {
        val oldUser = currentUser ?: return
        val updatedTrips = oldUser.trips.map {
            if (it.id == updatedTrip.id) updatedTrip else it
        }.toMutableList()

        currentUser = oldUser.copy(trips = updatedTrips)
    }
}
