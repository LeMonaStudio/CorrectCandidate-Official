package com.thenativecitizens.correctcandidate_recruiter.ui.auth


import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.thenativecitizens.correctcandidate_recruiter.R
import com.thenativecitizens.correctcandidate_recruiter.databinding.FragmentUserTypeBinding
import com.thenativecitizens.correctcandidate_recruiter.util.Candidate
import com.thenativecitizens.correctcandidate_recruiter.util.Recruiter
import com.thenativecitizens.correctcandidate_recruiter.util.User
import com.thenativecitizens.correctcandidate_recruiter.util.UserType

class UserTypeFragment : Fragment() {

    private lateinit var binding: FragmentUserTypeBinding
    private lateinit var userTypeViewModel: UserTypeViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabaseRef: DatabaseReference


    private var userTypeSelected = UserType.UNDECIDED
    private var colorUnSelected: Int = 0

    /**
     * onStart function override
     */
    override fun onStart() {
        super.onStart()

        //If night mode is activated by the user
        when(requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)){
            Configuration.UI_MODE_NIGHT_YES -> {
                colorUnSelected = ContextCompat.getColor(requireContext(), R.color.black_shade)
            }
        }
    }

    /**
     * onCreateView
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user_type, container, false)

        //ViewModel
        userTypeViewModel = ViewModelProvider(this).get(UserTypeViewModel::class.java)

        //Authentication
        auth = Firebase.auth
        if(auth.currentUser == null)
        //Navigate User back to login
            findNavController().navigate(UserTypeFragmentDirections.actionUserTypeFragmentToLoginFragment())

        //Firebase Database Reference
        firebaseDatabaseRef = Firebase.database.reference

        //NEXT button should be enabled or disabled
        //based on the current or default value of the UserType Selected
        enableDisableNextBtn()

        //Tell the Layout about the ViewModel and the UserTypes
        binding.userTypeViewModel = userTypeViewModel

        //Observe for the selected UserType
        userTypeViewModel.userTypeSelected.observe(viewLifecycleOwner){
            this.userTypeSelected = it
            updateUserTypeSelectionUI(it)
        }

        //Initialize the CardView's unselected and title text colors
        colorUnSelected = ContextCompat.getColor(requireContext(), R.color.white)


        //When the Next Button is Clicked
        //Add User to database and show a dialog while at it
        binding.nextBtn.setOnClickListener {
            //Disable all click-able UI element
            disableUI()
            when(this.userTypeSelected){
                UserType.RECRUITER -> {
                    //Insert new recruiter into the database
                    insertNewRecruiter()
                }
                UserType.CANDIDATE ->{
                    //Insert new candidate into the database
                    insertNewCandidate()
                }
                else ->{
                    Snackbar.make(binding.root, "Please, make a selection first.", Snackbar.LENGTH_SHORT).show()
                    //enable the UI again
                    enableUI()
                }
            }
        }


        return binding.root
    }

    //Disable UI element when an operation is ongoing
    private fun disableUI(){
        binding.apply {
            //disable the next button
            nextBtn.isEnabled = false
            //and the selectable card views
            candidate.isEnabled = false
            recruiter.isEnabled = false
            //show the progress bar
            progress.visibility = View.VISIBLE
        }
    }

    //Enable UI elements if no operation is active or has failed
    private fun enableUI(){
        binding.apply {
            //enable the selectable card views
            candidate.isEnabled = true
            recruiter.isEnabled = true
            //hide the progress bar
            progress.visibility = View.GONE
        }
        //decide if the next button should be enabled or not
        enableDisableNextBtn()
    }


    //Updates the UI as user chooses UserType
    private fun updateUserTypeSelectionUI(userType: UserType) {
        val colorSelected = ContextCompat.getColor(requireContext(), R.color.color_secondary)
        when(userType){
            UserType.CANDIDATE -> {
                binding.apply {
                    candidate.apply {
                        setCardBackgroundColor(colorSelected)
                        cardElevation = 16.dpToPX()
                    }
                    recruiter.apply{
                        setCardBackgroundColor(colorUnSelected)
                        cardElevation = 4.dpToPX()
                    }
                }
            }
            UserType.RECRUITER -> {
                binding.apply {
                    candidate.apply {
                        setCardBackgroundColor(colorUnSelected)
                        cardElevation = 4.dpToPX()
                    }
                    recruiter.apply{
                        setCardBackgroundColor(colorSelected)
                        cardElevation = 16.dpToPX()
                    }
                }
            }
            else -> {
                binding.apply {
                    candidate.apply {
                        setCardBackgroundColor(colorUnSelected)
                        cardElevation = 4.dpToPX()
                    }
                    recruiter.apply{
                        setCardBackgroundColor(colorUnSelected)
                        cardElevation = 4.dpToPX()
                    }
                }
            }
        }
        //Call to enable or disable the NEXT button
        enableDisableNextBtn()
    }


    //This is called to decide if the NEXT button should be enabled or disabled
    private fun enableDisableNextBtn() {
       binding.nextBtn.isEnabled = userTypeSelected != UserType.UNDECIDED
    }

    //helper method to convert DP(Density Independent Pixel) to Pixels
    private fun Int.dpToPX(): Float{
        return this * requireContext().resources.displayMetrics.density
    }


    //Inserts new Candidate into the database
    private fun insertNewCandidate() {
        auth.currentUser?.let {firebaseUser ->
            val newCandidate = Candidate(firebaseUser.uid, firebaseUser.email, 0)
            firebaseDatabaseRef.child("users").child("candidates").child(firebaseUser.uid).setValue(newCandidate)
                .addOnSuccessListener {
                    //Candidate was successfully added to the Candidate database
                    //Now add user to the general database
                    val user  = User(firebaseUser.uid, 1)
                    firebaseDatabaseRef.child("allUsers").child(firebaseUser.uid).setValue(user)
                        .addOnSuccessListener {
                            //Move User to Candidate Home screen
                            findNavController().navigate(UserTypeFragmentDirections.actionUserTypeFragmentToHomeFragmentCandidate())
                        }
                        .addOnFailureListener {
                            //Operation failed and new Candidate was not added to the database
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

    //Inserts new Recruiter into the database
    private fun insertNewRecruiter(){
        auth.currentUser?.let { firebaseUser ->
            //Add New Recruiter to the Recruiters Database
            val newRecruiter = Recruiter(firebaseUser.uid, "",
                    "", "", firebaseUser.email, 0)
            firebaseDatabaseRef.child("users").child("recruiters").child(firebaseUser.uid).setValue(newRecruiter)
                    .addOnSuccessListener {
                        //Recruiter was successfully added to the Recruiter database
                        //Now add user to the general database
                        val user  = User(firebaseUser.uid, 2)
                        firebaseDatabaseRef.child("allUsers").child(firebaseUser.uid).setValue(user)
                                .addOnSuccessListener {
                                    //Move User to Recruiter Home screen
                                    findNavController().navigate(UserTypeFragmentDirections
                                            .actionUserTypeFragmentToHomeFragmentRecruiter())
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