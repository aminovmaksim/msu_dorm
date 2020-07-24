package com.devian.msu_dorm.domain

data class Student(
        val firstName: String,
        val secondName: String,
        val patronymic: String,
        val faculty: Faculty,
        val course: String = "",
        var room: String = "",
        val phone: String = ""
)