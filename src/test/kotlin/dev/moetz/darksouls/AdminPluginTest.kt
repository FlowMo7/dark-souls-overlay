package dev.moetz.darksouls

import dev.moetz.darksouls.data.ChangeLogger
import dev.moetz.darksouls.data.DataManager
import dev.moetz.darksouls.data.DataSource
import dev.moetz.darksouls.data.DivManager
import dev.moetz.darksouls.plugins.configure
import dev.moetz.darksouls.plugins.configureAdmin
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.After
import org.junit.Before
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class AdminPluginTest {

    private lateinit var contentFile: File
    private lateinit var colorFile: File
    private lateinit var logFile: File
    private lateinit var dataSource: DataSource
    private lateinit var changeLogger: ChangeLogger
    private lateinit var dataManager: DataManager
    private lateinit var divManager: DivManager
    private lateinit var adminUsername: String
    private lateinit var adminPassword: String

    private lateinit var publicHostname: String
    private var isSecure: Boolean = true

    @Before
    fun setUp() {
        contentFile = File("./test_content_${this.hashCode()}")
        colorFile = File("./test_color_${this.hashCode()}")
        logFile = File("./test_log_${this.hashCode()}")

        dataSource = DataSource(contentFile = contentFile, colorFile = colorFile)
        changeLogger = ChangeLogger(logFile = logFile)
        dataManager = DataManager(dataSource, changeLogger)
        divManager = DivManager("/static/font/EBGaramond.ttf")

        adminUsername = "admin"
        adminPassword = "This1Is2A3Password4"

        publicHostname = "some.hostname.com"
        isSecure = true

    }

    @After
    fun tearDown() {
        contentFile.delete()
        colorFile.delete()
        logFile.delete()
    }

    @Test
    fun testSetGetAuthorization() {
        withTestApplication({ configureAdmin(dataManager, adminUsername, adminPassword) }) {
            handleRequest(HttpMethod.Get, "/set").apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }
    }

    @Test
    fun testSetGetWithTrailingSlashRedirect() {
        contentFile.delete()
        withTestApplication({ configureAdmin(dataManager, adminUsername, adminPassword) }) {
            handleRequest(
                method = HttpMethod.Get,
                uri = "/set/",
                setup = { addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==") }).apply {

                assertEquals(HttpStatusCode.Found, response.status())
                assertEquals("/set", response.headers["Location"])
            }
        }
    }

    @Test
    fun testSetGetWithNonExistingFile() {
        contentFile.delete()
        withTestApplication({ configureAdmin(dataManager, adminUsername, adminPassword) }) {
            handleRequest(
                method = HttpMethod.Get,
                uri = "/set",
                setup = { addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==") }).apply {


                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(
                    """<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <title>Set the Dark Souls Counter Text</title>
  </head>
  <body>
    <div>
      <p>This is the console for the dark souls stream overlay.<br>The Text entered here will be displayed in the stream once submitted by clicking 'Submit'.<br>'\n' can be used to create a line-break.</p>
      <form action="/set" method="post"><input type="text" name="content" value=""><input type="submit"><br><br><br>Additionally, the font-color can be adjusted here.<br>Only 6-digit HEX color codes are allowed. Will be updated by clicking 'Submit' (above) as well.<br>#<input type="text" name="color" value="FFFFFF"></form>
    </div>
  </body>
</html>
""", response.content
                )
            }
        }
    }

    @Test
    fun testSetGetWithEmptyFile() {
        contentFile.writeText("")
        withTestApplication({ configureAdmin(dataManager, adminUsername, adminPassword) }) {
            handleRequest(
                method = HttpMethod.Get,
                uri = "/set",
                setup = { addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==") }).apply {


                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(
                    """<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <title>Set the Dark Souls Counter Text</title>
  </head>
  <body>
    <div>
      <p>This is the console for the dark souls stream overlay.<br>The Text entered here will be displayed in the stream once submitted by clicking 'Submit'.<br>'\n' can be used to create a line-break.</p>
      <form action="/set" method="post"><input type="text" name="content" value=""><input type="submit"><br><br><br>Additionally, the font-color can be adjusted here.<br>Only 6-digit HEX color codes are allowed. Will be updated by clicking 'Submit' (above) as well.<br>#<input type="text" name="color" value="FFFFFF"></form>
    </div>
  </body>
</html>
""", response.content
                )
            }
        }
    }

    @Test
    fun testSetGetWithNonEmptyFileHelloWorld() {
        contentFile.writeText("Hello World!")
        withTestApplication({ configureAdmin(dataManager, adminUsername, adminPassword) }) {
            handleRequest(
                method = HttpMethod.Get,
                uri = "/set",
                setup = { addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==") }).apply {


                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(
                    """<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <title>Set the Dark Souls Counter Text</title>
  </head>
  <body>
    <div>
      <p>This is the console for the dark souls stream overlay.<br>The Text entered here will be displayed in the stream once submitted by clicking 'Submit'.<br>'\n' can be used to create a line-break.</p>
      <form action="/set" method="post"><input type="text" name="content" value="Hello World!"><input type="submit"><br><br><br>Additionally, the font-color can be adjusted here.<br>Only 6-digit HEX color codes are allowed. Will be updated by clicking 'Submit' (above) as well.<br>#<input type="text" name="color" value="FFFFFF"></form>
    </div>
  </body>
</html>
""", response.content
                )
            }
        }
    }

    @Test
    fun testSetGetWithNonEmptyFileWithUmlaute() {
        contentFile.writeText("Hello World! äöüÖÄÜß")
        withTestApplication({ configureAdmin(dataManager, adminUsername, adminPassword) }) {
            handleRequest(
                method = HttpMethod.Get,
                uri = "/set",
                setup = { addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==") }).apply {


                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(
                    """<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <title>Set the Dark Souls Counter Text</title>
  </head>
  <body>
    <div>
      <p>This is the console for the dark souls stream overlay.<br>The Text entered here will be displayed in the stream once submitted by clicking 'Submit'.<br>'\n' can be used to create a line-break.</p>
      <form action="/set" method="post"><input type="text" name="content" value="Hello World! äöüÖÄÜß"><input type="submit"><br><br><br>Additionally, the font-color can be adjusted here.<br>Only 6-digit HEX color codes are allowed. Will be updated by clicking 'Submit' (above) as well.<br>#<input type="text" name="color" value="FFFFFF"></form>
    </div>
  </body>
</html>
""", response.content
                )
            }
        }
    }

    @Test
    fun testSetGetWithNonEmptyFileWithHtmlCharacters() {
        contentFile.writeText("<i>Hello, world!</i><script>alert('Test');</script>")
        withTestApplication({ configureAdmin(dataManager, adminUsername, adminPassword) }) {
            handleRequest(
                method = HttpMethod.Get,
                uri = "/set",
                setup = { addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==") }).apply {


                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(
                    """<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <title>Set the Dark Souls Counter Text</title>
  </head>
  <body>
    <div>
      <p>This is the console for the dark souls stream overlay.<br>The Text entered here will be displayed in the stream once submitted by clicking 'Submit'.<br>'\n' can be used to create a line-break.</p>
      <form action="/set" method="post"><input type="text" name="content" value="&amp;lt;i&amp;gt;Hello, world!&amp;lt;/i&amp;gt;&amp;lt;script&amp;gt;alert(&amp;#x27;Test&amp;#x27;);&amp;lt;/script&amp;gt;"><input type="submit"><br><br><br>Additionally, the font-color can be adjusted here.<br>Only 6-digit HEX color codes are allowed. Will be updated by clicking 'Submit' (above) as well.<br>#<input type="text" name="color" value="FFFFFF"></form>
    </div>
  </body>
</html>
""", response.content
                )
            }
        }
    }


    @Test
    fun testSetPostAuthorization() {
        withTestApplication({ configureAdmin(dataManager, adminUsername, adminPassword) }) {
            handleRequest(HttpMethod.Post, "/set").apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }
    }

//    @Test
//    fun testSetPostWithMissingParameter() {
//        contentFile.writeText("")
//        colorFile.writeText("")
//        withTestApplication({ configureAdmin(dataSource, changeLogger, adminUsername, adminPassword) }) {
//            handleRequest(
//                method = HttpMethod.Post,
//                uri = "/set",
//                setup = { addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==") }).apply {
//
//
//                assertEquals(HttpStatusCode.Found, response.status())
//                assertEquals("/set", response.headers["Location"])
//            }
//
//            assertEquals("", contentFile.readText())
//            assertEquals("", colorFile.readText())
//
//            handleRequest(
//                method = HttpMethod.Get,
//                uri = "/content"
//            ).apply {
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals("<div style=\"color:#FFFFFF;\"></div>", response.content)
//            }
//        }
//    }
//
//    @Test
//    fun testSetPostWithEmptyParameter1() {
//        contentFile.writeText("")
//        colorFile.writeText("")
//        withTestApplication({ configureAdmin(dataSource, changeLogger, adminUsername, adminPassword) }) {
//            handleRequest(
//                method = HttpMethod.Post,
//                uri = "/set",
//                setup = {
//                    addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==")
//                    setBody("content=")
//                }).apply {
//
//
//                assertEquals(HttpStatusCode.Found, response.status())
//                assertEquals("/set", response.headers["Location"])
//            }
//
//            assertEquals("", contentFile.readText())
//
//            handleRequest(
//                method = HttpMethod.Get,
//                uri = "/content"
//            ).apply {
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals("<div style=\"color:#FFFFFF;\"></div>", response.content)
//            }
//        }
//    }
//
//    @Test
//    fun testSetPostWithEmptyParameter2() {
//        contentFile.writeText("")
//        colorFile.writeText("")
//        withTestApplication({ configureAdmin(dataSource, changeLogger, adminUsername, adminPassword) }) {
//            handleRequest(
//                method = HttpMethod.Post,
//                uri = "/set",
//                setup = {
//                    addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==")
//                    setBody("content=&color=")
//                }).apply {
//
//
//                assertEquals(HttpStatusCode.Found, response.status())
//                assertEquals("/set", response.headers["Location"])
//            }
//
//            assertEquals("", contentFile.readText())
//            assertEquals("", colorFile.readText())
//
//            handleRequest(
//                method = HttpMethod.Get,
//                uri = "/content"
//            ).apply {
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals("<div style=\"color:#FFFFFF;\"></div>", response.content)
//            }
//        }
//    }
//
//    @Test
//    fun testSetPostWithEmptyParameter3() {
//        contentFile.writeText("")
//        colorFile.writeText("")
//        withTestApplication({ configureAdmin(dataSource, changeLogger, adminUsername, adminPassword) }) {
//            handleRequest(
//                method = HttpMethod.Post,
//                uri = "/set",
//                setup = {
//                    addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==")
//                    setBody("color=")
//                }).apply {
//
//
//                assertEquals(HttpStatusCode.Found, response.status())
//                assertEquals("/set", response.headers["Location"])
//            }
//
//            assertEquals("", contentFile.readText())
//            assertEquals("", colorFile.readText())
//
//            handleRequest(
//                method = HttpMethod.Get,
//                uri = "/content"
//            ).apply {
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals("<div style=\"color:#FFFFFF;\"></div>", response.content)
//            }
//        }
//    }
//
//    @Test
//    fun testSetPostWithEmptyParameter4() {
//        contentFile.writeText("")
//        colorFile.writeText("")
//        withTestApplication({ configureAdmin(dataSource, changeLogger, adminUsername, adminPassword) }) {
//            handleRequest(
//                method = HttpMethod.Post,
//                uri = "/set",
//                setup = {
//                    addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==")
//                    setBody("color=&content=")
//                }).apply {
//
//
//                assertEquals(HttpStatusCode.Found, response.status())
//                assertEquals("/set", response.headers["Location"])
//            }
//
//            assertEquals("", contentFile.readText())
//            assertEquals("", colorFile.readText())
//
//            handleRequest(
//                method = HttpMethod.Get,
//                uri = "/content"
//            ).apply {
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals("<div style=\"color:#FFFFFF;\"></div>", response.content)
//            }
//        }
//    }
//
//    @Test
//    fun testSetPostWithColorEmpty() {
//        contentFile.writeText("")
//        colorFile.writeText("")
//        withTestApplication({ configureAdmin(dataSource, changeLogger, adminUsername, adminPassword) }) {
//            handleRequest(
//                method = HttpMethod.Post,
//                uri = "/set",
//                setup = {
//                    addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==")
//                    addHeader("Content-Type", "application/x-www-form-urlencoded")
//                    setBody("content=Test&color=")
//                }).apply {
//
//
//                assertEquals(HttpStatusCode.Found, response.status())
//                assertEquals("/set", response.headers["Location"])
//            }
//
//            assertEquals("Test", contentFile.readText())
//            assertEquals("", colorFile.readText())
//
//            handleRequest(
//                method = HttpMethod.Get,
//                uri = "/content"
//            ).apply {
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals("<div style=\"color:#FFFFFF;\">Test</div>", response.content)
//            }
//        }
//    }
//
//    @Test
//    fun testSetPostWithColorInvalid1() {
//        contentFile.writeText("")
//        colorFile.writeText("")
//        withTestApplication({ configureAdmin(dataSource, changeLogger, adminUsername, adminPassword) }) {
//            handleRequest(
//                method = HttpMethod.Post,
//                uri = "/set",
//                setup = {
//                    addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==")
//                    addHeader("Content-Type", "application/x-www-form-urlencoded")
//                    setBody("content=Test&color=ASD")
//                }).apply {
//
//
//                assertEquals(HttpStatusCode.Found, response.status())
//                assertEquals("/set", response.headers["Location"])
//            }
//
//            assertEquals("Test", contentFile.readText())
//            assertEquals("", colorFile.readText())
//
//            handleRequest(
//                method = HttpMethod.Get,
//                uri = "/content"
//            ).apply {
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals("<div style=\"color:#FFFFFF;\">Test</div>", response.content)
//            }
//        }
//    }
//
//    @Test
//    fun testSetPostWithColorInvalid2() {
//        contentFile.writeText("")
//        colorFile.writeText("")
//        withTestApplication({ configureAdmin(dataSource, changeLogger, adminUsername, adminPassword) }) {
//            handleRequest(
//                method = HttpMethod.Post,
//                uri = "/set",
//                setup = {
//                    addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==")
//                    addHeader("Content-Type", "application/x-www-form-urlencoded")
//                    setBody("content=Test&color=<script>alert('Test');</script>")
//                }).apply {
//
//
//                assertEquals(HttpStatusCode.Found, response.status())
//                assertEquals("/set", response.headers["Location"])
//            }
//
//            assertEquals("Test", contentFile.readText())
//            assertEquals("", colorFile.readText())
//
//            handleRequest(
//                method = HttpMethod.Get,
//                uri = "/content"
//            ).apply {
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals("<div style=\"color:#FFFFFF;\">Test</div>", response.content)
//            }
//        }
//    }
//
//    @Test
//    fun testSetPostWithHelloWorldParameter() {
//        contentFile.writeText("")
//        colorFile.writeText("")
//        withTestApplication({ configureAdmin(dataSource, changeLogger, adminUsername, adminPassword) }) {
//            handleRequest(
//                method = HttpMethod.Post,
//                uri = "/set",
//                setup = {
//                    addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==")
//                    addHeader("Content-Type", "application/x-www-form-urlencoded")
//                    setBody("content=Hello+World&color=FFFFFF")
//                }).apply {
//
//                assertEquals(HttpStatusCode.Found, response.status())
//                assertEquals("/set", response.headers["Location"])
//            }
//
//            assertEquals("Hello World", contentFile.readText())
//            assertEquals("FFFFFF", colorFile.readText())
//
//            handleRequest(
//                method = HttpMethod.Get,
//                uri = "/content"
//            ).apply {
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals("<div style=\"color:#FFFFFF;\">Hello World</div>", response.content)
//            }
//        }
//    }
//
//    @Test
//    fun testSetPostWithHtml() {
//        contentFile.writeText("")
//        colorFile.writeText("")
//        withTestApplication({ configureAdmin(dataSource, changeLogger, adminUsername, adminPassword) }) {
//            handleRequest(
//                method = HttpMethod.Post,
//                uri = "/set",
//                setup = {
//                    addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==")
//                    addHeader("Content-Type", "application/x-www-form-urlencoded")
//                    setBody("content=<i>Hello World!</i>&color=AFAFAF")
//                }).apply {
//
//                assertEquals(HttpStatusCode.Found, response.status())
//                assertEquals("/set", response.headers["Location"])
//            }
//
//            assertEquals("<i>Hello World!</i>", contentFile.readText())
//            assertEquals("AFAFAF", colorFile.readText())
//
//            handleRequest(
//                method = HttpMethod.Get,
//                uri = "/content"
//            ).apply {
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals("<div style=\"color:#AFAFAF;\">&lt;i&gt;Hello World!&lt;/i&gt;</div>", response.content)
//            }
//        }
//    }
//
//    @Test
//    fun testSetPostWithAlertInjectionTest() {
//        contentFile.writeText("")
//        withTestApplication({ configureAdmin(dataSource, changeLogger, adminUsername, adminPassword) }) {
//            handleRequest(
//                method = HttpMethod.Post,
//                uri = "/set",
//                setup = {
//                    addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==")
//                    addHeader("Content-Type", "application/x-www-form-urlencoded")
//                    setBody("content=<script>alert('Hello');</script>&color=000000")
//                }).apply {
//
//                assertEquals(HttpStatusCode.Found, response.status())
//                assertEquals("/set", response.headers["Location"])
//            }
//
//            assertEquals("<script>alert('Hello');</script>", contentFile.readText())
//            assertEquals("000000", colorFile.readText())
//
//            handleRequest(
//                method = HttpMethod.Get,
//                uri = "/content"
//            ).apply {
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals("<div style=\"color:#000000;\">&lt;script&gt;alert(&#x27;Hello&#x27;);&lt;/script&gt;</div>", response.content)
//            }
//        }
//    }

}