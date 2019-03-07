package weaver.lyf.action;

import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetTrans;
import weaver.general.TimeUtil;
import weaver.general.Util;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;
import weaver.zwl.common.ToolUtil;

/**
 * 流程数据拼接成XML格式然后通过HttpClient方式发送至NC中
 * @author bleach
 *
 */
public class WorkflowDataNCAction extends ToolUtil implements Action {
	
	String this_requestid = "";//当前流程请求ID

    String this_requestname = "";//当前流程请求标题

    String this_requestmark = "";//当前流程流程编号

	/**
	 * 实现父类方法
	 */
	@Override
	public String execute(RequestInfo requestInfo) {
		// TODO Auto-generated method stub
		this.writeLog("-------------------------WorkflowDataNCAction Begin-------------------------");
        //获取流程类型ID
        String workflowid = Util.null2String(requestInfo.getWorkflowid());
        //获取流程请求ID
        this_requestid = Util.null2String(requestInfo.getRequestid());
        //获取流程表单ID
        int formid = requestInfo.getRequestManager().getFormid();

        //获取流程事务
         RecordSetTrans rsts = requestInfo.getRsTrans();

        //获取流程基础数据
        String select_base_sql = "select * from workflow_requestbase where requestid = ?";
        try {
            if(rsts == null){
                rsts = new RecordSetTrans();
            }

            if(rsts.executeQuery(select_base_sql,this_requestid)){
                while(rsts.next()){
                    this.this_requestname = Util.null2String(rsts.getString("requestname"));
                    this.this_requestmark = Util.null2String(rsts.getString("requestmark"));
                }
            }

            this.writeLog("this_requestname:[" + this_requestname + "],this_requestmark:[" + this_requestmark + "],workflowid:["+workflowid+"],this_requestid:[" + this_requestid + "],formid:[" + formid + "]");
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            this.writeLog("获取该流程事物数据集异常:[" + e1.getMessage() + "/" + e1.toString() + "]");

            return Action.FAILURE_AND_CONTINUE;
        }

        RecordSet rs = new RecordSet();

        int config_main_key = -1;
        
        //明细表序列
        int detailTableIndex = -1;
        
        //获取当前流程配置信息
        String select_main_config_sql = "select * from uf_nc_data_config where  workflowid in (select id from workflow_base where activeVersionID in (select activeVersionID  from workflow_base where id = ?) or id = ?)";

        if(rs.executeQuery(select_main_config_sql,workflowid,workflowid)){
            if(rs.next()){
                config_main_key = Util.getIntValue(rs.getString("id"),0);
                
                detailTableIndex = Util.getIntValue(rs.getString("detailTableIndex"),-1);
            }
        }
        
        //XML表头字段集合
        List<Map<String, Object>> voucher_head_list = new ArrayList<Map<String,Object>>();
        
        //XML根节点属性字段集合
        List<Map<String, Object>> root_attribute_list = new ArrayList<Map<String,Object>>();
        
        //XML表体字段集合
        List<Map<String, Object>> voucher_item_list = new ArrayList<Map<String,Object>>();

        //url参数字段集合
        List<Map<String, Object>> url_item_list = new ArrayList<Map<String,Object>>();
        
        if(config_main_key > 0) {//说明该流程存在配置
            //获取当前流程明细字段配置信息
            String select_detail_config_sql = "select dt.*,wb.fieldname,wb.viewtype,wb.detailtable from uf_nc_data_config_dt1 dt left join workflow_billfield wb on dt.fieldid = wb.id where dt.mainid = ?";

            if(rs.executeQuery(select_detail_config_sql,config_main_key)){
                while(rs.next()){
                    //XML节点名称
                    String xmlfield = Util.null2String(rs.getString("xmlfield"));
                    //XML节点所属
                    int xmlbelongto = Util.getIntValue(rs.getString("xmlbelong"),0);
                    //OA流程字段
                    String fieldname = Util.null2String(rs.getString("fieldname"));
                    //OA流程字段所属
                    int viewtype = Util.getIntValue(rs.getString("viewtype"),0);
                    //转换规则
                    int changerule = Util.getIntValue(rs.getString("changerule"),0);
                    //自定义转换规则
                    String cussql = Util.null2String(rs.getString("cussql"));
                    //明细表名称
                    String detailtable = Util.null2String(rs.getString("detailtable"));
                    //特殊属性
//                    int special_attribute = Util.getIntValue(rs.getString("special_attribute"),0);

                    if(!"".equals(xmlfield)){
                        Map<String,Object> detail_map = new HashMap<String,Object>();

                        detail_map.put("xmlfield",xmlfield);
                        detail_map.put("xmlbelongto",xmlbelongto);
                        detail_map.put("fieldname",fieldname);
                        detail_map.put("viewtype",viewtype);
                        detail_map.put("changerule",changerule);
                        detail_map.put("cussql",cussql);
                        detail_map.put("detailtable",detailtable);
//                        detail_map.put("special_attribute",special_attribute);
                        
                        if(xmlbelongto == 0) {//凭证头字段配置集合
                            voucher_head_list.add(detail_map);
                        }else{//明细分录字段配置
                        	voucher_item_list.add(detail_map);
                        }
                    }
                }
            }
            
            //获取根节点属性配置
            select_detail_config_sql = "select dt.*,wb.fieldname,wb.viewtype from uf_nc_data_config_dt2 dt left join workflow_billfield wb on dt.fieldid = wb.id where dt.mainid = ?";
            if(rs.executeQuery(select_detail_config_sql,config_main_key)){
                while(rs.next()){
                    //XML节点名称
                    String attributeName = Util.null2String(rs.getString("attributeName"));
                    //OA流程字段
                    String fieldname = Util.null2String(rs.getString("fieldname"));
                    //OA流程字段所属
                    int viewtype = Util.getIntValue(rs.getString("viewtype"),0);
                    //转换规则
                    int changerule = Util.getIntValue(rs.getString("changerule"),0);
                    //自定义转换规则
                    String cussql = Util.null2String(rs.getString("cussql"));

                    if(!"".equals(attributeName)){
                        Map<String,Object> detail_map = new HashMap<String,Object>();

                        detail_map.put("xmlfield",attributeName);
                        detail_map.put("fieldname",fieldname);
                        detail_map.put("viewtype",viewtype);
                        detail_map.put("changerule",changerule);
                        detail_map.put("cussql",cussql);
                        
                        root_attribute_list.add(detail_map);
                    }
                }
            }
            //获取url参数配置
            select_detail_config_sql = "select dt.*,wb.fieldname,wb.viewtype from uf_nc_data_config_dt3 dt left join workflow_billfield wb on dt.fieldid = wb.id where dt.mainid = ?";
            if(rs.executeQuery(select_detail_config_sql,config_main_key)){
                while(rs.next()){
                    //XML节点名称
                    String urlParameter = Util.null2String(rs.getString("urlParameter"));
                    //OA流程字段
                    String fieldname = Util.null2String(rs.getString("fieldname"));
                    //OA流程字段所属
                    int viewtype = Util.getIntValue(rs.getString("viewtype"),0);
                    //转换规则
                    int changerule = Util.getIntValue(rs.getString("changerule"),0);
                    //自定义转换规则
                    String cussql = Util.null2String(rs.getString("cussql"));

                    if(!"".equals(urlParameter)){
                        Map<String,Object> detail_map = new HashMap<String,Object>();
                        detail_map.put("urlParameter",urlParameter);
                        detail_map.put("fieldname",fieldname);
                        detail_map.put("viewtype",viewtype);
                        detail_map.put("changerule",changerule);
                        detail_map.put("cussql",cussql);
                        url_item_list.add(detail_map);
                    }
                }
            }
        }
        
        //流程主表主键
        int workflow_main_key = 0;

        //获取流程主表信息
        String select_workflow_main = "select * from formtable_main_" + Math.abs(formid) + " where requestid=? ";

        this.writeLog("查询流程主表数据:[" + select_workflow_main + "],[" + this_requestid + "]");
        
        RecordSet rs_main = new RecordSet();
        
        if(rs_main.executeQuery(select_workflow_main,this_requestid)){
            if(rs_main.next()) {
                workflow_main_key = Util.getIntValue(rs_main.getString("id"), 0);
            }
        }
        
        String root_attr_xml = "";
        
        //获取根节点属性
        for(Map<String,Object> detail_map : root_attribute_list){
        	//属性名称
        	String attributeName = Util.null2String(detail_map.get("xmlfield").toString());
        	
        	if("".equals(attributeName)){
        		continue;
        	}
        	
        	//获取属性值
        	String attributeValue = getFieldValue(detail_map,rs_main,null,-1);
        	
        	root_attr_xml+= attributeName + "=\"" + attributeValue + "\" ";
        }
        
        //XML凭证头节点拼接
        String header_xml = "<voucher_head>";
        
        for(Map<String,Object> detail_map : voucher_head_list){
        	//属性名称
        	String xml_node_name = Util.null2String(detail_map.get("xmlfield").toString());
        	
        	if("".equals(xml_node_name)){
        		continue;
        	}
        	
        	//获取属性值
        	String xml_node_value = getFieldValue(detail_map,rs_main,null,-1);
        	
        	header_xml += "<" + xml_node_name + ">";
        	header_xml += xml_node_value;
        	header_xml += "</" + xml_node_name + ">";
        }
        
        header_xml += "</voucher_head>";
        
        
        //XML凭证分录节点
        String item_xml = "<voucher_body>";
        
        if(detailTableIndex > 0){//说明存在明细表
        	
        	RecordSet rs_detail = new RecordSet();
        	
        	String select_detail = "select * from formtable_main_" + Math.abs(formid) + "_dt" + detailTableIndex + " where mainid = ?";
        	
        	if(rs_detail.executeQuery(select_detail, workflow_main_key)){
        		int rownum = 1;
        		while(rs_detail.next()){
        			item_xml += "<entry>";
        			
        			  for(Map<String,Object> detail_map : voucher_item_list){
        		        	//属性名称
        		        	String xml_node_name = Util.null2String(detail_map.get("xmlfield").toString());
        		        	
        		        	if("".equals(xml_node_name)){
        		        		continue;
        		        	}
        		        	
        		        	//获取属性值
        		        	String xml_node_value = getFieldValue(detail_map,rs_main,rs_detail,rownum);
        		        	
        		        	item_xml += "<" + xml_node_name + ">";
        		        	item_xml += xml_node_value;
        		        	item_xml += "</" + xml_node_name + ">";
        		        }
        			
        			item_xml += "</entry>";
        			
        			rownum++;
        		}
        	}
        	
        }else{//不存在明细表，说明所有的都取自明细字段
        	item_xml += "<entry>";
			
			  for(Map<String,Object> detail_map : voucher_item_list){
		        	//属性名称
		        	String xml_node_name = Util.null2String(detail_map.get("xmlfield").toString());
		        	
		        	if("".equals(xml_node_name)){
		        		continue;
		        	}
		        	
		        	//获取属性值
		        	String xml_node_value = getFieldValue(detail_map,rs_main,null,1);
		        	
		        	item_xml += "<" + xml_node_name + ">";
		        	item_xml += xml_node_value;
		        	item_xml += "</" + xml_node_name + ">";
		        }
			
			item_xml += "</entry>";
        }
        
        item_xml += "</voucher_body>";
        
        //发送的XML
        StringBuffer  xml = new StringBuffer();
        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        
        xml.append("<ufinterface ").append(root_attr_xml).append(">");
        xml.append("<voucher id=\"").append(this_requestid).append("\">");
        xml.append(header_xml);
        
        xml.append(item_xml);
        xml.append("</voucher>");
        xml.append("</ufinterface>");
        
        this.writeDebugLog("发送的XML文件内容为:[" + xml.toString() + "]");
        
        
        String returnxml = "";
		
		//获取接口地址
		String request_uri_address = this.getSystemParamValue("NC_HttpRequest_URI");

		if(url_item_list!=null&&url_item_list.size()>0){
            int i=0;
            for(Map<String,Object> url_map : url_item_list){
               String urlParameter = Util.null2String(url_map.get("urlParameter").toString());
               String urlValue = getFieldValue(url_map,rs_main,null,1);
               if(!"".equals(urlParameter)){
                   if(i==0){//第一个参数用?
                       request_uri_address=request_uri_address+"?";
                   }else{//后面的参数用&
                       request_uri_address=request_uri_address+"&";
                   }
                   request_uri_address=request_uri_address+urlParameter+"="+urlValue;
                   i++;
               }
            }
        }
		this.writeDebugLog("发送HTTP请求的地址:[" + request_uri_address + "]");
		
		//发送POST请求至NC系统
		returnxml = httpPostWithXML(xml.toString(),request_uri_address);
		
		this.writeDebugLog("获取请求返回的XML:[" + returnxml + "]");
		
		if(!"-1".equals(returnxml) && !"-2".equals(returnxml)){
			Map<String,String> returnmap = analysisXML(returnxml);
			
			String resultcode = returnmap.get("resultcode").toString();
			
			String resultdescription = returnmap.get("resultdescription").toString();
			
			if(!"1".equals(resultcode)){
				requestInfo.getRequestManager().setMessageid("11111"+ this_requestid +"22222");
	            requestInfo.getRequestManager().setMessagecontent("接口调用失败，失败原因[" + resultdescription  + "]，结果标识码:[" + resultcode + "]！");

	             return Action.FAILURE_AND_CONTINUE;
			}
		}else{
			 requestInfo.getRequestManager().setMessageid("11111"+ this_requestid +"22222");
             requestInfo.getRequestManager().setMessagecontent("接口调用失败，请联系系统管理员！");

             return Action.FAILURE_AND_CONTINUE;
		}
        
    	this.writeLog("-------------------------WorkflowDataNCAction End-------------------------");
        
        return Action.SUCCESS;
	}
	
