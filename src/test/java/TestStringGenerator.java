import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class TestStringGenerator {

    @Test
    public void testGen() {
        Map<Integer, String> map = new HashMap<>();

        map.put(1, "Val");
        map.put(2, "Kek");

        map.put(1, "Val 2");

        System.out.println(map.remove(1));
    }

}