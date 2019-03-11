package weaver.les.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;
import weaver.yky.common.ToolUtil;

/**
 * 
 * @author YKY
 *
 */
public class OA2NCInterface_Action extends BaseBean implements Action{

	@Override
	public String execute(RequestInfo requestInfo) {
		this.writeLog("--------------------Do A OA2NCInterface_Action Begin---------------------------");

		String workflowid = Util.null2String(requestInfo.getWorkflowid());//工作流ID
		
		String requestid = Util.null2String(requestInfo.getRequestid());// 流程请求ID
		
		int formid = requestInfo.getRequestManager().getFormid();// 流程表单ID
		
		String src = requestInfo.getRequestManager().getSrc();// 当前操作类型 submit:提交/reject:退回
		
		this.writeLog("workflowid："+workflowid+" formid：" + formid);
		this.writeLog("requestid："+requestid+" src：" + src);
		
		try {
			
			String interfacetype = getInterfacetype(workflowid);
			this.writeLog("interfacetype："+interfacetype);
			
			String sql = "";
			
			String chkMsg = "";
			
			RecordSet rs = new RecordSet();
			RecordSet rs_1 = new RecordSet();
			
			ToolUtil ToolUtil = new ToolUtil();
			
			String tablename = "formtable_main_" + Math.abs(formid);//流程主表
			
			String mainid = "";
			
			String dttable = "";//明细表
			
			String main = "";
			String header = "";
			String body = "";
			
			String roottag = "";
			String billtype = "";
			String subtype = "";
			String replace = "";
			String receiver = "";
			String sender = "";
			String isexchange = "";
			String proc1 = "";
			String filename = "";
			
			sql = "select * from uf_oa_ncsystem where type = "+interfacetype+" and workflowid = " + workflowid;
			if(rs.executeQuery(sql)){
				rs.next();
				
				mainid = Util.null2String(rs.getString("id"));
				
				dttable = Util.null2String(rs.getString("dttable"));
				
				main = Util.null2String(rs.getString("main"));
				header = Util.null2String(rs.getString("header"));
				body = Util.null2String(rs.getString("body"));

				roottag = Util.null2String(rs.getString("roottag"));
				billtype = Util.null2String(rs.getString("billtype"));
				subtype = Util.null2String(rs.getString("subtype"));
				replace = Util.null2String(rs.getString("replace"));
				receiver = Util.null2String(rs.getString("receiver"));
				sender = Util.null2String(rs.getString("sender"));
				isexchange = Util.null2String(rs.getString("isexchange"));
				proc1 = Util.null2String(rs.getString("proc1"));
				filename = Util.null2String(rs.getString("filename"));
			}

			List<Map<String, String>> lists = new ArrayList<Map<String, String>>();//自定义主表集合
			sql = "select * from uf_oa_ncsystem_dt1 where mainid = " + mainid;
			if(rs.executeQuery(sql)){
				while(rs.next()){
					
					String oa_fieldid = Util.null2String(rs.getString("oa_field"));//OA字段
					String interf_field = Util.null2String(rs.getString("interf_field"));//接口字段
					String convert_type = Util.null2String(rs.getString("convert_type"));//转换类型
					String convert_value = Util.null2String(rs.getString("convert_value")).replaceAll("<br>", " ").replaceAll("&nbsp;", " ");//转换值
					String field_type = Util.null2String(rs.getString("field_type"));//字段类型
					
					if("".equals(interf_field)){
						continue;
					}
					
					String oa_field = "";
					String viewtype = "";
					
					if(!"".equals(oa_fieldid)){
						String select_sql = "select fieldname, viewtype from Workflow_Field_View where fieldid = " + oa_fieldid;
						if(rs_1.executeQuery(select_sql)){
							rs_1.next();
							
							oa_field = Util.null2String(rs_1.getString(1));
							viewtype = Util.null2String(rs_1.getString(2));
						}
						
					}
					
					Map<String, String> map = new HashMap<String, String>();
					map.put("oa_field", oa_field);
					map.put("viewtype", viewtype);
					map.put("interf_field", interf_field);
					map.put("convert_type", convert_type);
					map.put("convert_value", convert_value);
					map.put("field_type", field_type);
					
					lists.add(map);
					
				}
			}
			
			List<Map<String, String>> pramLlists = new ArrayList<Map<String, String>>();//自定义参数集合
			sql = "select * from uf_oa_ncsystem_dt2 where mainid = " + mainid;
			if(rs.executeQuery(sql)){
				while(rs.next()){
					
					
					String urlParameter = Util.null2String(rs.getString("urlParameter"));//接口字段
					String oa_fieldid = Util.null2String(rs.getString("oa_field"));//OA字段
					String convert_type = Util.null2String(rs.getString("convert_type"));//转换类型
					String convert_value = Util.null2String(rs.getString("convert_value")).replaceAll("<br>", " ").replaceAll("&nbsp;", " ");//转换值
					
					if("".equals(urlParameter)){
						continue;
					}
					
					String oa_field = "";
					
					if(!"".equals(oa_fieldid)){
						String select_sql = "select fieldname from Workflow_Field_View where fieldid = " + oa_fieldid;
						if(rs_1.executeQuery(select_sql)){
							rs_1.next();
							
							oa_field = Util.null2String(rs_1.getString(1));
						}
						
					}
					
					Map<String, String> map = new HashMap<String, String>();
					map.put("urlParameter", urlParameter);
					map.put("oa_field", oa_field);
					map.put("convert_type", convert_type);
					map.put("convert_value", convert_value);
					
					pramLlists.add(map);
					
				}
			}
			
			String requestXML = "<?xml version='1.0' encoding='utf-8'?>";
			requestXML += "<ufinterface roottag='"+roottag+"' billtype='"+billtype+"' subtype='"+subtype+"' replace='"+replace+"' receiver='"+receiver+"' sender='"+sender+"' isexchange='"+isexchange+"' proc='"+proc1+"' filename='"+filename+"'>";
			
			this.writeLog("tablename："+tablename+" dttable：" + dttable);
			
			String param = "";
			
			sql = "select * from "+tablename+" where requestid = " + requestid;//取主表数据
			if(rs.executeQuery(sql)){
				rs.next();
				
				mainid = Util.null2String(rs.getString("id"));
				
				param = getParam(pramLlists, rs, requestid);
				
				String select_sql = "select * from "+dttable+" where mainid = " + mainid;
				if(rs_1.executeQuery(select_sql)){
					while(rs_1.next()){
						
						String id = Util.null2String(rs_1.getString("id"));//明细表ID
						
						String[] res = handleXml(lists, rs, rs_1, requestid);
						
						requestXML += "<"+main+" id='"+requestid+id+"'>";
						requestXML += "<"+header+">";
						
						requestXML += res[0];
						
						requestXML += "</"+header+">";
						if(!"".equals(body)){
							requestXML += "<"+body+">";
							
							requestXML += res[1];
							
							requestXML += "</"+body+">";
						}
						requestXML += "</"+main+">";
						
					}
					
				}
				
			}
			
			requestXML += "</ufinterface>";
			
			this.writeLog("requestXML："+requestXML);
			
			String url = ToolUtil.getSystemParamValue("NC_HttpRequest_URI");
			
			this.writeLog("param："+param);
			
			if(!"".equals(param)){
				url += "?" + param;
			}
			
			this.writeLog("url："+url);
			
			String responseXML = ToolUtil.post(url, requestXML);
			
			this.writeLog("responseXML："+responseXML);
			
			String resultcode = ToolUtil.getnode(responseXML, "resultcode");
			
			this.writeLog("resultcode："+resultcode);
			
			if(!"1".equals(resultcode)){//失败
				
				chkMsg = ToolUtil.getnode(responseXML, "resultdescription");//错误消息
				
				this.writeLog("chkMsg：" + chkMsg);
				
			}
			
			if(!"".equals(chkMsg)){
				
				requestInfo.getRequestManager().setMessageid("111" + requestid + "222");
				requestInfo.getRequestManager().setMessagecontent(chkMsg);
				
				return Action.FAILURE_AND_CONTINUE;
			}
			
		} catch (Exception e) {
			
			requestInfo.getRequestManager().setMessageid("111" + requestid + "222");
			requestInfo.getRequestManager().setMessagecontent(e.toString() + "/" + e.getMessage());
			
			this.writeLog("接口异常：" + e.toString() + "/" + e.getMessage());
			
			return Action.FAILURE_AND_CONTINUE;
		}

		this.writeLog("--------------------Do A OA2NCInterface_Action End---------------------------");
		return Action.SUCCESS;

	}
	
