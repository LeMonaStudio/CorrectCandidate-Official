package com.thenativecitizens.correctcandidate_recruiter.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.thenativecitizens.correctcandidate_recruiter.R
import com.thenativecitizens.correctcandidate_recruiter.databinding.DialogVerifyAgainBinding

class DialogVerifyAgain : DialogFragment() {
    private lateinit var binding: DialogVerifyAgainBinding
    private val verifyMeResultKey = "VERIFY ME"

    private var onVerifyAgain = false


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.dialog_verify_again, null, false)

        binding.verifyMeBtn.setOnClickListener {
            onVerifyAgain = true
            onVerifyMeBtnPressed(true)
            dialog?.dismiss()
        }

        binding.okButton.setOnClickListener {
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

    private fun onVerifyMeBtnPressed(b: Boolean) {
        val bundle = Bundle()
        bundle.putBoolean("Request",b)
        parentFragmentManager.setFragmentResult(verifyMeResultKey, bundle)
    }

    override fun onDismiss(dialog: DialogInterface) {
        if(!onVerifyAgain) onVerifyMeBtnPressed(false)
        super.onDismiss(dialog)
    }
}