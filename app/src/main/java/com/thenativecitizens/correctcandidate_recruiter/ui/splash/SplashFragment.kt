package com.thenativecitizens.correctcandidate_recruiter.ui.splash

import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.thenativecitizens.correctcandidate_recruiter.R
import com.thenativecitizens.correctcandidate_recruiter.databinding.FragmentSplashBinding
import com.thenativecitizens.correctcandidate_recruiter.util.UserType

class SplashFragment : Fragment() {

    private lateinit var viewModel: SplashViewModel
    private lateinit var binding: FragmentSplashBinding
    private lateinit var auth: FirebaseAuth

    //SharePreference
    private lateinit var sharedPreferences: SharedPreferences


    override fun onStart() {
        super.onStart()
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        var splashBgColor = R.color.color_primary

        //If night mode is activated by the user
        when(requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)){
            Configuration.UI_MODE_NIGHT_YES -> {
                splashBgColor = R.color.color_primary_dark
            }
        }

        //Set the NavigationBarColor
        binding.root.setBackgroundColor(ContextCompat.getColor(requireContext(), splashBgColor))
    }


    /**
     * onCreateView()
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_splash, container, false)

        viewModel = ViewModelProvider(this).get(SplashViewModel::class.java)

        auth = FirebaseAuth.getInstance()

        viewModel.onSplashCompleted.observe(viewLifecycleOwner){ isSplashCompleted ->
            if(isSplashCompleted){
                updateUI(auth.currentUser)
            }
        }

        //SharePreference initialized
        sharedPreferences = requireContext()
                .getSharedPreferences("com.thenativecitizens.correctcandidate_recruiter", AppCompatActivity.MODE_PRIVATE)


        return binding.root
    }

    //Updates the UI after successful login or failure
    private fun updateUI(currentUser: FirebaseUser?) {
        if(currentUser != null){
            //User is signed in
            when(checkUserType()){
                UserType.RECRUITER -> {
                    //The user is signed in as a recruiter, move user to HomeFragment()
                    findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToHomeFragmentRecruiter())
                }
                UserType.CANDIDATE -> {
                    //The User is signed in as a Candidate
                    findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToHomeFragmentCandidate())
                }
                else -> {
                    //User most definitely did not finish sign up
                    //Sign the User out
                    auth.signOut()
                    //Navigate to login
                    findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToLoginFragment())
                }
            }
        } else{
            //No Signed in user
            findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToLoginFragment())
        }
    }


    //Check the type of User that is trying to Login to the App to decide where they go
    private fun checkUserType(): UserType {
        return when(sharedPreferences.getInt("UserType", 0)){
            0 -> UserType.UNDECIDED
            1 -> UserType.CANDIDATE
            2 -> UserType.RECRUITER
            else -> UserType.UNDECIDED
        }
    }


    //Remove the Fullscreen flags on destroy
    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }
}