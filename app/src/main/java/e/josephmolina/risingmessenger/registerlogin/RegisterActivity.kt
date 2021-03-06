package e.josephmolina.risingmessenger.registerlogin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import e.josephmolina.risingmessenger.R
import e.josephmolina.risingmessenger.messages.LatestMessagesActivity
import e.josephmolina.risingmessenger.models.User
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    var selectedPhotoUri: Uri? = null
    val CHOOSE_PHOTO_INTENT_REQUEST_CODE = 0;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        register_button.setOnClickListener {
            performRegister()
        }

        already_have_account_editTextview.setOnClickListener {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }

        select_photo_button.setOnClickListener {
            val choosePhotoIntent = Intent(Intent.ACTION_PICK)
            choosePhotoIntent.type = "image/*"
            startActivityForResult(choosePhotoIntent, CHOOSE_PHOTO_INTENT_REQUEST_CODE)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            selectphoto_imageview.setImageBitmap(bitmap)
            select_photo_button.alpha = 0f // makes the imageview transparent.

//            val bitmapDrawable = BitmapDrawable(bitmap)
//            select_photo_button.setBackgroundDrawable(bitmapDrawable)

        }
    }

    private fun performRegister() {
        val email = email_editText.text.toString()
        val password = password_editText.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_LONG).show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                Log.d("Register", "Successfully created user ${it.result?.user?.uid}")

                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("images/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("Register", "Successfully uploaded image: ${it.metadata?.path}")
                ref.downloadUrl.addOnSuccessListener { listener ->
                    Log.d("Register", "File location: $it")
                    saveUserToFirebaseDatabase(listener.toString())
                }
            }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        Log.d("Register", "inside save user")
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user =
            User(uid, username_editText.text.toString(), profileImageUrl)

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("Register", "Finally we saved the user to firebase db")
                val goToLatestMessagesActivityIntent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(goToLatestMessagesActivityIntent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_SHORT).show()
                Log.d("Register", "${it.message}")

            }
    }
}
