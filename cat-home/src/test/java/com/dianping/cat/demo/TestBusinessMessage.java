package com.dianping.cat.demo;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class TestBusinessMessage {
	private static final String Puma = "PumaServer";

	private static final String PayOrder = "PayOrder";

	@Test
	public void testMutilThead() throws Exception {
		int total = 10;
		CountDownLatch latch = new CountDownLatch(total);

		for (int i = 0; i < 10; i++) {
			Threads.forGroup("cat").start(new CatThread(latch, i));
		}

		Thread.sleep(10000);
	}

	public class CatThread implements Task {

		private CountDownLatch m_latch;

		private int m_count;

		public CatThread(CountDownLatch latch, int count) {
			m_latch = latch;
			m_count = count;
		}

		@Override
		public void run() {
			m_latch.countDown();
			try {
				m_latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			for (int i = 0; i < 100; i++) {
				Transaction transaction = Cat.newTransaction("test", "test" + m_count);

				transaction.setStatus(Message.SUCCESS);
				transaction.complete();
			}
		}

		@Override
		public String getName() {
			return "cat-test-thread";
		}

		@Override
		public void shutdown() {
		}

	}

	@Test
	public void testEvent() throws Exception {
		for (int i = 0; i < 1000; i++) {
			Cat.logError(new NullPointerException());
		}

		Thread.sleep(10000);
	}

	@Test
	public void test() throws Exception {
		while (true) {
			for (int i = 0; i < 1000; i++) {
				Transaction t = Cat.newTransaction("URL", "/index");
				Cat.logEvent("RemoteLink", "sina", Event.SUCCESS, "http://sina.com.cn/");
				t.addData("channel=channel" + i % 5);

				Cat.logMetricForCount(PayOrder, "Receipt Verify Success");
				Cat.logMetricForDuration(PayOrder, "Receipt Verify Druation", 10);
				Cat.logMetricForSum(PayOrder, "sum Value", 20);

				MessageTree tree = (MessageTree) Cat.getManager().getThreadLocalMessageTree();
				tree.setDomain(Puma);
				t.complete();
			}

			for (int i = 0; i < 900; i++) {
				Transaction t = Cat.newTransaction("URL", "/detail");
				MessageTree tree = (MessageTree) Cat.getManager().getThreadLocalMessageTree();

				tree.setDomain(Puma);
				t.addData("channel=channel" + i % 5);
				t.complete();
			}

			for (int i = 0; i < 1000; i++) {
				Transaction t = Cat.newTransaction("URL", "t");
				Cat.logEvent("RemoteLink", "sina", Event.SUCCESS, "http://sina.com.cn/");
				t.complete();
			}
			for (int i = 0; i < 900; i++) {
				Transaction t = Cat.newTransaction("URL", "e");
				t.complete();
			}

			Thread.sleep(1000);
		}
	}

	@Test
	public void test2() throws Exception {
		while (true) {

			for (int i = 0; i < 1000; i++) {
				Transaction t = Cat.newTransaction("URL", "/index");
				Cat.logEvent("RemoteLink", "sina", Event.SUCCESS, "http://sina.com.cn/");
				t.addData("channel=channel" + i % 5);

				t.complete();
			}
			for (int i = 0; i < 900; i++) {
				Transaction t = Cat.newTransaction("URL", "/detail");
				t.addData("channel=channel" + i % 5);
				t.complete();
			}

			Thread.sleep(1000);
			break;
		}
	}

	@Test
	public void test3() throws InterruptedException {
		for (int i = 0; i < 500; i++) {
			Transaction t = Cat.newTransaction("test", "test");

			Cat.logMetricForCount(PayOrder, "MemberCardSuccess");
			Cat.logMetricForCount(PayOrder, "MemberCardFail", 2);

			MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
			((DefaultMessageTree) tree).setDomain("MobileMembercardMainApiWeb");
			t.complete();
		}
		Thread.sleep(100000);
	}

	public void sample() {
		String pageName = "";
		String serverIp = "";

		Transaction t = Cat.newTransaction("URL", pageName); // 创建一个Transaction

		try {
			// 记录一个事件
			Cat.logEvent("URL.Server", serverIp, Event.SUCCESS, "ip=" + serverIp + "&...");
			// 记录一个业务指标，记录订单次数
			Cat.logMetricForCount(PayOrder, "OrderCount");
			// 记录一个业务指标，记录支付次数
			Cat.logMetricForCount(PayOrder, "PayCount");

			yourBusiness();// 自己业务代码

			t.setStatus(Transaction.SUCCESS);// 设置状态
		} catch (Exception e) {
			t.setStatus(e);// 设置错误状态
		} finally {
			t.complete();// 结束Transaction
		}
	}

	private void yourBusiness() {

	}

}