	public String[] handleXml(List<Map<String, String>> lists, RecordSet rs, RecordSet rs_1, String requestid){
		
		ToolUtil ToolUtil = new ToolUtil();
		
		String[] resarray = {"",""};
		
		String headervalue = "";
		String bodyvalue = "";
		
		for(Map<String, String> map : lists){
			
			String oa_field = map.get("oa_field");//OA字段
			String interf_field = map.get("interf_field");//接口字段
			String convert_type = map.get("convert_type");//转换类型
			String convert_value = map.get("convert_value");//转换值
			
			String viewtype = map.get("viewtype");//主表/明细表
			String field_type = map.get("field_type");//header/body
			
			String values = "";
			
			if("0".equals(convert_type)){//不转换
				
				values = getValueByViewtype(viewtype, oa_field, rs, rs_1);
				
			}else if("1".equals(convert_type)){//固定值
				
				values = convert_value;
				
			}else if("2".equals(convert_type)){//sql转换
				
				values = getValueByViewtype(viewtype, oa_field, rs, rs_1);
				
				values = "'" + values + "'";
				
				values = ToolUtil.getValueByChangeRule(convert_value, values, requestid);
				
			}
			
			if("0".equals(field_type)){//header
				
				headervalue += "<"+interf_field+">"+values+"</"+interf_field+">";
				
			}else if("1".equals(field_type)){//body
				
				bodyvalue += "<entry><def_quote>"+interf_field+"</def_quote><def_value>"+values+"</def_value></entry>";
			}
			
		}
		
		resarray[0] = headervalue;
		resarray[1] = bodyvalue;
		
		return resarray;
	}
	
