package com.akash.pocketdocs.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.akash.pocketdocs.R
import com.akash.pocketdocs.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolBar()

    }

    private fun setupToolBar() {
        val toolbar = requireActivity().findViewById<Toolbar>(R.id.mainToolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_homeFragment)
        }
    }

}