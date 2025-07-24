package com.akash.pocketdocs.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.akash.pocketdocs.R
import com.akash.pocketdocs.data.Document
import com.akash.pocketdocs.databinding.FragmentDocumentDetailsBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DocumentDetailsFragment : Fragment() {

    private lateinit var binding: FragmentDocumentDetailsBinding
    private lateinit var document: Document
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var pdfLauncher: ActivityResultLauncher<String>
    private lateinit var cameraImageUri: Uri

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

        setupDetails()
        setupEditButton()
        setupShareButton()
        setupDeleteButton()
        setupOnDocumentClick()
        setupPdfLauncher()
        setupGalleyLauncher()
        setupCameraLauncher()
    }

    private fun setupDeleteButton() {
        val alertDialog = AlertDialog.Builder(requireContext())

        alertDialog
            .setTitle("Alert!!")
            .setMessage("Document will be deleted permanently.\n Do you want to continue?")
            .setCancelable(true)
            .setPositiveButton("Continue"){ _, _->
                //TODO show toast
               findNavController().navigate(R.id.action_documentDetailsFragment_to_homeFragment)
            }
            .setNeutralButton("Cancel"){ dialog, _ ->
                dialog.cancel()
            }
            .create()

        binding.buttonDelete.setOnClickListener {
            alertDialog.show()
        }
    }

    private fun setupShareButton() {
        binding.buttonShare.setOnClickListener {
            val file = File(document.filePath)
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )

            val mimeType = if (file.extension.equals("pdf", true)) {
                "application/pdf"
            } else {
                "image/*"
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            requireContext().startActivity(
                Intent.createChooser(shareIntent, "Share Document")
            )
        }
    }

    private fun setupPdfLauncher() {
        pdfLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val existingFile = File(requireContext().filesDir, File(Uri.parse(document.filePath).path!!).name)
                copyUriToFile(requireContext(), it, existingFile)
                setupDetails()
            }
        }
    }

    private fun setupGalleyLauncher() {
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                uri?.let {
                    val existingFile = File(requireContext().filesDir, File(Uri.parse(document.filePath).path!!).name)
                    copyUriToFile(requireContext(), it, existingFile)
                    setupDetails()
                }
            }
        }
    }

    private fun setupCameraLauncher() {
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                setupDetails()
            }
        }
    }

    fun copyUriToFile(context: Context, sourceUri: Uri, targetFile: File) {
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun setupOnDocumentClick() {
        binding.imagePreview.setOnClickListener {
            showAddDocumentOptions()
        }
    }

    private fun showAddDocumentOptions() {
        val bottomSheet = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_add_options, null)
        bottomSheet.setContentView(view)

        view.findViewById<TextView>(R.id.optionCamera).setOnClickListener {
            bottomSheet.dismiss()
            openCamera()
        }

        view.findViewById<TextView>(R.id.optionGallery).setOnClickListener {
            bottomSheet.dismiss()
            openGallery()
        }

        view.findViewById<TextView>(R.id.optionPdf).setOnClickListener {
            bottomSheet.dismiss()
            openPdfPicker()
        }

        bottomSheet.show()

    }

    private fun openPdfPicker() {
        pdfLauncher.launch("application/pdf")
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        galleryLauncher.launch(intent)
    }

    private fun openCamera() {
        val existingFile = File(requireContext().filesDir, File(Uri.parse(document.filePath).path!!).name)
        cameraImageUri = FileProvider.getUriForFile(
            requireContext(),
            "com.akash.pocketdocs.fileprovider",
            existingFile
        )
        cameraLauncher.launch(cameraImageUri)
    }

    private fun setupDetails() {
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
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(binding.imagePreview)
        }
    }

    private fun setupEditButton() {
        binding.buttonEdit.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_add_edit_document, null)
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            bottomSheetDialog.setContentView(dialogView)

            val editTitle = dialogView.findViewById<EditText>(R.id.editTitle)
            val editIdNumber = dialogView.findViewById<EditText>(R.id.editIdNumber)
            val editRelation = dialogView.findViewById<EditText>(R.id.editRelation)
            val spinnerType = dialogView.findViewById<Spinner>(R.id.spinnerType)
            val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
            val editExpiryDate = dialogView.findViewById<EditText>(R.id.editExpiryDate)


            val categories = listOf("Photos", "Bills", "ID Cards", "Certificates", "Results")
            val types = listOf("Image", "Pdf")

            editTitle.setText(document.name)
            editIdNumber.setText(document.idNumber)
            editRelation.setText(document.relation)
            spinnerType.setSelection(types.indexOf(document.type))
            spinnerCategory.setSelection(categories.indexOf(document.category))

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = sdf.format(Date(document.expiryDate!!))
            editExpiryDate.setText(formattedDate)

            var expiryDateMillis: Long? = null

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Expiry Date")
                .setSelection(
                    MaterialDatePicker.todayInUtcMilliseconds()
                )
                .build()

            dialogView.findViewById<EditText>(R.id.editExpiryDate).setOnClickListener {
                datePicker.addOnPositiveButtonClickListener { selection ->
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val formattedDate = sdf.format(Date(selection))
                    expiryDateMillis = Date(selection).time
                    editExpiryDate.setText(formattedDate)
                }
                datePicker.show(parentFragmentManager, "expiry_date_picker")
            }

            spinnerCategory.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
            spinnerType.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, types)

            dialogView.findViewById<Button>(R.id.btnSave).setOnClickListener {
                val title = editTitle.text.toString()
                val idNumber = editIdNumber.text.toString()
                val relation = editRelation.text.toString()
                val type = spinnerType.selectedItem.toString()
                val category = spinnerCategory.selectedItem.toString()

                document = Document(name = title, category = category, relation = relation, expiryDate = expiryDateMillis, addedDate = System.currentTimeMillis(), filePath = document.filePath, type = type, idNumber = idNumber)
                setupDetails()
                bottomSheetDialog.dismiss()
            }
            bottomSheetDialog.show()
        }
    }

}