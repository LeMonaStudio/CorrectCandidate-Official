package com.thenativecitizens.correctcandidate_recruiter.ui.auth

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.thenativecitizens.correctcandidate_recruiter.R
import com.thenativecitizens.correctcandidate_recruiter.databinding.FragmentLoginBinding
import com.thenativecitizens.correctcandidate_recruiter.util.ConnectionTimeOut
import com.thenativecitizens.correctcandidate_recruiter.util.User
import com.thenativecitizens.correctcandidate_recruiter.util.UserType


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    //Connection countdown viewModel
    private lateinit var connectionTimeOut: ConnectionTimeOut
    //Auth
    private lateinit var auth: FirebaseAuth
    //Firebase Database Reference
    private lateinit var firebaseDatabaseRef: DatabaseReference

    private var readyEmail = ""
    private var readyPassword = ""

    private var validPasswordIcon: Drawable? = null
    private var waitingPasswordIcon: Drawable? = null


    /**
     * OnStart function
     */
    override fun onStart() {
        super.onStart()
        //Set the Password visibility or non_visibility icon
        var isPasswordVisible = false
        binding.passwordVisibilityBtn.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_password_visibility_on)
        binding.password.transformationMethod = PasswordTransformationMethod()

        binding.passwordVisibilityBtn.setOnClickListener {
            when(isPasswordVisible){
                false -> {
                    isPasswordVisible = true
                    binding.passwordVisibilityBtn.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_password_visibility_off)
                    binding.password.transformationMethod = HideReturnsTransformationMethod()
                }
                true -> {
                    isPasswordVisible = false
                    binding.passwordVisibilityBtn.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_password_visibility_on)
                    binding.password.transformationMethod = PasswordTransformationMethod()
                }
            }
        }
    }

    /**
     * OnCreateView function
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)

        connectionTimeOut = ViewModelProvider(this).get(ConnectionTimeOut::class.java)

        //Initialize Firebase Auth
        auth = Firebase.auth

        //Initialize Firebase Database Reference
        firebaseDatabaseRef = Firebase.database.reference

        //Initialized all edit text field's TextWatchers
        initTextWatchers()

        //Navigate to SignUpFragment
        binding.signUpBtn.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToSignUpFragment())
        }

        //Observing for Connection Time Out
        connectionTimeOut.timeOut.observe(viewLifecycleOwner){
            it?.let {
                //The timer counts down for 15 seconds
                when(it){
                    1 -> {
                        Snackbar.make(binding.root, "Connection timeout! Please check your internet connection and try again.",
                            Snackbar.LENGTH_LONG).show()
                        //enable the UI elements
                        enableUI()
                    }
                }
            }
        }

        //This drawables will let the User know if the password entered
        //is valid i.e its not less or more than 8 characters
        validPasswordIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_check).apply {
            this?.setTint(ContextCompat.getColor(requireContext(), R.color.valid_color))
        }
        waitingPasswordIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_awaiting_validation)


        //When the SignIn button is clicked
        binding.signInBtn.setOnClickListener {
            //disable all clickable UI element
            disableUI()
            //begin Connection TimeOut count down for 20 secs
            connectionTimeOut.beginTimeOutCountDown(20_000)
            //Begin SignIn operation
            signIn()
        }


        return binding.root
    }

    //function called to sign-in an existing user
    private fun signIn(){
        Firebase.auth.signInWithEmailAndPassword(readyEmail, readyPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        //check if the connection was not timed out before
                        //successful signing in
                        if (!connectionTimeOut.getIsConnectionTimeOutStat()){
                            //Connection has not timeout
                            //Cancel the connection timeout countdown
                            connectionTimeOut.cancelTimeOutCountDown()
                            //Update the UI nad navigate
                            updateUI()
                        }
                        //If connection had timeout, a UI would
                        //have been displayed to the user, so just sign user out
                        //and enable the disabled UI
                        else{
                            //Sign out the currently signed in user
                            //so that user can try again
                            auth.signOut()
                            //enable the disable UI element
                            enableUI()
                        }
                    } else{
                        //Task was not successful
                        //display possible error to the user
                        displayPossibleError(task)
                        //enable all disabled UI element
                        enableUI()
                    }
                }
    }

    //To disable all clickable UI elements while an operation is ongoing
    private fun disableUI() {
        binding.apply {
            progress.visibility = View.VISIBLE
            binding.usernameContainer.apply {
                with(this){
                    isEnabled = false
                    alpha = 0.5f
                }
            }
            binding.passwordContainer.apply {
                with(this){
                    isEnabled = false
                    alpha = 0.5f
                }
            }
            binding.passwordVisibilityBtn.isEnabled = false
            binding.signInBtn.isEnabled = false
            binding.forgotPasswordBtn.isEnabled = false
            binding.signUpBtn.isEnabled = false
        }
    }

    //To enable all clickable UI elements when an operation is done
    private fun enableUI(){
        binding.apply {
            progress.visibility = View.GONE
            binding.usernameContainer.apply {
                with(this){
                    isEnabled = true
                    alpha = 1f
                }
            }
            binding.passwordContainer.apply {
                with(this){
                    isEnabled = true
                    alpha = 1f
                }
            }
            binding.passwordVisibilityBtn.isEnabled = true
            binding.signInBtn.isEnabled = true
            binding.forgotPasswordBtn.isEnabled = true
            binding.signUpBtn.isEnabled = true
        }
        //Decide if the Sign in button should be enabled or not
        enableDisableSignInBtn()
    }

    private fun initTextWatchers() {
        //TextWatcher for the Email
        binding.username.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(email: CharSequence?, start: Int, before: Int, count: Int) {
                when(email?.isValidEmail()){
                    true -> {
                        binding.usernameContainer.error = null
                        binding.usernameContainer.isErrorEnabled = false
                    }
                    false -> {
                        binding.usernameContainer.isErrorEnabled = true
                        binding.usernameContainer.error = getString(R.string.email_validator_error_text)
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {
                val validEmailChar: CharSequence = s.toString()
                readyEmail = if (validEmailChar.isValidEmail()) s.toString() else ""
                //enable or disable the Login/SignUp Buttons
                enableDisableSignInBtn()
            }
        })

        //password TextChange
        binding.password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(password: CharSequence?, start: Int, before: Int, count: Int) {
                enableDisableSignInBtn()
            }
            override fun afterTextChanged(s: Editable?) {
                val validPassword: CharSequence = s.toString()
                binding.passwordValidator.apply {
                    if (validPassword.isValidPassword())
                        setCompoundDrawablesRelativeWithIntrinsicBounds(validPasswordIcon, null, null, null)
                    else
                        setCompoundDrawablesRelativeWithIntrinsicBounds(waitingPasswordIcon,null,null,null)
                }
                readyPassword = if(validPassword.isValidPassword()) s.toString()  else ""
                enableDisableSignInBtn()
            }
        })
    }

    /**
     * Text Field Validator functions
     */
    //function to check if email entered is valid email
    fun CharSequence.isValidEmail(): Boolean{
        return this.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }
    //function to check if the password is valid
    fun CharSequence.isValidPassword(): Boolean{
        return this.isNotEmpty() && this.length == 8
    }

    //this function will be called to enable or disable the Login and SignUp button
    private fun enableDisableSignInBtn(){
        val emailChar: CharSequence = readyEmail
        val passwordChar: CharSequence = readyPassword
        binding.signInBtn.isEnabled = emailChar.isValidEmail() && passwordChar.isValidPassword()
    }


    //Called to Update the UI if sign was successful or fails
    private fun updateUI(){
        auth.currentUser?.let {
            //Check the UserType to know where to navigate to
            checkUserType()
        }
    }

    //To display possible error that occur in signing user in
    private fun displayPossibleError(task: Task<AuthResult>) {
        //SignIn failed
        //Check if connection has not timeout
        if(!connectionTimeOut.getIsConnectionTimeOutStat()){
            //Cancel the connection timeout countdown
            connectionTimeOut.cancelTimeOutCountDown()
            try {
                //get the particular exception that occur for the failure
                val exception = task.exception as FirebaseAuthException
                val message =  when(exception.errorCode){
                    "ERROR_USER_NOT_FOUND" ->  "Failed! No user with this email. Please create an account."
                    else -> "Failed! Invalid email and/or password."
                }
                Snackbar.make(binding.root, message , Snackbar.LENGTH_INDEFINITE).setAction("OK"){
                    Log.i("TAG", "TAG")
                }.show()
            } catch (exception: Exception){
                //It must have been due to poor internet
                Snackbar.make(binding.root, "Poor or no internet connection. Try again!" , Snackbar.LENGTH_LONG).show()
            }
        }
        //If connection had timeout, a UI would
        //have been displayed to the user, so need
        //to handle that again.
    }


    //Check the type of User that is trying to Login to the App to decide where they go
    private fun checkUserType(){
        auth.currentUser?.let {firebaseUser ->
            firebaseDatabaseRef.child("allUsers").child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userDetails = snapshot.getValue<User>()
                        userDetails?.let {
                            when(it.userType){
                                1 -> {
                                    //The User is signed in as a Candidate
                                    findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToHomeFragmentCandidate())
                                }
                                2 -> {
                                    //The user has successfully signed in as a recruiter, move user to HomeFragment()
                                    findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToHomeFragmentRecruiter())
                                }
                                else -> {
                                    //User most definitely did not finish sign up as the UserType is undefined
                                    findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToUserTypeFragment())
                                }
                            }
                        }
                        if (userDetails == null)
                            //User most definitely did not finish sign up as the UserType is undefined
                            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToUserTypeFragment())
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }
}
