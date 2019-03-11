package weaver.yky.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.GCONST;
import weaver.general.TimeUtil;
import weaver.general.Util;
import weaver.hrm.User;
import weaver.workflow.field.FieldValue;

/**
 * 常用工具方法-公用类
 * @author bleach
 * @date 2018-01-18
 * @version 1.0
 */
public class ToolUtil extends BaseBean {
	boolean isDebug = false;
	
	/**
	 * 构造方法
	 */
	public ToolUtil() {
		// TODO Auto-generated constructor stub
		String isopen = getSystemParamValue("Debug_Mode");
		
		if("1".equals(isopen)){
			isDebug = true;
		}
	}
	
	/**
	 * 查询满足模糊查询的所有标识集合
	 * @param likestr 模糊条件
	 * @return
	 */
	public Map<String,String> getSystemParamValueMap(String likestr){
		return getSystemParamList(likestr);
	}
	
	/**
	 * 查询系统中所有参数配置
	 * @return
	 */
	public Map<String,String> getAllSystemParamValue(){
		return getSystemParamList("");
	}
	
	
	/**
	 * 获取参数集合
	 * @param likestr 模糊查询的条件
	 * @return 集合
	 */
	private Map<String,String> getSystemParamList(String likestr){
		Map<String,String> param_map = new HashMap<String, String>();
		
		String select_sql = "select uuid,paramvalue from uf_systemconfig";
		
		RecordSet rs = new RecordSet();
		
		if(!"".equals(likestr)){
			select_sql += " where uuid like '%" + likestr + "%'";
		}
		
		if(rs.execute(select_sql)){
			while(rs.next()){
				String uuid = Util.null2String(rs.getString(1));
				String paramvalue = Util.null2String(rs.getString(2));
				
				param_map.put(uuid, paramvalue);
			}
		}
		
		return param_map;
		
	}
	
	/**
     * 获取系统参数设置值
     * @param ssid
     * @return
     */
    public String getSystemParamValue(String uuid){
    	String paramvalue = "";
    	
    	if(!"".equals(uuid)){
    		String select_sql = "select paramvalue from uf_systemconfig where uuid = '" + uuid + "'";
    		
    		RecordSet rs = new RecordSet();
    		rs.executeSql(select_sql);
    		if(rs.next()){
    			paramvalue = Util.null2String(rs.getString(1));
    		}
    	}
    	
    	return paramvalue;
    }
    
    /**
	 * 用数据库值，根据规则转换，获取其最终结果
	 * @param cus_sql 自定义转换的SQL
	 * @param value 参数值
	 * @return
	 */
	public String getValueByChangeRule(String cus_sql,String value){
		
		return getValueByChangeRule(cus_sql,value,"");
	}
	
	 /**
	 * 用数据库值，根据规则转换，获取其最终结果
	 * @param cus_sql 自定义转换的SQL
	 * @param value 参数值
	 * @param requestid 流程请求ID
	 * @return
	 */
	public String getValueByChangeRule(String cus_sql,String value,String requestid){
		String endValue = "";
		
		cus_sql = cus_sql.replace("&nbsp;", " ");
		
		//参数进行替换
		String sqlString = cus_sql.replace("{?requestid}", requestid);
		
		sqlString = sqlString.replace("?", value);
		
		RecordSet rs = new RecordSet();
		
		if(rs.executeSql(sqlString)){
			rs.next();
			
			endValue = Util.null2String(rs.getString(1));
		}
		
		return endValue;
	}
    
