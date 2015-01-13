package com.dianping.cat.report.page.heartbeat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.heartbeat.model.entity.Disk;
import com.dianping.cat.consumer.heartbeat.model.entity.Extension;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Machine;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.BaseHistoryGraphs;
import com.dianping.cat.report.page.JsonBuilder;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.service.ReportServiceManager;
import com.dianping.cat.system.config.DisplayPolicyManager;

public class HistoryGraphs extends BaseHistoryGraphs {

	@Inject
	private ReportServiceManager m_reportService;

	@Inject
	private DisplayPolicyManager m_manager;

	public static final int K = 1024;

	private static final int MINUTE_ONE_DAY = 1440;

	private Set<String> m_extensionMetrics = new HashSet<String>();

	private void addMachineDataToMap(Map<String, double[]> datas, Machine machine) {
		for (Period period : machine.getPeriods()) {
			int minute = period.getMinute();

			updateMetricArray(datas, minute, "ActiveThread", period.getThreadCount());
			updateMetricArray(datas, minute, "HttpThread", period.getHttpThreadCount());
			updateMetricArray(datas, minute, "CatMessageOverflow", period.getCatMessageOverflow());
			updateMetricArray(datas, minute, "CatMessageProduced", period.getCatMessageProduced());
			updateMetricArray(datas, minute, "CatMessageSize", period.getCatMessageSize());
			updateMetricArray(datas, minute, "CatThreadCount", period.getCatThreadCount());
			updateMetricArray(datas, minute, "DaemonThread", period.getDaemonCount());
			updateMetricArray(datas, minute, "HeapUsage", period.getHeapUsage());
			updateMetricArray(datas, minute, "MemoryFree", period.getMemoryFree());
			updateMetricArray(datas, minute, "NewGcCount", period.getNewGcCount());
			updateMetricArray(datas, minute, "NoneHeapUsage", period.getNoneHeapUsage());
			updateMetricArray(datas, minute, "OldGcCount", period.getOldGcCount());
			updateMetricArray(datas, minute, "PigeonStartedThread", period.getPigeonThreadCount());
			updateMetricArray(datas, minute, "SystemLoadAverage", period.getSystemLoadAverage());
			updateMetricArray(datas, minute, "TotalStartedThread", period.getTotalStartedCount());
			updateMetricArray(datas, minute, "StartedThread", period.getTotalStartedCount());
			for (Disk disk : period.getDisks()) {
				String diskName = "Disk: " + disk.getPath();

				updateMetricArray(datas, minute, diskName, disk.getFree());
			}
			dealWithExtensions(datas, minute, period);
		}
		convertToDeltaArray(datas);
	}

	private Map<String, double[]> buildHeartbeatDatas(HeartbeatReport report, String ip) {
		m_extensionMetrics = new HashSet<String>();
		Map<String, double[]> datas = new HashMap<String, double[]>();
		Machine machine = report.findMachine(ip);

		if (machine != null) {
			addMachineDataToMap(datas, machine);
		}
		return datas;
	}

	private void convertToDeltaArray(Map<String, double[]> datas) {
		convertToDeltaArrayPerHour(datas, "TotalStartedThread");
		convertToDeltaArrayPerHour(datas, "StartedThread");
		convertToDeltaArrayPerHour(datas, "NewGcCount");
		convertToDeltaArrayPerHour(datas, "OldGcCount");
		convertToDeltaArrayPerHour(datas, "CatMessageSize");
		convertToDeltaArrayPerHour(datas, "CatMessageOverflow");
		for (String metric : m_extensionMetrics) {
			convertToDeltaArrayPerHour(datas, metric);
		}
	}

	private void convertToDeltaArrayPerHour(Map<String, double[]> datas, String metric) {
		double[] values = datas.get(metric);

		if (values != null) {
			double[] targets = new double[MINUTE_ONE_DAY];

			for (int i = 1; i < MINUTE_ONE_DAY; i++) {
				if (values[i - 1] > 0) {
					double delta = values[i] - values[i - 1];

					if (delta >= 0) {
						targets[i] = delta;
					}
				}
			}
			datas.put(metric, targets);
		}
	}

	private void dealWithExtensions(Map<String, double[]> datas, int minute, Period period) {
		for (String group : period.getExtensions().keySet()) {
			Extension currentExtension = period.findExtension(group);

			for (String metric : currentExtension.getDetails().keySet()) {
				m_extensionMetrics.add(metric);
				double value = currentExtension.findDetail(metric).getValue();
				int unit = m_manager.queryUnit(metric);
				double actualValue = value / unit;

				updateMetricArray(datas, minute, metric, actualValue);
			}
		}
	}

