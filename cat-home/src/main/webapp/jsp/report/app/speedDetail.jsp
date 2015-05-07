<%@ page contentType="text/html; charset=utf-8"%>
	<table>
		<tr>
			<th align=left>
					<div style="float:left;">
						&nbsp;日期
					<input type="text" id="time" style="width:110px;"/>
					</div>
					&nbsp;页面 <select id="page" style="width: 240px;">
					<c:forEach var="item" items="${model.pages}" varStatus="status">
							<option value='${item}'>${item}</option>
					</c:forEach>
					</select> 
					阶段 <select id="step" style="width: 240px;">
					</select> 
					 网络类型 <select id="network" style="width: 80px;">
					<option value=''>All</option>
					<c:forEach var="item" items="${model.networks}" varStatus="status">
						<option value='${item.value.id}'>${item.value.name}</option>
					</c:forEach>
			</select>
			</th>
		</tr>
		<tr>
			<th align=left>&nbsp;版本 <select id="version" style="width: 100px;">
					<option value=''>All</option>
					<c:forEach var="item" items="${model.versions}" varStatus="status">
						<option value='${item.value.id}'>${item.value.name}</option>
					</c:forEach>
			</select> 平台 <select id="platform" style="width: 100px;">
					<option value=''>All</option>
					<c:forEach var="item" items="${model.platforms}"
						varStatus="status">
						<option value='${item.value.id}'>${item.value.name}</option>
					</c:forEach>
			</select> 地区 <select id="city" style="width: 100px;">
					<option value=''>All</option>
					<c:forEach var="item" items="${model.cities}" varStatus="status">
						<option value='${item.value.id}'>${item.value.name}</option>
					</c:forEach>
			</select> 运营商 <select id="operator" style="width: 100px;">
					<option value=''>All</option>
					<c:forEach var="item" items="${model.operators}"
						varStatus="status">
						<option value='${item.value.id}'>${item.value.name}</option>
					</c:forEach>
			</select> <input class="btn btn-primary btn-sm"
				value="&nbsp;&nbsp;&nbsp;查询&nbsp;&nbsp;&nbsp;" onclick="query()"
				type="submit" /> <input class="btn btn-primary" id="checkbox"
				onclick="check()" type="checkbox" /> <label for="checkbox"
				style="display: -webkit-inline-box">选择对比</label>
			</th>
		</tr>
	</table>
	<table id="history" style="display: none">
		<tr>
			<th align=left>
				<div style="float:left;">
						&nbsp;开始
					<input type="text" id="time2" style="width:110px;"/>
					</div>
				 &nbsp;页面 <select id="page2" style="width: 240px;">
					<c:forEach var="item" items="${model.pages}" varStatus="status">
							<option value='${item}'>${item}</option>
					</c:forEach>
					</select> 
					阶段 <select id="step2" style="width: 240px;">
					</select> 
					网络类型 <select id="network2" style="width: 80px;">
					<option value=''>All</option>
					<c:forEach var="item" items="${model.networks}" varStatus="status">
						<option value='${item.value.id}'>${item.value.name}</option>
					</c:forEach>
			</select>
			</th>
		</tr>
		<tr>
			<th align=left>&nbsp;版本 <select id="version2" style="width: 100px;">
					<option value=''>All</option>
					<c:forEach var="item" items="${model.versions}" varStatus="status">
						<option value='${item.value.id}'>${item.value.name}</option>
					</c:forEach>
			</select> 平台 <select id="platform2" style="width: 100px;">
					<option value=''>All</option>
					<c:forEach var="item" items="${model.platforms}"
						varStatus="status">
						<option value='${item.value.id}'>${item.value.name}</option>
					</c:forEach>
			</select> 地区 <select id="city2" style="width: 100px;">
					<option value=''>All</option>
					<c:forEach var="item" items="${model.cities}" varStatus="status">
						<option value='${item.value.id}'>${item.value.name}</option>
					</c:forEach>
			</select> 运营商 <select id="operator2" style="width: 100px;">
					<option value=''>All</option>
					<c:forEach var="item" items="${model.operators}"
						varStatus="status">
						<option value='${item.value.id}'>${item.value.name}</option>
					</c:forEach>
			</select>
			</th>
		</tr>
	</table>

	<div style="float: left; width: 100%;">
		<div id="${model.appSpeedDisplayInfo.lineChart.id}"></div>
	</div>
