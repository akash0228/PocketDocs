package com.akash.pocketdocs.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.akash.pocketdocs.data.Document
import com.akash.pocketdocs.databinding.FragmentDocumentDetailsBinding
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DocumentDetailsFragment : Fragment() {

    private lateinit var binding: FragmentDocumentDetailsBinding
    private lateinit var document: Document

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            document = it.getParcelable("document", Document::class.java)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDocumentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            textTitle.text = document.name
            textCategory.text = document.category
            textRelation.text = document.relation
            textIdNumber.text = document.idNumber
            textType.text = document.type

            var sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            document.expiryDate?.let{textExpiry.text = "${sdf.format(Date(it))}"}

            sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            textAddedDate.text = "${sdf.format(Date(document.addedDate))}"


            Glide.with(requireContext())
                .load(document.filePath)
                .into(binding.imagePreview)
        }
    }

}