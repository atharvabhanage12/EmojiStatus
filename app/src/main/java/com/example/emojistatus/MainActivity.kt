package com.example.emojistatus

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.emojistatus.databinding.ActivityLoginBinding
import com.example.emojistatus.databinding.ActivityMainBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


data class User(
    val displayName :String =" ",
    val emojis :String =" ",
)

class UserViewHolder(itemView: View) :  RecyclerView.ViewHolder(itemView)

class MainActivity : AppCompatActivity() {

    private companion object{
        const val TAG="MainActivity"
    }



    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    val db= Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        auth = Firebase.auth




//        the below part is for making a recycle view using firebase
        val query :CollectionReference = db.collection("users")
        val options =  FirestoreRecyclerOptions.Builder<User>().setQuery(query,User::class.java)
            .setLifecycleOwner(this).build()
        val adapter = object : FirestoreRecyclerAdapter<User, UserViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {

                val view  =LayoutInflater.from(this@MainActivity).inflate(R.layout.unit_recycler,parent,false)

                return UserViewHolder(view!!)
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {

                val tvName :TextView? = holder.itemView.findViewById(R.id.textView2)
                val tvEmojis :TextView? = holder.itemView.findViewById(R.id.textView3)

//                if (tvName != null) {
                    tvName!!.text =model.displayName
//                }
//                if (tvEmojis != null) {
                    tvEmojis!!.text =model.emojis
//                }
            }

        }

        binding.rvUsers.adapter = adapter
        binding.rvUsers.layoutManager =LinearLayoutManager(this)


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
       if(item.itemId==R.id.miLogout){
           Log.i(TAG,"logout")
           auth.signOut()
           val logoutIntent = Intent(this,LoginActivity::class.java)
            logoutIntent.flags= Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

           startActivity(logoutIntent)

       }
        else if(item.itemId ==R.id.miEdit){
            Log.i(TAG,"jsjs")
           showAlertDialog()
       }
        return super.onOptionsItemSelected(item)
    }

    inner class EmojiFilter :InputFilter{
        override fun filter(
            p0: CharSequence?,
            p1: Int,
            p2: Int,
            p3: Spanned?,
            p4: Int,
            p5: Int
        ): CharSequence {
            if(p0 ==null || p0.isBlank()){
                return ""
            }
            val  validCharTypes = listOf(
                Character.NON_SPACING_MARK, // 6
                Character.SURROGATE, // 19
                Character.OTHER_SYMBOL // 28
            ).map { it.toInt() }.toSet()

            for (inputChar in p0){
                val type= Character.getType(inputChar)
                if(!validCharTypes.contains(type)){
                    Toast.makeText(this@MainActivity,"Only emojis to be enterd",Toast.LENGTH_SHORT).show()
                    return ""
                }
            }

            return p0
        }


    }

    private fun showAlertDialog() {

        val editText = EditText(this)
        //Todo 2 filter 1 emji and length
        val lengthFilter=  InputFilter.LengthFilter(9)
        val emojiFilter = EmojiFilter()
        editText.filters = arrayOf(lengthFilter,emojiFilter)

        val dialog =AlertDialog.Builder(this)
            .setTitle("Update your Emoji's")
            .setView(editText)
            .setNegativeButton("cancel",null)
            .setPositiveButton("OK",null)
            .show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            Log.i(TAG,"Clicked on postive button" )
            val emojisEntered = editText.text.toString()
            if(emojisEntered.isBlank()){
                    Toast.makeText(this,"thisis invaild",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val currentUser= auth.currentUser

            if(currentUser==null){
                Toast.makeText(this,"please login",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //update firestore with new emoji
            db.collection("users").document(currentUser.uid)
                .update("emojis",emojisEntered)
            dialog.dismiss()

        }
    }
}
