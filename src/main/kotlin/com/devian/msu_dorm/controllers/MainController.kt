package com.devian.msu_dorm.controllers

import com.devian.msu_dorm.domain.Faculty
import com.devian.msu_dorm.domain.Student
import com.devian.msu_dorm.utils.GoogleSheetsUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.util.FileCopyUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets


@Controller
@RequestMapping("/")
class MainController {

    @Autowired
    lateinit var googleSheetsUtil: GoogleSheetsUtil

    @Autowired
    lateinit var resourceLoader: ResourceLoader

    @GetMapping("/")
    fun main(
            model: Model,
            @RequestParam("name", required = false) name: String?,
            @RequestParam("surname", required = false) surname: String?,
            @RequestParam("patronymic", required = false) patronymic: String?): String {
        model.addAttribute("name", if (name.isNullOrBlank()) "" else name)
        model.addAttribute("surname", if (surname.isNullOrBlank()) "" else surname)
        model.addAttribute("patronymic", if (name.isNullOrBlank()) "" else patronymic)
        return "index"
    }

    @GetMapping("/find")
    fun find(
            model: Model,
            @RequestParam("faculty") faculty: String,
            @RequestParam("name") name: String,
            @RequestParam("surname") surname: String,
            @RequestParam("patronymic") patronymic: String
    ): String {
        val student = Student(name.toLowerCase().trim(), surname.toLowerCase().trim(), patronymic.toLowerCase().trim(), Faculty.valueOf(faculty))
        println(student)

        val allStudents = googleSheetsUtil.getStudentsFromAllDorms(student.faculty)
        val rooms = googleSheetsUtil.getStudentsRoom(student, allStudents)

        val results = mutableListOf<Student>()
        for (room in rooms)
            results.addAll(googleSheetsUtil.getStudentsByRoom(room, allStudents))

        model.addAttribute("student", student)
        model.addAttribute("findNull", rooms.isEmpty())
        model.addAttribute("rooms", rooms)
        model.addAttribute("findSingle", results.size == 1)
        model.addAttribute("results", results)

        return "find"
    }

    @GetMapping("/static/styles.less")
    @ResponseBody
    fun styles(): String {
        val resource: Resource = ClassPathResource("static/styles.less")
        val inputStream: InputStream = resource.inputStream
        return try {
            val byteData = FileCopyUtils.copyToByteArray(inputStream)
            String(byteData, StandardCharsets.UTF_8)
        } catch (e: IOException) {
            "null"
        }
    }
}