 	/**
	 * 用数据库值，根据规则转换，获取其最终结果
	 * @param cus_sql 自定义转换的SQL
	 * @param value 参数值
	 * @param requestid 流程请求ID
	 * @return
	 */
	public String getValueByChangeRule(String cus_sql,String value,String requestid,int detailKeyvalue){
		String endValue = "";
		
		cus_sql = cus_sql.replace("&nbsp;", " ");
		
		cus_sql = cus_sql.replace("{?dt.id}", String.valueOf(detailKeyvalue));
		
		//参数进行替换
		String sqlString = cus_sql.replace("{?requestid}", requestid);
		
		sqlString = sqlString.replace("?", value);
		
		if(detailKeyvalue > 0){
			this.writeLog("执行的转换后的SQL:[" + sqlString + "]");
		}
		
		RecordSet rs = new RecordSet();
		
		if(rs.executeSql(sqlString)){
			rs.next();
			
			endValue = Util.null2String(rs.getString(1));
		}
		
		return endValue;
	}
	    
	
	/**
	 * 用数据库值，根据规则转换，获取其最终结果
	 * @param cus_sql 自定义转换的SQL
	 * @param value 参数值
	 * @param requestid 流程请求ID
	 * @return
	 */
	public String getValueByChangeRule_SingleParam(String cus_sql,String value){
		String endValue = "";
		
		cus_sql = cus_sql.replace("&nbsp;", " ");
		
		RecordSet rs = new RecordSet();
		
		if(rs.executeQuery(cus_sql,value)){
			rs.next();
			
			endValue = Util.null2String(rs.getString(1));
		}
		
		return endValue;
	}
    
	
	/**
	 * 根据字段ID获取其对应的字段名称
	 * @param fieldid
	 * @return
	 */
	public String getFieldNameByFieldid(String fieldid){
		String fieldname = "";
		
		if(!"".equals(fieldid)){
			
			if(fieldid.startsWith(",")){
				fieldid = fieldid.substring(1);
			}
			
			if(fieldid.endsWith(",")){
				fieldid =fieldid.substring(0,fieldid.length() - 1);
			}
			
			String select_sql = "select fieldname from workflow_billfield where id in (" + fieldid + ")";
			
			RecordSet rs = new RecordSet();
			
			if(rs.executeSql(select_sql)){
				while(rs.next()){
				
					fieldname += "," + Util.null2String(rs.getString(1));
				}
			}
		}
		
		if(fieldname.startsWith(",")){
			fieldname = fieldname.substring(1);
		}
		
		return fieldname;
	}
	
	  /**
     * 日志输出
     * @param logstr
     */
    public void writeDebugLog(Object logstr){
    	if(isDebug){
    		this.writeLog(logstr.toString());
    	}
    }
	
    /**
     * 日志输出
     * @param logstr
     */
    public void writeNewDebuggerLog(Object o,Object logstr){
    	if(isDebug){
    		writeNewLog(o.toString(),logstr.toString());
    	}
    }
    
