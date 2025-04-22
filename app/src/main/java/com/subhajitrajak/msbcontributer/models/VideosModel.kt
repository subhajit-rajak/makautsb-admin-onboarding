package com.subhajitrajak.msbcontributer.models

import com.subhajitrajak.msbcontributer.utils.Constants.PENDING
import com.subhajitrajak.msbcontributer.utils.Constants.VIDEOS

data class VideosModel( // from firebase response
    val title: String? = null,
    val thumbnailUrl: String? = null,
    val channelTitle: String? = null,

    val playlistId: String? = null,
    val type: String = VIDEOS,
    val status: String = PENDING
)