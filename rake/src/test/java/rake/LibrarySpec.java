package rake;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class LibrarySpec {

    @Test
    public void test_exception() {
        Throwable e = new RuntimeException("acac");

        System.out.println(e.getCause());
        System.out.println(e.getMessage());
        e.printStackTrace();
    }


}
