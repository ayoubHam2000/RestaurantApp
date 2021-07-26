package com.example.restaurantapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.restaurantapp.R
import com.example.restaurantapp.services.DataServices
import com.example.restaurantapp.services.PropertiesServices

class DetailAdapter(val context : Context, val details : ArrayList<ArrayList<String>>, val itemClicked : (ArrayList<String>) -> Unit ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if(holder.itemViewType == 1){
            (holder as ViewHolder1).bindDetail(position)
        }else{
            (holder as ViewHolder2).bindDetail()
        }
    }

    override fun getItemCount(): Int {
        return details.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if(position < details.size){
            1
        }else{
            2
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val view1 = LayoutInflater.from(context).inflate(R.layout.detail_list_view, parent, false)
        val view2 = LayoutInflater.from(context).inflate(R.layout.detail_add_list_view, parent, false)

        return if(viewType == 1){
            ViewHolder1(view1)
        }else{
            ViewHolder2(view2)
        }
    }

    inner class ViewHolder1(itemView : View?) : RecyclerView.ViewHolder(itemView!!){

        private val detailCheckBox = itemView?.findViewById<CheckBox>(R.id.detailCheckBox)
        private val detailPrice = itemView?.findViewById<TextView>(R.id.detailPrice)
        private val detailPriceUnit = itemView?.findViewById<TextView>(R.id.detailPriceUnit)

        private val deleteDelete = itemView?.findViewById<ImageView>(R.id.deleteDetail)
        private val modifyDetail = itemView?.findViewById<ImageView>(R.id.modifyDetail)

        fun bindDetail(position : Int){

            detailCheckBox?.text = DataServices.setNameLanguage(details[position])
            detailCheckBox?.isChecked = details[position][2].toBoolean()

            detailPrice?.text = details[position][3]
            detailPriceUnit?.text = DataServices.productPriceUnit

            if(details[position][3] == "0"){
                detailPrice?.visibility = INVISIBLE
                detailPriceUnit?.visibility = INVISIBLE
            }else{
                detailPrice?.visibility = VISIBLE
                detailPriceUnit?.visibility = VISIBLE
            }

            deleteDelete?.setOnClickListener {
                itemClicked(arrayListOf("delete", position.toString()))
            }

            modifyDetail?.setOnClickListener{
                itemClicked(arrayListOf("patch", position.toString()))
            }

            detailCheckBox?.setOnClickListener {
                itemClicked(arrayListOf("patchCheckBox", position.toString(), detailCheckBox.isChecked.toString()))
            }

        }
    }

    inner class ViewHolder2(itemView: View?) : RecyclerView.ViewHolder(itemView!!){

        private val addDetailButton = itemView?.findViewById<ImageView>(R.id.addDetailButton)

        fun bindDetail(){

            addDetailButton?.setOnClickListener {
                itemClicked(arrayListOf("post", PropertiesServices.actualPropertyId))
            }
        }

    }
}