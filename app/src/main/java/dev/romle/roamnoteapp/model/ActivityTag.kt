package dev.romle.roamnoteapp.model

enum class ActivityTag(val displayName: String){
    SPORT("sport"),
    CULTURE("Culture"),
    NATURE("Nature"),
    NIGHTLIFE("Nightlife"),
    HISTORIC("Historic"),
    OTHER("Other");

    override fun toString(): String = displayName

}
