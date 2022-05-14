package com.example.bookstore

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
class BookList : AppCompatActivity() {

    private lateinit var addBooks: FloatingActionButton
    private lateinit var rcvBook: RecyclerView

    lateinit var database: DatabaseReference;
    private var adapter: FirebaseRecyclerAdapter<Books, BookViewHolder>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_books_list)

        addBooks = findViewById(R.id.add_Books)

        database = Firebase.database.reference
        getAllBook()

        addBooks.setOnClickListener {
            startActivity(Intent(this, AddBook::class.java))
        }
    }

    private fun getAllBook() {
        rcvBook = findViewById(R.id.rvBook)


        val query = database.child("Books")
        val options = FirebaseRecyclerOptions.Builder<Books>().setQuery(query, Books::class.java).build()

        adapter = object : FirebaseRecyclerAdapter<Books, BookViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
                val view = LayoutInflater.from(this@BookList).inflate(R.layout.book_item, parent, false)
                return BookViewHolder(view)
            }

            override fun onBindViewHolder(holder: BookViewHolder, position: Int, model: Books) {
                holder.bookName.text = model.Book_Name
                holder.bookAuthor.text = model.Author_Name
                holder.launchYear.text = model.Launch_Year
                holder.bookReview.text = model.Book_Review
                holder.bookPrice.text = model.Book_Price
                Glide.with(this@BookList).load(model.Book_Image).into(holder.imageBook)
                holder.editBook.setOnClickListener {

                    intent(
                        model.id,
                        model.Book_Name,
                        model.Author_Name,
                        model.Launch_Year,
                        model.Book_Image,
                        model.Book_Review.toFloat(),
                        model.Book_Price
                    )
                }
            }
        }
        rcvBook.layoutManager = LinearLayoutManager(this)
        rcvBook.adapter = adapter
    }

    class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var imageBook = view.findViewById<ImageView>(R.id.image_book)!!
        var bookName = view.findViewById<TextView>(R.id.book_name)!!
        var bookAuthor = view.findViewById<TextView>(R.id.book_author)!!
        var launchYear = view.findViewById<TextView>(R.id.launch_year)!!
        var bookReview = view.findViewById<TextView>(R.id.book_review)!!
        var bookPrice = view.findViewById<TextView>(R.id.book_price)!!
        val editBook = view.findViewById<Button>(R.id.editBtn)!!
    }

    fun intent(
        id: String,
        Name_Book: String,
        Name_Author: String,
        Launch_Year: String,
        Image_Book: String,
        Book_Review: Float,
        Price_Book: String
    ) {
        val i = Intent(this, BookEdit::class.java)
        i.putExtra("id", id)
        i.putExtra("Name_Book", Name_Book)
        i.putExtra("Name_Author", Name_Author)
        i.putExtra("Launch_Year", Launch_Year)
        i.putExtra("Image_Book", Image_Book)
        i.putExtra("Book_Review", Book_Review)
        i.putExtra("Price_Book", Price_Book)
        startActivity(i)
    }

    override fun onStart() {
        super.onStart()
        adapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter!!.stopListening()
    }
}