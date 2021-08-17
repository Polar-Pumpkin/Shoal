import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignGetterTest {

    private final static Pattern KEY_WORD_PATTERN = Pattern.compile("\\{(.*)}");

    private final static List<String> identifies = new ArrayList<>(Arrays.asList(
            "测试",
            "测试内容捕捉 {test1}",
            "抓 {test2}"
    ));

    private final static List<String> lores = new ArrayList<>(Arrays.asList(
            "",
            "呜呜呜",
            "测试",
            "测试内容捕捉 ???",
            "抓 owo",
            "!!!"
    ));

    public static Map<String, String> getContent(final List<String> lores) {
        final Map<String, String> result = new HashMap<>();
        int lastMatchIndex = -1;
        identify_Loop:
        for (String identify : identifies) {
            final Matcher keywordMatcher = KEY_WORD_PATTERN.matcher(identify);
            final boolean hasVariable = keywordMatcher.find();
            String keyword = null;
            Pattern pattern = null;
            if (hasVariable) {
                keyword = keywordMatcher.group(1);
                pattern = Pattern.compile(identify.replace("{" + keyword + "}", "(.*)"));
            }

            if (lastMatchIndex != -1) {
                if (lastMatchIndex + 1 >= lores.size()) {
                    break;
                }
                String lore = lores.get(lastMatchIndex + 1);
                if (hasVariable) {
                    final Matcher loreMatcher = pattern.matcher(lore);
                    if (!loreMatcher.find()) {
                        break;
                    }
                    result.put(keyword, loreMatcher.group(1));
                } else {
                    if (!lore.equals(identify)) {
                        break;
                    }
                }
                lastMatchIndex++;
            } else {
                for (String lore : lores) {
                    final int index = lores.indexOf(lore);

                    if (hasVariable) {
                        final Matcher loreMatcher = pattern.matcher(lore);
                        if (!loreMatcher.find()) {
                            continue;
                        }
                        result.put(keyword, loreMatcher.group());
                    } else {
                        if (!lore.equals(identify)) {
                            continue;
                        }
                    }
                    lastMatchIndex = index;
                    continue identify_Loop;
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(getContent(lores).toString());
    }

}
