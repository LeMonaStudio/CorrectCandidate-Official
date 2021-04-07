package com.thenativecitizens.correctcandidate_recruiter.ui.auth

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
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
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.thenativecitizens.correctcandidate_recruiter.R
import com.thenativecitizens.correctcandidate_recruiter.databinding.FragmentSignUpBinding
import com.thenativecitizens.correctcandidate_recruiter.util.ConnectionTimeOut


class SignUpFragment : Fragment() {

    private lateinit var binding: FragmentSignUpBinding
    //Connection countdown viewModel
    private lateinit var connectionTimeOut: ConnectionTimeOut

    //Firebase Auth
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
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_up, container, false)

        //Connection Timeout ViewModel initialized
        connectionTimeOut = ViewModelProvider(this).get(ConnectionTimeOut::class.java)

        //initialize Auth
        auth = Firebase.auth
        //initialize Firebase Database Reference
        firebaseDatabaseRef = Firebase.database.reference

        //This drawables will let the User know if the password entered
        //is valid i.e its not less or more than 8 characters
        validPasswordIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_check).apply {
            this?.setTint(ContextCompat.getColor(requireContext(), R.color.valid_color))
        }
        waitingPasswordIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_awaiting_validation)

        //Initialize all edit text fields TextWatchers
        initTextWatchers()


        //Navigate to LoginFragment
        binding.signInBtn.setOnClickListener {
            findNavController().navigate(SignUpFragmentDirections.actionSignUpFragmentToLoginFragment())
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

        //When the Create Account button is clicked
        binding.signUpBtn.setOnClickListener {
            //disable all other clickable UI elements
            disableUI()
            //begin connection timeout countdown for 30 secs
            connectionTimeOut.beginTimeOutCountDown(30_000)
            //begin SignUp operation
            signUp()
        }


        return binding.root
    }


    //To disable all clickable UI elements while an operation is ongoing
    private fun disableUI() {
        binding.apply {
            progress.visibility = View.VISIBLE
            binding.passwordContainer.apply {
                with(this){
                    isEnabled = false
                    alpha = 0.5f
                }
            }
            binding.passwordVisibilityBtn.isEnabled = false
            binding.signUpBtn.isEnabled = false
            binding.signInBtn.isEnabled = false
        }
    }

    //To enable all clickable UI elements when an operation is done
    private fun enableUI(){
        binding.apply {
            progress.visibility = View.GONE
            binding.passwordContainer.apply {
                with(this){
                    isEnabled = true
                    alpha = 1f
                }
            }
            binding.passwordVisibilityBtn.isEnabled = true
            binding.signUpBtn.isEnabled = true
            binding.signInBtn.isEnabled = true
        }
        //Decide if the SignUp button should be enabled or disabled
        enableDisableSignUpBtn()
    }

    /**
     * TextWatchers
     */
    private fun initTextWatchers() {
        //Email TextWatchers for text changes
        binding.username.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(email: CharSequence?, start: Int, before: Int, count: Int) {
                enableDisableSignUpBtn()
            }
            override fun afterTextChanged(s: Editable?) {
                val validEmailChar: CharSequence = s.toString()
                when(validEmailChar.isValidEmail()){
                    true -> {
                        //Email address is valid email
                        binding.usernameContainer.isErrorEnabled = false
                        binding.usernameContainer.error = null
                        readyEmail = s.toString()
                    }
                    false -> {
                        binding.usernameContainer.isErrorEnabled = true
                        binding.usernameContainer.error = getString(R.string.email_validator_error_text)
                        readyEmail = ""
                    }
                }
                //enable or disable the SignUp Button
                enableDisableSignUpBtn()
            }
        })

        //Password TextWatcher for text changes
        binding.password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(password: CharSequence?, start: Int, before: Int, count: Int) {
                enableDisableSignUpBtn()
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
                enableDisableSignUpBtn()
            }
        })
    }

    /**
     * Operation functions
     */
    //function called to sign-up a new user
    private fun signUp(){
        Firebase.auth.createUserWithEmailAndPassword(readyEmail, readyPassword)
                .addOnCompleteListener{ task ->
                    if(task.isSuccessful){
                        //Sign Up was successful
                        if (!connectionTimeOut.getIsConnectionTimeOutStat()){
                            //Connection has not timeout
                            connectionTimeOut.cancelTimeOutCountDown()
                            //Take User to the UserTypeFragment
                            findNavController().navigate(SignUpFragmentDirections.actionSignUpFragmentToUserTypeFragment())
                        } else{
                            //Connection has timeout before the Signing up was successful
                            //So Sign the User out and let then try Login in
                            auth.signOut()
                            Snackbar.make(binding.root, "Error signing you up due to poor internet connection. Please try again.",
                                Snackbar.LENGTH_LONG).show()
                            enableUI()
                        }
                    } else{
                        //The Signing up failed so let the user
                        //know what error has occur
                        if (!connectionTimeOut.getIsConnectionTimeOutStat()){
                            //Connection has not timeout
                                connectionTimeOut.cancelTimeOutCountDown()
                            try {
                                //Show Failure
                                val exception = task.exception as FirebaseAuthException
                                if (exception.errorCode == "ERROR_EMAIL_ALREADY_IN_USE"){
                                    //The is a user with this email
                                    Snackbar.make(binding.root, "Failed! This email has been registered already. Try signing in.", Snackbar.LENGTH_LONG).show()
                                }
                            } catch (exception: Exception){
                                Snackbar.make(binding.root, "Failed! Please check your internet connection and try again.",
                                        Snackbar.LENGTH_LONG).show()
                            }
                            //cancel the connection timeout count down
                            enableUI()
                        }
                    }
                }
    }

    /**
     * Functions for field Validations
     */
    //function to check if email entered is valid email
    fun CharSequence.isValidEmail(): Boolean{
        return this.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }
    //function to check if the password is valid
    fun CharSequence.isValidPassword(): Boolean{
        return this.isNotEmpty() && this.length == 8
    }
    //to enable or disable the SignUp button
    private fun enableDisableSignUpBtn(){
        val emailChar: CharSequence = readyEmail
        val passwordChar: CharSequence = readyPassword
        binding.signUpBtn.isEnabled = emailChar.isValidEmail() && passwordChar.isValidPassword()
    }
}