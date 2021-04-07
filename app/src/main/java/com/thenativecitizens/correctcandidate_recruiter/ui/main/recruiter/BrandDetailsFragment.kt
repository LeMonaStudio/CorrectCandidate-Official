package com.thenativecitizens.correctcandidate_recruiter.ui.main.recruiter

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.thenativecitizens.correctcandidate_recruiter.R
import com.thenativecitizens.correctcandidate_recruiter.databinding.DialogBrandDetailsBinding
import com.thenativecitizens.correctcandidate_recruiter.util.ConnectionTimeOut
import com.thenativecitizens.correctcandidate_recruiter.util.Recruiter
import com.thenativecitizens.correctcandidate_recruiter.util.User


class BrandDetailsFragment : Fragment() {
    private lateinit var binding: DialogBrandDetailsBinding

    private var readyCompanyName: String = ""
    private var readyCompanyLogoUrlString: String = ""
    private var readyCompanyAboutUs: String = ""
    private var localFileLogoUri: Uri? = null

    //Result Launcher to pick image from the user's device
    private lateinit var pickImageContract: ActivityResultLauncher<String?>

    //ConnectionTimeOut ViewModel
    private lateinit var connectionTimeOut: ConnectionTimeOut

    //Currently SignedIn User
    private lateinit var auth: FirebaseAuth

    //Firebase Database Reference
    private lateinit var firebaseDatabaseRef: DatabaseReference

    //Key to receive callback from the Fragment
    private var brandDetailsKey = "BRAND DETAILS KEY"