	private ArrayList<LineChart> getDiskInfo(Map<String, double[]> graphData, Date start, int size) {
		ArrayList<LineChart> diskInfo = new ArrayList<LineChart>();

		Iterator<Entry<String, double[]>> iterator = graphData.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, double[]> entry = iterator.next();
			String name = (String) entry.getKey();
			if (name.startsWith("Disk")) {
				double[] data = graphData.get(name);
				for (int i = 0; i < data.length; i++) {
					data[i] = data[i] / (K * K * K);
				}
				String title = name + " Free [GB]";
				LineChart disk = getGraphItem(title, name, start, size, graphData);
				diskInfo.add(disk);
			}
		}
		return diskInfo;
	}

	private List<LineChart> getExtensionGraphs(List<String> metrics, Map<String, double[]> graphData, Date start,
	      int size) {
		List<LineChart> graphs = new ArrayList<LineChart>();

		for (String metric : metrics) {
			graphs.add(getGraphItem(metric, metric, start, size, graphData));
		}
		return graphs;
	}

	private LineChart getGraphItem(String title, String key, Date start, int size, Map<String, double[]> graphData) {
		LineChart item = new LineChart();
		item.setStart(start);
		item.setSize(size);
		item.setTitle(title);
		item.addSubTitle(title);
		item.setStep(TimeHelper.ONE_MINUTE);
		double[] activeThread = graphData.get(key);
		item.addValue(activeThread);
		return item;
	}

	private Set<String> queryMetricNames(HeartbeatReport report, String groupName) {
		Set<String> result = new HashSet<String>();

		for (Machine machine : report.getMachines().values()) {
			for (Period period : machine.getPeriods()) {
				Extension extension = period.findExtension(groupName);

				if (extension != null) {
					result.addAll(extension.getDetails().keySet());
				}
			}
		}
		return result;
	}

	// show the graph of heartbeat
	public void showHeartBeatGraph(Model model, Payload payload) {
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();

		int size = (int) ((end.getTime() - start.getTime()) / TimeHelper.ONE_HOUR * 60);
		HeartbeatReport report = m_reportService.queryHeartbeatReport(payload.getDomain(), start, end);
		Map<String, double[]> graphData = buildHeartbeatDatas(report, payload.getIpAddress());
		String queryType = payload.getType();

		if (queryType.equalsIgnoreCase("thread")) {
			model.setHttpThreadGraph(getGraphItem("Http Thread (Count) ", "HttpThread", start, size, graphData)
			      .getJsonString());
			model.setActiveThreadGraph(getGraphItem("Thread (Count) ", "ActiveThread", start, size, graphData)
			      .getJsonString());
			model.setStartedThreadGraph(getGraphItem("Started Thread (Count) ", "StartedThread", start, size, graphData)
			      .getJsonString());
		} else if (queryType.equalsIgnoreCase("system")) {
			model.setNewGcCountGraph(getGraphItem("NewGc Count (Count) ", "NewGcCount", start, size, graphData)
			      .getJsonString());
			model.setOldGcCountGraph(getGraphItem("OldGc Count (Count) ", "OldGcCount", start, size, graphData)
			      .getJsonString());
			model.setSystemLoadAverageGraph(getGraphItem("System Load Average ", "SystemLoadAverage", start, size,
			      graphData).getJsonString());
		} else if (queryType.equalsIgnoreCase("memory")) {
			model.setMemoryFreeGraph(getGraphItem("Memory Free (MB) ", "MemoryFree", start, size, graphData)
			      .getJsonString());
			model.setHeapUsageGraph(getGraphItem("Heap Usage (MB) ", "HeapUsage", start, size, graphData).getJsonString());
			model.setNoneHeapUsageGraph(getGraphItem("None Heap Usage (MB) ", "NoneHeapUsage", start, size, graphData)
			      .getJsonString());
		} else if (queryType.equalsIgnoreCase("disk")) {
			List<LineChart> diskInfo = getDiskInfo(graphData, start, size);

			model.setDisks(diskInfo.size());
			model.setDiskHistoryGraph(new JsonBuilder().toJson(diskInfo));
		} else if (queryType.equalsIgnoreCase("cat")) {
			model.setCatMessageProducedGraph(getGraphItem("Cat Message Produced (Count) / Minute", "CatMessageProduced",
			      start, size, graphData).getJsonString());
			model.setCatMessageOverflowGraph(getGraphItem("Cat Message Overflow (Count) / Minute", "CatMessageOverflow",
			      start, size, graphData).getJsonString());
			model.setCatMessageSizeGraph(getGraphItem("Cat Message Size (MB) / Minute", "CatMessageSize", start, size,
			      graphData).getJsonString());
		} else if (queryType.equalsIgnoreCase("extension")) {
			String groupName = payload.getExtensionType();
			List<String> metrics = m_manager.sortMetricNames(groupName, queryMetricNames(report, groupName));
			List<LineChart> graphs = getExtensionGraphs(metrics, graphData, start, size);

			model.setExtensionCount(metrics.size());
			model.setExtensionHistoryGraphs(new JsonBuilder().toJson(graphs));
		}
	}

	private void updateMetricArray(Map<String, double[]> datas, int minute, String metricName, double value) {
		double[] values = datas.get(metricName);

		if (values == null) {
			values = new double[MINUTE_ONE_DAY];

			datas.put(metricName, values);
		}
		values[minute] = value;
	}
}
