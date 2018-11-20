package com.dfire.common.mybatis.action;

import com.dfire.common.entity.HeraAction;
import com.google.common.base.CaseFormat;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 仅仅用于heraAction
 * @author:  火锅
 * @time: Created in 上午11:13 2018/5/17
 * @desc
 */
public class HeraActionBatchUpdateDriver extends XMLLanguageDriver implements LanguageDriver {

    private final Pattern inPattern = Pattern.compile("\\(#\\{(\\w+)\\}\\)");

    @Override
    public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {

        Matcher matcher = inPattern.matcher(script);
        if (matcher.find()) {
            StringBuilder sb = new StringBuilder(" SET ");
            for (Field field : HeraAction.class.getDeclaredFields()) {
                if(field.getName().equalsIgnoreCase("id")) {
                    continue;
                }
                String  dbName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());
                String  fieldName = field.getName();
                String tmp = dbName +" = CASE id "+
                        "<foreach collection=\"$1\" item=\"item\" > " +
                        " WHEN #{item.id,jdbcType=BIGINT}  THEN #{item."+fieldName+"}" +
                        "</foreach> END ,";
                sb.append(tmp);
            }
            sb.deleteCharAt(sb.lastIndexOf(","));
            sb.append(" where id in ");
            String whereSql =  "<foreach collection=\"$1\" item=\"item\"  " +
                    "separator=\",\" open=\"(\" close=\")\"> #{item.id,jdbcType=BIGINT} </foreach>";
            sb.append(whereSql);

            script = matcher.replaceAll(sb.toString());


            script = "<script>" + script + "</script>";
        }
        return super.createSqlSource(configuration, script, parameterType);
    }


}
