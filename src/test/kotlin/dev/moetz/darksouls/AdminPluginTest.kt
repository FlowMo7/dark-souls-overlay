package dev.moetz.darksouls

import dev.moetz.darksouls.data.ChangeLogger
import dev.moetz.darksouls.data.DataManager
import dev.moetz.darksouls.data.DataSource
import dev.moetz.darksouls.data.DivManager
import dev.moetz.darksouls.plugins.configure
import dev.moetz.darksouls.plugins.configureAdmin
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.server.testing.*
import io.ktor.websocket.*
import org.junit.After
import org.junit.Before
import java.io.File
import java.time.Duration
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
        divManager = DivManager()

        adminUsername = "admin"
        adminPassword = "This1Is2A3Password4"

        publicHostname = "some.hostname.com"
        isSecure = true

    }

    private fun Application.configureForTest() {
        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(15)
        }
        configureAdmin(dataManager, changeLogger, adminUsername, adminPassword, isSecure, publicHostname)
    }

    @After
    fun tearDown() {
        contentFile.delete()
        colorFile.delete()
        logFile.delete()
    }

    @Test
    fun testSetGetAuthorization() {
        withTestApplication({ configureForTest() }) {
            handleRequest(HttpMethod.Get, "/set").apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }
    }

    @Test
    fun testSetGetWithTrailingSlashRedirect() {
        contentFile.delete()
        withTestApplication({ configureForTest() }) {
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
        withTestApplication({ configureForTest() }) {
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
    <title>Dark Souls Overlay</title>
    <script type="text/javascript" src="/static/reconnecting-websocket.min.js"></script>
    <script type="text/javascript" src="/static/admin-script.js"></script>
    <script type="text/javascript">window.onload = function() { initLogWebsocket('wss://some.hostname.com/api/admin/ws/log'); };</script>
    <style>body {font-size: 1em;}.inputfield {border: 1px solid #B8B8B8;padding: 0.8em 0.8em;border-radius: 5px;font-size: 1em;-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;}.inputbutton {color: #FFFFFF;background-color: orange;cursor: pointer;border: 0;border-radius: 5px;padding: 0.8em 1.2em;font-size: 1em;}</style>
  </head>
  <body>
    <h2>Dark Souls Overlay</h2>
    <div>
      <p>The text entered here will be displayed in the stream once submitted by clicking 'Submit'.<br>'\n' can be used to create a line-break.</p>
      <form action="/set" method="post">
        <table>
          <tr>
            <td>Content to Display:</td>
            <td><input type="text" name="content" class="inputfield" value=""></td>
          </tr>
          <tr>
            <td>Font-Color: </td>
            <td><input type="text" name="color" class="inputfield" value="FFFFFF"></td>
            <td><input type="submit" class="inputbutton"></td>
          </tr>
          <tr>
            <td></td>
            <td><i>Only <a href="https://htmlcolorcodes.com/" target="_blank">6-digit HEX color codes </a> are allowed.</i></td>
          </tr>
        </table>
      </form>
<br><br><b>Changelog (last 10 entries):</b>
      <pre>
        <div id="log_content"></div>
      </pre>
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
        withTestApplication({ configureForTest() }) {
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
    <title>Dark Souls Overlay</title>
    <script type="text/javascript" src="/static/reconnecting-websocket.min.js"></script>
    <script type="text/javascript" src="/static/admin-script.js"></script>
    <script type="text/javascript">window.onload = function() { initLogWebsocket('wss://some.hostname.com/api/admin/ws/log'); };</script>
    <style>body {font-size: 1em;}.inputfield {border: 1px solid #B8B8B8;padding: 0.8em 0.8em;border-radius: 5px;font-size: 1em;-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;}.inputbutton {color: #FFFFFF;background-color: orange;cursor: pointer;border: 0;border-radius: 5px;padding: 0.8em 1.2em;font-size: 1em;}</style>
  </head>
  <body>
    <h2>Dark Souls Overlay</h2>
    <div>
      <p>The text entered here will be displayed in the stream once submitted by clicking 'Submit'.<br>'\n' can be used to create a line-break.</p>
      <form action="/set" method="post">
        <table>
          <tr>
            <td>Content to Display:</td>
            <td><input type="text" name="content" class="inputfield" value=""></td>
          </tr>
          <tr>
            <td>Font-Color: </td>
            <td><input type="text" name="color" class="inputfield" value="FFFFFF"></td>
            <td><input type="submit" class="inputbutton"></td>
          </tr>
          <tr>
            <td></td>
            <td><i>Only <a href="https://htmlcolorcodes.com/" target="_blank">6-digit HEX color codes </a> are allowed.</i></td>
          </tr>
        </table>
      </form>
<br><br><b>Changelog (last 10 entries):</b>
      <pre>
        <div id="log_content"></div>
      </pre>
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
        withTestApplication({ configureForTest() }) {
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
    <title>Dark Souls Overlay</title>
    <script type="text/javascript" src="/static/reconnecting-websocket.min.js"></script>
    <script type="text/javascript" src="/static/admin-script.js"></script>
    <script type="text/javascript">window.onload = function() { initLogWebsocket('wss://some.hostname.com/api/admin/ws/log'); };</script>
    <style>body {font-size: 1em;}.inputfield {border: 1px solid #B8B8B8;padding: 0.8em 0.8em;border-radius: 5px;font-size: 1em;-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;}.inputbutton {color: #FFFFFF;background-color: orange;cursor: pointer;border: 0;border-radius: 5px;padding: 0.8em 1.2em;font-size: 1em;}</style>
  </head>
  <body>
    <h2>Dark Souls Overlay</h2>
    <div>
      <p>The text entered here will be displayed in the stream once submitted by clicking 'Submit'.<br>'\n' can be used to create a line-break.</p>
      <form action="/set" method="post">
        <table>
          <tr>
            <td>Content to Display:</td>
            <td><input type="text" name="content" class="inputfield" value="Hello World!"></td>
          </tr>
          <tr>
            <td>Font-Color: </td>
            <td><input type="text" name="color" class="inputfield" value="FFFFFF"></td>
            <td><input type="submit" class="inputbutton"></td>
          </tr>
          <tr>
            <td></td>
            <td><i>Only <a href="https://htmlcolorcodes.com/" target="_blank">6-digit HEX color codes </a> are allowed.</i></td>
          </tr>
        </table>
      </form>
<br><br><b>Changelog (last 10 entries):</b>
      <pre>
        <div id="log_content"></div>
      </pre>
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
        withTestApplication({ configureForTest() }) {
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
    <title>Dark Souls Overlay</title>
    <script type="text/javascript" src="/static/reconnecting-websocket.min.js"></script>
    <script type="text/javascript" src="/static/admin-script.js"></script>
    <script type="text/javascript">window.onload = function() { initLogWebsocket('wss://some.hostname.com/api/admin/ws/log'); };</script>
    <style>body {font-size: 1em;}.inputfield {border: 1px solid #B8B8B8;padding: 0.8em 0.8em;border-radius: 5px;font-size: 1em;-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;}.inputbutton {color: #FFFFFF;background-color: orange;cursor: pointer;border: 0;border-radius: 5px;padding: 0.8em 1.2em;font-size: 1em;}</style>
  </head>
  <body>
    <h2>Dark Souls Overlay</h2>
    <div>
      <p>The text entered here will be displayed in the stream once submitted by clicking 'Submit'.<br>'\n' can be used to create a line-break.</p>
      <form action="/set" method="post">
        <table>
          <tr>
            <td>Content to Display:</td>
            <td><input type="text" name="content" class="inputfield" value="Hello World! äöüÖÄÜß"></td>
          </tr>
          <tr>
            <td>Font-Color: </td>
            <td><input type="text" name="color" class="inputfield" value="FFFFFF"></td>
            <td><input type="submit" class="inputbutton"></td>
          </tr>
          <tr>
            <td></td>
            <td><i>Only <a href="https://htmlcolorcodes.com/" target="_blank">6-digit HEX color codes </a> are allowed.</i></td>
          </tr>
        </table>
      </form>
<br><br><b>Changelog (last 10 entries):</b>
      <pre>
        <div id="log_content"></div>
      </pre>
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
        withTestApplication({ configureForTest() }) {
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
    <title>Dark Souls Overlay</title>
    <script type="text/javascript" src="/static/reconnecting-websocket.min.js"></script>
    <script type="text/javascript" src="/static/admin-script.js"></script>
    <script type="text/javascript">window.onload = function() { initLogWebsocket('wss://some.hostname.com/api/admin/ws/log'); };</script>
    <style>body {font-size: 1em;}.inputfield {border: 1px solid #B8B8B8;padding: 0.8em 0.8em;border-radius: 5px;font-size: 1em;-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;}.inputbutton {color: #FFFFFF;background-color: orange;cursor: pointer;border: 0;border-radius: 5px;padding: 0.8em 1.2em;font-size: 1em;}</style>
  </head>
  <body>
    <h2>Dark Souls Overlay</h2>
    <div>
      <p>The text entered here will be displayed in the stream once submitted by clicking 'Submit'.<br>'\n' can be used to create a line-break.</p>
      <form action="/set" method="post">
        <table>
          <tr>
            <td>Content to Display:</td>
            <td><input type="text" name="content" class="inputfield" value="&amp;lt;i&amp;gt;Hello, world!&amp;lt;/i&amp;gt;&amp;lt;script&amp;gt;alert(&amp;#x27;Test&amp;#x27;);&amp;lt;/script&amp;gt;"></td>
          </tr>
          <tr>
            <td>Font-Color: </td>
            <td><input type="text" name="color" class="inputfield" value="FFFFFF"></td>
            <td><input type="submit" class="inputbutton"></td>
          </tr>
          <tr>
            <td></td>
            <td><i>Only <a href="https://htmlcolorcodes.com/" target="_blank">6-digit HEX color codes </a> are allowed.</i></td>
          </tr>
        </table>
      </form>
<br><br><b>Changelog (last 10 entries):</b>
      <pre>
        <div id="log_content"></div>
      </pre>
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
        withTestApplication({ configureForTest() }) {
            handleRequest(HttpMethod.Post, "/set").apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }
    }

}