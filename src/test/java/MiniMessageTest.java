import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.junit.jupiter.api.Test;
import ru.abstractmenus.api.text.Colors;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiniMessageTest {

    private static final char legacyChar = LegacyComponentSerializer.SECTION_CHAR;
    private static final Pattern colorPattern = Pattern.compile(legacyChar + "([0-9a-fk-rx])");
    private static final Map<String, String> colorTags = new HashMap<>();

    static {
        colorTags.put("0", "<black>");
        colorTags.put("1", "<dark_blue>");
        colorTags.put("2", "<dark_green>");
        colorTags.put("3", "<dark_aqua>");
        colorTags.put("4", "<dark_red>");
        colorTags.put("5", "<dark_purple>");
        colorTags.put("6", "<gold>");
        colorTags.put("7", "<gray>");
        colorTags.put("8", "<dark_gray>");
        colorTags.put("9", "<blue>");
        colorTags.put("a", "<green>");
        colorTags.put("b", "<aqua>");
        colorTags.put("c", "<red>");
        colorTags.put("d", "<light_purple>");
        colorTags.put("e", "<yellow>");
        colorTags.put("f", "<white>");
        colorTags.put("k", "<obf>");
        colorTags.put("l", "<b>");
        colorTags.put("m", "<st>");
        colorTags.put("n", "<u>");
        colorTags.put("o", "<i>");
        colorTags.put("r", "<reset>");
    }

    @Test
    public void testLegacy() {
        Colors.init(true);
        String str = Colors.of("&0Hello&1World");
        System.out.println(replaceLegacyTags(str));
    }

    private String replaceLegacyTags(String input) {
        String replaced = input;
        Matcher matcher = colorPattern.matcher(replaced);

        while (matcher.find()) {
            String code = matcher.group(1);
            String replacement = colorTags.get(code);

            System.out.println("Code = " + code);
            System.out.println("Replacement = " + replaced);

            if (replacement != null) {
                String before = replaced.substring(0, matcher.start());
                String after = replaced.substring(matcher.end());
                StringBuilder result = new StringBuilder(before);

                replaced = result.append(replacement)
                        .append(after)
                        .toString();
            }
            matcher = colorPattern.matcher(replaced);
        }

        return replaced;
    }

}
