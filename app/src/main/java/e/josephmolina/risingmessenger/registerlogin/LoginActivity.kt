package e.josephmolina.risingmessenger.registerlogin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import e.josephmolina.risingmessenger.R
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_button.setOnClickListener {
            val email = login_email_editText.text.toString()
            val password = login_password_editText.text.toString()
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("LOGIN", "signin with email successful")

                } else {
                    Toast.makeText(this, "Authentification failed", Toast.LENGTH_LONG).show()
                }
            }
        }
        back_to_register_textView.setOnClickListener {
            finish()
        }
    }
}
