package com.thenativecitizens.correctcandidate_recruiter.ui.main.recruiter.aptitude

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thenativecitizens.correctcandidate_recruiter.R
import com.thenativecitizens.correctcandidate_recruiter.databinding.FragmentQuestionDetailsBinding
import com.thenativecitizens.correctcandidate_recruiter.util.SectionListAdapter
import com.thenativecitizens.correctcandidate_recruiter.util.SectionListListener

class QuestionDetailsFragment : Fragment() {
    private lateinit var binding: FragmentQuestionDetailsBinding
    private lateinit var viewModel: QuestionDetailsViewModel

    private var aptitudeTestTitle = ""
    private var aptitudeTestSections: MutableList<String>? = null
    private var aptitudeTestDurationInMinutes = 0
    private var aptitudeTestDescription = ""
    private var aptitudeMayContainGraphics = false
    private var aptitudeTestUseLogoColor = false
    private var aptitudeTestBg: Uri? = null


    /**
     * OnCreateView
     */

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        //the layout
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_question_details, null, false)

        viewModel = ViewModelProvider(this).get(QuestionDetailsViewModel::class.java)

        //Activate TextWatchers
        activateTextWatchers()

        //The section list recyclerView
        val layoutManager = GridLayoutManager(requireContext(), 1, RecyclerView.VERTICAL, false)
        val sectionListAdapter = SectionListAdapter(SectionListListener {itemPosition ->
            //Remove the section when clicked
            viewModel.removeSection(itemPosition)
        })
        binding.sectionNamesList.apply {
            this.layoutManager = layoutManager
            this.adapter = sectionListAdapter
        }

        //Observe the adding of a new section to the list of sections for the Aptitude test
        viewModel.sectionList.observe(viewLifecycleOwner){
            if (!it.isNullOrEmpty()){
                aptitudeTestSections = it
                sectionListAdapter.data = it
                //show the RecyclerView and hide the section names hint
                binding.sectionNamesList.visibility = View.VISIBLE
            } else {
                aptitudeTestSections = null
                sectionListAdapter.data = mutableListOf()
                //hide the RecyclerView and show the section names hint
                binding.sectionNamesList.visibility = View.GONE
            }
            enableContinueButton()
        }

        //Array and ArrayAdapter for the AutoCompleteTextView
        val arrayOfSections: Array<String> = arrayOf("Maths", "English", "Verbal Ability", "Abstract Reasoning",
            "Quantitative", "Numerical Analysis", "Cognitive Ability", "Spatial Reasoning", "Logical & Critical",
            "Visual Reasoning", "Problem-solving", "Decision-Making & Judgement", "Situational Judgement",
            "Mechanical Reasoning", "Error Checking", "Product Survey")
        val adapter = ArrayAdapter(
            requireContext() , android.R.layout.simple_dropdown_item_1line, arrayOfSections
        )
        binding.sectionName.setAdapter(adapter)

        //When the + button is clicked to add section
        binding.addSectionButton.setOnClickListener {
            if(binding.sectionName.text.toString().isNotEmpty()){
                //Pass the content of the AutoCompleteTextView to the ViewModel
                viewModel.addSection(binding.sectionName.text.toString())
                //Empty the AutoCompleteTextView
                binding.sectionName.setText("", TextView.BufferType.EDITABLE)
            }
        }

        //The Switches been handled
        binding.containsMedia.setOnCheckedChangeListener { _, isChecked ->
            aptitudeMayContainGraphics = isChecked
        }
        binding.logoColorAsBgSwitch.setOnCheckedChangeListener { _, isChecked ->
            aptitudeTestUseLogoColor = isChecked
            //User wants to use color from logo when isChecked is true, or else false
            binding.uploadFromDeviceBtn.isEnabled = !isChecked
            enableContinueButton()
        }


        return binding.root
    }


    //Activate text watchers
    private fun activateTextWatchers(){
        //Title
        binding.questionTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                enableContinueButton()
            }
            override fun afterTextChanged(s: Editable?) {
                val title = s.toString()
                if(title.length <= 9){
                    //Title is less than 10 chars
                    binding.questionTitleContainer.apply {
                        isErrorEnabled = true
                        error = "The title is too short"
                    }
                    aptitudeTestTitle = ""
                } else {
                    //Title is okay
                    binding.questionTitleContainer.apply {
                        isErrorEnabled = false
                        error = null
                    }
                    aptitudeTestTitle = title
                }
                enableContinueButton()
            }
        })
        //Short Description
        binding.testDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                enableContinueButton()
            }
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().length <= 24){
                    //description is less than 24 chars
                    binding.testDescriptionContainer.apply {
                        isErrorEnabled = true
                        error = "This description is too short"
                    }
                    aptitudeTestDescription = ""
                } else{
                    //description is okay
                    binding.testDescriptionContainer.apply {
                        isErrorEnabled = false
                        error = null
                    }
                    aptitudeTestDescription = s.toString()
                }
                enableContinueButton()
            }
        })
        //Test Duration
        binding.testDuration.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s.toString().startsWith("0"))
                    binding.testDuration.setText("", TextView.BufferType.EDITABLE)
                enableContinueButton()
            }
            override fun afterTextChanged(s: Editable?) {
                if(s.toString().isNotEmpty() && s.toString().toInt() >= 10){
                    //The user has entered a duration in the duration field
                    aptitudeTestDurationInMinutes = s.toString().toInt()
                    binding.testDurationContainer.apply {
                        //Test Duration is empty or less tha 10 minutes
                        isErrorEnabled = false
                        error = null
                    }
                } else{
                    binding.testDurationContainer.apply {
                        //Test Duration is empty or less tha 10 minutes
                        isErrorEnabled = true
                        error = "Test duration must not be less than 10 minutes"
                    }
                    aptitudeTestDurationInMinutes = 0
                }
            }
        })
    }


    //Enables or disables the Continue Button
    private fun enableContinueButton(){
        //enable or disable the continue button based on constraints
        binding.createTestBtn.isEnabled = aptitudeTestTitle.isNotEmpty() && aptitudeTestSections != null
                && aptitudeTestDescription.isNotEmpty() && aptitudeTestDurationInMinutes >= 10 && (aptitudeTestUseLogoColor || aptitudeTestBg != null)
    }
}
