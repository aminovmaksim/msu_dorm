package com.devian.msu_dorm.utils

import com.devian.msu_dorm.domain.Faculty
import com.devian.msu_dorm.domain.Student
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import org.springframework.stereotype.Component
import java.io.*
import java.lang.Exception
import java.util.*

@Component
class GoogleSheetsUtil {

    private val APPLICATION_NAME = "MSU Dorm"
    private val JSON_FACTORY: JsonFactory = JacksonFactory.getDefaultInstance()
    private val TOKENS_DIRECTORY_PATH = "tokens"

    private val SHEET_IDs = listOf(
            "19aHJ1aRCgmuYEkD3myZPG1Ah37LAPlVwq94DG4ESUd0", /* ФИЗФАК */
            "", /* ВМК */
            "", /* ХИМФАК */
            "" /* ГЕОЛОГИЧЕСКИЙ */
    )

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private val SCOPES = listOf(SheetsScopes.SPREADSHEETS_READONLY)
    private val CREDENTIALS_FILE_PATH = "/credentials.json"

    /**
     * Получение всех комнат, в которых был найден студент с данным ФИО
     * @param student студент
     * @param allStudents список студентов, среди которого искать
     */
    fun getStudentsRoom(student: Student, allStudents: List<Student>): List<String> {
        val result = mutableListOf<String>()

        for (s in allStudents)
            if (s.secondName == student.secondName && s.firstName == student.firstName && s.patronymic == student.patronymic)
                result.add(s.room)

        return result
    }

    /**
     * Получение всех студентов, проживающей в заданной комнате
     * @param room комната
     * @param allStudents список студентов, среди которого искать
     */
    fun getStudentsByRoom(room: String, allStudents: List<Student>): List<Student> {
        val result = mutableListOf<Student>()

        for (student in allStudents) {
            if (student.room == room.toUpperCase()) {
                student.room += " " + student.block
                student.firstName = student.firstName.capitalize()
                student.secondName = student.secondName.capitalize()
                student.patronymic = student.patronymic.capitalize()
                result.add(student)
            }
        }
        return result
    }

    /**
     * Получение списка всех студентов во всех общежитиях
     * @param faculty факультет
     */
    fun getStudentsFromAllDorms(faculty: Faculty): List<Student> {
        val sheetId = when (faculty) {
            Faculty.PHYSICAL -> SHEET_IDs[0]
            Faculty.CMC -> SHEET_IDs[1]
            Faculty.CHEMICAL -> SHEET_IDs[2]
            Faculty.GEOLOGICAL -> SHEET_IDs[3]
        }

        val dslStudents = getAllRows("DSL!A2:G", sheetId)
        val dsStudents = getAllRows("DS!A2:G", sheetId)
        val fdsStudents = getAllRows("FDS!A2:G", sheetId)

        val result = mutableListOf<Student>()

        if (dslStudents.isNotEmpty())
            for (row in dslStudents) result.add(getStudentFromRow(row, "ДСЛ", faculty))

        if (dsStudents.isNotEmpty())
            for (row in dsStudents) result.add(getStudentFromRow(row, "ДС", faculty))

        if (fdsStudents.isNotEmpty())
            for (row in fdsStudents) result.add(getStudentFromRow(row, "ФДС", faculty))

        return result
    }

    private fun getStudentFromRow(row: List<Any>, dorm: String, faculty: Faculty): Student {
        return try {
            val student = Student(
                    UUID.randomUUID().toString(),
                    row[3].toString().toLowerCase().trim(),
                    row[2].toString().toLowerCase().trim(),
                    row[4].toString().toLowerCase().trim(),
                    faculty,
                    row[5].toString(),
                    (dorm + ": " + row[0].toString()).toUpperCase()
            )
            if (row.size >= 6) {
                student.block = row[1].toString()
            }
            if (row.size >= 7) {
                student.vk = row[6].toString()
            }
            return student
        } catch (e: Exception) {
            println(e.message)
            Student(UUID.randomUUID().toString(), "", "", "", Faculty.PHYSICAL)
        }
    }

    private fun getAllRows(range: String, sheet_id: String): List<List<Any>> {
        println("Google request")
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val service = Sheets.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport))
                .setApplicationName(APPLICATION_NAME)
                .build()
        val response: ValueRange = service.spreadsheets().values()[sheet_id, range]
                .execute()
        return response.getValues()
    }

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    @Throws(IOException::class)
    private fun getCredentials(HTTP_TRANSPORT: NetHttpTransport): Credential? {
        // Load client secrets.
        val `in`: InputStream = GoogleSheetsUtil::class.java.getResourceAsStream(CREDENTIALS_FILE_PATH)
                ?: throw FileNotFoundException("Resource not found: $CREDENTIALS_FILE_PATH")
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(`in`))

        // Build flow and trigger user authorization request.
        val flow = GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build()
        val receiver = LocalServerReceiver.Builder().setPort(8888).build()
        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }
}