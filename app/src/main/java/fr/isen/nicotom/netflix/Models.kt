package fr.isen.nicotom.netflix

import com.google.gson.annotations.SerializedName

data class DisneyDatabase(

    @SerializedName("categories")
    val categories: List<Categorie> = emptyList()

)

data class Categorie(

    @SerializedName("categorie")
    val categorie: String = "",

    @SerializedName("franchises")
    val franchises: List<Franchise> = emptyList()

)

data class Franchise(

    @SerializedName("nom")
    val nom: String = "",

    @SerializedName("sous_sagas")
    val sousSagas: List<SousSaga>? = null,

    @SerializedName("films")
    val films: List<Film>? = null

) {

    fun tousLesFilms(): List<Film> {

        return sousSagas?.flatMap { it.films }
            ?: films
            ?: emptyList()

    }

}


data class SousSaga(

    @SerializedName("nom")
    val nom: String = "",

    @SerializedName("films")
    val films: List<Film> = emptyList()

)


data class Film(

    @SerializedName("numero")
    val numero: Int = 0,

    @SerializedName("titre")
    val titre: String = "",

    @SerializedName("genre")
    val genre: String? = null,

    @SerializedName("annee")
    val annee: Int? = null

)