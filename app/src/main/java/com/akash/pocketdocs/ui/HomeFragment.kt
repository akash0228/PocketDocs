package com.akash.pocketdocs.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.akash.pocketdocs.R
import com.akash.pocketdocs.data.Document
import com.akash.pocketdocs.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: DocumentAdapter
    val sampleDocs = mutableListOf(
        Document(name="Aadhar Card", category="ID", relation="Self", idNumber="XXXX", expiryDate=null, filePath="", addedDate=System.currentTimeMillis(), type = "pdf" ),
        Document(name="PAN Card", category="ID", relation="Self", idNumber="YYYY", expiryDate=null, filePath="", addedDate=System.currentTimeMillis(), type = "pdf")
    )

    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var pdfLauncher: ActivityResultLauncher<String>
    private lateinit var cameraImageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTabs()
        setupMenu()
        setupToolBar()
        setupRecyclerView()
        setupAddButton()
        setupCameraLauncher()
        setupGalleyLauncher()
        setupPdfLauncher()
    }

    private fun setupToolBar() {
        val toolbar = requireActivity().findViewById<Toolbar>(R.id.mainToolbar)
        toolbar.setNavigationIcon(R.drawable.ic_options)
        toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
        }
    }

    private fun setupPdfLauncher() {
        pdfLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val path = copyUriToInternalStorage(requireContext(), uri)
                setupAndShowAddDocument(path!!)
            }
        }
    }

    private fun setupGalleyLauncher() {
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                uri?.let {
                    val path = copyUriToInternalStorage(requireContext(), it)
                    setupAndShowAddDocument(path!!)
                }
            }
        }
    }

    private fun setupCameraLauncher() {
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                val path = copyUriToInternalStorage(requireContext(), cameraImageUri)
                setupAndShowAddDocument(path!!)
            }
        }
    }

    private fun setupAndShowAddDocument(path : String) {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_add_edit_document, null)
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(dialogView)

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
                dialogView.findViewById<EditText>(R.id.editExpiryDate).setText(formattedDate)
            }
            datePicker.show(parentFragmentManager, "expiry_date_picker")
        }


        val categories = listOf("Photos", "Bills", "ID Cards", "Certificates", "Results")
        val types = listOf("Image", "Pdf")

        dialogView.findViewById<Spinner>(R.id.spinnerCategory).adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
        dialogView.findViewById<Spinner>(R.id.spinnerType).adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, types)

        dialogView.findViewById<Button>(R.id.btnSave).setOnClickListener {
            val title = dialogView.findViewById<EditText>(R.id.editTitle).text.toString()
            val idNumber = dialogView.findViewById<EditText>(R.id.editIdNumber).text.toString()
            val relation = dialogView.findViewById<EditText>(R.id.editRelation).text.toString()
            val type = dialogView.findViewById<Spinner>(R.id.spinnerType).selectedItem.toString()
            val category = dialogView.findViewById<Spinner>(R.id.spinnerCategory).selectedItem.toString()

            sampleDocs.add(Document(name = title, category = category, relation = relation, expiryDate = expiryDateMillis, addedDate = System.currentTimeMillis(), filePath = path, type = type, idNumber = idNumber))
            setupRecyclerView()

            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    private fun setupAddButton() {
        binding.documentAddButton.setOnClickListener {
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
        val imageFile = File(requireContext().filesDir, "photo_${System.currentTimeMillis()}.jpg")
        cameraImageUri = FileProvider.getUriForFile(
            requireContext(),
            "com.akash.pocketdocs.fileprovider",
            imageFile
        )
        cameraLauncher.launch(cameraImageUri)
    }

    private fun setupRecyclerView() {
        adapter = DocumentAdapter(sampleDocs, object: DocumentAdapter.DocumentAdapterListener{
            override fun onDeleteClicked(position: Int) {
                deleteDocumentAtPosition(position)
            }

            override fun onEditClicked(position: Int) {
                editDocumentAtPosition(position)
            }

            override fun onShareClicked(position: Int) {
                shareDocumentAtPosition(position)
            }

            override fun onItemClicked(position: Int) {
                showDetailsOfDocumentAtPosition(position)
            }

        })
        binding.documentRecyclerView.layoutManager = GridLayoutManager(requireContext(), 1)
        binding.documentRecyclerView.adapter = adapter
    }

    private fun deleteDocumentAtPosition(position : Int) {
        sampleDocs.removeAt(position)
        setupRecyclerView()
    }

    private fun editDocumentAtPosition(position : Int) {
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

        editTitle.setText(sampleDocs[position].name)
        editIdNumber.setText(sampleDocs[position].idNumber)
        editRelation.setText(sampleDocs[position].relation)
        spinnerType.setSelection(types.indexOf(sampleDocs[position].type))
        spinnerCategory.setSelection(categories.indexOf(sampleDocs[position].category))

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formattedDate = sdf.format(Date(sampleDocs[position].expiryDate!!))
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

            val newDocument = Document(name = title, category = category, relation = relation, expiryDate = expiryDateMillis, addedDate = System.currentTimeMillis(), filePath = sampleDocs[position].filePath, type = type, idNumber = idNumber)
            sampleDocs.removeAt(position)
            sampleDocs.add(position, newDocument)
            setupRecyclerView()

            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    private fun shareDocumentAtPosition(position : Int) {
        val file = File(sampleDocs[position].filePath)
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

    private fun showDetailsOfDocumentAtPosition(position: Int){
        val bundle = Bundle().apply {
            putParcelable("document", sampleDocs[position])
        }
        findNavController().navigate(R.id.action_homeFragment_to_documentDetailsFragment, bundle)
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_home, menu)
                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem.actionView as SearchView
                searchItem.icon?.setTint(ContextCompat.getColor(requireContext(), R.color.white))

                searchView.queryHint = "Search documents..."

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("All"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("ID Cards"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Bills"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Certificates"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Photos"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Results"))
    }

    private fun copyUriToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.filesDir, "image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}