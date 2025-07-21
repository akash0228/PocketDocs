package com.akash.pocketdocs.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class Document(
    val id : String = UUID.randomUUID().toString(),
    val name : String,
    val category : String,
    val relation : String,
    val idNumber : String?,
    val expiryDate : Long?,
    var filePath : String,
    val addedDate : Long,
    val type : String
) : Parcelable
