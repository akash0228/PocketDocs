package com.akash.pocketdocs.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.akash.pocketdocs.data.Document

class DocumentViewModel : ViewModel() {

    private val _allDocuments = MutableLiveData<List<Document>>(
        listOf(
        Document(name="Aadhar Card", category="ID Cards", relation="Self", idNumber="XXXX", expiryDate=null, filePath="", addedDate=System.currentTimeMillis(), type = "pdf" ),
        Document(name="PAN Card", category="ID Cards", relation="Self", idNumber="YYYY", expiryDate=null, filePath="", addedDate=System.currentTimeMillis(), type = "pdf")
    ))
    val allDocuments : LiveData<List<Document>> = _allDocuments

    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery : LiveData<String> = _searchQuery

    private val _selectedCategory = MutableLiveData<String?>(null)
    val selectedCategory: LiveData<String?> = _selectedCategory

    val filteredDocuments  = MediatorLiveData<List<Document>>().apply {
        fun update() {
            val all = _allDocuments.value.orEmpty()
            val query = _searchQuery.value.orEmpty().lowercase()
            val category = _selectedCategory.value

            value = all.filter { document ->
                val matchesQuery = document.name.lowercase().contains(query) || document.type.lowercase().contains(query) || document.id.lowercase().contains(query)
                val matchesCategory = category == null || document.category.contains(category) || category == "All"
                matchesQuery && matchesCategory
            }
        }

        addSource(_searchQuery) {update()}
        addSource(_allDocuments) {update()}
        addSource(_selectedCategory) {update()}
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun addDocument(document: Document){
        val currList = _allDocuments.value?.toMutableList() ?: mutableListOf()
        currList.add(document)
        _allDocuments.value = currList
    }

    fun deleteDocument(position : Int){
        val currList = _allDocuments.value?.toMutableList() ?: mutableListOf()
        currList.removeAt(position)
        _allDocuments.value = currList
    }

    fun updateDocument(documentId: String, document: Document) {
        val currList = _allDocuments.value?.toMutableList() ?: mutableListOf()
        val position = currList.indexOfFirst { it.id == documentId }
        currList[position] = document
        _allDocuments.value = currList
    }
}