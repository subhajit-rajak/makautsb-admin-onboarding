package com.subhajitrajak.msbcontributer.utils

import com.subhajitrajak.msbcontributer.utils.Constants.BOOKS
import com.subhajitrajak.msbcontributer.utils.Constants.BOOKS_DATA
import com.subhajitrajak.msbcontributer.utils.Constants.CE
import com.subhajitrajak.msbcontributer.utils.Constants.CSE
import com.subhajitrajak.msbcontributer.utils.Constants.ECE
import com.subhajitrajak.msbcontributer.utils.Constants.EE
import com.subhajitrajak.msbcontributer.utils.Constants.IT
import com.subhajitrajak.msbcontributer.utils.Constants.ME
import com.subhajitrajak.msbcontributer.utils.Constants.NOTES
import com.subhajitrajak.msbcontributer.utils.Constants.NOTES_DATA
import com.subhajitrajak.msbcontributer.utils.Constants.ORGANIZERS_DATA

fun getBranchCode(branch: String): String {
    return when(branch) {
        CSE -> "0"
        IT -> "1"
        ECE -> "2"
        ME -> "3"
        CE -> "4"
        EE -> "5"
        else -> "9999"
    }
}

fun getTypeCode(type: String): String {
    return when(type) {
        NOTES -> NOTES_DATA
        BOOKS -> BOOKS_DATA
        else -> ORGANIZERS_DATA
    }
}