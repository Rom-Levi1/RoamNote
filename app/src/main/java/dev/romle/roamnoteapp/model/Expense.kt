package dev.romle.roamnoteapp.model

import java.io.Serializable

data class Expense (
    val id: String,
    val name: String,
    val tags: List<ExpenseTag>,
    val cost: Double,
): Serializable {
    class Builder(
        var id: String = "",
        var name: String = "",
        var tags: List<ExpenseTag> = listOf(),
        var cost: Double = 0.0
    ){
        fun id(id: String) = apply { this.id = id }
        fun name(name: String) = apply { this.name = name }
        fun tags(tags: List<ExpenseTag>) = apply { this.tags = tags }
        fun cost(cost: Double) = apply{this.cost = cost}

        fun build() = Expense(
            id,name, tags, cost
        )
    }

    constructor() : this(
        id = "",
        name = "",
        tags = listOf(),
        cost = 0.0
    )
}
