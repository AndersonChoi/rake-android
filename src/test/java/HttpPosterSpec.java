import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

@RunWith(JUnit4.class)
public class HttpPosterSpec {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8091);

    @Before
    public void setUp() {
        // HttpPoster poster = new HttpPoster("http://localhost:8091");
    }

    @Test
    public void testHttpEngine() {
        // create a mock end-point
        stubFor(get(urlEqualTo("/test"))
                .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "text/html")
                            .withBody("Hello World!")));

        // send a GET request
        String url = "http://localhost:8091/test";

        HttpParams params = new BasicHttpParams();
        HttpClient client = new DefaultHttpClient(params);
        HttpGet req = new HttpGet(url);

        try {
            HttpResponse res = client.execute(req);
            assertEquals(200, res.getStatusLine().getStatusCode());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