<h5 class="center text-success"><strong>计算规则：所选天中288个5分钟点数据求和再平均的结果</strong></h5>
<table id="web_content" class="table table-striped table-condensed table-bordered table-hover">
	<thead>
	<tr>
		<th class="right text-success"  width="10%">日期</th>
		<th class="right text-success" width="10%">访问次数</th>
		<th class="right text-success" width="10%">慢用户比例</th>
		<th class="right text-success" width="10%">延时(ms)</th>
		<c:if test="${fn:length(model.appSpeedDisplayInfo.appSpeedSummarys) gt 1}">
			<th class="right text-success" width="10%">对比日期</th>
			<th class="right text-success" width="10%">对比访问次数</th>
			<th class="right text-success" width="10%">对比慢用户比例</th>
			<th class="right text-success" width="10%">对比延时(ms)</th>
			<th class="right text-success" width="10%">变化比例</th>
		</c:if>
	</tr>
	</thead>
	<tbody>
	<c:set var="summarys" value="${model.appSpeedSummarys}" />
		<c:forEach var="entry" items="${summarys['当前值']}" >
		<tr class="right">
	 		<td class="right">${entry.value.dayTime}</td>
			<td>${w:format(entry.value.accessNumberSum,'#,###,###,###,##0')}</td>
			<td>${w:format(entry.value.slowRatio,'#0.000')}%</td>
			<td>${w:format(entry.value.responseTimeAvg,'#,###,###,###,##0')}</td>
			<c:if test="${fn:length(model.appSpeedDisplayInfo.appSpeedSummarys) gt 1}">
				<c:set var="response" value="${summarys['对比值'][entry.value.minuteOrder].responseTimeAvg}" />
				<c:set var="ratio" value="${(entry.value.responseTimeAvg - response) / response * 100}" />
				<td class="right">${summarys['对比值'][entry.value.minuteOrder].dayTime}</td>
				<td>${w:format(summarys['对比值'][entry.value.minuteOrder].accessNumberSum,'#,###,###,###,##0')}</td>
				<td>${w:format(summarys['对比值'][entry.value.minuteOrder].slowRatio,'#0.000')}%</td>
				<td>${w:format(summarys['对比值'][entry.value.minuteOrder].responseTimeAvg,'#,###,###,###,##0')}</td>
				<c:choose>
				<c:when test="${ratio < 0 }">
				<td class="text-success">${w:format(ratio,'#0.000')}%</td>
				</c:when>
				<c:otherwise>
				<td class="text-danger">${w:format(ratio,'#0.000')}%</td>
				</c:otherwise>
				</c:choose>
			</c:if>
		</tr>
		</c:forEach>
	</tbody>
</table>
<h5 class="center text-success"><strong>计算规则：这个测速点5分钟内所有数据求和再平均的结果</strong></h5>
<table id="web_content" class="table table-striped table-condensed table-bordered table-hover">
	<thead>
	<tr>
		<th class="right text-success" width="10%">时间</th>
		<th class="right text-success" width="10%">访问次数</th>
		<th class="right text-success" width="10%">慢用户比例</th>
		<th class="right text-success" width="10%">延时(ms)</th>
		<c:if test="${fn:length(model.appSpeedDisplayInfo.appSpeedDetails) gt 1}">
			<th class="right text-success" width="10%">对比时间</th>
			<th class="right text-success" width="10%">对比访问次数</th>
			<th class="right text-success" width="10%">对比慢用户比例</th>
			<th class="right text-success" width="10%">对比延时(ms)</th>
			<th class="right text-success" width="10%">变化比例</th>
		</c:if>
	</tr></thead>
	<tbody id="details">
		<c:set var="details" value="${model.appSpeedDetails}" />
		<c:forEach var="entry" items="${details['当前值']}" >
		<tr class="right" >
	 		<td class="right" width="10%">${entry.value.dateTime}</td>
			<td>${w:format(entry.value.accessNumberSum,'#,###,###,###,##0')}</td>
			<td>${w:format(entry.value.slowRatio,'#0.000')}%</td>
			<td>${w:format(entry.value.responseTimeAvg,'#,###,###,###,##0')}</td>
			<c:if test="${fn:length(model.appSpeedDisplayInfo.appSpeedDetails) gt 1}">
				<c:set var="response" value="${details['对比值'][entry.value.minuteOrder].responseTimeAvg}" />
				<c:set var="ratio" value="${(entry.value.responseTimeAvg - response) / response * 100}" />
				<td class="right">${details['对比值'][entry.value.minuteOrder].dateTime}</td>
				<td>${w:format(details['对比值'][entry.value.minuteOrder].accessNumberSum,'#,###,###,###,##0')}</td>
				<td>${w:format(details['对比值'][entry.value.minuteOrder].slowRatio,'#0.000')}%</td>
				<td>${w:format(details['对比值'][entry.value.minuteOrder].responseTimeAvg,'#,###,###,###,##0')}</td>
				<c:choose>
				<c:when test="${ratio < 0 }">
				<td class="text-success">${w:format(ratio,'#0.000')}%</td>
				</c:when>
				<c:otherwise>
				<td class="text-danger">${w:format(ratio,'#0.000')}%</td>
				</c:otherwise>
				</c:choose>
			</c:if>
		</tr>
		</c:forEach>
	</tbody>
</table>
