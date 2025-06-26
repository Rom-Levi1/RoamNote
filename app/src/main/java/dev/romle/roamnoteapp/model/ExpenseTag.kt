package dev.romle.roamnoteapp.model

enum class ExpenseTag(val displayName: String) {
    FOOD("Food & Drink"),
    TRANSPORT("Transport"),
    SHOPPING("Shopping"),
    ESSENTIALS("Essentials"),
    TICKETS("Tickets & Entry"),
    OTHER("Other");

    override fun toString(): String = displayName
}