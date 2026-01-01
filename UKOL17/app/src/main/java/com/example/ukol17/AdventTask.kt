package com.example.ukol17

data class AdventTask(
    val day: Int,
    val title: String,
    val description: String
)

object AdventTasks {
    val tasks = listOf(
        AdventTask(1, "Upeč cukroví", "Zkus upéct vánoční cukroví"),
        AdventTask(2, "Zavolej babičce", "Popovídej si o Vánocích"),
        AdventTask(3, "Ozdobit stromeček", "Vyzdobte vánoční stromeček"),
        AdventTask(4, "Napsat přání", "Napiš si vánoční přání"),
        AdventTask(5, "Zazpívat koledy", "Zazpívej si vánoční koledy"),
        AdventTask(6, "Nakoupit dárky", "Začni nakupovat dárky"),
        AdventTask(7, "Vánoční film", "Pusť si vánoční film"),
        AdventTask(8, "Udělat perníčky", "Upeč perníčky"),
        AdventTask(9, "Vyrobit dekoraci", "Vytvoř vánoční dekoraci"),
        AdventTask(10, "Číst příběh", "Přečti vánoční příběh"),
        AdventTask(11, "Napsat přáníčka", "Napiš vánoční přání rodině"),
        AdventTask(12, "Poslat pohlednici", "Pošli pohlednici"),
        AdventTask(13, "Dárek sousedům", "Obdaruj sousedy"),
        AdventTask(14, "Zabalit dárky", "Zabal vánoční dárky"),
        AdventTask(15, "Upéct vánočku", "Zkus upéct vánočku"),
        AdventTask(16, "Vyrobit betlém", "Vytvoř si betlém"),
        AdventTask(17, "Darovat hračky", "Daruj hračky potřebným"),
        AdventTask(18, "Adventní věnec", "Vytvoř adventní věnec"),
        AdventTask(19, "Vánoční čaj", "Připrav čaj s kořením"),
        AdventTask(20, "Vyfotit stromeček", "Vyfoť stromeček"),
        AdventTask(21, "Zpívat koledy", "Zpívej u stromečku"),
        AdventTask(22, "Připravit večeři", "Pomoz s večeří"),
        AdventTask(23, "Uklidit dům", "Pomoz uklidit před Štědrým dnem"),
        AdventTask(24, "Rozdat dárky", "Rozdej dárky rodině! 🎁")
    )

    fun getTaskByDay(day: Int): AdventTask? {
        return tasks.find { it.day == day }
    }
}