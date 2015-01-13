<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.heartbeat.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.heartbeat.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.heartbeat.Model" scope="request" />

<a:historyReport title="HeartBeat History Report">
	<jsp:attribute name="subtitle">${w:format(payload.historyStartDate,'yyyy-MM-dd HH:mm:ss')} to ${w:format(payload.historyDisplayEndDate,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
	<jsp:body>
	<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js"/>
<table class="machines">
	<tr style="text-align: left">
		<th>
   	  		 <c:forEach var="ip" items="${model.ips}">&nbsp;[&nbsp;
   	  		<c:choose>
					<c:when test="${payload.realIp eq ip}">
						<a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${ip}&reportType=${model.reportType}"
									class="current">${ip}</a>
					</c:when>
					<c:otherwise>
						<a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${ip}&reportType=${model.reportType}">${ip}</a>
					</c:otherwise>
				</c:choose>
   	 		&nbsp;]&nbsp;
			 </c:forEach>
		</th>
	</tr>
</table>
<br>
<table class="graph" id="graph">
<tr>
	<th	colspan="3" style="text-align:left">JVM Thread Info</th>
</tr>
<tr>
	<td><div id="NewGcCount" class="graph"></div></td>
	<td><div id="OldGcCount" class="graph"></div></td>
	<td><div id="SystemLoadAverage" class="graph"></div></td>
</tr>

<tr>
	<th colspan="3" style="text-align:left"><a  data-status="memory" class="heartbeat_graph_link" href="?op=historyPart&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&type=memory">Memory Info</a>&nbsp;&nbsp;&nbsp;&nbsp;(Memory Free,Heap Usage,None Heap Usage)</th>
</tr>
<tr>
	<td colspan="3"><iframe id="memory" style="display:none;" width="100%" height="400px"></iframe></td>
</tr>

<tr>
	<th colspan="3" style="text-align:left"><a  data-status="thread" class="heartbeat_graph_link" href="?op=historyPart&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&type=thread">JVM Thread Info</a>&nbsp;&nbsp;&nbsp;&nbsp;(HTTP Active Started Thread)</th>
</tr>
<tr>
	<td colspan="3"><iframe id="thread" style="display:none;" width="100%" height="400px"></iframe></td>
</tr>

<tr>
	<th colspan="3" style="text-align:left"><a  data-status="disk" class="heartbeat_graph_link" href="?op=historyPart&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&type=disk">Disk Info</a></th>
</tr>
<tr>
	<td colspan="3"><iframe id="disk" style="display:none;" width="100%" height="400px"></iframe></td>
</tr>

<tr>
	<th colspan="3" style="text-align:left"><a  data-status="cat" class="heartbeat_graph_link" href="?op=historyPart&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&type=cat">Cat Info</a></th>
</tr>
<tr>
	<td colspan="3"><iframe id="cat" style="display:none;" width="100%" height="400px"></iframe></td>
</tr>

<c:forEach var="extensionGroup" items="${model.extensionGroups}">
	<tr>
		<th colspan="3" style="text-align:left"><a  data-status="${extensionGroup}" class="heartbeat_graph_link" href="?op=historyPart&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&type=extension&extensionType=${extensionGroup}">${extensionGroup}</a></th>
	</tr>
	<tr>
		<td colspan="3"><iframe id="${extensionGroup}" style="display:none;" width="100%" height="400px"></iframe></td>
	</tr>
</c:forEach>

</table>
	<res:useJs value="${res.js.local['heartbeatHistory_js']}" target="head-buttom"/>
	<script>
	
	//07
	var newGcCountGraphData = ${model.newGcCountGraph};
	graphLineChart(document.getElementById('NewGcCount'), newGcCountGraphData);
	//08
	var oldGcCountGraphData = ${model.oldGcCountGraph};
	graphLineChart(document.getElementById('OldGcCount'), oldGcCountGraphData);
	//09
	var systemLoadAverageGraphData = ${model.systemLoadAverageGraph};
	graphLineChart(document.getElementById('SystemLoadAverage'), systemLoadAverageGraphData);
	
	
	</script>
</jsp:body>
</a:historyReport>
<script type="text/javascript" src="/cat/js/heartbeatHistory.js"></script>
<script type="text/javascript" src="/cat/js/appendHostname.js"></script>
<script type="text/javascript">
	$(document).ready(function() {
		appendHostname(${model.ipToHostnameStr});
		$('a:contains("month")').parent().hide();
		$('a:contains("week")').parent().hide();
	});
</script>