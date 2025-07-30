package com.akash.pocketdocs.ui

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.akash.pocketdocs.R
import com.akash.pocketdocs.data.Document
import com.akash.pocketdocs.databinding.ItemDocumentBinding
import com.bumptech.glide.Glide
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DocumentAdapter(val listener: DocumentAdapterListener): RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder>() {

    private val documentList: MutableList<Document> = mutableListOf()

    class DocumentViewHolder(val binding: ItemDocumentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
        val binding = ItemDocumentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DocumentViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return documentList.size
    }

    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
        val document = documentList[position]
        holder.binding.apply {
            textName.text = document.name
            textCategory.text = "Category: ${document.category}"
            textIdNumber.text = "Id no: ${document.idNumber}"
            document.expiryDate?.let {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = sdf.format(Date(document.expiryDate))
                textExpiry.text = "Expires: $formattedDate"
            }
        }

        if (document.type.lowercase(Locale.ROOT) == "pdf"){
            val bitmap = renderFirstPageOfPdf(document.filePath)
            if (bitmap != null) {
                holder.binding.imageThumbnail.setImageBitmap(bitmap)
            } else {
                holder.binding.imageThumbnail.setImageBitmap(bitmap)
            }
        }
        else {
            Glide.with(holder.itemView)
                .load(File(document.filePath))
                .placeholder(R.drawable.image_placeholder_bg)
                .into(holder.binding.imageThumbnail)
        }

        holder.binding.btnOptions.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.menu_document_options, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit -> {
                        listener.onEditClicked(document)
                        true
                    }
                    R.id.action_delete -> {
                        listener.onDeleteClicked(position)
                        true
                    }
                    R.id.action_share -> {
                        listener.onShareClicked(document)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        holder.binding.root.setOnClickListener {
            listener.onItemClicked(document)
        }
    }

    private fun renderFirstPageOfPdf(pdfPath: String): Bitmap? {
        val file = File(pdfPath)
        if (!file.exists()) return null

        val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val pdfRenderer = PdfRenderer(fileDescriptor)

        if (pdfRenderer.pageCount <= 0) {
            pdfRenderer.close()
            fileDescriptor.close()
            return null
        }

        val page = pdfRenderer.openPage(0)
        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        pdfRenderer.close()
        fileDescriptor.close()

        return bitmap
    }

    fun submitList(newList : List<Document>){
        documentList.clear()
        documentList.addAll(newList)
        notifyDataSetChanged()
    }

    interface DocumentAdapterListener {
        fun onDeleteClicked(position: Int)
        fun onEditClicked(document: Document)
        fun onShareClicked(document: Document)
        fun onItemClicked(document: Document)
    }
}