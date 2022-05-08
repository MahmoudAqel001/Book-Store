package com.example.bookstore

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.util.*

class BookEdit : AppCompatActivity() {

    private lateinit var editNameBook: EditText
    private lateinit var editNameAuthor: EditText
    private lateinit var editLaunchYear: EditText
    private lateinit var editPrice: EditText
    private lateinit var editRatingBar: RatingBar
    private lateinit var editBookCover: ImageView
    private lateinit var editBook: Button
    private lateinit var deleteBook: Button

    lateinit var database: DatabaseReference
    private var progressDialog: ProgressDialog? = null
    private val PICK_IMAGE_REQUEST = 111
    var imageURI: Uri? = null
    private var flo = 0f
    private var edo = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_edit)

        editNameBook = findViewById(R.id.editNameBook)
        editNameAuthor = findViewById(R.id.editNameAuthor)
        editLaunchYear = findViewById(R.id.editLaunchYear)
        editPrice = findViewById(R.id.editPrice)
        editBookCover = findViewById(R.id.editBookCover)
        editRatingBar = findViewById(R.id.editRatingBar)
        editBook = findViewById(R.id.editBook)
        deleteBook = findViewById(R.id.deleteBook)

        database = Firebase.database.reference
        val storageRef = Firebase.storage.reference
        val imageRef = storageRef.child("Image Book")

        editNameBook.setText(intent.getStringExtra("Name_Book").toString())
        editNameAuthor.setText(intent.getStringExtra("Name_Author").toString())
        editLaunchYear.setText(intent.getStringExtra("Launch_Year").toString())
        editPrice.setText(intent.getStringExtra("Price_Book").toString())
        editRatingBar.rating = intent.getFloatExtra("Book_Review", 0f)

        editLaunchYear.setOnClickListener {
            val currentDate = Calendar.getInstance()
            val day = currentDate.get(Calendar.DAY_OF_MONTH)
            val month = currentDate.get(Calendar.MONTH)
            val year = currentDate.get(Calendar.YEAR)
            val picker = DatePickerDialog(
                this, { _, y, m, d ->
                    editLaunchYear.setText("$y / ${m + 1} / $d")
                }, year, month, day
            )
            picker.show()
        }

        editBookCover.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_PICK
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
            editBookCover.setBackgroundResource(0)
            edo = 1
        }

        editRatingBar.setOnRatingBarChangeListener { _, fl, _ ->
            flo = fl
        }

        deleteBook.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete Book")
            builder.setMessage("Do you want to Delete the Book?")
            builder.setPositiveButton("Yes") { _, _ ->
                deleteBook()
                Toast.makeText(this, "Delete Successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, BookList::class.java))
            }
            builder.setNegativeButton("No") { d, _ ->
                d.dismiss()
            }
            builder.create().show()
        }

        editBook.setOnClickListener {
            if (editNameBook.text.isEmpty() || editNameAuthor.text.isEmpty() || editLaunchYear.text.isEmpty() || editPrice.text.isEmpty()) {
                Toast.makeText(this, "Fill Fields", Toast.LENGTH_SHORT).show()
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Edit Book")
                builder.setMessage("Do you want to Edit the Book?")
                builder.setPositiveButton("Yes") { _, _ ->
                    showDialog()
                    if (edo == 1) {
                        val bitmap = (editBookCover.drawable as BitmapDrawable).bitmap
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                        val data = baos.toByteArray()
                        val childRef =
                            imageRef.child(System.currentTimeMillis().toString() + ".png")
                        var uploadTask = childRef.putBytes(data)
                        uploadTask.addOnFailureListener { exception ->
                            hideDialog()
                        }.addOnSuccessListener {
                            childRef.downloadUrl.addOnSuccessListener { uri ->
                                editBook(edo, uri.toString())
                                hideDialog()
                                Toast.makeText(this, "Edit Successfully", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, BookList::class.java))
                            }
                        }
                    } else {
                        editBook(edo, "")
                        hideDialog()
                        Toast.makeText(this, "Edit Successfully", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, BookList::class.java))
                    }
                }
                builder.setNegativeButton("No") { d, _ ->
                    d.dismiss()
                }
                builder.create().show()
            }
        }
    }

    private fun editBook(edo: Int, Image_Book: String) {

        var values = hashMapOf<String, Any>()
        if (edo == 1) {
            values = hashMapOf(
                "Name_Book" to editNameBook.text.toString(),
                "Name_Author" to editNameAuthor.text.toString(),
                "Launch_Year" to editLaunchYear.text.toString(),
                "Price_Book" to editPrice.text.toString(),
                "Book_Review" to flo.toString(),
                "Image_Book" to Image_Book
            )
        } else {
            values = hashMapOf(
                "Name_Book" to editNameBook.text.toString(),
                "Name_Author" to editNameAuthor.text.toString(),
                "Launch_Year" to editLaunchYear.text.toString(),
                "Price_Book" to editPrice.text.toString(),
                "Book_Review" to flo.toString()
            )
        }

        val id = intent.getStringExtra("id").toString()
        database.child("Books").child(id).updateChildren(values)
    }

    private fun deleteBook() {
        val id = intent.getStringExtra("id").toString()
        database.child("Books").child(id).removeValue()
    }

    private fun showDialog() {
        progressDialog = ProgressDialog(this)
        progressDialog!!.setMessage("Uploading ...")
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()
    }

    private fun hideDialog() {
        if (progressDialog!!.isShowing)
            progressDialog!!.dismiss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            imageURI = data!!.data
            editBookCover.setImageURI(imageURI)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.mainmenu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.Back -> startActivity(Intent(this, BookList::class.java))
        }
        return super.onOptionsItemSelected(item)
    }
}
