package com.subhajitrajak.msbcontributer.models

import java.io.Serializable

data class BooksModel (
    val id: String = "",
    val semester: String = "",
    val bookName: String = "",
    val topicName: String? = null,
    val authorName: String? = null,
    val preview: String? = null,
    val bookPDF: String = "",
    val contributor: String? = null,
    val contributorEmail: String? = null,
    val type: String? = null,
    val branch: String? = null,
    val status: String? = null
): Serializable