package com.example.restaurantapp.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.restaurantapp.R
import com.example.restaurantapp.Structure.product
import com.example.restaurantapp.services.DataServices
import com.example.restaurantapp.services.ImagesServices
import com.example.restaurantapp.services.ProductsServices
import java.io.File
import java.text.DecimalFormat


class ProductAdapter(val context : Context, val products : ArrayList<product>, val itemClick : (ArrayList<String>)-> Unit ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {



    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            if(holder.itemViewType == 1){
                (holder as ViewHolder1).bindProduct(position)
            }else{
                (holder as ViewHolder2).bindProduct()
            }

    }

    override fun getItemCount(): Int {
        return products.size + 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view1 = LayoutInflater.from(context).inflate(R.layout.product_list_view, parent, false)
        val view2 = LayoutInflater.from(context).inflate(R.layout.product_add_list_view, parent, false)
        return if (viewType == 1){
            ViewHolder1(view1)
        }else{
            ViewHolder2(view2)
        }

    }


    override fun getItemViewType(position: Int): Int {
        // here you can get decide from your model's ArrayList, which type of view you need to load. Like
        return if (position < products.size) { // put your condition, according to your requirements
            1
        } else 2
    }

    inner class ViewHolder1(itemView : View?): RecyclerView.ViewHolder(itemView!!){
        private val productImage = itemView?.findViewById<ImageView>(R.id.ProductImage)
        private val productName = itemView?.findViewById<TextView>(R.id.productName)
        private val productProperties = itemView?.findViewById<TextView>(R.id.productProperties)
        private val crossLine = itemView?.findViewById<View>(R.id.crossLine)

        private val productPrice = itemView?.findViewById<TextView>(R.id.productPrice)
        private val productPriceUnit = itemView?.findViewById<TextView>(R.id.productUnitPrice)
        private val priceBeforeDiscount = itemView?.findViewById<TextView>(R.id.priceBeforDiscount)
        private val priceDiscount =  itemView?.findViewById<TextView>(R.id.priceDiscount)

        private val productRemoveButton = itemView?.findViewById<ImageView>(R.id.ProductRemoveButton)
        private val editProduct = itemView?.findViewById<ImageView>(R.id.patchProduct)


        @SuppressLint("SetTextI18n")
        fun bindProduct(position: Int){

            //Set Image
            setImageFromUrl(productImage , products[position].id)


            //Set Texts
            productName?.text = DataServices.setNameLanguage(products[position].productName)
            println(products[position].productDetails)
            productProperties?.text = DataServices.setNameLanguage(products[position].productDetails)

            //Set price
            productPriceUnit?.text = DataServices.productPriceUnit
            priceBeforeDiscount?.text = products[position].productPrice.toString() + " " + DataServices.productPriceUnit
            priceDiscount?.text = products[position].productDiscount.toString() + "%"

            //if discount == 0
            if(products[position].productDiscount == 0){
                priceDiscount?.visibility = View.INVISIBLE
                crossLine?.visibility = View.INVISIBLE
                priceBeforeDiscount?.visibility = View.INVISIBLE

                productPriceUnit?.setTextSize(TypedValue.COMPLEX_UNIT_DIP,8.0f)
                productPrice?.setTextSize(TypedValue.COMPLEX_UNIT_DIP,12.0f)
                setMargins(productPriceUnit, 75, 0,0,16)

            }else{
                priceDiscount?.visibility = View.VISIBLE
                crossLine?.visibility = View.VISIBLE
                priceBeforeDiscount?.visibility = View.VISIBLE

                productPriceUnit?.setTextSize(TypedValue.COMPLEX_UNIT_DIP,6.0f)
                productPrice?.setTextSize(TypedValue.COMPLEX_UNIT_DIP,8.0f)
                setMargins(productPriceUnit, 0, 0,0,10)
            }


            val newPrice = products[position].productPrice*(1 - (products[position].productDiscount.toDouble())/100)
            productPrice?.text = DecimalFormat("##.##").format(newPrice)

            //Actions
            productRemoveButton?.setOnClickListener {
                itemClick(arrayListOf("delete", products[position].id))
            }
            productProperties?.setOnClickListener {
                ProductsServices.productName2 =DataServices.setNameLanguage(products[position].productName)
                itemClick(arrayListOf("getProperties", products[position].id))
            }
            editProduct?.setOnClickListener {
                itemClick(  arrayListOf(
                    "patch",
                    products[position].id,
                    products[position].productName[0],
                    products[position].productName[1],
                    products[position].productPrice.toString(),
                    products[position].productDiscount.toString(),
                    products[position].productDetails[0],
                    products[position].productDetails[1]
                    ))
            }

        }

        private fun setMargins (textV : TextView?,  left : Int,  top : Int,  right : Int,  bottom : Int) {

            val params = textV?.layoutParams as ConstraintLayout.LayoutParams
            params.setMargins(left, top, right,bottom)
            textV.layoutParams = (params)
        }

    }

    inner class ViewHolder2(itemView : View?): RecyclerView.ViewHolder(itemView!!){
        private val productAddButton = itemView?.findViewById<ImageView>(R.id.ProductAddButton)

        fun bindProduct(){
            productAddButton?.setOnClickListener {
                itemClick(arrayListOf("post"))
            }
        }

    }

    fun setImageFromUrl(imageView : ImageView?, productId :  String){
        val url = context.getExternalFilesDir("productsImages/${productId}.jpg").toString()
        val file = File(url)
        if(!file.isDirectory){
            val bitmap = BitmapFactory.decodeFile(url)
            imageView?.setImageBitmap((bitmap))
        }else{
            Log.d("NOTFOUND", "IMAGE NOT FOUNT $productId")
            file.delete()
            ImagesServices.continueDownloading = true
        }
    }


}