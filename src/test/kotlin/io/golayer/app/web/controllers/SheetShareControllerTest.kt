package io.golayer.app.web.controllers

import io.golayer.app.config.AppConfig
import io.golayer.app.config.StorageConfig
import io.golayer.app.domain.RequestShare
import io.golayer.app.domain.ShareRequestDTO
import io.golayer.app.domain.ShareSection
import io.golayer.app.domain.SharedCreatedRecordsDTO
import io.golayer.app.utils.concatRand
import io.golayer.app.web.util.HttpUtil
import io.golayer.app.web.util.eventually
import io.javalin.Javalin
import org.eclipse.jetty.http.HttpStatus
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.regex.Pattern


class SheetShareControllerTest {
    private lateinit var app: Javalin
    private lateinit var http: HttpUtil
    private lateinit var es: HttpUtil

    @Before
    fun start() {
        es = HttpUtil(9200)
        app = AppConfig().setup().start()
        http = HttpUtil(app.port())

        warmUp(this)

        es.delete("/${StorageConfig.SPREADSHEET_SHARE_INDEX}")
    }

    @After
    fun stop() {
        app.stop()
    }


    @Test
    fun `create one share request with one sheet and one email`() {
        val user1 = "reginaldo".concatRand()
        val element1 = "HRReport!A1"
        val email1 = """${"aaa".concatRand()}@pm.me"""

        val shareRequest = RequestShare(user1, listOf(ShareSection(element1, listOf(email1))))

        val postResponse = http.post<String>("/api/sheet", ShareRequestDTO(listOf(shareRequest)))

        await()
        assertEquals(postResponse.status, HttpStatus.OK_200)

        eventually {
            assertTrue("check generated uuid4 command id ", uuidPattern.matcher(postResponse.body).matches())
        }


        val emailLookup =
                http.get<SharedCreatedRecordsDTO>("/api/sheet/$email1")
        assertEquals(emailLookup.status, HttpStatus.OK_200)
        assertTrue("match a previous added request",
                emailLookup.body.sharedRecords
                        .any { it.email == email1 && it.element == element1 && it.userId == user1 })

    }

    @Test
    fun `create composed share request with one sheet and two email`() {

        val user = "fabio".concatRand()

        val element0 = "HRReport!A1"
        val element1 = "'Dashboard'!B2:B5"


        val email0 = """${"bbb".concatRand()}@pm.me"""
        val email1 = """${"ccc".concatRand()}@pm.me"""
        val email2 = """${"ddd".concatRand()}@pm.me"""

        val shareRequest =
                RequestShare(user,
                        listOf(
                                ShareSection(element0, listOf(email0, email1)),
                                ShareSection(element1, listOf(email2))
                        )
                )

        val response = http.post<String>("/api/sheet", ShareRequestDTO(listOf(shareRequest)))

        await()
        assertEquals(response.status, HttpStatus.OK_200)
        assertTrue("check generated uuid4 command id ", uuidPattern.matcher(response.body).matches())

        val emailLookup1 = http.get<SharedCreatedRecordsDTO>("/api/sheet/$email0")
        assertEquals(emailLookup1.status, HttpStatus.OK_200)
        assertTrue("match a previous added request email0",
                emailLookup1.body.sharedRecords
                        .any { it.email == email0 && it.element == element0 && it.userId == user })


        val emailLookup2 = http.get<SharedCreatedRecordsDTO>("/api/sheet/$email1")
        assertEquals(emailLookup2.status, HttpStatus.OK_200)
        assertTrue("match a previous added request email1",
                emailLookup2.body.sharedRecords
                        .any { it.email == email1 && it.element == element0 && it.userId == user })

        val emailLookup3 = http.get<SharedCreatedRecordsDTO>("/api/sheet/$email2")
        assertEquals(emailLookup3.status, HttpStatus.OK_200)
        assertTrue("match a previous added request email2",
                emailLookup3.body.sharedRecords
                        .any { it.email == email2 && it.element == element1 && it.userId == user })
    }

    @Test
    fun `invalid shared element`() {
        val shareRequest =
                RequestShare("reginaldo",
                        listOf(ShareSection("HR!A1", listOf("eee@pm.me")))
                )
        val response = http.post<String>("/api/sheet", ShareRequestDTO(listOf(shareRequest)))
        assertEquals(response.status, HttpStatus.UNPROCESSABLE_ENTITY_422)
    }

    @Test
    fun `invalid email`() {

        val shareRequest =
                RequestShare("reginaldo",
                        listOf(ShareSection("HRReport!A1", listOf("user1")))
                )
        val response = http.post<String>("/api/sheet", ShareRequestDTO(listOf(shareRequest)))
        assertEquals(response.status, HttpStatus.UNPROCESSABLE_ENTITY_422)
    }

    @Test
    fun `get all sheets`() {
        val response = http.get<SharedCreatedRecordsDTO>("/api/sheet")
        assertEquals(response.status, HttpStatus.OK_200)
        assertEquals(0, response.body.sharedRecords.size)


        http.post<String>("/api/sheet", ShareRequestDTO(listOf(createSimpleShareRequest())))
        http.post<String>("/api/sheet", ShareRequestDTO(listOf(createComposeShareRequest())))

        await()
        val responseAfter = http.get<SharedCreatedRecordsDTO>("/api/sheet")
        assertEquals(responseAfter.status, HttpStatus.OK_200)
        assertEquals(10, responseAfter.body.sharedRecords.size)
    }


    companion object {
        private const val WAIT_TIME: Long = 1500
        private fun await(millis: Long = WAIT_TIME) {
            Thread.sleep(millis)
        }

        private val uuidPattern =
                Pattern.compile("\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b")

        private fun createSimpleShareRequest(): RequestShare {
            val user = "reginaldo".concatRand()
            val element = "HRReport!A1"
            val email1 = """${"aaa".concatRand()}@pm.me"""

            return RequestShare(user, listOf(ShareSection(element, listOf(email1))))
        }

        private fun createComposeShareRequest(): RequestShare {
            val user = "fabio".concatRand()

            val element0 = "HRReport!A1"
            val element1 = "'Dashboard'!B2:B5"


            val email0 = """${"bbb".concatRand()}@pm.me"""
            val email1 = """${"ccc".concatRand()}@pm.me"""
            val email2 = """${"ddd".concatRand()}@pm.me"""


            return RequestShare(user,
                    listOf(
                            ShareSection(element0, listOf(email0, email1)),
                            ShareSection(element1, listOf(email2))
                    )
            )
        }

        private fun warmUp(sheetControllerTest: SheetShareControllerTest) {
            sheetControllerTest.runCatching {
                createSimpleShareRequest()
                await()
                createComposeShareRequest()
                await()
            }
        }
    }

}