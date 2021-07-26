package com.example.restaurantapp.Fragement

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.restaurantapp.Controller.MainActivity

import com.example.restaurantapp.R
import com.example.restaurantapp.Structure.restaurantInfo
import com.example.restaurantapp.services.DataServices
import com.example.restaurantapp.services.RestaurantInfoServices


class SettingFragement : Fragment(), AdapterView.OnItemSelectedListener{

    lateinit var globalContext : Context
    lateinit var theView : View
    lateinit var languageAdapter : ArrayAdapter<CharSequence>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        globalContext = container!!.context
        theView = inflater.inflate(R.layout.fragment_setting_fragement, container, false)

        /*###################################################################*/
        /*###################################################################*/
        /*#######################-- the beginning  --########################*/
        /*###################################################################*/
        /*###################################################################*/

        //resources
        val setRestaurantNameButton = theView.findViewById<Button>(R.id.setRestaurantNameButton)
        val restaurantNameSetting = theView.findViewById<EditText>(R.id.restaurantNameSetting)
        val englishLanguageCheck = theView.findViewById<CheckBox>(R.id.englishLanguageCheck)


        setRestaurantNameButton?.setOnClickListener {
            if(restaurantNameSetting?.text!!.isNotBlank()){
                val name = restaurantNameSetting.text!!.toString()
                val updateResInfo = restaurantInfo("", name, "", "")

                updateRestaurantInfo(updateResInfo)
            }else{
                makeMessageToast(R.string.invalide_input)
            }
        }

        englishLanguageCheck?.setOnClickListener {
            val check = englishLanguageCheck.isChecked.toString()
            val updateResInfo = restaurantInfo("", "", check, "")
            updateRestaurantInfo(updateResInfo)
        }

        //for the spinner is located on onItemSelected
        setPriceUnitSpinner()

        getRestaurantInfo()

        return theView
    }



    private fun setPriceUnitSpinner(){

        val priceUnitSpinner = theView.findViewById<Spinner>(R.id.priceUnitSpinner)
        languageAdapter = ArrayAdapter.createFromResource(globalContext, R.array.price_unit , R.layout.support_simple_spinner_dropdown_item)
        languageAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)

        priceUnitSpinner?.adapter = languageAdapter
        when(DataServices.productPriceUnit){
            "MAD" -> priceUnitSpinner?.setSelection(0)
            "USD" -> priceUnitSpinner?.setSelection(1)
            "EUR" -> priceUnitSpinner?.setSelection(2)
        }
        priceUnitSpinner?.onItemSelectedListener = this
    }



    /*###################################################################*/
    /*###################################################################*/
    /*####################--   server management  --#####################*/
    /*###################################################################*/
    /*###################################################################*/

    private fun getRestaurantInfo(){
        val tryAgainSetting = theView.findViewById<Button>(R.id.tryAgainSetting)
        tryAgainSetting.setOnClickListener {
            tryAgain(R.string.get_restaurant_info)
            getRestaurantInfo()
        }
        tryAgain(R.string.get_restaurant_info)

        RestaurantInfoServices.getRestaurantInfo(globalContext){success ->
            if(success){
                updateInfo()
                success()
            }else{
                Log.d("Failed", "failed to get info")
                failed()
            }
        }


    }



    private fun updateRestaurantInfo(theInfo : restaurantInfo){
        val tryAgainSetting = theView.findViewById<Button>(R.id.tryAgainSetting)
        tryAgainSetting.setOnClickListener {
            tryAgain(R.string.post_restaurant_info)
            updateRestaurantInfo(theInfo)
        }
        tryAgain(R.string.post_restaurant_info)

        RestaurantInfoServices.patchRestaurantInfo(globalContext, theInfo){success ->
            if(success){
                Log.d("Patch", "success")
                getRestaurantInfo()
            }else{
                Log.d("Failed", "failed to update info")
                failed()
            }
        }


    }

    /*###################################################################*/
    /*###################################################################*/
    /*####################--      functions       --#####################*/
    /*###################################################################*/
    /*###################################################################*/

    private fun updateInfo(){

        if(activity != null){
            val restaurantName = (activity as MainActivity).findViewById<TextView>(R.id.RestorantName)
            restaurantName.text = RestaurantInfoServices.restaurantInfo.name
        }

        val textEditor = theView.findViewById<EditText>(R.id.restaurantNameSetting)
        textEditor?.setText(RestaurantInfoServices.restaurantInfo.name)

        val englishLanguageCheck = theView.findViewById<CheckBox>(R.id.englishLanguageCheck)
        englishLanguageCheck.isChecked = RestaurantInfoServices.restaurantInfo.english.toBoolean()

        DataServices.productPriceUnit = RestaurantInfoServices.restaurantInfo.priceUnit
        DataServices.restaurantName = RestaurantInfoServices.restaurantInfo.name
        DataServices.englishLanguage = RestaurantInfoServices.restaurantInfo.english.toBoolean()
    }

    private fun onSpinnerClicked(action : String){
        val updateResInfo = restaurantInfo("", "", "", action)
        updateRestaurantInfo(updateResInfo)
    }

    private fun makeMessageToast(theMessage : Int){
        val languageArray = resources.getString(theMessage)
        Toast.makeText(globalContext, languageArray, Toast.LENGTH_SHORT).show()
    }

    /*###################################################################*/
    /*###################################################################*/
    /*####################--       other          --#####################*/
    /*###################################################################*/
    /*###################################################################*/

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val priceUnit = parent?.getItemAtPosition(position).toString()
        onSpinnerClicked(priceUnit)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }


    private fun failed(){
        val progressBarSetting = theView.findViewById<ProgressBar>(R.id.progressBarSetting)
        val tryAgainSetting = theView.findViewById<Button>(R.id.tryAgainSetting)

        progressBarSetting.visibility = View.INVISIBLE
        tryAgainSetting.visibility = View.VISIBLE
    }

    private fun tryAgain(message : Int){
        val waitForProcess = theView.findViewById<ConstraintLayout>(R.id.waitForProcess)
        val stepProgressSetting = theView.findViewById<TextView>(R.id.stepProgressSetting)
        val progressBarSetting = theView.findViewById<ProgressBar>(R.id.progressBarSetting)
        val tryAgainSetting = theView.findViewById<Button>(R.id.tryAgainSetting)

        waitForProcess.visibility = View.VISIBLE
        progressBarSetting.visibility = View.VISIBLE
        tryAgainSetting.visibility = View.INVISIBLE
        stepProgressSetting.text = resources.getString(message)
    }

    private fun success(){
        val waitForProcess = theView.findViewById<ConstraintLayout>(R.id.waitForProcess)
        val progressBarSetting = theView.findViewById<ProgressBar>(R.id.progressBarSetting)
        val tryAgainSetting = theView.findViewById<Button>(R.id.tryAgainSetting)

        waitForProcess.visibility = View.INVISIBLE
        progressBarSetting.visibility = View.VISIBLE
        tryAgainSetting.visibility = View.INVISIBLE
    }


}