	/**
	 * HttpClient请求 POST提交
	 * 
	 * @param xml 传输的XML字符串
	 * @param url 请求的地址
	 * @return 返回请求后返回的结果
	 */
	public String httpPostWithXML(String xml, String url) {
		String returnvalue = "";

		HttpPost httpPost = null;
		try {
			HttpClient httpClient = new DefaultHttpClient();

			httpPost = new HttpPost(url);
			// 构造消息头
			httpPost.setHeader("Content-type", "text/html; charset=utf-8");
			
			// 构建消息实体
			StringEntity entity = new StringEntity(xml, Charset.forName("UTF-8"));
			entity.setContentEncoding("UTF-8");
			// 发送Json格式的数据请求
			httpPost.setEntity(entity);
			
			HttpResponse response = httpClient.execute(httpPost);

			// 检验返回码
			int statusCode = response.getStatusLine().getStatusCode();
			this.writeLog("请求的状态码:[" + statusCode + "]");
			if (statusCode == HttpStatus.SC_OK) {
				HttpEntity httpEntity = response.getEntity();
				returnvalue = EntityUtils.toString(httpEntity, "UTF-8");
			}else{
				returnvalue = "-1";
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnvalue = "-2";
			this.writeDebugLog("Http请求异常:[" + e.getMessage() + "/" + e.toString() + "]");
		} finally {
			if (httpPost != null) {
				try {
					httpPost.releaseConnection();
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		return returnvalue;
	}
	
    /**
     * 解析XML字符串
     * @param xml
     * @return
     */
    @SuppressWarnings("unchecked")
    public  Map<String,String> analysisXML(String xml){
        Map<String,String> resultMap = new HashMap<String,String>();
        try{
            //创建一个新的字符串
            StringReader read = new StringReader(xml);
            //创建新的输入源SAX 解析器将使用 InputSource 对象来确定如何读取 XML 输入
            InputSource source = new InputSource(read);
            //创建一个新的SAXBuilder
            SAXBuilder sb = new SAXBuilder();
            //通过输入源构造一个Document
            Document doc = sb.build(source);
            //取根元素
            Element root = doc.getRootElement();
            //获取ufinterface根节点中的子节点
            List<Element> child = root.getChildren();
            //获取ufinterface根节点中第一个子节点 sendresult
            Element first_child = (Element)child.get(0);

            //获取formdata根节点中的子节点
            List<Element> first_child_grands = first_child.getChildren();
            for(int i = 0 ; i < first_child_grands.size();i++){
                Element leave = (Element)first_child_grands.get(i);

                resultMap.put(leave.getName(), leave.getText());
                //System.err.println("--------" + i + "--->" + leave.getText() + "/" + leave.getName());
            }
        }catch (Exception e) {
            // TODO: handle exception
            this.writeLog("解析XML字符串异常:[" + e.getMessage() + "/" + e.toString() + "]");
        }

        return resultMap;
    }
	
	/**
     * 获取字段值
     * @param configMap
     * @param rs
     * @param rs_detail
     * @param rownum 明细序列
     * @param detail_keyid 明细记录所在主键
     * @return
     */
    private String getFieldValue(Map<String,Object> configMap,RecordSet rs,RecordSet rs_detail,int rownum){
        //流程字段名称
        String wffieldname = Util.null2String(configMap.get("fieldname").toString());
        //转换规则
        int changerule = Util.getIntValue(configMap.get("changerule").toString(),0);
        //自定义规则
        String cussql = Util.null2String(configMap.get("cussql").toString());

        //流程字段所属
        int viewtype = Util.getIntValue(configMap.get("viewtype").toString(),0);

        //流程字段值
        String wffieldvalue = "";
        //明细表主键
        int detail_keyid = -1;
        

        if(!"".equals(wffieldname)){
            if(viewtype == 0){//主表
                wffieldvalue = Util.null2String(rs.getString(wffieldname));
            }else{//明细表
                wffieldvalue = Util.null2String(rs_detail.getString(wffieldname));
            }
        }
        if(rs_detail != null){
        	detail_keyid = Util.getIntValue(rs_detail.getString("id"),-1);
        }

        if(changerule == 1){//表示流程REQUESTID
            wffieldvalue = this_requestid;
        }else if(changerule == 2){//表示流程标题
            wffieldvalue = this_requestname;
        }else if(changerule == 3){//表示流程编号
            wffieldvalue = this_requestmark;
        }else if(changerule == 4){//表示系统日期
            wffieldvalue = TimeUtil.getCurrentDateString().toString();
        }else if(changerule == 5){//系统日期时间
            wffieldvalue = TimeUtil.getCurrentTimeString();
        }else if(changerule == 6){//表示固定值
            wffieldvalue = cussql;
        }else if(changerule == 7){//表示明细行号
            wffieldvalue = String.valueOf(rownum);
        }else if(changerule == 8) {//表示自定义转换
            //if(!"".equals(wffieldvalue)){
        	this.writeLog("需要转换规则的SQL:[" + cussql + "],fieldvalue:[" + wffieldvalue + "],requestid:[" + this_requestid + "],detailkeyid:[" + detail_keyid + "]");
            String changevalue = getValueByChangeRule(cussql, wffieldvalue,this_requestid,detail_keyid);
            wffieldvalue = changevalue;
            //}
        }else{//表示不转换
            //wffieldvalue
        }

        //XML特殊字符处理
        wffieldvalue = wffieldvalue.replace("<", "&lt;");
        wffieldvalue = wffieldvalue.replace(">", "&gt;");
        wffieldvalue = wffieldvalue.replace("&", "&amp;");
        wffieldvalue = wffieldvalue.replace("'", "&apos;");
        wffieldvalue = wffieldvalue.replace("\"", "&quot;");

        return wffieldvalue;
    }

}
