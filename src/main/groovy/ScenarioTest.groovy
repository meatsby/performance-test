import HTTPClient.Cookie
import groovy.json.JsonSlurper
import net.grinder.script.GTest
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.ngrinder.http.HTTPRequest
import org.ngrinder.http.HTTPRequestControl
import org.ngrinder.http.HTTPResponse

import static net.grinder.script.Grinder.grinder
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

@RunWith(GrinderRunner)
class ScenarioTest {

    public static GTest test1
    public static GTest test2
    public static GTest test3
    public static GTest test4
    public static HTTPRequest request
    public static Map<String, String> headers = [:]
    public static String body = "{\n    \"password\" : \"0000\"\n}"

    private String accessToken

    @BeforeProcess
    static void beforeProcess() {
        HTTPRequestControl.setConnectionTimeout(300000)
        test1 = new GTest(1, "POST /api/hosts/aWonJbS5IHeNeuUVJ2yqOw/enter")
        test2 = new GTest(2, "GET  /api/spaces")
        test3 = new GTest(3, "GET  /api/spaces/42/jobs")
        test4 = new GTest(4, "GET  /api/jobs/164/active")
        request = new HTTPRequest()
        grinder.logger.info("before process.")
    }

    @BeforeThread
    void beforeThread() {
        test1.record(this, "test1")
        test2.record(this, "test2")
        test3.record(this, "test2")
        test4.record(this, "test2")

        grinder.statistics.delayReports = true
        grinder.logger.info("before thread.")
    }

    @Before
    void before() {
        request.setHeaders(headers)
        grinder.logger.info("before. init headers and cookies")
    }

    @Test
    void test1() {
        request.setHeaders(headers)
        HTTPResponse response = request.POST("https://dev.gongcheck.shop/api/hosts/aWonJbS5IHeNeuUVJ2yqOw/enter", body.getBytes())
        def slurper = new JsonSlurper()
        def toJSON = { slurper.parseText(it) }
        def result = response.getBody(toJSON);
        accessToken = result.token
        if (response.statusCode == 301 || response.statusCode == 302) {
            grinder.logger.warn("Warning. The response may not be correct. The response code was {}.", response.statusCode)
        } else {
            assertThat(response.statusCode, is(200))
        }
        grinder.logger.info("POST(https://dev.gongcheck.shop/api/hosts/aWonJbS5IHeNeuUVJ2yqOw/enter) 완료")
    }

    @Test
    void test2() {
        headers.put("Authorization", "Bearer " + accessToken)
        request.setHeaders(headers)
        HTTPResponse response = request.GET("https://dev.gongcheck.shop/api/spaces")

        if (response.statusCode == 301 || response.statusCode == 302) {
            grinder.logger.warn("Warning. The response may not be correct. The response code was {}.", response.statusCode)
        } else {
            assertThat(response.statusCode, is(200))
        }
        grinder.logger.info("GET(https://dev.gongcheck.shop/api/spaces) 완료")
    }

    @Test
    void test3() {
        headers.put("Authorization", "Bearer " + accessToken)
        request.setHeaders(headers)
        HTTPResponse response = request.GET("https://dev.gongcheck.shop/api/spaces/42/jobs")
        if (response.statusCode == 301 || response.statusCode == 302) {
            grinder.logger.warn("Warning. The response may not be correct. The response code was {}.", response.statusCode)
        } else {
            assertThat(response.statusCode, is(200))
        }
        grinder.logger.info("GET(https://dev.gongcheck.shop/api/spaces/42/jobs) 완료")
    }
    private boolean active

    @Test
    void test4() {
        headers.put("Authorization", "Bearer " + accessToken)
        request.setHeaders(headers)
        HTTPResponse response = request.GET("https://dev.gongcheck.shop/api/jobs/164/active")
        def slurper = new JsonSlurper()
        def toJSON = { slurper.parseText(it) }
        def result = response.getBody(toJSON);
        active = result.active
        if (response.statusCode == 301 || response.statusCode == 302) {
            grinder.logger.warn("Warning. The response may not be correct. The response code was {}.", response.statusCode)
        } else {
            assertThat(response.statusCode, is(200))
        }
        grinder.logger.info("GET(https://dev.gongcheck.shop/api/jobs/164/active) 완료")
    }
}
