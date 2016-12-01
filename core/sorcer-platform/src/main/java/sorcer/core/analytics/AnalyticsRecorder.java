/*
 * Copyright to the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sorcer.core.analytics;

import net.jini.core.lookup.ServiceID;
import org.rioproject.impl.system.ComputeResourceAccessor;
import org.rioproject.system.ComputeResourceUtilization;
import org.rioproject.system.MeasuredResource;
import org.rioproject.system.SystemWatchID;
import org.rioproject.system.measurable.memory.ProcessMemoryUtilization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.monitoring.Monitor;
import sorcer.core.monitoring.MonitorAgent;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Dennis Reedy
 */
public class AnalyticsRecorder {
    private Logger logger = LoggerFactory.getLogger(AnalyticsRecorder.class);
    private final Map<String, MethodInvocationRecord> activityMap = new ConcurrentHashMap<>();
    private ServiceID serviceID;
    private String hostName;
    private String name;
    private String principal;
    private Map<Integer, MonitorAgent> agents = new ConcurrentHashMap<>();

    private NumberFormat percentFormatter = NumberFormat.getPercentInstance();

    public AnalyticsRecorder(String hostName, ServiceID serviceID, String name, String principal) {
        this.hostName = hostName;
        this.serviceID = serviceID;
        this.name = name;
        this.principal = principal;
        percentFormatter.setMaximumFractionDigits(3);
    }

    public Map<String, MethodAnalytics> getMethodAnalytics() {
        Map<String, MethodAnalytics> result = new HashMap<>();
        for(Map.Entry<String, MethodInvocationRecord> entry : activityMap.entrySet()) {
            result.put(entry.getKey(), entry.getValue().create(serviceID, hostName));
        }
        return result;
    }

    public MethodAnalytics getMethodAnalytics(String m) {
        MethodInvocationRecord record = getMethodInvocationRecord(m);
        if(record!=null) {
            return record.create(serviceID, hostName);
        }
        return null;
    }

    public int inprocess(String m) {
        MonitorAgent monitorAgent = create(m);
        MethodInvocationRecord record = getMethodInvocationRecord(m);
        int id = record.inprocess();
        agents.put(id, monitorAgent);
        logger.info("{} num active: {}", m, record.numActiveOperations.get());
        monitorAgent.inprocess(record.create(serviceID, hostName));
        return id;
    }

    public void completed(String m, int id) {
        MethodInvocationRecord record = getMethodInvocationRecord(m);
        record.complete(id);
        activityMap.put(m, record);
        MonitorAgent monitorAgent = get(id);
        monitorAgent.completed(record.create(serviceID, hostName));
    }

    public void failed(String m, int id) {
        MethodInvocationRecord record = getMethodInvocationRecord(m);
        record.failed(id);
        activityMap.put(m, record);
        MonitorAgent monitorAgent = get(id);
        monitorAgent.update(Monitor.Status.FAILED, record.create(serviceID, hostName));
    }

    public SystemAnalytics getSystemAnalytics() {
        ComputeResourceUtilization cru = ComputeResourceAccessor.getComputeResource().getComputeResourceUtilization();
        String systemCPUPercent = formatPercent(cru.getCpuUtilization().getValue());
        String processCPUUtilization = formatPercent(getMeasuredValue(SystemWatchID.PROC_CPU, cru));
        double systemMemoryTotal = cru.getSystemMemoryUtilization().getTotal();
        double systemMemoryUsed = cru.getSystemMemoryUtilization().getUsed();
        ProcessMemoryUtilization mem = cru.getProcessMemoryUtilization();
        double processMemoryTotal = mem.getMaxHeap();
        double processMemoryUsed = mem.getUsedHeap();
        return new SystemAnalytics()
                   .setSystemCPUPercent(systemCPUPercent)
                   .setProcessCPUUtilization(processCPUUtilization)
                   .setSystemMemoryTotal(systemMemoryTotal)
                   .setSystemMemoryUsed(systemMemoryUsed)
                   .setProcessMemoryTotal(processMemoryTotal)
                   .setProcessMemoryUsed(processMemoryUsed);
    }

    private MonitorAgent create(String m) {
        MonitorAgent monitorAgent = new MonitorAgent();
        monitorAgent.register(String.format("%s#%s", name, m), principal);
        return monitorAgent;
    }

    private MonitorAgent get(int id) {
        MonitorAgent monitorAgent = agents.get(id);
        if(monitorAgent==null) {
            logger.error("No MonitorAgent found for {}", id);
        }
        return monitorAgent;
    }

    private MethodInvocationRecord getMethodInvocationRecord(String m) {
        MethodInvocationRecord methodInvocationRecord;
        synchronized(activityMap) {
            methodInvocationRecord = activityMap.get(m);
            if (methodInvocationRecord == null) {
                methodInvocationRecord = new MethodInvocationRecord(m);
                activityMap.put(m, methodInvocationRecord);
            }
        }
        return methodInvocationRecord;
    }

    private Double getMeasuredValue(final String label,
                                    final ComputeResourceUtilization cru) {
        Double value = Double.NaN;
        for (MeasuredResource mRes : cru.getMeasuredResources()) {
            if (mRes.getIdentifier().equals(label)) {
                value = mRes.getValue();
                break;
            }
        }
        return value;
    }

    private String formatPercent(final Double value) {
        if (value != null && !Double.isNaN(value))
            return (percentFormatter.format(value.doubleValue()));
        return ("?");
    }
}