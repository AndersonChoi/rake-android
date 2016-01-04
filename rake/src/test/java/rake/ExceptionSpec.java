package rake;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

@RunWith(JUnit4.class)
public class ExceptionSpec {

    @Test
    public void test_getExceptionType() {
        try {
            throwIOException();
            fail("should not be here");
        } catch (Exception e) {
            assertThat(e.getClass().getCanonicalName()).isEqualTo("java.io.IOException");
        }
    }

    public void throwIOException() throws IOException {
        throw new IOException("e");
    }
}


