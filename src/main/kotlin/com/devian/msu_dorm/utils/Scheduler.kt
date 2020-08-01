package com.devian.msu_dorm.utils

import com.devian.msu_dorm.domain.Faculty
import com.devian.msu_dorm.repo.StudentsRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@EnableScheduling
class Scheduler {

    @Autowired
    lateinit var googleSheetsUtil: GoogleSheetsUtil

    @Autowired
    lateinit var studentsRepository: StudentsRepository

    @Scheduled(fixedRate = 60000)
    fun scheduledUpdate() {
        println("Updating list of students")
        studentsRepository.saveAll(googleSheetsUtil.getStudentsFromAllDorms(Faculty.PHYSICAL))
    }
}