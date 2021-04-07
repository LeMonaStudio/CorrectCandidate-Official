package com.thenativecitizens.correctcandidate_recruiter.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.thenativecitizens.correctcandidate_recruiter.R
import com.thenativecitizens.correctcandidate_recruiter.databinding.DialogVerificationEmailBinding


class DialogVerificationEmail : DialogFragment() {
    private lateinit var binding: DialogVerificationEmailBinding
    private var continueRequestKey = "CONTINUE"

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.dialog_verification_email, null, false)

        requireArguments().let {
            binding.verificationMsg.text = getString(R.string.email_verification_prompt_text, it.getString("Email", ""))
        }

        binding.continueCreateAccountBtn.setOnClickListener {
            dialog?.dismiss()
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)
        val dialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        return dialog
    }

    private fun onContinueBtnPressed(){
        val bundle = Bundle()
        parentFragmentManager.setFragmentResult(continueRequestKey, bundle)
    }

    override fun onDismiss(dialog: DialogInterface) {
        onContinueBtnPressed()
        super.onDismiss(dialog)
    }
}