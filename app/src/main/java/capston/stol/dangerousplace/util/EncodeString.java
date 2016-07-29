package capston.stol.dangerousplace.util;

import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Created by 이수정 on 2016-01-04.
 */
public class EncodeString {
    private static StringBuffer sb;
    private static Enumeration names;

    public static String EncodeString(Properties params) {
        sb = new StringBuffer(256);
        names = params.propertyNames();

        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            String value = params.getProperty(name);
            sb.append(URLEncoder.encode(name) + "=" + URLEncoder.encode(value) );

            if (names.hasMoreElements()) sb.append("&");
        }
        return sb.toString();
    }
}
