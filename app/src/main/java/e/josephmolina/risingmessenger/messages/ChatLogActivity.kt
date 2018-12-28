package e.josephmolina.risingmessenger.messages

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import e.josephmolina.risingmessenger.R
import e.josephmolina.risingmessenger.models.ChatMessage
import e.josephmolina.risingmessenger.models.User
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogActivity : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()
    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerview_chat_log.adapter = adapter

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.username

        listenForMessages()
        send_button_chat_log.setOnClickListener {
            performSendMessage()
        }
    }

    private fun listenForMessages() {
        val ref = FirebaseDatabase.getInstance().getReference("/messages")
        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)

                if (chatMessage != null) {

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatestMessagesActivity.currentUser ?: return

                        adapter.add(ChatFromItem(chatMessage.text, currentUser))
                    } else {
                        adapter.add(ChatToItem(chatMessage.text, toUser!!))
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })
    }

    private fun performSendMessage() {
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val text = editText_chat_log.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val toId = user.uid
        val timestamp = System.currentTimeMillis() / 1000
        if (fromId == null) return

        val reference = FirebaseDatabase.getInstance().getReference("/messages").push()

        val chatMessage = ChatMessage(reference.key!!, text, fromId, toId, timestamp)
        reference.setValue(chatMessage)
    }
}

class ChatFromItem(val text: String, val currentUser: User) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView_from_row.text = text
        val currentUserUri = currentUser.profileImageUrl
        val currentUserImageView = viewHolder.itemView.imageView_chat_from_row
        Picasso.get().load(currentUserUri).into(currentUserImageView)
    }

}

class ChatToItem(val text: String, val user: User) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.textView_to_row.text = text

        val photoUri = user.profileImageUrl
        val imageViewOfSender = viewHolder.itemView.imageView_chat_to_row
        Picasso.get().load(photoUri).into(imageViewOfSender)
    }

}