    /**
	 * 写入同步的日志文件
	 * @param logList
	 */
	public void writeNewLog(String o,String s){
		try {
			String filename = "cus_" + TimeUtil.getCurrentDateString() + "_ecoogy.log";
			
			
			String folder = GCONST.getRootPath() + "log" + File.separatorChar + "cus";
			
			//this.writeDebugLog("folder:[" + folder + "]");
			
			 File f = new File(folder);

	        // 创建文件夹
	        if (!f.exists()) {
	            f.mkdirs();
	        }
			
			f = new File(folder + File.separatorChar  + filename);
			
			if(!f.exists()){//文件不存在，则直接创建
				f.createNewFile();
			}
			
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f, true)));
			 
			out.write("[" + o.getClass() + "][" + TimeUtil.getCurrentTimeString() + "]:"+ s + "\r\n");
			 
			 //关闭写入流
			 out.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.writeDebugLog("创建日志文件存在异常:[" + e.getMessage() + "/" + e.toString() + "]");
		}
	}
	
	/**
	 * 根据下一节点操作人获取用户id集合
	 * 
	 * @param hashtable
	 * @return
	 */
	public List<String> getUseridList(Hashtable hashtable) {
		List<String> list = new ArrayList<String>();
		if (null != hashtable && !hashtable.isEmpty()) {
			for (Iterator iterator = hashtable.keySet().iterator(); iterator.hasNext();) {
				String value = hashtable.get(iterator.next()).toString();
				int strStartIndex = value.indexOf("[") + 1;
				int strEndIndex = value.indexOf("_");
				String userid = value.substring(strStartIndex, strEndIndex);
				list.add(userid);
			}
		}
		return list;
	}
	
	/**
	 * 根据下一节点操作人获取用户id字符串
	 * 
	 * @param hashtable
	 * @return
	 */
	public String getUseridStr(Hashtable hashtable) {
		String useridStr = "";
		
		if (null != hashtable && !hashtable.isEmpty()) {
			for (Iterator iterator = hashtable.keySet().iterator(); iterator.hasNext();) {
				String value = hashtable.get(iterator.next()).toString();
				int strStartIndex = value.indexOf("[") + 1;
				int strEndIndex = value.indexOf("_");
				useridStr += value.substring(strStartIndex, strEndIndex) + ",";
			}
			
			useridStr = removeStrComma(useridStr);
		}
		
		return useridStr;
	}
	
	/**
	 * 
	 * 去除字符串的逗号
	 * 
	 * @param str
	 * @return
	 */
	public String removeStrComma(String str) {
		
		if(!"".equals(str)){
			if(str.endsWith(",")){
				
				str = str.substring(0, str.length() - 1);
				
			}
			
		}
		
		return str;
		
	}
	
	public String getMailByHrmids(String hrmids){
		String mails = "";
		
		if(!"".equals(hrmids)){
			
			RecordSet rs = new RecordSet();
			
			String sql = "select email from hrmresource where id in ("+hrmids+")";
			
			if(rs.executeQuery(sql)){
				while(rs.next()){
					
					String mail = Util.null2String(rs.getString(1));
					
					if(!"".equals(mail)){
						
						mails += mail + ",";
						
					}
					
				}
			}
			
			mails = removeStrComma(mails);
			
		}
		
		return mails;
	}
	
	public String cotentConvert(String content, RecordSet rs, int isbill, User user) throws Exception {
		String finalcotent = content;
		
		if(!"".equals(finalcotent)){
			
			RecordSet rs_dt = new RecordSet();
			
			while (content.indexOf("$") >= 0) {
				
				// 第一个$出现的位置
				int charstart = content.indexOf("$");
				
				// 从第一个$后开始截取字符串
				String tmp_String = content.substring(charstart + 1, content.length());
				
				// 第二个$出现的位置
				int charend = tmp_String.indexOf("$");
				
				// 第一个和第二个$中间的内容即为字段ID
				String fieldid = tmp_String.substring(0, charend);
				
				FieldValue FieldValue = new FieldValue();
				
				String select_sql = "select * from workflow_billfield where id = ? ";
				if (rs_dt.executeQuery(select_sql, fieldid)) {
					rs_dt.next();
					
					int fieldHtmlType = Util.getIntValue(rs_dt.getString("fieldhtmltype"), 0);
					int type = Util.getIntValue(rs_dt.getString("type"), 0);
					String fieldname = Util.null2String(rs_dt.getString("fieldname"));
					
					String field_value = "";
					if(!"".equals(fieldname)){
						field_value = Util.null2String(rs.getString(fieldname));
						
						field_value = FieldValue.getFieldValue(user, Util.getIntValue(fieldid, 0), fieldHtmlType, type, field_value, isbill);
						
						finalcotent = finalcotent.replace("$" + fieldid + "$", field_value);
					}
					
				}
				
				content = tmp_String.substring(charend + 1, tmp_String.length());
				
			}
			
		}
		
		return finalcotent;
	}
	
	public String fieldConvert(RecordSet rs, String fields) {
		
		String values = "";
		
		if(!"".equals(fields)){
			
			String[] fields_array = fields.split(",");
			
			for(String field : fields_array){
				
				values += Util.null2String(rs.getString(field));
				
			}
			
			values = removeStrComma(values);
			
		}
		
		return values;
		
	}
	
	/**
	 * 获取打开流程请求的链接地址
	 * 
	 * @param requestid
	 * @param request
	 * @param fu
	 * @param isRequest
	 * @return
	 */
	public String getOnlyLinkAddress(int requestid, HttpServletRequest request) {
		String linkAddress = "";

		String host = "";
		String oaaddress = "";
		RecordSet rs = new RecordSet();

		rs.executeQuery("select oaaddress from systemset");
		if (rs.next())
			oaaddress = rs.getString("oaaddress");

		if (request != null) {
			host = Util.getRequestHost(request);
		}

		if (!"".equals(oaaddress)) {
			host = oaaddress;
		} else {
			host = "http://" + host;
		}

		if (!"".equals(host)) {
			linkAddress = host + "/workflow/request/ViewRequest.jsp?requestid=" + requestid;
		}

		return linkAddress;
	}
	
	/**
	 * 获取流程标题加链接
	 * 
	 * @param request
	 * @param requestid
	 * @param requestname
	 * @return
	 */
	public String getWfTitle_Link(int requestid, String requestname, HttpServletRequest request) {
		String host = "";
		String oaaddress = "";

		RecordSet rs = new RecordSet();

		rs.executeQuery("select oaaddress from systemset");
		if (rs.next())
			oaaddress = rs.getString("oaaddress");

		if (request != null) {
			host = Util.getRequestHost(request);
		}

		if (!"".equals(oaaddress)) {
			host = oaaddress;
		} else {
			host = "http://" + host;
		}

		String loginPage = "login/Login.jsp";
		String gotoPage = "workflow/request/ViewRequest.jsp";

		if (GCONST.getMailReminderSet()) {
			loginPage = GCONST.getMailLoginPage();
			gotoPage = GCONST.getMailGotoPage();
		}
		// 邮件提醒的登录中转页面
		String mailrequestname = "(<a style='text-decoration: underline; color: blue;cursor:hand'  target='_blank' href=\""
				+ host + "/" + loginPage + "?gopage=/" + gotoPage + "?requestid=" + requestid + "\" >" + requestname
				+ "</a>)";

		return mailrequestname;
	}

	/**
	 * 
	 * 根据工作流ID获取流程的表名
	 * 
	 * @param workflowid
	 * @return
	 */
	public String getTablenameByworkflowid(int workflowid){
		
		String tablename = "";
		
		RecordSet rs = new RecordSet();
		
		String sql = "select tablename from workflow_bill  where id = (select formid from workflow_base where id = "+workflowid+")";
		
		if(rs.executeQuery(sql)){
			rs.next();
			
			tablename = Util.null2String(rs.getString(1));
			
		}
		
		return tablename;
	}
	
	
	public String getValueByCusDateRule(String paramDate, String paramString) {
		String value = "";

		if (null == paramString || "".equals(paramString)) {
			paramString = "yyyy-MM-dd";
		}

		if (null != paramDate && !"".equals(paramDate)) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date date = null;
			try {
				date = format.parse(paramDate);
			} catch (ParseException e) {
				System.out.println(e.getMessage());
			}

			SimpleDateFormat sdf = new SimpleDateFormat(paramString);
			value = sdf.format(date);
		}

		return value;
	}
	
	/**
	 * HTTP协议
	 * 
	 * @param url
	 * @param param
	 * @return
	 */
	public String post(String url, String param) {
		String date = param;
		try {
			URL testurl = new URL(url);
			// 注意提交的编码 这边是需要改变的 这边默认的是Default：系统当前编码 byte[] postData
			byte[] postData = date.getBytes("UTF-8");
			// 设置提交的相关参数
			HttpURLConnection httpConn = (HttpURLConnection) testurl.openConnection();
			httpConn.setRequestMethod("POST");
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);
			httpConn.setRequestProperty("Content-Type", "application/json");
			httpConn.setRequestProperty("Charset", "UTF-8");

			// 提交请求数据
			OutputStream outputStream = httpConn.getOutputStream();
			outputStream.write(postData);
			outputStream.flush();

			BufferedReader reader;
			String srcString;
			StringBuffer sb = new StringBuffer();
			reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
			while ((srcString = reader.readLine()) != null) {
				sb.append(srcString).append("\n");
			}
			reader.close();
			String result = sb.toString();
			return result;

		} catch (Exception ex) {
			return ex.getMessage();
		}

	}
	
	/**
	 * 解析XML格式的报文，取出相应的节点所对应的值
	 * 
	 * @param xml:XMl格式报文
	 * @param nodename:节点名称
	 * @return String:节点名称对应的值
	 */
	public String getnode(String xml, String nodename) {
		String val = "";
		if (xml.contains("<" + nodename + ">")) {
			val = xml.substring(xml.indexOf("<" + nodename + ">") + ("<" + nodename + ">").length(),
					xml.indexOf("</" + nodename + ">"));
		}
		if (val.equals("")) {
			val = null;
		}
		return val;
	}
	
	/**
	 * @return the isDebug
	 */
	public boolean isDebug() {
		return isDebug;
	}

	/**
	 * @param isDebug the isDebug to set
	 */
	public void setDebug(boolean isDebug) {
		this.isDebug = isDebug;
	}
}
