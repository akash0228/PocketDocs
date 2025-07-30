package com.akash.pocketdocs.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.akash.pocketdocs.R
import com.akash.pocketdocs.databinding.FragmentChangePinBinding
import com.akash.pocketdocs.security.LockManager
import kotlinx.coroutines.launch

class ChangePinFragment : Fragment() {

    private lateinit var binding: FragmentChangePinBinding
    enum class ChangePinStep { ASK_CURRENT, ASK_NEW, CONFIRM_NEW }
    private var step: ChangePinStep = ChangePinStep.ASK_CURRENT
    private var newPinTemporary: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangePinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val pinSetCheckJob = lifecycleScope.launch {
            if (LockManager.isPinSet()){
                step = ChangePinStep.ASK_CURRENT
                binding.titleTextView.setText("Enter current PIN")
            }
            else{
                binding.titleTextView.setText("Enter new PIN")
                step = ChangePinStep.ASK_NEW
            }
        }
        handleSubmitPinButtonClick()
    }

    private fun handleSubmitPinButtonClick() {
        binding.submitPinButton.setOnClickListener {

            val enteredPin = binding.pinEditText.text.toString()

            when(step) {
                ChangePinStep.ASK_CURRENT -> {
                    lifecycleScope.launch {
                        if (enteredPin == LockManager.getPin()){
                            step = ChangePinStep.ASK_NEW
                            binding.titleTextView.setText("Enter new PIN")
                        }
                        else{
                            Toast.makeText(requireContext(), "Incorrect current PIN", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                ChangePinStep.ASK_NEW -> {
                    lifecycleScope.launch {
                        newPinTemporary = enteredPin
                        step = ChangePinStep.CONFIRM_NEW
                        binding.titleTextView.setText("Confirm new PIN")
                    }
                }
                ChangePinStep.CONFIRM_NEW -> {
                    lifecycleScope.launch {
                        if (newPinTemporary == enteredPin) {
                            LockManager.savePin(enteredPin)
                            findNavController().navigate(R.id.action_changePinFragment_to_settingsFragment)
                        }
                        else{
                            Toast.makeText(requireContext(), "PINs do not match. Try again.", Toast.LENGTH_SHORT).show()
                            step = ChangePinStep.ASK_NEW
                            binding.titleTextView.text = "Enter new PIN"
                        }
                    }
                }
            }
            binding.pinEditText.text.clear()
        }
    }

}