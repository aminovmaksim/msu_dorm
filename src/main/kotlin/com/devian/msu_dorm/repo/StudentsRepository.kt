package com.devian.msu_dorm.repo

import com.devian.msu_dorm.domain.Student
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface StudentsRepository: CrudRepository<Student, Int> {

}