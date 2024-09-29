package ru.abstractmenus.util.proxy;

/**
 * From <a href="https://github.com/SkinsRestorer/SkinsRestorer/blob/dev/shared/src/main/java/net/skinsrestorer/shared/utils/ReflectionUtil.java">SkinRestorer</a>
 */
public class ReflectionUtil {

    private ReflectionUtil() {
    }

    public static boolean classExists(String clazz) {
        try {
            Class.forName(clazz);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean classExists(String... classNames) {
        for (String className : classNames) {
            if (classExists(className)) {
                return true;
            }
        }

        return false;
    }

}