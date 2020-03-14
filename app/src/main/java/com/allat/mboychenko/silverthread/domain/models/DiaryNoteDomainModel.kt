package com.allat.mboychenko.silverthread.domain.models

import java.util.*

class DiaryNoteDomainModel(val note: String = "",
                           val id: String = UUID.randomUUID().toString(),
                           val start: Calendar = Calendar.getInstance())