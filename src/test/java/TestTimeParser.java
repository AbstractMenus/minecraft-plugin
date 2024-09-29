import org.junit.jupiter.api.Test;
import ru.abstractmenus.util.TimeParser;

public class TestTimeParser {

    @Test
    public void testZeroSeconds() {
        TimeParser parser = new TimeParser();

        System.out.println(parser.toString(999999));
        System.out.println(parser.toString(1000));
        System.out.println(parser.toString(500));
        System.out.println(parser.toString(0));
    }

}
