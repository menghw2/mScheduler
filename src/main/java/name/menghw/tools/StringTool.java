package name.menghw.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author: menghw
 * @create: 2024/7/2
 * @Description:
 */
public class StringTool {

    /**
     * 判断两个字符串是否相等 如果都为null则判断为相等,一个为null另一个not null则判断不相等 否则如果s1=s2则相等
     *
     * @param s1 参数1
     * @param s2 参数2
     * @return 是否
     */
    public static boolean equals(Object s1, Object s2) {
        if (!(s1 instanceof String)) {
            return false;
        }
        if (null != s1 && null != s2) {
            return s1.equals(s2);
        }
        return false;
    }


    /**
     * 获取一个对象的字符串的值，如果该字符串为空则取默认值
     *
     * @param value 对象
     * @param defaultValue 默认值
     * @return 对象的字符串表示
     */
    public static final String getOrElse(Object value, String defaultValue) {
        if (value != null && !isEmpty(value.toString())) {
            return value.toString();
        }
        else if (!isEmpty(defaultValue)) {
            return defaultValue;
        }
        return "";
    }

    /**
     * 获取一个对象的字符串的值，如果该字符串为空则取默认值
     *
     * @param value 对象
     * @param supplier 默认值,延迟计算
     * @return 对象的字符串表示
     */
    public static final String getOrElse(Object value, Supplier<String> supplier) {
        if (value != null && !isEmpty(value.toString())) {
            return value.toString();
        }
        else if (supplier != null) {
            return supplier.get();
        }
        return "";
    }


    /**
     * 自定义的分隔字符串函数 例如: 1,2,3 =&gt; [1,2,3] 3个元素 ,2,3 =&gt; [,2,3] 3个元素 ,2,3,=&gt;[,2,3,] 4个元素
     * ,,,=&gt;[,,,] 4个元素 5.22算法修改，为提高速度不用正则表达式 两个间隔符,,返回""元素（空字符串或者null会返回空的集合）
     *
     * @param split 分割字符 默认,
     * @param src 输入字符串
     * @return 分隔后的list
     * @author Robin
     */
    public static List<String> splitToList(String split, String src) {
        if (isEmpty(src)) {
            return new ArrayList<>();
        }
        // 默认,
        String sp = ",";
        if (split != null && split.length() == 1) {
            sp = split;
        }
        List<String> r = new ArrayList<String>();
        int lastIndex = -1;
        int index = src.indexOf(sp);
        if (-1 == index && src != null) {
            r.add(src);
            return r;
        }
        while (index >= 0) {
            if (index > lastIndex) {
                r.add(src.substring(lastIndex + 1, index));
            }
            else {
                r.add("");
            }

            lastIndex = index;
            index = src.indexOf(sp, index + 1);
            if (index == -1) {
                r.add(src.substring(lastIndex + 1, src.length()));
            }
        }
        return r;
    }

    /**
     * Description:判断list是否为空
     *
     * @param list 需要判断的list
     * @return 是否为空
     */
    public static boolean isEmptyList(List<?> list) {
        if (list == null || list.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * 将list转为指定分隔符分割的字符串
     *
     * @param list 列表
     * @param delimiter 分隔符
     * @return 字符串
     * @see
     * @since 1.0
     */
    public static String joinString(List<String> list, CharSequence delimiter) {
        return list.stream().collect(Collectors.joining(delimiter));
    }

    /**
     * 将list转为指定分隔符分割的字符串，同时提供前缀和后缀
     *
     * @param list 列表
     * @param delimiter 分隔符
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 字符串
     * @see
     * @since 1.0
     */
    public static String joinString(List<String> list, CharSequence delimiter, CharSequence prefix,
                                    CharSequence suffix) {
        return list.stream().collect(Collectors.joining(delimiter, prefix, suffix));
    }

    /**
     * 存文本替换
     *
     * @param s 源字符串
     * @param sf 子字符串
     * @param sb 替换字符串
     * @return 替换后的字符串
     */
    public static String replaceAll(String s, String sf, String sb) {
        int i = 0;
        int j = 0;
        int l = sf.length();
        boolean b = true;
        boolean o = true;
        String str = "";
        do {
            j = i;
            i = s.indexOf(sf, j);
            if (i > j) {
                str += s.substring(j, i);
                str += sb;
                i += l;
                o = false;
            }
            else {
                str += s.substring(j);
                b = false;
            }
        }
        while (b);
        if (o) {
            str = s;
        }
        return str;
    }
    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static String shortMethod(String method) {
        if (method == null)
            return method;

        int length = method.length();
        int cnt = 0;
        for (int i = length - 1; i >=0; i--) {
            if (method.charAt(i) == '.') {
                cnt++;
                if(cnt == 2){
                    return method.substring(i+1);
                }
            }
        }
        return method;
    }
}
