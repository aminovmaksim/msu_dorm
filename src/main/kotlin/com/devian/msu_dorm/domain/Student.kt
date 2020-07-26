package com.devian.msu_dorm.domain

data class Student(
        var firstName: String,
        var secondName: String,
        var patronymic: String,
        var faculty: Faculty,
        var course: String = "",
        var room: String = "",
        var vk: String = "",
        var block: String = ""
)