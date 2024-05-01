package com.example.taller3

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView.*
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ActiveUsersAdapter(pContext: Context, pActiveUsers: ArrayList<User>, pImgMaps : ArrayList <Bitmap>) : Adapter<ActiveUsersAdapter.MyViewHolder>()
{
    var context : Context
    var activeUsers : ArrayList<User>
    var profilePicBitmaps : ArrayList<Bitmap>

    init
    {
        this.context = pContext
        this.activeUsers = pActiveUsers
        this.profilePicBitmaps = pImgMaps
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder
    {
        val inflater : LayoutInflater = LayoutInflater.from(context)
        val view : View = inflater.inflate(R.layout.active_user_row, parent, false)

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int)
    {
        holder.nameTxtView.text = activeUsers[position].name.plus(" ").plus(activeUsers[position].lastName)
        holder.profileImg.setImageBitmap(profilePicBitmaps[position])
        holder.locationBtn.setOnClickListener {
            val trackUserIntent = Intent(context, TrackUserActivity::class.java)
            trackUserIntent.putExtra("trackedUid", activeUsers[position].uid)
            context.startActivity(trackUserIntent)
        }
    }

    override fun getItemCount(): Int
    {
        return activeUsers.size
    }


    class MyViewHolder(itemView: View) : ViewHolder(itemView)
    {
        val profileImg : ImageView = itemView.findViewById(R.id.cardProfilePicImg)
        val nameTxtView : TextView = itemView.findViewById(R.id.profileNameTxt)
        val locationBtn : FloatingActionButton = itemView.findViewById(R.id.userLocationBtn)
    }
}