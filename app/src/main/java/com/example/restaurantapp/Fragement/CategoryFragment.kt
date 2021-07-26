package com.example.restaurantapp.Fragement


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.restaurantapp.Adapter.CategoryAdapter

import com.example.restaurantapp.R
import com.example.restaurantapp.Structure.category
import com.example.restaurantapp.Utilities.RESTAURANT_ID
import com.example.restaurantapp.services.CategoriesServices
import com.example.restaurantapp.services.DataServices
import com.example.restaurantapp.services.ImagesServices
import java.io.File


class CategoryFragment : Fragment() {

    private lateinit var globalContext : Context
    private lateinit var categoryAdapter : CategoryAdapter
    private lateinit var theView : View


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        globalContext = container!!.context
        theView = inflater.inflate(R.layout.fragment_category, container, false)
        // Inflate the layout for this fragment


        /*###################################################################*/
        /*###################################################################*/
        /*#######################-- the beginning  --########################*/
        /*###################################################################*/
        /*###################################################################*/

        //get all categories
        if(RESTAURANT_ID != ""){
            firstGet()
        }

        return theView
    }

    fun firstGet(){
        CategoriesServices.getAllCategories(globalContext) { success ->
            val tryAgainSetting = theView.findViewById<Button>(R.id.tryAgainSettingCategory)
            tryAgainSetting.setOnClickListener {
                tryAgain(R.string.get_category)
                firstGet()
            }
            tryAgain(R.string.get_category)

            if (success) {
                setUpAdapter(theView)
                success()
            }else{
                failed()
            }
        }
    }

    private fun setUpAdapter(view : View){
        categoryAdapter = CategoryAdapter(globalContext, CategoriesServices.categories){action ->
            // add - remove

            when(action[0]){
                "post" -> {
                    val category = category("", arrayListOf(), arrayListOf())
                    postOrPatchCategory(category, true)
                }
                "patch" -> {
                    val category = category(action[1], arrayListOf(action[2], action[3]), arrayListOf())
                    postOrPatchCategory(category, false)
                }
                "delete" -> deleteCategory(action[1])
                "getProducts" -> {
                    getProducts(action[1])
                    DataServices.actualFragment = "products"
                }
            }
        }
        val categoryListView = view.findViewById<RecyclerView>(R.id.CategoryRecyclerView)
        categoryListView.adapter = categoryAdapter
       // val layoutManager = LinearLayoutManager(context)
        val layoutManager = GridLayoutManager(globalContext, 2)
        categoryListView.layoutManager = layoutManager
    }


    /*###################################################################*/
    /*###################################################################*/
    /*#######################-- Methods server --########################*/
    /*###################################################################*/
    /*###################################################################*/



    private fun postOrPatchCategory(theCategory : category, post : Boolean){

        //set up builder
        val builder = AlertDialog.Builder(globalContext)
        val dialogView = layoutInflater.inflate(R.layout.category_text_input, null)
        builder.setView(dialogView).setPositiveButton("ok", null)
        val dialog = builder.create()

        //set up show
        dialog.setOnShowListener {

            //resources
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val categoryTextFieldFrench = dialogView.findViewById<EditText>(R.id.categoryTextFieldFrench)
            val categoryTextFieldEnglish = dialogView.findViewById<EditText>(R.id.categoryTextFieldEnglish)


            if(!post){
                categoryTextFieldFrench.setText(theCategory.name[0])
                categoryTextFieldEnglish.setText(theCategory.name[1])
            }
            if(!DataServices.englishLanguage){
                categoryTextFieldEnglish.layoutParams.height = 0
                categoryTextFieldEnglish.requestLayout()
            }

            okButton.setOnClickListener {

                val categoryEnglishName = categoryTextFieldEnglish.text.trim().toString()
                val categoryFrenchName = categoryTextFieldFrench.text.trim().toString()

                if(categoryFrenchName.isNotEmpty()){
                    val newCategory = category(theCategory.id, arrayListOf(categoryFrenchName, categoryEnglishName), arrayListOf())
                    if(post){
                        createCategory(newCategory.name)
                    }else{
                        setPatchCategory(newCategory)
                    }
                    dialog.dismiss()
                } else{
                    makeMessageToast(R.string.category_name)
                }
            }
        }
        dialog.show()


    }

    /*###################################################################*/
    /*###################################################################*/
    /*#######################-- set or get --############################*/
    /*###################################################################*/
    /*###################################################################*/

    private fun getAllCategories(){
        val tryAgainSetting = theView.findViewById<Button>(R.id.tryAgainSettingCategory)
        tryAgainSetting.setOnClickListener {
            tryAgain(R.string.get_category)
            getAllCategories()
        }
        tryAgain(R.string.get_category)

        CategoriesServices.getAllCategories(globalContext){success ->
            if(success){
                categoryAdapter.notifyDataSetChanged()
                success()
            }else{
                failed()
            }
        }
    }

    private fun createCategory(categoryName : ArrayList<String>){
        val tryAgainSetting = theView.findViewById<Button>(R.id.tryAgainSettingCategory)
        tryAgainSetting.setOnClickListener {
            tryAgain(R.string.post_category)
            createCategory(categoryName)
        }
        tryAgain(R.string.post_category)


        CategoriesServices.postCategory(globalContext, categoryName){success->
            if(success){
                getAllCategories()
            }else{
                failed()
            }
        }
    }


    private fun setPatchCategory(patchedCategory : category){
        val tryAgainSetting = theView.findViewById<Button>(R.id.tryAgainSettingCategory)
        tryAgainSetting.setOnClickListener {
            tryAgain(R.string.patch_category)
            setPatchCategory(patchedCategory)
        }
        tryAgain(R.string.post_category)

        CategoriesServices.patchCategory(globalContext, patchedCategory){success->
            if(success){
                getAllCategories()
            }else{
                failed()
            }
        }
    }

    private fun deleteCategory(id : String){
        val tryAgainSetting = theView.findViewById<Button>(R.id.tryAgainSettingCategory)
        tryAgainSetting.setOnClickListener {
            tryAgain(R.string.delete_category)
            deleteCategory(id)
        }
        tryAgain(R.string.delete_category)

        CategoriesServices.deleteCategory(globalContext, id){ success ->
            if (success){
                delteProductImages()
                getAllCategories()
            }else{
                failed()
            }
        }
    }

    private fun getProducts(id : String){
        val tryAgainSetting = theView.findViewById<Button>(R.id.tryAgainSettingCategory)
        tryAgainSetting.setOnClickListener {
            tryAgain(R.string.get_products)
            getProducts(id)
        }
        tryAgain(R.string.get_products)

        CategoriesServices.getCategoryProducts(globalContext, id){success ->
            if (success){
                val fragmentTransaction = fragmentManager?.beginTransaction()?.replace(R.id.fragment, ProductsFragment())
                fragmentTransaction?.commit()
            }else{
                failed()
            }
        }
    }


    /*###################################################################*/
    /*###################################################################*/
    /*#######################-- function   --############################*/
    /*###################################################################*/
    /*###################################################################*/

    private fun delteProductImages(){
        for(item in CategoriesServices.deletetedProduct){
            deleteImage(item)
        }
    }

    private fun deleteImage(id : String){
        val storageDirectory = globalContext.getExternalFilesDir("productsImages")
        val file = File(storageDirectory, "${id}.jpg")
        file.delete()
    }

    private fun makeMessageToast(theMessage : Int){
        Toast.makeText(globalContext, theMessage, Toast.LENGTH_LONG).show()
    }

    private fun failed(){
        val progressBarSetting = theView.findViewById<ProgressBar>(R.id.progressBarSettingCategory)
        val tryAgainSetting = theView.findViewById<Button>(R.id.tryAgainSettingCategory)

        progressBarSetting.visibility = View.INVISIBLE
        tryAgainSetting.visibility = View.VISIBLE
    }

    private fun tryAgain(message : Int){
        val waitForProcess = theView.findViewById<ConstraintLayout>(R.id.waitForProcessCategory)
        val stepProgressSetting = theView.findViewById<TextView>(R.id.stepProgressSettingCategory)
        val progressBarSetting = theView.findViewById<ProgressBar>(R.id.progressBarSettingCategory)
        val tryAgainSetting = theView.findViewById<Button>(R.id.tryAgainSettingCategory)

        waitForProcess.visibility = View.VISIBLE
        progressBarSetting.visibility = View.VISIBLE
        tryAgainSetting.visibility = View.INVISIBLE
        stepProgressSetting.text = globalContext.resources.getString(message)
    }

    private fun success(){
        val waitForProcess = theView.findViewById<ConstraintLayout>(R.id.waitForProcessCategory)
        val progressBarSetting = theView.findViewById<ProgressBar>(R.id.progressBarSettingCategory)
        val tryAgainSetting = theView.findViewById<Button>(R.id.tryAgainSettingCategory)

        waitForProcess.visibility = View.INVISIBLE
        progressBarSetting.visibility = View.VISIBLE
        tryAgainSetting.visibility = View.INVISIBLE
    }



}
