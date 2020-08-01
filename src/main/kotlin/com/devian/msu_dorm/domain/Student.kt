package com.devian.msu_dorm.domain

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "students")
class Student(
        @Id
        var id: String,
        var firstName: String,
        var secondName: String,
        var patronymic: String,
        var faculty: Faculty,
        var course: String = "",
        var room: String = "",
        var vk: String = "",
        var block: String = ""
) {
    constructor(): this(UUID.randomUUID().toString(), "", "", "", Faculty.PHYSICAL)

    override fun toString(): String {
        return "Студент: фамилия - $secondName, имя - $firstName, отчество - $patronymic"
    }
}