package com.dfire.api;

import com.dfire.common.util.ActionUtil;
import com.dfire.common.util.RenderHierarchyProperties;
import com.dfire.common.util.StringUtil;
import com.dfire.core.job.ProcessJob;
import com.dfire.core.util.CronParse;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 18:29 2018/3/22
 * @desc
 */
public class HeraStringUtilTest {

    @Test
    public void md5() {
        System.out.println(StringUtil.EncoderByMd5("123456"));

    }

    @Test
    public void parseDate() {
        String str = "1097:ods_clear_sigmafacadebizdata_timing_info(定时信息日志清洗),1102:BiDwGatewayProductClear(原始日志关联设备和产品信息的通用程序),1106:StartAndEndTimeLiveDevice(设备活跃天数明细),1107:StartAndEndTimeLiveDeviceNumber(活跃设备数),1109:app_quasar_live_device_number(导出live_devices_statistices表),1110:app_quasar_live_devices_detail(导出live_devices_detail_statistics),1131:avg_offline_num_alarm(平均下线次数越阈告警),1133:dw_gateway_user_app(gateway表关联user和App表),1134:avg_report_num_alarm(平均上报次数越阈告警),1168:app_smart_dp_user_stastic_sum(电量统计用户uid纬度之和),1174:export_dws_log_config_net_wifi_success_rate(导出hive表bi_dw.dws_log_config_net_wifi_success_rate 配网成功率统计表),1205:app_smart_dp_user_stastic_sum(用户纬度的电量和统计导出),1231:dws_ty2c_device_pid_vbline_live_rate_1d(产品下设备日活率 (按产品、基线版本)),1234:dws_ty2c_device_gw_offon_line_num_top_1d(设备离线次数top),1235:dws_log_config_net_wifi_success_rate(设备wifi配网成功率统计 目前仅统计ap、ez类配网模式),1239:dim_product_category(生成:产品品类纬度表),1279:import_basic_weather_city(导入mysql表tuya_basic.basic_weather_city),1293:dim_product_item(产品维度表),1301:dim_region_hash4(区域纬度表),1306:import_ods_smart_device_module(导入mysql表tuya_devices.ods_smart_device_module),1330:dim_region_hash5(区域纬度表5位hash),1331:import_smart_group(导入mysql表tuya_smart.smart_group),1332:dwd_device_item(设备事实表),1340:import_smart_device_authorize_api_token(导入mysql表tuya_smart.smart_device_authorize_api_token),1341:import_crm_attribute_detail(导入mysql表tuya_crm.crm_attribute_detail),1348:import_crm_stock_record_attribute(导入mysql表smart_crm.crm_stock_record_attribute 出库属性表),1358:import_smart_device_test_new(导入mysql表tuya_smart.smart_device_test_new 压力测试),1362:import_smart_location(导入mysql表tuya_smart.smart_location 位置信息表),1365:import_smart_home_app(导入mysql表tuya_venus.smart_home_app 全屋大盘-app表),1366:import_track_event(导入mysql表tuya_venus.track_event 埋点事件表),1367:import_promotion(导入mysql表promotion.promotion 推广链接清单表),1368:ods_clear_smart_request(清洗ods_log_smart_request),1373:import_smart_group_user(导入msyql表tuya_smart.smart_group_user 用户与组的关联表),1374:import_smart_linkage_timer(导入mysql表tuya_timer.smart_linkage_timer 定时数据表),1379:ods_clear_datapoint_publish(下发日志清洗),1380:ods_clear_datapoint_report(上报日志清洗),1381:ods_clear_gateway_onoffline(上下线日志清洗),1382:ods_clear_smart_request_api(从ods_clear_smart_request表抽取Api.java数据),1383:ods_clear_smart_request_gw(从ods_clear_smart_request表抽取Gw.java数据),1385:import_jira_issuestatus(导入mysql表jira.issuestatus 状态表),1386:import_jira_jiraissue(导入mysql表jira.jiraissue 事实表),1393:ods_clear_smart_hades_mobile(清洗日志表 ods_log_smart_hades_mobile),1399:import_pro_project(导入mysql表tuya_metis.pro_project 项目表),1400:import_pro_demand(导入mysql表tuya_metis.pro_demand 需求表),1404:import_track_page(导入mysql表tuya_venus.track_page 埋点页面表),1405:import_ods_voicetube_smart_product_voice_control_config(导入mysql表tuya_voicetube.smart_product_voice_control_config 产品语音控制配置表),1435:dim_product_category监控(dim_product_category监控),1438:dwd_device_item监控(dwd_device_item监控),1439:dim_region_hash4监控(dim_region_hash4监控),1440:dim_region_hash5监控(dim_region_hash5监控),1441:ods_clear_gateway_onoffline监控(ods_clear_gateway_onoffline监控),1442:ods_clear_smart_request监控(ods_clear_smart_request监控),1443:ods_clear_smart_request_api监控(ods_clear_smart_request_api监控),1444:ods_clear_smart_request_gw监控(ods_clear_smart_request_gw监控),1452:ods_clear_smart_hades_mobile监控(ods_clear_smart_hades_mobile监控),1460:ods_clear_sigmafacadebizdata_timing_info监控(ods_clear_sigmafacadebizdata_timing_info监控),1467:import_jira_customfieldvalue(导入mysql表jira.customfieldvalue ),1478:dws_app_biz_user_statistic(按biz_type 用户相关指标统计 结果表),1489:dws_app_biz_device_statistic(按biz_type 设备相关指标统计 结果表),1490:ods_clear_datapoint_publish监控(ods_clear_datapoint_publish监控),1491:dws_app_biz_area_user_statistic(按biz_type 各区域下用户相关指标统计 结果表),1492:dws_app_biz_category_device_statistic(按biz_type 各品类下设备相关指标统计 结果表),1493:ods_clear_datapoint_report监控(ods_clear_datapoint_report监控),1495:ads_app_biz_statistic(按biz_type 无分类 各项指标统计 汇总表),1511:ods_clear_smartapollobizdata_device_factory_reset(device_factory_reset日志清洗),1515:ods_clear_smartapollobizdata_device_factory_reset监控(ods_clear_smartapollobizdata_device_factory_reset监控),1519:import_gaea_address(导入mysql表tuya_bss.gaea_address 收货地址管理表),1523:import_council_ticket(导入mysql表tuya_council.council_ticket 工单记录表),1524:import_council_ticket_index(导入mysql表tuya_council.council_ticket_index 工单索引表),1525:import_council_ticket_schema(导入mysql表tuya_council.council_ticket_schema 工单模板表),1526:import_council_ticket_schema_field(导入mysql表tuya_council.council_ticket_schema_field 工单模板字段表),1532:import_hades_error_code_cfg(导入mysql表tuya_venus.hades_error_code_cfg 错误码的配置表),1545:import_jira_changeitem(导入msyql表jira.changeitem),1546:import_jira_changegroup(导入mysql表jira.changegroup),1553:import_lion_category(导入mysql 表tuya_lion.lion_category 商品类目表),1554:import_lion_attr(导入mysql表tuya_lion.lion_attr 属性表),1555:import_smart_datapoint(导入msyql表tuya_shadows.smart_datapoint),1557:venus_data_app(venus_data_app biz_type),1558:import_smart_device_test(导入msyql表tuya_smart.smart_device_test  设备测试),1559:import_smart_scale_user(导入msyql表tuya_venus.smart_scale_user),1560:import_smart_scale_history(导入msyql表tuya_venus.smart_scale_history),1561:import_health_scale_history(导入mysql表tuya_venus.health_scale_history 体脂称称重历史记录),1562:import_scale_user_invite_relation(导入msyql表tuya_venus.scale_user_invite_relation),1563:import_smart_scale_trend(导入mysql表tuya_venus.smart_scale_trend),1570:ods_clear_smartapollobizdata_gateway_token_create(清洗gateway_token_create日志),1579:ads_app_biz_area_statistic(按biz_type country分类 各项指标统计 汇总表),1580:ads_app_biz_category_statistic(按biz_type category分类 各项指标统计 汇总表),1582:import_smart_device_authorize(导入mysql表tuya_smart.smart_device_authorize 设备注册授权权限),1584:export_venus_data_app(导出hive表bi_app.venus_data_app 到mysql),1587:import_smart_schema_datapoint(导入mysql表tuya_smart.smart_schema_datapoint),1616:dwd_log_device_upgrade_all(设备升级日志宽表),1618:dwd_log_device_runstat(设备信号量日志宽表),1619:dwd_log_device_restart(设备重启日志宽表),1621:dwd_log_device_upgrade_all(dwd_log_device_upgrade_all报警),1622:dwd_log_device_restart(dwd_log_device_restart报警),1623:dwd_log_device_runstat(dwd_log_device_runstat报警),1627:import_venus_work_statistics(导入mysql表tuya_venus.venus_work_statistics),1647:import_gaea_dictionary(导入mysql表tuya_gaea.gaea_dictionary),1648:import_pro_remark(导入mysql表tuya_metis.pro_remark 备注表),1667:export_dws_device_product_config_net_error(导出表 设备配网异常日志统计表-产品维度,用于提供给客户端组做报表展示),1668:export_dws_device_user_config_net_error(导出hive表 设备配网异常日志统计表-用户维度,用于提供给客户端组做报表展示),1669:app_venus_data(venus_data总体指标表),1676:import_track_event_page(导入mysql表tuya_venus.track_event_page 埋点页面与事件关联表),1692:dwd_log_dev_dp_factory_reset_report(设备最新的datapoint_report上报日志 宽表),1695:import_lion_commodity(导入mysql表tuya_lion.lion_commodity 商品表),1696:import_lion_commodity_sku(导入mysql表tuya_lion.lion_commodity_sku 商品sku表),1697:import_lion_commodity_attr(导入mysql表tuya_lion.lion_commodity_attr  商品关联属性：含关键属性、标签、认证等),1698:import_lion_commodity_ext(导入mysql表tuya_lion.lion_commodity_ext 商品扩展属性表),1699:import_lion_commodity_attr_value(导入mysql表tuya_lion.lion_commodity_attr_value 商品属性值表),1700:import_lion_commodity_sku_attr_value(导入mysql表tuya_lion.lion_commodity_sku_attr_value 商品sku属性值表),1701:import_lion_commodity_biz(导入msyql表tuya_lion.lion_commodity_biz ),1702:import_lion_attr_value(导入mysql表tuya_lion.lion_attr_value),1703:import_lion_attr_group(导入mysql表tuya_lion.lion_attr_group 属性集表，含关键属性、sku属性、标签、认证等),1704:import_lion_business_line(导入mysql表tuya_lion.lion_business_line 业务线),1705:import_lion_commodity_step_price(导入msyql表tuya_lion.lion_commodity_step_price ),1712:start_avoid_the_peak045(避开大表查询高峰期),1716:import_hongjun_invoice(导入mysql表tuya_hongjun.hongjun_invoice 发票表),1717:import_smart_act(导入mysql表tuya_smart.smart_act 产品动作表),1725:smart_datapoint_stastic_new(设备未出厂重置和设备出厂重置日期之后的 设备上报点日志值指标统计汇总应用表),1732:dws_device_geohash3_statistic_1d(设备和用户的goehash3纬度统计指标),1733:dws_product_category1_statistic_1d(产品一级类目纬度统计指标),1736:dws_device_city_devnum_1d(城市纬度下设备数),1739:dws_log_app_voice_alexa_skill_number_1d(alexa平台每天每个技能的语音日志量),1744:dws_device_total_indicators_1d(设备相关统计总数指标),1745:dws_user_total_indicators_1d(用户相关总数指标),1750:import_savanna_shopping_cart(导入mysql表tuya_savanna.savanna_shopping_cart 购物车),1776:dwd_device_uuid_active_item(设备按status，active_time时间排序的uuid纬度宽表),1791:export_smart_datapoint_stastic_new(导出hive表smart_datapoint_stastic_new),1793:import_smart_user_property(导入msyql表tuya_smart.smart_user_property 用户属性扩展表),1809:import_atop_appinfo(导入mysql表tuya_basic.atop_appinfo app信息表),1816:dwd_device_uuid_item监控(dwd_device_uuid_item监控),1821:ods_log_file_count监控(ods_log库原始日志文件数数报警),1830:dws_log_config_net_success_rate_statistics_10min(统计一天内每10分钟的配网成功率),1832:dws_device_city_devnum_1d监控(城市纬度下设备数监控),1834:dws_device_geohash3_statistic_1d监控(dws_device_geohash3_statistic_1d监控),1835:dws_log_app_voice_alexa_skill_number_1d监控(dws_log_app_voice_alexa_skill_number_1d监控),1836:dws_product_category1_statistic_1d监控(dws_product_category1_statistic_1d监控),1856:ads_log_success_rate_statistics_1d(各种成功率统计结果汇总 按天),1870:success_rate_statistics(各种成功率统计结果应用表),1874:export_success_rate_statistics(导出hive 表bi_app.success_rate_statistics 各种成功率统计结果表),1881:ads_device_city_devnum_1d(城市纬度下设备数),1882:ads_device_geohash3_statistic_1d(设备和用户的goehash3纬度统计指标),1883:ads_total_indicators_1d(总数指标),1885:ads_log_app_voice_alexa_skill_number_1d(alexa平台每天每个技能的语音日志量),1886:ads_product_category1_statistic_1d(产品一级类目纬度统计指标),1887:dws_log_config_net_success_rate_statistics_10min监控(dws_log_config_net_success_rate_statistics_10min监控),1888:config_net_success_rate_statistics监控(config_net_success_rate_statistics监控),1898:export_venus_data(导出venus_data重构后的任务结果),1922:huashu_device__export(华数项目临时导出激活设备),1923:dwd_device_uuid_create_item(设备uuid创建时间表),1971:start_avoid_the_peak025(避开高峰期 零点25执行),2033:ods_clear_smart_request_pt(清洗smart_request中的Pt.java),2050:end_focus_import(监控重点关照的导入任务),2054:ods_clear_smart_request_1(清洗smart_request第一步：正则解析),2055:start_avoid_the_peak110(避开高峰期 1点10分执行),2114:oem_app_apirequest_count(每周日运行这一周的公版版本tuya.m.api.batch.invoke请求数量),2126:dwd_log_device_config_net_time(配网从创建token到激活再到上线 各个阶段的时间等详细数据),2131:dws_log_config_net_time_interval_statistics_1d(配网从创建token到激活再到上线 各个阶段的时间差 统计),2137:dws_log_config_net_time_rank_statistics_1d(配网各阶段时间差排名详细),2189:ods_clear_hades_livemq(ods_clear_hades_livemq),2196:dws_rate_publish_report_detail(上报下发成功率明细建表语句),2198:ods_clear_device_ten_logs(十种日志类型（日志收费）),2201:RateOfPublishAndReportDetail(上报下发成功率明细),2205:dws_log_livemq_rate_ack_detail(ack下发上报成功率明细),2206:ods_clear_hades_livemq监控(ods_clear_hades_livemq监控),2226:import_smart_device_authorize_incr(导入msyql tuya_smart_product.smart_device_authorize 产测表增量数据),2237:dws_app_recom_scenes_statistic(按biz_type 场景相关指标统计 结果表),2246:dim_app_recom_rule_library(未发布-app推荐规则库维度表),2265:dwd_app_linkage_rule(未发布-app规则推荐详细表),2275:import_ods_smart_gateway_incr(ods_smart_gateway 增量导入),2277:import_ods_smart_device_module_incr(设备模板表增量查询)";

        System.out.println(str.split(",").length);
        System.out.println(str.replace(",", "\n"));
    }

