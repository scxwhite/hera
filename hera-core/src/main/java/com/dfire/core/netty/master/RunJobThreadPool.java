package com.dfire.core.netty.master;

import com.dfire.common.config.ExecuteFilter;
import com.dfire.common.config.FilterType;
import com.dfire.common.config.ServiceLoader;
import com.dfire.common.entity.vo.HeraDebugHistoryVo;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.enums.JobStatus;
import com.dfire.common.enums.TriggerTypeEnum;
import com.dfire.common.service.HeraDebugHistoryService;
import com.dfire.common.service.HeraJobHistoryService;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.common.vo.JobElement;
import com.dfire.config.HeraGlobalEnv;
import com.dfire.core.emr.EmrJob;
import com.dfire.core.emr.FixedEmr;
import com.dfire.core.emr.WrapEmr;
import com.dfire.logs.ErrorLog;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * desc:
 *
 * @author scx
 * @create 2019/04/24
 */
public class RunJobThreadPool extends ThreadPoolExecutor {


    private static ConcurrentHashMap<Runnable, JobElement> jobEmrType;
    private final EmrJob emr;
    private final HeraJobHistoryService jobHistoryService;

    private final HeraDebugHistoryService debugHistoryService;

    private final boolean emrCluster;

    public RunJobThreadPool(MasterContext masterContext, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        emr = new WrapEmr();
        jobHistoryService = masterContext.getHeraJobHistoryService();
        debugHistoryService = masterContext.getHeraDebugHistoryService();
        jobEmrType = new ConcurrentHashMap<>(maximumPoolSize);
        emrCluster = HeraGlobalEnv.isEmrJob() && FixedEmr.NAME.equals(HeraGlobalEnv.getEmrCluster());
    }

    public static List<Long> getWaitClusterJob(TriggerTypeEnum... typeEnum) {
        if (jobEmrType == null) {
            return new ArrayList<>(0);
        }
        Set<TriggerTypeEnum> typeSet = new HashSet<>(Arrays.asList(typeEnum));
        return new ArrayList<>(jobEmrType.values())
                .stream()
                .filter(element -> element.getStatus().equals(JobStatus.waitCluster) && typeSet.contains(element.getTriggerType()))
                .map(JobElement::getJobId)
                .collect(Collectors.toList());
    }

    public static boolean cancelJob(Long id, TriggerTypeEnum... typeEnum) {
        if (jobEmrType == null) {
            return false;
        }
        Set<TriggerTypeEnum> typeSet = new HashSet<>(Arrays.asList(typeEnum));
        Optional<JobElement> jobElement = jobEmrType.values().stream()
                .filter(element -> element.getStatus().equals(JobStatus.waitCluster) && typeSet.contains(element.getTriggerType()))
                .filter(element -> element.getJobId().equals(id)).findFirst();
        if (jobElement.isPresent()) {
            jobElement.get().setCancel(true);
            return true;
        }
        return false;
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        JobElement jobElement = jobEmrType.get(r);
        try {
            if (jobElement.isCancel()) {
                appendCreateLog(jobElement, "任务手动取消");
                //使用异常来取消该任务的执行
                throw new IllegalStateException("任务被手动取消:" + jobElement.getJobId());
            }
            if (!emrCluster) {
                appendCreateLog(jobElement, "本地执行任务");
            } else {
                if (isEmrDynamicJob(jobElement)) {
                    appendCreateLog(jobElement, "动态集群创建中..");
                    emr.addJob(jobElement.getOwner());
                } else {
                    appendCreateLog(jobElement, "使用固定集群执行任务");
                }
            }
            jobElement.setStatus(JobStatus.running);
        } catch (Exception e) {
            ErrorLog.error("任务前置执行异常" + e.getMessage(), e);
        }

        if (jobElement.isCancel()) {
            appendCreateLog(jobElement, "任务手动取消");
            //手动触发一次后置执行
            afterExecute(r, null);
            //抛出异常打断任务的执行
            throw new IllegalStateException("任务被手动取消:" + jobElement.getJobId());
        }
        doFilter(FilterType.execute, jobElement);
    }

    private void doFilter(FilterType filterType, JobElement element) {
        List<ExecuteFilter> filters = ServiceLoader.getFilters();
        try {
            switch (filterType) {
                case execute:
                    for (ExecuteFilter filter : filters) {
                        try {
                            filter.onExecute(element);
                        } catch (Exception e) {
                            ErrorLog.error("拦截器前置执行异常", e);
                        }
                    }
                    break;
                case response:
                    for (ExecuteFilter filter : filters) {
                        try {
                            filter.onResponse(element);
                        } catch (Exception e) {
                            ErrorLog.error("拦截器后置执行异常", e);
                        }
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            ErrorLog.error("拦截器异常:" + e.getMessage(), e);
        }
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        JobElement jobElement = jobEmrType.get(r);
        try {
            if (isEmrDynamicJob(jobElement)) {
                emr.removeJob(jobElement.getOwner());
            }
        } catch (Exception e) {
            ErrorLog.error("任务后置执行异常" + e.getMessage(), e);
        } finally {
            jobEmrType.remove(r);
            jobElement.setStatus(JobStatus.complete);
            doFilter(FilterType.response, jobElement);
        }
    }

    private void appendCreateLog(JobElement element, String log) {
        switch (element.getTriggerType()) {
            case SCHEDULE:
            case MANUAL_RECOVER:
            case MANUAL:
            case AUTO_RERUN:
            case SUPER_RECOVER:
                HeraJobHistoryVo historyVo = BeanConvertUtils.convert(jobHistoryService.findById(element.getHistoryId()));
                historyVo.getLog().appendHera(log);
                jobHistoryService.updateHeraJobHistoryLog(BeanConvertUtils.convert(historyVo));
                break;
            case DEBUG:
                HeraDebugHistoryVo heraDebugHistoryVo = debugHistoryService.findById(element.getHistoryId());
                heraDebugHistoryVo.getLog().appendHera(log);
                debugHistoryService.updateLog(BeanConvertUtils.convert(heraDebugHistoryVo));
                break;
            default:
                ErrorLog.error("未知的执行类型:" + element.getTriggerType().toString());
                break;
        }
    }

    private boolean isEmrDynamicJob(JobElement element) {
        return emrCluster && element != null && !element.isFixedEmr();
    }

    @Override
    public List<Runnable> shutdownNow() {
        List<Runnable> alive = super.shutdownNow();
        for (Runnable runnable : alive) {
            jobEmrType.remove(runnable);
        }
        return alive;
    }

    /**
     * 任务执行的入口
     *
     * @param command    执行的内容
     * @param jobElement JobElement
     */
    public void execute(Runnable command, JobElement jobElement) {
        jobElement.setStatus(JobStatus.waitCluster);
        jobEmrType.putIfAbsent(command, jobElement);
        super.execute(command);
    }
}
