package com.dianping.cat.consumer.heartbeat;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Constants;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.internal.DefaultHeartbeat;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class HeartbeatAnalyzerTest extends ComponentTestCase {

	private long m_timestamp;

	private HeartbeatAnalyzer m_analyzer;

	private String m_domain = "group";

	private String m_status;

	@Before
	public void setUp() throws Exception {
		super.setUp();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm");

		Date date = sdf.parse("20120101 00:00");

		m_timestamp = date.getTime();

		m_analyzer = (HeartbeatAnalyzer) lookup(MessageAnalyzer.class, HeartbeatAnalyzer.ID);

		m_analyzer.initialize(date.getTime(), Constants.HOUR, Constants.MINUTE * 5);
	}

	@Test
	public void testProcess() throws Exception {
		for (int i = 1; i <= 10; i++) {
			MessageTree tree = generateMessageTree(i);

			m_analyzer.process(tree);
		}

		HeartbeatReport report = m_analyzer.getReport(m_domain);

		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("heartbeat_analyzer.xml"), "utf-8");
		Assert.assertEquals(expected.replaceAll("\r", ""), report.toString().replaceAll("\r", ""));
	}

	protected MessageTree generateMessageTree(int i) throws IOException {
		MessageTree tree = new DefaultMessageTree();

		tree.setMessageId("" + i);
		tree.setDomain(m_domain);
		tree.setHostName("group001");
		tree.setIpAddress("192.168.1.1");

		DefaultTransaction t = new DefaultTransaction("A", "n" + i % 2, null);

		Heartbeat heartbeat = newHeartbeat("heartbeat", "fail", m_timestamp, "0");

		t.addChild(heartbeat);

		t.complete();
		t.setDurationInMillis(i * 2);
		t.setTimestamp(m_timestamp + i * 1000 * 60);
		tree.setMessage(t);

		return tree;
	}

	private Heartbeat newHeartbeat(String type, String name, long timestamp, String status) throws IOException {
		DefaultHeartbeat heartbeat = new DefaultHeartbeat(type, name);

		heartbeat.setStatus(status);
		heartbeat.setTimestamp(timestamp);

		if (m_status == null) {
			m_status = Files.forIO().readFrom(getClass().getResourceAsStream("status_info.xml"), "utf-8");
		}

		heartbeat.addData(m_status);

		return heartbeat;
	}
}
