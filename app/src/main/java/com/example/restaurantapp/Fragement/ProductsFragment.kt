package com.example.restaurantapp.Fragement

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.restaurantapp.Adapter.ProductAdapter

import com.example.restaurantapp.R
import com.example.restaurantapp.Structure.imageInfo
import com.example.restaurantapp.Structure.product
import com.example.restaurantapp.Utilities.*
import com.example.restaurantapp.services.*
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


class ProductsFragment : Fragment() {


    lateinit var globalContext : Context
    lateinit var productsAdapter : ProductAdapter
    private lateinit var theView : View

    private val GALLERY_REQUEST_CODE = 1234
    lateinit var imageUri : Uri
    lateinit var imageView: ImageView
    var imageChange = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        /*###################################################################*/
        /*###################################################################*/
        /*#######################-- the beginning  --########################*/
        /*###################################################################*/
        /*###################################################################*/
        globalContext = container!!.context
        theView = inflater.inflate(R.layout.fragment_products, container, false)
        setUpAdapter(theView)
        downloadImages()

        return theView
    }

    /*###################################################################*/
    /*###################################################################*/
    /*#######################-     set up      --########################*/
    /*###################################################################*/
    /*###################################################################*/
    private fun downloadImages(){
        for(product in CategoriesServices.categoryProducts){
            if(ImagesServices.continueDownloading){
                thread{
                    ImagesServices.getImageFromUrl(globalContext, product.id, product.imageURL){complete->
                        if(complete){
                            Handler(Looper.getMainLooper()).post {
                                ImagesServices.continueDownloading = false
                                productsAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
                Thread.sleep(1000)
            }
        }
    }

    private fun setUpAdapter(view : View){
        productsAdapter = ProductAdapter(globalContext, CategoriesServices.categoryProducts){action ->
            // add - remove

            when(action[0]){
                "post" -> {
                    val product = product(
                        "",
                        arrayListOf(),
                        1.0,
                        1,
                        arrayListOf(),
                        arrayListOf(),
                        ""
                    )
                    postOrPatchProduct(product, true)
                }
                "patch" -> {
                    val product = product(
                        action[1],
                        arrayListOf(action[2], action[3]),
                        action[4].toDouble(),
                        action[5].toInt(),
                        arrayListOf(action[6], action[7]),
                        arrayListOf(), "")
                    postOrPatchProduct(product, false)
                }
                "delete" -> deleteProduct(action[1])
                "getProperties" -> {
                    //the first time the id is empty
                    PropertiesServices.actualPropertyId = ""
                    PropertiesServices.actualPropertyName = ""
                    getProperties(action[1])
                    DataServices.actualFragment = "properties"
                }
            }
        }
        val productsRecyclerView = view.findViewById<RecyclerView>(R.id.ProductsRecyclerView)
        productsRecyclerView.adapter = productsAdapter
        // val layoutManager = LinearLayoutManager(context)
        val layoutManager = GridLayoutManager(globalContext, 2)
        productsRecyclerView.layoutManager = layoutManager!!
    }

    /*###################################################################*/
    /*###################################################################*/
    /*#######################-- post patch     --########################*/
    /*###################################################################*/
    /*###################################################################*/

    private fun postOrPatchProduct(productInfo : product, post : Boolean){

        //set up builder
        val builder = AlertDialog.Builder(globalContext)
        val dialogView = layoutInflater.inflate(R.layout.product_text_input, null)
        builder.setView(dialogView).setPositiveButton("ok", null)
        val dialog = builder.create()


        //set up builder show
        dialog.setOnShowListener {
            imageChange = false

            //resources
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val productImage = dialogView.findViewById<ImageView>(R.id.productImageView)
            val nextProductName = dialogView.findViewById<ImageView>(R.id.nextProductName)
            val beforeProductName = dialogView.findViewById<ImageView>(R.id.beforProductName)
            val nextDetailName = dialogView.findViewById<ImageView>(R.id.nextDetailName)
            val beforeDetailsName = dialogView.findViewById<ImageView>(R.id.beforDetailName)
            val productNameTextView = dialogView.findViewById<TextView>(R.id.productTextView)
            val productNameEditText = dialogView.findViewById<EditText>(R.id.productEditText)
            val detailNameTextView = dialogView.findViewById<TextView>(R.id.detailTextView)
            val detailNameEditText = dialogView.findViewById<EditText>(R.id.detailEditText)
            val productPriceEditText = dialogView.findViewById<EditText>(R.id.productPriceEditText)
            val productDiscountEditText = dialogView.findViewById<EditText>(R.id.productDiscountEditeText)
            val productPriceUnitTextView = dialogView.findViewById<TextView>(R.id.productPriceUnitTextView)

            //variables
            var productPos = 0
            var detailPos = 0
            val productsName = arrayListOf("", "")
            val detailName = arrayListOf("", "")

            //set product info to view if post == false
            if(!post){
                setImageFromUrl(productImage, productInfo.id)
                productsName[0] = productInfo.productName[0]
                productsName[1] = productInfo.productName[1]
                detailName[0] =  productInfo.productDetails[0]
                detailName[1] =  productInfo.productDetails[1]
                productNameEditText.setText(productsName[0])
                detailNameEditText.setText(detailName[0])
                productPriceEditText.setText(productInfo.productPrice.toString())
                productDiscountEditText.setText(productInfo.productDiscount.toString())
            }else{
                //added for only test you can remove it
                productNameEditText.setText("new Product")
                detailNameEditText.setText("new detail")
                productPriceEditText.setText("20")
                //this is necessary
                productDiscountEditText.setText("0")
            }

            val maxNext = if(DataServices.englishLanguage) 1 else 0
            if(!DataServices.englishLanguage){
                nextProductName.visibility = INVISIBLE
                beforeProductName.visibility = INVISIBLE
                nextDetailName.visibility = INVISIBLE
                beforeDetailsName.visibility = INVISIBLE
            }

            //functions
            productPriceUnitTextView.text = DataServices.productPriceUnit
            setProductText(productPos, productNameTextView, productNameEditText)
            setDetailText(detailPos, detailNameTextView, detailNameEditText)

            productImage.setOnClickListener {
                imageView = productImage
                pickFormatGallery()
            }

            productNameEditText.setOnEditorActionListener { _, actionId, _ ->
                when(actionId){
                    EditorInfo.IME_ACTION_NEXT ->
                    {
                        if(productPos < maxNext){
                            nextProductName.callOnClick()
                        }else{
                            detailNameEditText.requestFocus()
                        }
                        true
                    }
                    else -> false
                }
            }

            detailNameEditText.setOnEditorActionListener { _, actionId, _ ->
                when(actionId){
                    EditorInfo.IME_ACTION_NEXT ->
                    {
                        if(detailPos < maxNext){
                            nextDetailName.callOnClick()
                        }else{
                            productPriceEditText.requestFocus()
                        }
                        true
                    }
                    else -> false
                }
            }

            nextProductName.setOnClickListener {
                if(productPos < maxNext){
                    productsName[productPos] = productNameEditText.text.trim().toString()
                    productPos++
                    setColorButton(productPos, nextProductName, beforeProductName)
                    setProductText(productPos, productNameTextView, productNameEditText)
                    productNameEditText.setText(productsName[productPos])
                }
            }

            beforeProductName.setOnClickListener {
                if(0 < productPos){
                    productsName[productPos] = productNameEditText.text.trim().toString()
                    productPos--
                    setColorButton(productPos, nextProductName, beforeProductName)
                    setProductText(productPos, productNameTextView, productNameEditText)
                    productNameEditText.setText(productsName[productPos])
                }
            }

            nextDetailName.setOnClickListener {
                if(detailPos < maxNext){
                    detailName[detailPos] = detailNameEditText.text.trim().toString()
                    detailPos++
                    setColorButton(detailPos, nextDetailName, beforeDetailsName)
                    setDetailText(detailPos, detailNameTextView, detailNameEditText)
                    detailNameEditText.setText(detailName[detailPos])
                }
            }

            beforeDetailsName.setOnClickListener {
                if(0 < detailPos){
                    detailName[detailPos] = detailNameEditText.text.trim().toString()
                    detailPos--
                    setColorButton(detailPos, nextDetailName, beforeDetailsName)
                    setDetailText(detailPos, detailNameTextView, detailNameEditText)
                    detailNameEditText.setText(detailName[detailPos])
                }
            }

            okButton.setOnClickListener {
                productsName[productPos] = productNameEditText.text.toString()
                detailName[detailPos] = detailNameEditText.text.toString()
                val allow = postValidation(productsName, detailName)

                if(allow && productPriceEditText.text.isNotEmpty() && productDiscountEditText.text.isNotEmpty()){
                    val productPrice = productPriceEditText.text.toString().toDouble()
                    val productDiscount = productDiscountEditText.text.toString().toInt()
                    val newProduct = product(
                        productInfo.id,
                        productsName,
                        productPrice,
                        productDiscount,
                        detailName,
                        arrayListOf(),"")

                    if(post){
                        if(imageChange){
                            dialog.dismiss()
                            setPostProduct(newProduct)
                        }else{
                            makeMessageToast(R.string.pick_image)
                        }

                    }else{
                        dialog.dismiss()
                        if(imageChange){
                            setPatchProductWithImage(newProduct)
                        }else{
                            setPatchProductWithOutImage(newProduct)
                        }
                    }
                }else{
                    makeMessageToast(R.string.invalide_input)
                }
            }
        }

        //dialog window
        dialog.show()
        val window = dialog.window
        window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

    }

    /*###################################################################*/
    /*###################################################################*/
    /*#################-- set get delete patch post --###################*/
    /*###################################################################*/
    /*###################################################################*/

    private fun getAllProducts(){
        val tryAgainSetting = theView.findViewById<Button>(R.id.tryAgainSetting)
        tryAgainSetting.setOnClickListener {
            tryAgain(R.string.get_products)
            getAllProducts()
        }
        tryAgain(R.string.get_products)


        val id = CategoriesServices.categoryId
        CategoriesServices.getCategoryProducts(globalContext, id){success ->
            if (success){
                productsAdapter.notifyDataSetChanged()
                success()
            }else{
                Log.d("Failed", "Failed to get products from category")
                failed()
            }
        }
    }

    private fun getProperties(id : String){
        val tryAgainSetting = theView.findViewById<Button>(R.id.tryAgainSetting)
        tryAgainSetting.setOnClickListener {
            tryAgain(R.string.get_properties)
            getProperties(id)
        }
        tryAgain(R.string.get_properties)


        ProductsServices.getProductProperties(globalContext, id){success ->
            if (success){
                val fragmentTransaction = fragmentManager?.beginTransaction()?.replace(R.id.fragment, PropertiesFragment())
                fragmentTransaction?.commit()
            }else{
                Log.d("Failed", "Failed to get product properties")
                failed()
            }
        }
    }

    private fun setPostProduct(theProduct : product){
        println("------->setPostProduct")
        val tryAgainSetting = theView.findViewById<Button>(R.id.tryAgainSetting)
        tryAgainSetting.setOnClickListener {
            tryAgain(R.string.post_product)
            setPostProduct(theProduct)
        }
        tryAgain(R.string.post_product)

        println(Calendar.getInstance().time)
        val bitmapImage = saveImageToStorage(globalContext, imageUri, "ProductImage")!!
        ProductsServices.postProduct(globalContext, theProduct, bitmapImage){success, productId ->
            if(success){
                saveImageToStorage(globalContext, imageUri, productId)
                getAllProducts()
            }else{
                Log.d("Failed", "Failed to post product")
                println(Calendar.getInstance().time)
                failed()
            }
        }
    }

    private fun setPatchProductWithImage(theProduct : product){
        val tryAgainSetting = theView.findViewById<Button>(R.id.tryAgainSetting)
        tryAgainSetting.setOnClickListener {
            tryAgain(R.string.patch_product)
            setPatchProductWithOutImage(theProduct)

        }
        tryAgain(R.string.patch_product)

        val bitmapImage = saveImageToStorage(globalContext, imageUri, "ProductImage")!!
        ProductsServices.patchProductWithImage(globalContext, theProduct, bitmapImage){success, productId ->
            if(success){
                saveImageToStorage(globalContext, imageUri, productId)
                getAllProducts()
            }else{
                Log.d("Failed", "Failed patch product")
                failed()
            }
        }
    }

    private fun setPatchProductWithOutImage(theProduct : product){
        println("setPatchProductWithOutImage")
        val tryAgainSetting = theView.findViewById<Button>(R.id.tryAgainSetting)
        tryAgainSetting.setOnClickListener {
            tryAgain(R.string.patch_product)
            setPatchProductWithOutImage(theProduct)
        }
        tryAgain(R.string.patch_product)


        ProductsServices.patchProductWithOutImage(globalContext, theProduct){success ->
            if(success){
                getAllProducts()
            }else{
                Log.d("Failed", "Failed patch product")
                failed()
            }
        }
    }

    private fun deleteProduct(id : String){
        val tryAgainSetting = theView.findViewById<Button>(R.id.tryAgainSetting)
        tryAgainSetting.setOnClickListener {
            tryAgain(R.string.delete_product)
            deleteProduct(id)
        }
        tryAgain(R.string.delete_product)


        ProductsServices.deleteProduct(globalContext, id){ success ->
            if(success){
                deleteImage(id)
                getAllProducts()
            }else{
                Log.d("Failed", "Failed to post delete product")
                failed()
            }

        }
    }

    private fun deleteImage(id : String){
        val storageDirectory = globalContext.getExternalFilesDir("productsImages")
        val file = File(storageDirectory, "${id}.jpg")
        file.delete()
    }

    /*###################################################################*/
    /*###################################################################*/
    /*#################--       functions helper    --###################*/
    /*###################################################################*/
    /*###################################################################*/

    /*--------------------------------image------------------------*/


    private fun pickFormatGallery(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        val mimeType = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType)
        intent.flags = Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            GALLERY_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK){
                    data?.data?.let{uri ->
                        launchImageCrop(uri)
                    }
                }else{
                    Log.d("ERROR", "couldn't select image from the gallery")
                }
            }

            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if(resultCode == Activity.RESULT_OK){
                    result.uri?.let{
                        //set Image
                        imageUri = it
                        imageView.setImageURI(Uri.parse(imageUri.toString()))
                        imageChange = true
                    }

                }else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                    Log.d("ERROR", "CropError : ${result.error}")
                }
            }
        }
    }

    private fun launchImageCrop(uri : Uri){
        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(700, 547)
            .setCropShape(CropImageView.CropShape.RECTANGLE)
            .start(globalContext, this)
    }


    private fun saveImageToStorage(context : Context, uri: Uri, productId : String) : Bitmap?{
        val externalStorageStats = Environment.getExternalStorageState()
        if(externalStorageStats == Environment.MEDIA_MOUNTED){
            val storageDirectory = context.getExternalFilesDir("productsImages")
            val file = File(storageDirectory, "${productId}.jpg")
            try{
                val stream  = FileOutputStream(file)
                val bitmap = BitmapFactory.decodeFile(uri.encodedPath)
                val dataSize = bitmap.rowBytes * bitmap.height


                when {
                    dataSize > HUGE_IMAGE -> {
                        bitmap?.compress(Bitmap.CompressFormat.JPEG, HUGE_COMPRESS, stream)
                    }
                    dataSize > BIG_IMAGE -> {
                        bitmap?.compress(Bitmap.CompressFormat.JPEG, HEIGHT_COMPRESS, stream)
                    }
                    else -> {
                        bitmap?.compress(Bitmap.CompressFormat.JPEG, LOW_COMPRESS, stream)
                    }
                }
                stream.flush()
                stream.close()
                return bitmap
            }catch(e : Exception){
                Log.d("IError", "${e.printStackTrace()}")
            }
        }else{
            Log.d("IError", "Enable To Access The Storage")
        }
        return null
    }

    private fun setImageFromUrl(imageView : ImageView, productId :  String){
        val url = globalContext.getExternalFilesDir("productsImages/${productId}.jpg").toString()
        val bitmap = BitmapFactory.decodeFile(url)
        imageView.setImageBitmap((bitmap))
    }


    /*-----------------------------patch and post------------------------*/


    private fun setColorButton(position : Int, nextButton: ImageView, beforeButton : ImageView){

        when(position){
            0 -> {
                DrawableCompat.setTint(nextButton.drawable, ContextCompat.getColor(globalContext, R.color.nextButtonActive))
                DrawableCompat.setTint(beforeButton.drawable, ContextCompat.getColor(globalContext, R.color.nextButtonDeactivate))
            }
            1 -> {
                DrawableCompat.setTint(nextButton.drawable, ContextCompat.getColor(globalContext, R.color.nextButtonDeactivate))
                DrawableCompat.setTint(beforeButton.drawable, ContextCompat.getColor(globalContext, R.color.nextButtonActive))
            }
        }
    }

    private fun  setProductText(position : Int, textView : TextView, editText : EditText){

        val languageArray = resources.getStringArray(R.array.name)
        textView.text = languageArray[position]
        editText.hint = languageArray[position]
    }

    private fun  setDetailText(position : Int, textView : TextView, editText : EditText){

        val languageArray = resources.getStringArray(R.array.detail)
        textView.text = languageArray[position]
        editText.hint = languageArray[position]
    }

    private fun checkEmpty(info : ArrayList<String>) : Boolean{

        if(info[0].isEmpty()){
            return false
        }
        return true
    }

    private fun postValidation(a1 : ArrayList<String>, a2 : ArrayList<String>) : Boolean{
        if( !checkEmpty(a1) || !checkEmpty(a2)) {
            return false
        }
        return true
    }

    /*-----------------------------other------------------------*/

    private fun makeMessageToast(theMessage : Int){
        Toast.makeText(globalContext, theMessage, Toast.LENGTH_SHORT).show()
    }

    private fun failed(){
        val progressBarSetting = theView.findViewById<ProgressBar>(R.id.progressBarSetting)
        val tryAgainSetting = theView.findViewById<Button>(R.id.tryAgainSetting)

        progressBarSetting.visibility = INVISIBLE
        tryAgainSetting.visibility = VISIBLE
    }

    private fun tryAgain(message : Int){
        val waitForProcess = theView.findViewById<ConstraintLayout>(R.id.waitForProcess)
        val stepProgressSetting = theView.findViewById<TextView>(R.id.stepProgressSetting)
        val progressBarSetting = theView.findViewById<ProgressBar>(R.id.progressBarSetting)
        val tryAgainSetting = theView.findViewById<Button>(R.id.tryAgainSetting)

        waitForProcess.visibility = VISIBLE
        progressBarSetting.visibility = VISIBLE
        tryAgainSetting.visibility = INVISIBLE
        stepProgressSetting.text = resources.getString(message)
    }

    private fun success(){
        val waitForProcess = theView.findViewById<ConstraintLayout>(R.id.waitForProcess)
        val progressBarSetting = theView.findViewById<ProgressBar>(R.id.progressBarSetting)
        val tryAgainSetting = theView.findViewById<Button>(R.id.tryAgainSetting)

        waitForProcess.visibility = INVISIBLE
        progressBarSetting.visibility = VISIBLE
        tryAgainSetting.visibility = INVISIBLE
    }

    /*android:digits="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"*/

}
