package com.example.firebasestorage

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.firebasestorage.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE = 0

    var curFile: Uri? = null
    var imageRef = Firebase.storage.reference
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {
            imageView.setOnClickListener {
                Intent(Intent.ACTION_GET_CONTENT).also {
                    it.type = "image/*"
                    startActivityForResult(it, REQUEST_CODE)
                }
            }
            buttonUpload.setOnClickListener {
                uploadImageStorage(System.currentTimeMillis().toString() + ".jpg")
            }
            btnDownload.setOnClickListener {
                downloadImage("myImage")
            }

            btnDelete.setOnClickListener {
                deleteImage("1708337819285.jpg")
            }
        }
        rvImages()
    }

    fun rvImages() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val images = mutableListOf<String>()
            imageRef.child("images/").listAll().await()
                .items.forEach {
                    val url = it.downloadUrl.await()
                    images.add(url.toString())
                }

            withContext(Dispatchers.Main) {
                val adapter = AdapterRv(images)
                binding.recyclerView.adapter = adapter
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteImage(fileName: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            imageRef.child("images/$fileName").delete().await()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "File deleted", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun downloadImage(fileName: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val maxDownloadSize = 1024 * 1024 * 5L
            val bytes = imageRef.child("images/$fileName").getBytes(maxDownloadSize).await()
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            withContext(Dispatchers.Main) {
                binding.imageView.setImageBitmap(bmp)
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun uploadImageStorage(fileName: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            curFile?.let {
                imageRef.child("images/$fileName").putFile(it).addOnSuccessListener {
                    Toast.makeText(this@MainActivity, "File uploaded", Toast.LENGTH_SHORT).show()
                }

            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let {
                curFile = it
                binding.imageView.setImageURI(it)
            }
        }

    }
}