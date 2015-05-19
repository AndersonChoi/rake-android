package rake;

import com.rake.android.rkmetrics.RakeAPI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class RakeAPISpec {

    DummyContext dummyContext;
    String dummyToken;
    Boolean useDevServer;

    @Before
    public void setUp() {
        dummyContext = new DummyContext();
        dummyToken = "exampleToken";
        useDevServer = true;
    }

    @Test
    public void testCreateRakeInstance() {
        RakeAPI rake = RakeAPI.getInstance(dummyContext, dummyToken, useDevServer);

        assertEquals(true, true);
    }
}