	public String getParam(List<Map<String, String>> lists, RecordSet rs, String requestid){
		
		String param = "";
		
		ToolUtil ToolUtil = new ToolUtil();
		
		for(Map<String, String> map : lists){
			
			String oa_field = map.get("oa_field");//OA字段
			String urlParameter = map.get("urlParameter");
			String convert_type = map.get("convert_type");//转换类型
			String convert_value = map.get("convert_value");//转换值
			
			String values = "";
			
			if("0".equals(convert_type)){//不转换
				
				values = Util.null2String(rs.getString(oa_field));
				
			}else if("1".equals(convert_type)){//固定值
				
				values = convert_value;
				
			}else if("2".equals(convert_type)){//sql转换
				
				values = Util.null2String(rs.getString(oa_field));
				
				values = "'" + values + "'";
				
				values = ToolUtil.getValueByChangeRule(convert_value, values, requestid);
				
			}
			
			
			param += urlParameter + "=" + values + "&";
			
		}
		
		if(!"".equals(param)){
			if(param.endsWith("&")){
				
				param = param.substring(0, param.length() - 1);
				
			}
		}
		
		return param;
		
	}
	
	public String getInterfacetype(String workflowid){
		
		String interfacetype = "";
		
		RecordSet rs = new RecordSet();
		
		String sql = "select type from uf_oa_ncsystem where workflowid = '"+workflowid+"'";
		
		if(rs.executeQuery(sql)){
			rs.next();
			
			interfacetype = Util.null2String(rs.getString(1));
			
		}
		
		return interfacetype;
		
	}
	
	public String getValueByViewtype(String viewtype, String oa_field, RecordSet rs, RecordSet rs_1){
		String value = "";
		
		if("0".equals(viewtype)){//主表
			
			value = Util.null2String(rs.getString(oa_field));
			
		}else if("1".equals(viewtype)){//明细表
			
			value = Util.null2String(rs_1.getString(oa_field));
			
		}
		
		return value;
	}
	
}
