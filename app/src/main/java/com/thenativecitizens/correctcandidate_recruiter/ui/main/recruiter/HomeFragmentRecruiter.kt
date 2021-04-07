package com.thenativecitizens.correctcandidate_recruiter.ui.main.recruiter

import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.thenativecitizens.correctcandidate_recruiter.R
import com.thenativecitizens.correctcandidate_recruiter.databinding.HomeFragmentRecruiterBinding
import com.thenativecitizens.correctcandidate_recruiter.util.Recruiter

class HomeFragmentRecruiter : Fragment() {
    //Binding
    private lateinit var binding: HomeFragmentRecruiterBinding

    //Authentication
    private lateinit var auth: FirebaseAuth

    //Firebase Database root reference
    private lateinit var firebaseDatabaseRef: DatabaseReference

    //sharedPreference
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var recruiterViewModel: HomeRecruiterViewModel

    //Key to receive callback from the Fragment
    private var brandDetailsKey = "BRAND DETAILS KEY"


    /**
     * onCreateView()
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.home_fragment_recruiter, container, false)

        //Initialize Auth
        auth = Firebase.auth
        with(auth.currentUser){
            if (this == null){
                //Navigate back to Login Fragment
                findNavController().navigate(HomeFragmentRecruiterDirections.actionHomeFragmentRecruiterToLoginFragment())
            }
        }

        //Initialize Firebase database root reference
        firebaseDatabaseRef = Firebase.database.reference

        //ViewModel initialized
        recruiterViewModel = ViewModelProvider(this).get(HomeRecruiterViewModel::class.java)

        //SharePreference initialized
        sharedPreferences = requireContext()
                .getSharedPreferences("com.thenativecitizens.correctcandidate_recruiter", AppCompatActivity.MODE_PRIVATE)
        //Persist UserType for easy navigation from SplashFragment
        //without having to Login every time
        sharedPreferences.edit().putInt("UserType", 2).apply()

        //Check if Recruiter's registration is completed
        if(!sharedPreferences.getBoolean("isRecruiterRegCompleted", false)){
            //disable all UI
            disableUI()
            //This means Recruiter may not have completed registration
            //Call the function to set the Actual registration status
            confirmRegCompleted()
        }

        /**
         * Fragment Result Listeners
         */
        parentFragmentManager.setFragmentResultListener(
                brandDetailsKey,
                viewLifecycleOwner,
                {requestKey, _ ->
                    if(requestKey == brandDetailsKey){
                        //This is called because the Recruiter has completed their details
                        //i.e provided CompanyName, CompanyAboutUs and CompanyLogo
                        //Disable all UI element
                        disableUI()
                        //Save the completed status in sharedPreference
                        sharedPreferences.edit().putBoolean("isRecruiterRegCompleted", true).apply()
                        //enable all UI elements, and navigate to QuestionDetailsFragment()
                        //to create a New Aptitude Test
                        enableUI()
                        findNavController().navigate(HomeFragmentRecruiterDirections
                                .actionHomeFragmentRecruiterToQuestionDetailsFragment())
                    }
                }
        )

        //When the FAB is clicked
        binding.newAptitudeTestBtn.setOnClickListener {
            //Check if the Recruiter has a completed profile
            if (sharedPreferences.getBoolean("isRecruiterRegCompleted", false))
                findNavController().navigate(HomeFragmentRecruiterDirections.actionHomeFragmentRecruiterToQuestionDetailsFragment())
            else
                findNavController().navigate(HomeFragmentRecruiterDirections.actionHomeFragmentRecruiterToBrandDetailsFragment())
        }

        return binding.root
    }

    //Call to disable all UI elements while an operation is active
    private fun disableUI() {
        binding.newAptitudeTestBtn.isEnabled = false
        binding.progressContainer.visibility = View.VISIBLE
    }

    //Call to enable all UI elements when active operation ends
    private fun enableUI(){
        binding.newAptitudeTestBtn.isEnabled = true
        binding.progressContainer.visibility = View.GONE
    }

    private fun confirmRegCompleted() {
        auth.currentUser?.let {
            firebaseDatabaseRef.child("users").child("recruiters").child(it.uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val recruiter = snapshot.getValue<Recruiter>()
                            if (!recruiter?.companyName.isNullOrEmpty() && !recruiter?.companyLogoUri.isNullOrEmpty()){
                                //This means the Recruiter must have
                                //provided a company name and logo before
                                sharedPreferences.edit().putBoolean("isRecruiterRegCompleted", true).apply()
                            }
                            enableUI()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            enableUI()
                        }
                    })
        }
    }

}