    /**
     * OnCreateView
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_brand_details, container, false)


        //Authentication
        auth = Firebase.auth

        //Firebase Database Reference
        firebaseDatabaseRef = Firebase.database.reference

        //Attach all TextWatchers
        attachTextWatchers()

        //ConnectionTimeOut ViewModel
        connectionTimeOut = ViewModelProvider(this).get(ConnectionTimeOut::class.java)

        //Observing for Connection Time Out
        connectionTimeOut.timeOut.observe(viewLifecycleOwner){
            it?.let {
                //The timer counts down for 15 seconds
                when(it){
                    1 -> {
                        Snackbar.make(binding.root, "Connection timeout! Please check your internet connection and try again.",
                                Snackbar.LENGTH_LONG).show()
                        //handle the upload progress indicator
                        binding.uploadProgress.apply {
                            visibility = View.GONE
                            progress = 0
                        }
                        //enable the UI elements
                        enableUI()
                    }
                }
            }
        }


        //initialize the ImagePicker Result launcher
        //To pick the Image from user's device
        pickImageContract = registerForActivityResult(ActivityResultContracts.GetContent()) {
            it?.let{
                //Save the URI of the chosen file for later upload operation
                localFileLogoUri = it
                //Upload to FirebaseStorage
                uploadLogoToStorage()
                //TODO("Check the size of the file if it's not more than 100kb before uploading it to the file storage")
            }
        }

        //When user chooses to upload logo from device
        binding.uploadCompanyLogo.setOnClickListener {
            pickImageContract.launch("image/*")
        }

        //When the FinishSignUp Button is clicked
        binding.finishBtn.setOnClickListener {
            //disable all UI elements
            disableUI()
            //Update the Recruiter's details
            updateRecruiter()
        }


        return binding.root
    }


    //Called to load the Image/logo into an ImageView using Glide
    private fun loadImageIntoImageView(uri: Uri) {
        Glide.with(requireContext())
                .load(uri)
                .apply(RequestOptions()
                        .placeholder(R.drawable.ic_image_loading)
                        .error(R.drawable.ic_image_loading_error))
                .into(binding.companyLogoView)
    }

    private fun attachTextWatchers() {
        //Company Name TextWatcher for text changes
        binding.companyName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //Decide if the FinishUp button should be enabled or disabled
                enableDisableFinishBtn()
            }
            override fun afterTextChanged(companyName: Editable?) {
                when (companyName.toString().companyNameIsNotShort()) {
                    true -> {
                        //Company name is not too short
                        binding.companyNameContainer.apply {
                            isErrorEnabled = false
                            error = null
                        }
                        readyCompanyName = companyName.toString()
                    }
                    false -> {
                        //company name is too short
                        binding.companyNameContainer.apply {
                            isErrorEnabled = true
                            error = "The name is too short."
                        }
                        readyCompanyName = ""
                    }
                }
                enableDisableFinishBtn()
            }
        })
        //CompanyAboutUs TextWatcher for text changes
        binding.companyAboutUs.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //Decide if the FinishUp button should be enabled or disabled
                enableDisableFinishBtn()
            }
            override fun afterTextChanged(aboutUsText: Editable?) {
                if(aboutUsText.toString().companyAboutUsIsNotShort()){
                    binding.companyAboutUsContainer.apply {
                        isErrorEnabled = false
                        error = null
                    }
                    readyCompanyAboutUs = aboutUsText.toString()
                } else{
                    binding.companyAboutUsContainer.apply {
                        isErrorEnabled = true
                        error = "About Us text is too short"
                    }
                    readyCompanyAboutUs = ""
                }
                //Decide if the FinishUp button should be enabled or disabled
                enableDisableFinishBtn()
            }
        })
    }


    //Checks if the URI is valid
    private fun CharSequence.isValidUri(): Boolean {
        return this.isNotEmpty() && Patterns.WEB_URL.matcher(this).matches()
    }
    //function to check if company name is not too short
    private fun CharSequence.companyNameIsNotShort(): Boolean {
        return this.isNotEmpty() && this.length >= 4
    }
    //function to check if Company AboutUs us not too short
    private fun CharSequence.companyAboutUsIsNotShort(): Boolean {
        return this.isNotEmpty() && this.length >= 50
    }
    //this function will enable or disable the FinishSignUp button accordingly
    private fun enableDisableFinishBtn() {
        binding.finishBtn.isEnabled = readyCompanyName.companyNameIsNotShort() &&
                readyCompanyAboutUs.companyAboutUsIsNotShort() && readyCompanyLogoUrlString.isValidUri()
    }




    //Uploads a device-chosen logo to the Firebase storage
    private fun uploadLogoToStorage() {
        val currentUser = auth.currentUser
        //disable all clickable UI elements
        disableUI()
        //begin connection timeout countdown for 20 secs
        connectionTimeOut.beginTimeOutCountDown(20_000)
        //Get an instance of the firebase storage
        val storageRef: StorageReference = Firebase.storage.reference
        //upload the file to Firebase Storage using the URI of the chosen file
        localFileLogoUri?.let {theLocalUri ->
            if (currentUser != null){
                //Create a storage path with the companyLogoUID created earlier
                val companyLogoRef = storageRef.child("logos/${currentUser.uid}")
                //Upload to storage with the Uri
                companyLogoRef.putFile(theLocalUri)
                        .addOnProgressListener {
                            val currentProgress = ((100.0*it.bytesTransferred)/it.totalByteCount).toInt()
                            binding.uploadProgress.apply {
                                visibility = View.VISIBLE
                                progress = currentProgress
                            }
                            binding.uploadProgressText.apply {
                                this.visibility = View.VISIBLE
                                //val progressText = getString(R.string.upload_progress_text, currentProgress.toString(), "%")
                                text = getString(R.string.upload_progress_text, currentProgress.toString(), "%")
                            }
                        }
                        .addOnSuccessListener {
                            //If connection has not timed out in the process
                            //of trying to upload
                            if (!connectionTimeOut.getIsConnectionTimeOutStat()) {
                                //Cancel the connection timeout countdown
                                connectionTimeOut.cancelTimeOutCountDown()
                                //Get the download Url from Firebase storage
                                companyLogoRef.downloadUrl.addOnSuccessListener {downloadUri ->
                                    //Save the Uri
                                    readyCompanyLogoUrlString = downloadUri.toString()
                                    //handle the horizontal progress
                                    binding.uploadProgress.apply {
                                        visibility = View.GONE
                                        progress = 0
                                    }
                                    binding.uploadProgressText.apply {
                                        this.visibility = View.GONE
                                        text = "0"
                                    }
                                    //load the logo into the ImageView
                                    loadImageIntoImageView(theLocalUri)
                                    //enable all the disabled UI element
                                    enableUI()
                                }
                            }
                        }
                        .addOnFailureListener {
                            if (!connectionTimeOut.getIsConnectionTimeOutStat()) {
                                //Cancel the connection time out countdown
                                connectionTimeOut.cancelTimeOutCountDown()
                                Snackbar.make(binding.root, "Error! Check your internet connection and try again",
                                        Snackbar.LENGTH_LONG).show()
                                //Handle the upload progress indicator
                                binding.uploadProgress.apply {
                                    visibility = View.GONE
                                    progress = 0
                                }
                                //enable all the disabled UI element
                                enableUI()
                            }
                        }
            }
        }
    }

    //To disable all clickable UI elements while an operation is ongoing
    private fun disableUI() {
        binding.apply {
            progress.visibility = View.VISIBLE
            binding.companyNameContainer.apply {
                with(this){
                    isEnabled = false
                    alpha = 0.5f
                }
            }
            binding.companyAboutUsContainer.apply {
                with(this){
                    isEnabled = false
                    alpha = 0.5f
                }
            }
            binding.uploadCompanyLogo.isEnabled = false
            binding.finishBtn.isEnabled = false
        }
    }

    //To enable all clickable UI elements when an operation is done
    private fun enableUI(){
        binding.apply {
            progress.visibility = View.GONE
            binding.companyNameContainer.apply {
                with(this){
                    isEnabled = true
                    alpha = 1f
                }
            }
            binding.companyAboutUsContainer.apply {
                with(this){
                    isEnabled = true
                    alpha = 1f
                }
            }
            binding.uploadCompanyLogo.isEnabled = true
            binding.finishBtn.isEnabled = true
        }
        //Decide if the FinishUp button should be enabled or disabled
        enableDisableFinishBtn()
    }

    //Updates Recruiter details in the database
    private fun updateRecruiter(){
        auth.currentUser?.let { firebaseUser ->
            //Add New Recruiter to the Recruiters Database
            val newRecruiter = Recruiter(firebaseUser.uid, readyCompanyName,
                    readyCompanyLogoUrlString, readyCompanyAboutUs, firebaseUser.email, 0)
            firebaseDatabaseRef.child("users").child("recruiters").child(firebaseUser.uid).setValue(newRecruiter)
                    .addOnSuccessListener {
                        //Recruiter was successfully added to the Recruiter database
                        //Now add user to the general database
                        val user  = User(firebaseUser.uid, 2)
                        firebaseDatabaseRef.child("allUsers").child(firebaseUser.uid).setValue(user)
                                .addOnSuccessListener {
                                    //Tell the HomeFragment this operation has been successfully completed
                                    parentFragmentManager.setFragmentResult(brandDetailsKey, Bundle())
                                    //Move User to Recruiter Home screen
                                    findNavController().navigate(BrandDetailsFragmentDirections.actionBrandDetailsFragmentToHomeFragmentRecruiter())
                                }
                                .addOnFailureListener {
                                    //Operation failed and new Recruiter was not added to the database
                                    Snackbar.make(binding.root, "ERROR! This operation has failed. Check your internet and try again.",
                                            Snackbar.LENGTH_SHORT).show()
                                    enableUI()
                                }
                    }
                    .addOnFailureListener {
                        //Operation failed and new Candidate was not added to the database
                        Snackbar.make(binding.root, "ERROR! This operation has failed. Check your internet and try again.",
                                Snackbar.LENGTH_SHORT).show()
                        enableUI()
                    }
        }
    }
}

