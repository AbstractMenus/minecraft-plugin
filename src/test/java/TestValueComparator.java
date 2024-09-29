import org.junit.jupiter.api.Test;

public class TestValueComparator {

    @Test
    public void testComparator() {
        String str = "petya";
        change(str);
        System.out.println(str);
    }

    private void change(String str) {
        str = "vasya";
    }

}
