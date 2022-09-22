import com.launchdarkly.eventsource.EventHandler
import com.launchdarkly.eventsource.EventSource
import com.launchdarkly.eventsource.MessageEvent
import net.grinder.script.GTest
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.AfterThread
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread
import okhttp3.Headers
import org.junit.Test
import org.junit.runner.RunWith
import org.ngrinder.http.HTTPRequest

import static net.grinder.script.Grinder.grinder

@RunWith(GrinderRunner)
class FlipTest {

    private static GTest test;
    private static HTTPRequest request;
    private static Map<String, String> headers = [:]
    private static SimpleEventHandler eventHandler
    private static EventSource eventSource

    // This method is executed once per a process.
    @BeforeProcess
    static void beforeClass() {
        test = new GTest(1, "test1");
        request = new HTTPRequest()
//        HTTPResponse response = request.POST("https://www.gongcheck.shop/enter/aWonJbS5IHeNeuUVJ2yqOw/pwd", "{\n    \"password\" : \"0000\"\n}".getBytes())
//        println(response)
//        JsonSlurper slurper = new JsonSlurper()
//        String json = slurper.parseText(response.getBody())
//        JSONObject accessToken = json.token
        headers.put("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI4IiwiaWF0IjoxNjYzODMyMjg4LCJleHAiOjE2NjM4NDMwODgsImF1dGhvcml0eSI6IkdVRVNUIn0.-flW-OverJT8-2o6Mx_XtZ6Nlwn_JtHiM0yx8m3dKjM")
        request.setHeaders(headers)
        test.record(request);
        grinder.logger.info("before process.");
    }

    // This method is executed once per a thread.
    @BeforeThread
    void beforeThread() {
        grinder.statistics.delayReports = true
        grinder.logger.info("before thread.")
        eventHandler = new SimpleEventHandler()
        eventSource = new EventSource.Builder(eventHandler, URI.create("https://dev.gongcheck.shop/api/jobs/164/runningTasks/connect"))
                .headers(Headers.of(headers))
                .build()
        eventSource.start()
    }

    // This method is continuously executed until you stop the test
    @Test
    void test() {
        request.POST("https://dev.gongcheck.shop/api/tasks/1422/flip")
        request.POST("https://dev.gongcheck.shop/api/tasks/1423/flip")
    }

    @AfterThread
    void afterThread() {
        // request.POST("https://dev.gongcheck.shop/api/jobs/164/complete", Map.of("author", "Submission"))
        eventSource.close()

        for (String respRecord : eventHandler.respList) {
            grinder.logger.info("result: " + respRecord)
        }
    }

    class SimpleEventHandler implements EventHandler {

        public List<String> respList = new ArrayList<String>();

        @Override
        void onOpen() throws Exception {
            grinder.logger.info("The connection has been opened");
        }

        @Override
        void onClosed() throws Exception {
            grinder.logger.info("The connection has been closed");
        }

        @Override
        void onMessage(final String event, final MessageEvent messageEvent) throws Exception {
            respList.add(messageEvent.getData());
        }

        @Override
        void onComment(final String comment) throws Exception {
            grinder.logger.info(comment);
        }

        @Override
        void onError(final Throwable t) {
            grinder.logger.info("Error " + t);
        }
    };
}