    @Test
    public void commandTest() {
        String[] commands = ProcessJob.partitionCommandLine("free -m | grep buffers/cache");
        String[] commands2 = ProcessJob.partitionCommandLine("'free -m | grep buffers/cache'");
        String[] commands3 = ProcessJob.partitionCommandLine("\"free -m | grep buffers/cache\"");
        System.out.println(Arrays.asList(commands).stream().collect(Collectors.joining()));
        System.out.println(Arrays.asList(commands2).stream().collect(Collectors.joining()));
        System.out.println(Arrays.asList(commands3).stream().collect(Collectors.joining()));

    }

    @Test
    public void testFilePath() {
        File file = new File("1.txt");
        System.out.println(file.getAbsolutePath());
    }

    @Test
    public void testDateFormat() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("[yyyy-MM-dd][dd/MM/yyyy][MM-dd-yyyy]");
        LocalDate.parse("2018-09-23", formatter);
        System.out.println( LocalDate.parse("2018-09-23", formatter));
        System.out.println( LocalDate.parse("09-23-2018", formatter));
        System.out.println( LocalDate.parse("23/09/2018", formatter));
        System.out.println(ActionUtil.getCurrActionVersion());
    }

    @Test
    public  void testBoolean() {
        System.out.println(System.getenv());
        Map map = new HashMap(System.getenv());
        System.out.println(map.size());
    }

    @Test
    public void testDateUtil() {
        System.out.println(ActionUtil.getCurrActionVersion());

        String currString = ActionUtil.getNextDayString().getSource();
        Date nextDay = ActionUtil.getNextDayString().getTarget();
        SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");

        System.out.println(currString);
        System.out.println(dfDate.format(nextDay));

        List<String> list = new ArrayList<>();
        String cron = "0 0 2 * * ?";
        String cronDate = dfDate.format(nextDay);
        boolean isCronExp = CronParse.Parser(cron, cronDate, list);
        System.out.println(list.size());


        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();


        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyyMMdd0000000000");
        String actionDate = dateFormat.format(now);
        System.out.println(actionDate);
    }


    @Test
    public void filePathTest() {
        String path = this.getClass().getClassLoader().getResource("").getPath();
        if(path != null) {
            System.out.println(path);
        }

    }

    @Test
    public void getDate() {
        System.out.println(ActionUtil.longToDate(System.currentTimeMillis()));
    }

    @Test
    public void fileNameSplit() {
        String fileName = "sqoop.sh";
        String prefix = StringUtils.substringBefore(fileName, ".");
        System.out.println(prefix);
        String suffix = StringUtils.substringAfter(fileName,".");
        System.out.println(suffix);
    }
}
