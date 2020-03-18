package com.dfire.common.mybatis;

import com.dfire.common.config.SkipColumn;
import com.dfire.common.constants.Constants;
import com.google.common.base.CaseFormat;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午11:13 2018/5/17
 * @desc
 */
public class HeraInsertLangDriver extends XMLLanguageDriver implements LanguageDriver {

    private final Pattern inPattern = Pattern.compile("\\(#\\{(\\w+)\\}\\)");

    @Override
    public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {

        Matcher matcher = inPattern.matcher(script);
        if (matcher.find()) {
            StringBuilder columns = new StringBuilder();
            StringBuilder values = new StringBuilder();
            columns.append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
            values.append("<trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">");



            for (Field field : parameterType.getDeclaredFields()) {
                SkipColumn skipColumn = field.getAnnotation(SkipColumn.class);
                if (skipColumn == null) {
                    columns.append("<if test=\"").append(field.getName()).append("!= null\">");
                    columns.append(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName())).append(Constants.COMMA);
                    columns.append("</if>");


                    values.append("<if test=\"").append(field.getName()).append("!= null\">");
                    values.append("#{").append(field.getName()).append("}").append(Constants.COMMA);

                    values.append("</if>");
                }
            }
            columns.append("</trim>");
            values.append("</trim>");


            script = matcher.replaceAll(columns.append(values).toString());
            script = "<script>" + script + "</script>";
        }
        return super.createSqlSource(configuration, script, parameterType);
    }
}
