package com.example.restaurantapp.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.restaurantapp.R
import com.example.restaurantapp.Structure.property
import com.example.restaurantapp.services.DataServices

class PropertyAdapter(val context : Context, val properties : ArrayList<property>, val itemClicked : (ArrayList<String>) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if(holder.itemViewType == 1){
            (holder as ViewHolder1).bindProperty(position)
        }else{
            (holder as ViewHolder2).bindProperty()
        }
    }

    override fun getItemCount(): Int {
        return properties.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if(position < properties.size){
            1
        }else{
            2
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val view1 = LayoutInflater.from(context).inflate(R.layout.property_list_view, parent,false)
        val view2 = LayoutInflater.from(context).inflate(R.layout.property_add_list_view, parent,false)
        return if (viewType == 1){
             ViewHolder1(view1)
        }else{
             ViewHolder2(view2)
        }
    }

    inner class ViewHolder1(itemView : View?) : RecyclerView.ViewHolder(itemView!!) {

        private val propertyBackgroundColor = itemView?.findViewById<ImageView>(R.id.propertyBackgroundColor)
        private val propertyName = itemView?.findViewById<TextView>(R.id.propertyName2)
        private val detailNumber = itemView?.findViewById<TextView>(R.id.detailNumber)
        private val minDetail = itemView?.findViewById<TextView>(R.id.minDetail)
        private val maxDetail = itemView?.findViewById<TextView>(R.id.maxDetail)
        private val propertyRequire = itemView?.findViewById<Switch>(R.id.propertyRequire)

        private val detailButton = itemView?.findViewById<ImageView>(R.id.detailButton)
        private val deleteProperty = itemView?.findViewById<ImageView>(R.id.deleteProperty)
        private val modifyButton = itemView?.findViewById<ImageView>(R.id.modifyButton)

        @SuppressLint("SetTextI18n")
        fun bindProperty(position : Int){

            propertyName?.text =  DataServices.setNameLanguage(properties[position].name)
            detailNumber?.text = "${properties[position].details.size}"
            minDetail?.text = properties[position].min.toString()
            maxDetail?.text = properties[position].max.toString()
            propertyRequire?.isChecked = properties[position].require
            propertyRequire?.isClickable = false

            val color = DataServices.stringToRgb(DataServices.NextContrastColor(position))
            propertyBackgroundColor?.setBackgroundColor(color)

            detailButton?.setOnClickListener {
                itemClicked(arrayListOf("detail", properties[position].id))
            }

            deleteProperty?.setOnClickListener {
                itemClicked(arrayListOf("delete", properties[position].id))
            }

            modifyButton?.setOnClickListener {
                itemClicked(
                    arrayListOf(
                        "patch",
                        properties[position].id,
                        properties[position].name[0],
                        properties[position].name[1],
                        properties[position].require.toString(),
                        properties[position].min.toString(),
                        properties[position].max.toString()
                    )
                )
            }

        }

    }

    inner class ViewHolder2(itemView : View?) : RecyclerView.ViewHolder(itemView!!){

        private val addProperty = itemView?.findViewById<ImageView>(R.id.addPropertyButton)

        fun bindProperty(){

            addProperty?.setOnClickListener {
                itemClicked(arrayListOf("post"))
            }
        }

    }


}