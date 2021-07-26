package com.example.restaurantapp.Adapter

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.restaurantapp.R
import com.example.restaurantapp.Structure.category
import com.example.restaurantapp.services.DataServices

class CategoryAdapter(val context: Context, val listCategory : ArrayList<category>, val itemClicked : (String)-> Unit) :  RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == 1){
            (holder as ViewHolder1).bindCategory(position)
        }else{
            (holder as ViewHolder2).bindCategory()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view1 = LayoutInflater.from(context).inflate(R.layout.category_list_view, parent, false)
        val view2 = LayoutInflater.from(context).inflate(R.layout.category_add_list_view, parent, false)
        return if(viewType == 1){
            ViewHolder1(view1)
        }else{
            ViewHolder2(view2)
        }
    }

    override fun getItemViewType(position: Int): Int {
        // here you can get decide from your model's ArrayList, which type of view you need to load. Like
        return if (position < listCategory.size) { // put your condition, according to your requirements
            1
        } else 2
    }

    override fun getItemCount(): Int {
        return listCategory.size + 1
    }

    inner class ViewHolder1(itemView : View?) : RecyclerView.ViewHolder(itemView!!){
        val categoryName = itemView?.findViewById<TextView>(R.id.CategoryName)
        val categoryColor = itemView?.findViewById<ImageView>(R.id.CategoryColorBottom)
        val remouveCategoryButton = itemView?.findViewById<ImageView>(R.id.RemouveCategoryButton)
        val categoryProductsNumber = itemView?.findViewById<TextView>(R.id.CategoryProductsNumber)
        val patchCategoryButton = itemView?.findViewById<ImageView>(R.id.patchCategoryButton)

        fun bindCategory(position : Int){
            categoryName?.text = DataServices.setNameLanguage(listCategory[position].name)
            val color = DataServices.StringToRgb(DataServices.NextContrastColor(position))
            val drawableShape = categoryColor?.background as GradientDrawable
            drawableShape.setColor(color)
            categoryProductsNumber?.text = listCategory[position].ListProducts.size.toString()
            remouveCategoryButton?.setOnClickListener{
                itemClicked("delete ${listCategory[position].id}")
            }
            categoryColor.setOnClickListener {
                itemClicked("getProducts ${listCategory[position].id}")
            }
            patchCategoryButton?.setOnClickListener {
                itemClicked("patch ${listCategory[position].id} ${listCategory[position].name}")
            }
        }
    }

    inner class ViewHolder2(itemView : View?) : RecyclerView.ViewHolder(itemView!!){

        fun bindCategory(){
            itemView.setOnClickListener{
                itemClicked("post")
            }
        }
    }
}