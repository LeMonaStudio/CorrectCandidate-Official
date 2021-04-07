package com.thenativecitizens.correctcandidate_recruiter.ui.main.candidate

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.thenativecitizens.correctcandidate_recruiter.R
import com.thenativecitizens.correctcandidate_recruiter.databinding.HomeFragmentCandidateBinding

class HomeFragmentCandidate : Fragment() {

    private lateinit var binding: HomeFragmentCandidateBinding
    private lateinit var candidateViewModel: HomeCandidateViewModel
    //SharePreference
    private lateinit var sharedPreferences: SharedPreferences

    //Authentication
    private lateinit var auth: FirebaseAuth

    /**
     * onCreateView()
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        //Inflate the layout
        binding = DataBindingUtil.inflate(inflater, R.layout.home_fragment_candidate, container, false)

        //Initialize Auth
        auth = Firebase.auth
        with(auth.currentUser){
            if (this == null){
                //Navigate back to Login Fragment
                findNavController().navigate(HomeFragmentCandidateDirections.actionHomeFragmentCandidateToLoginFragment())
            }
        }

        //ViewModel initialized
        candidateViewModel = ViewModelProvider(this).get(HomeCandidateViewModel::class.java)

        //SharePreference initialized
        sharedPreferences = requireContext()
                .getSharedPreferences("com.thenativecitizens.correctcandidate_recruiter", AppCompatActivity.MODE_PRIVATE)
        //Persist UserType for easy navigation from SplashFragment
        //without having to Login every time
        sharedPreferences.edit().putInt("UserType", 1).apply()



        return binding.root
    }

}