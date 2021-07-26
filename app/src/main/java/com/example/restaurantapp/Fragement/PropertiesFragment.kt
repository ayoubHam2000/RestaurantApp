package com.example.restaurantapp.Fragement

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.restaurantapp.Adapter.DetailAdapter
import com.example.restaurantapp.Adapter.PropertyAdapter

import com.example.restaurantapp.R
import com.example.restaurantapp.Structure.property
import com.example.restaurantapp.services.DataServices
import com.example.restaurantapp.services.ProductsServices
import com.example.restaurantapp.services.PropertiesServices


class PropertiesFragment : Fragment() {

    lateinit var globalContext : Context
    lateinit var propertyAdapter : PropertyAdapter
    lateinit var detailAdapter : DetailAdapter
    lateinit var theView : View

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
        theView  = inflater.inflate(R.layout.fragment_properties, container, false)

        setUpPropertyRecyclerView(theView)
        setUpDetailRecyclerView(theView)
        return theView

    }

    /*###################################################################*/
    /*###################################################################*/
    /*###########################-- set Up --############################*/
    /*###################################################################*/
    /*###################################################################*/

    private fun setUpPropertyRecyclerView(view : View){

        val propProductName = view.findViewById<TextView>(R.id.PropProductName)
        propProductName.text = ProductsServices.productName2
        propertyAdapter = PropertyAdapter(globalContext, ProductsServices.productProperties){action ->

            when(action[0]){

                "detail" -> {
                    getPropertyDetail(action[1])
                }
                "post" -> {
                    val property = property("", arrayListOf(), true, 0,0, arrayListOf())
                    postOrPatchProperty(property, true)
                }
                "patch" -> {
                    val property = property(
                        action[1],
                        arrayListOf(action[2], action[3]),
                        action[4].toBoolean(),
                        action[5].toInt(),
                        action[6].toInt(),
                        arrayListOf()
                    )
                    postOrPatchProperty(property, false)
                }
                "delete" -> {deleteProperty(action[1])}
            }

        }
        val propertyRecyclerView = view.findViewById<RecyclerView>(R.id.propertiesRecyclerView)
        val layoutManager = LinearLayoutManager(globalContext)

        propertyRecyclerView.adapter = propertyAdapter
        propertyRecyclerView.layoutManager = layoutManager

    }

    /*------------------------------------------------------------------*/

    private fun setUpDetailRecyclerView(view : View){

        val propertyName = theView.findViewById<TextView>(R.id.propertyName)
        if(PropertiesServices.actualPropertyName != ""){
            propertyName.text = PropertiesServices.actualPropertyName
        }else{
            propertyName.text = "No Property is Selected"
        }
        detailAdapter = DetailAdapter(globalContext, PropertiesServices.propertyDetails){action ->
            when(action[0]){
                "post" -> {
                    if(action[1] == ""){
                        makeMessageToast(R.string.no_prop_selected)
                    }else{
                        postOrPatchDetail(0, true)
                    }

                }
                "patch" -> {
                    postOrPatchDetail(action[1].toInt(), false)
                }
                "delete" -> { deleteDetail(action[1].toInt()) }
                "patchCheckBox" ->{
                    patchDetailBox(action[1].toInt(), action[2].toBoolean())
                }
            }
        }

        val detailRecyclerView = view.findViewById<RecyclerView>(R.id.detailsRecyclerView)
        val layoutManager = LinearLayoutManager(globalContext)

        detailRecyclerView.adapter = detailAdapter
        detailRecyclerView.layoutManager = layoutManager

    }

    /*###################################################################*/
    /*###################################################################*/
    /*#######################-- Methods server --########################*/
    /*###################################################################*/
    /*###################################################################*/


    private fun postOrPatchProperty(theProperty : property, post : Boolean){
        //set up builder
        val builder = AlertDialog.Builder(globalContext)
        val builderLayout = layoutInflater.inflate(R.layout.property_text_input, null)
        builder.setView(builderLayout).setPositiveButton("ok", null)
        val dialog = builder.create()

        //dialog show
        dialog.setOnShowListener {

            //resources
            val okButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            val nextButton = builderLayout.findViewById<ImageView>(R.id.nextPropertyNameButton)
            val beforeButton = builderLayout.findViewById<ImageView>(R.id.beforPropertyNameButton)
            val setRequireProperty = builderLayout.findViewById<Switch>(R.id.setRequireProperty)
            val propertyName = builderLayout.findViewById<TextView>(R.id.setPropertyName)
            val propertyNameInput = builderLayout.findViewById<EditText>(R.id.setPropertyNameInput)
            val propertyMinChoices = builderLayout.findViewById<EditText>(R.id.setPropertyMinChoice)
            val propertyMaxChoices = builderLayout.findViewById<EditText>(R.id.setPropertyMaxChoice)

            //variables
            var position = 0
            val getPropertyName = arrayListOf("", "")

            //functions
            setPropertyText(position, propertyName, propertyNameInput)
            propertyMinChoices.setText("1")
            propertyMaxChoices.setText("1")

            var maxNext = if (DataServices.englishLanguage) 1 else 0
            if(!DataServices.englishLanguage){
                nextButton.visibility = INVISIBLE
                beforeButton.visibility = INVISIBLE
            }

            //set if post = false
            if(!post){
                getPropertyName[0] = theProperty.name[0]
                getPropertyName[1] = theProperty.name[1]
                setRequireProperty.isChecked = theProperty.require
                propertyNameInput.setText(getPropertyName[0])
                propertyMinChoices.setText(theProperty.min.toString())
                propertyMaxChoices.setText(theProperty.max.toString())
            }

            propertyNameInput.setOnEditorActionListener { _, actionId, _ ->
                when(actionId){
                    EditorInfo.IME_ACTION_NEXT ->
                    {
                        if(position < maxNext){
                            nextButton.callOnClick()
                        }else{
                            propertyMinChoices.requestFocus()
                        }
                        true
                    }
                    else -> false
                }
            }


            nextButton.setOnClickListener {
                if(position < maxNext){
                    getPropertyName[position] = propertyNameInput.text.trim().toString()
                    position++
                    setPropertyText(position, propertyName, propertyNameInput)
                    setColorButton(position, nextButton, beforeButton)
                    propertyNameInput.setText(getPropertyName[position])
                }
            }

            beforeButton.setOnClickListener {
                if(position > 0){
                    getPropertyName[position] = propertyNameInput.text.trim().toString()
                    position--
                    setPropertyText(position, propertyName, propertyNameInput)
                    setColorButton(position, nextButton, beforeButton)
                    propertyNameInput.setText(getPropertyName[position])
                }
            }

            okButton.setOnClickListener {

                getPropertyName[position] = propertyNameInput.text.toString()
                var allow = postValidation(getPropertyName)

                if(allow && propertyMinChoices.text.isNotEmpty() && propertyMaxChoices.text.isNotEmpty()){
                    val getPropertyRequire = setRequireProperty.isChecked
                    val getPropertyMinChoices = propertyMinChoices.text.toString().toInt()
                    val getPropertyMaxChoices = propertyMaxChoices.text.toString().toInt()
                    if(getPropertyMinChoices in 1..getPropertyMaxChoices){
                        val newProperty = property(theProperty.id,
                            getPropertyName,
                            getPropertyRequire,
                            getPropertyMinChoices,
                            getPropertyMaxChoices,
                            ArrayList()
                        )
                        if(post){
                            setPostProperty(newProperty)
                        }else{
                            setPatchProperty(newProperty)
                        }
                        dialog.dismiss()
                    }else{
                        makeMessageToast(R.string.min_max)
                    }
                }else{
                    makeMessageToast(R.string.invalide_input)
                }
            }
        }
        dialog.show()
    }



    /*------------------------------------------------------------------*/

    private fun postOrPatchDetail(pos : Int, post : Boolean){

        //set up builder
        val builder = AlertDialog.Builder(globalContext)
        val builderLayout = layoutInflater.inflate(R.layout.detail_text_input, null)
        builder.setView(builderLayout).setPositiveButton("ok", null)
        val dialog = builder.create()

        //set up builder show
        dialog.setOnShowListener {

            //resources
            val okButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            val nextButton = builderLayout.findViewById<ImageView>(R.id.detailNextButton)
            val beforeButton = builderLayout.findViewById<ImageView>(R.id.detailBeforButton)
            val detailName = builderLayout.findViewById<TextView>(R.id.setDetailName)
            val detailNameInput = builderLayout.findViewById<EditText>(R.id.setdetailNameInput)
            val setDetailPriceInput = builderLayout.findViewById<EditText>(R.id.setDetailPriceInput)

            //variables
            var position = 0
            val getDetailName = arrayListOf("", "")
            setDetailText(position, detailName, detailNameInput)

            //set if post = false
            if(!post){
                getDetailName[0] = PropertiesServices.propertyDetails[pos][0]
                getDetailName[1] = PropertiesServices.propertyDetails[pos][1]
                detailNameInput.setText(getDetailName[0])
                setDetailPriceInput.setText(PropertiesServices.propertyDetails[pos][3])
            }else{
                setDetailPriceInput.setText("0")
            }

            val maxNext = if(DataServices.englishLanguage) 1 else 0
            if(!DataServices.englishLanguage){
                nextButton.visibility = INVISIBLE
                beforeButton.visibility = INVISIBLE
            }

            detailNameInput.setOnEditorActionListener { _, actionId, _ ->
                when(actionId){
                    EditorInfo.IME_ACTION_NEXT ->
                    {
                        if(position < maxNext){
                            nextButton.callOnClick()
                        }else{
                            setDetailPriceInput.requestFocus()
                        }
                        true
                    }
                    else -> false
                }
            }

            nextButton.setOnClickListener {
                if(position < maxNext){
                    getDetailName[position] = detailNameInput.text.trim().toString()
                    position++
                    setColorButton(position, nextButton, beforeButton)
                    setDetailText(position, detailName, detailNameInput)
                    detailNameInput.setText(getDetailName[position])
                }
            }

            beforeButton.setOnClickListener {
                if(position > 0){
                    getDetailName[position] = detailNameInput.text.trim().toString()
                    position--
                    setColorButton(position, nextButton, beforeButton)
                    setDetailText(position, detailName, detailNameInput)
                    detailNameInput.setText(getDetailName[position])
                }
            }

            okButton.setOnClickListener {

                getDetailName[position] = detailNameInput.text.toString()
                val allow = postValidation(getDetailName)
                if(allow && setDetailPriceInput.text.isNotEmpty()){
                    if(post){
                        val newDetail = arrayListOf(getDetailName[0], getDetailName[1], "false" ,setDetailPriceInput.text.toString())
                        PropertiesServices.propertyDetails.add(newDetail)
                    }else{
                        val checkBox =  PropertiesServices.propertyDetails[pos][2]
                        val newDetail = arrayListOf(getDetailName[0], getDetailName[1], checkBox ,setDetailPriceInput.text.toString())
                        PropertiesServices.propertyDetails[pos] = newDetail
                    }
                    setPatchPropertyDetail(PropertiesServices.propertyDetails)
                    dialog.dismiss()
                }else{
                    makeMessageToast(R.string.invalide_input)
                }
            }
        }
        dialog.show()
    }

    private fun deleteDetail(pos : Int){
        PropertiesServices.propertyDetails.removeAt(pos)
        setPatchPropertyDetail(PropertiesServices.propertyDetails)
    }

    private fun patchDetailBox(pos : Int, checked : Boolean){
        val tryAgainSetting = theView.findViewById<Button>(R.id.tryAgainSetting)
        tryAgainSetting.setOnClickListener {
            tryAgain(R.string.patch_detail)
            patchDetailBox(pos, checked)
        }
        tryAgain(R.string.patch_detail)

        val oldDetail = PropertiesServices.propertyDetails[pos]
        val newDetail = arrayListOf(oldDetail[0], oldDetail[1], checked.toString(), oldDetail[3])
        PropertiesServices.propertyDetails[pos] = newDetail
        PropertiesServices.patchDetail(globalContext, PropertiesServices.propertyDetails){success ->
            if(!success){
                PropertiesServices.propertyDetails[pos] = oldDetail
                getPropertyDetail(PropertiesServices.actualPropertyId)
                detailAdapter.notifyDataSetChanged()
                failed()
            }else{
                success()
            }
        }
    }


    /*###################################################################*/
    /*###################################################################*/
    /*#######################-- set or get --############################*/
    /*###################################################################*/
    /*###################################################################*/

    private fun getPropertyDetail(id : String){
        val tryAgainSetting = theView.findViewById<Button>(R.id.tryAgainSetting)
        tryAgainSetting.setOnClickListener {
            tryAgain(R.string.get_details)
            getPropertyDetail(id)
        }
        tryAgain(R.string.get_details)

        PropertiesServices.getPropertyDetails(globalContext, id){success ->
            if(success){
                detailAdapter.notifyDataSetChanged()
                val propertyName = theView.findViewById<TextView>(R.id.propertyName)
                propertyName.text = PropertiesServices.actualPropertyName
                success()
            }else{
                Log.d("Failed", "Failed to get details")
                failed()
            }
        }

    }

    private fun getAllProperties(){
        val tryAgainSetting = theView.findViewById<Button>(R.id.tryAgainSetting)
        tryAgainSetting.setOnClickListener {
            tryAgain(R.string.get_properties)
            getAllProperties()
        }
        tryAgain(R.string.get_properties)


        ProductsServices.getProductProperties(globalContext, ProductsServices.productId){success ->
            if(success){
                propertyAdapter.notifyDataSetChanged()
                if(PropertiesServices.actualPropertyId != "") {
                    getPropertyDetail(PropertiesServices.actualPropertyId)
                }else{
                    val propertyName = theView.findViewById<TextView>(R.id.propertyName)
                    propertyName.text = "No Property Selected"
                    PropertiesServices.propertyDetails.clear()
                    success()
                }
            }else{
                Log.d("Failed", "Failed to get productProperties")
                failed()
            }
        }
    }

    private fun setPostProperty(newProperty : property){
        val tryAgainSetting = theView.findViewById<Button>(R.id.tryAgainSetting)
        tryAgainSetting.setOnClickListener {
            tryAgain(R.string.post_property)
            setPostProperty(newProperty)
        }
        tryAgain(R.string.properties)


        PropertiesServices.postProperty(globalContext, newProperty){success ->
            if(success){
                getAllProperties()
            }else{
                Log.d("Failed", "Failed to post new property")
                failed()
            }
        }

    }

    private fun setPatchProperty(newProperty : property){
        val tryAgainSetting = theView.findViewById<Button>(R.id.tryAgainSetting)
        tryAgainSetting.setOnClickListener {
            tryAgain(R.string.patch_property)
            setPatchProperty(newProperty)
        }
        tryAgain(R.string.patch_property)


        PropertiesServices.patchProperty(globalContext, newProperty){success ->
            if(success){
                getAllProperties()
            }else{
                Log.d("Failed", "Failed to patch property")
                failed()
            }
        }
    }

    private fun deleteProperty(id : String){
        val tryAgainSetting = theView.findViewById<Button>(R.id.tryAgainSetting)
        tryAgainSetting.setOnClickListener {
            tryAgain(R.string.delete_property)
            deleteProperty(id)
        }
        tryAgain(R.string.delete_property)


        PropertiesServices.deleteProperty(globalContext, id){success ->
            if(success){
                getAllProperties()
            }else{
                Log.d("Failed", "Failed to delete property")
                failed()
            }
        }
    }


    /*--------------------------------------------------------------------*/

    private fun setPatchPropertyDetail(details : ArrayList<ArrayList<String>>){
        val tryAgainSetting = theView.findViewById<Button>(R.id.tryAgainSetting)
        tryAgainSetting.setOnClickListener {
            tryAgain(R.string.patch_property)
            setPatchPropertyDetail(details)
        }
        tryAgain(R.string.patch_property)


        PropertiesServices.patchDetail(globalContext, details){success ->
            if(success){
               // getPropertyDetail(PropertiesServices.actualPropertyId)
               // detailAdapter.notifyDataSetChanged()
                getAllProperties()
            }else{
                Log.d("Failed", "Failed to patch detail")
                failed()
            }
        }
    }


    /*###################################################################*/
    /*###################################################################*/
    /*#######################-- functions --#############################*/
    /*###################################################################*/
    /*###################################################################*/


    private fun setColorButton(position : Int, nextButton: ImageView, beforButton : ImageView){

        when(position){
            0 -> {
                DrawableCompat.setTint(nextButton.drawable, ContextCompat.getColor(globalContext, R.color.nextButtonActive))
                DrawableCompat.setTint(beforButton.drawable, ContextCompat.getColor(globalContext, R.color.nextButtonDeactivate))
            }
            1 -> {
                DrawableCompat.setTint(nextButton.drawable, ContextCompat.getColor(globalContext, R.color.nextButtonDeactivate))
                DrawableCompat.setTint(beforButton.drawable, ContextCompat.getColor(globalContext, R.color.nextButtonActive))
            }
        }
    }

    private fun  setPropertyText(position : Int, textView : TextView, editText : EditText){

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

        if (info[0].isEmpty()){
            return false
        }

        return true
    }

    private fun postValidation(a1 : ArrayList<String>) : Boolean{
        if( !checkEmpty(a1)) {
            return false
        }
        return true
    }

    private fun makeMessageToast(theMessage : Int){
        val languageArray = resources.getString(theMessage)
        Toast.makeText(globalContext, languageArray, Toast.LENGTH_SHORT).show()
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

}
