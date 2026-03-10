package fr.isen.nicotom.netflix

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DataBaseHelper {

    private val database = FirebaseDatabase.getInstance()
    private val ref = database.getReference("categories")

    fun getCategories(handler: (List<Categorie>) -> Unit) {

        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val categories = mutableListOf<Categorie>()

                for (categorySnapshot in snapshot.children) {

                    val categorie =
                        categorySnapshot.getValue(Categorie::class.java)

                    if (categorie != null) {
                        categories.add(categorie)
                    }
                }

                handler(categories)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Database error", error.toException())
                handler(emptyList())
            }

        })
    }
}