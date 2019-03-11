package weaver.lyf.action;

import org.json.JSONArray;
import org.json.JSONObject;
import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestManager;
import weaver.zwl.common.ToolUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by minjie on 2019/3/7.
 */
public class StoreCloseToERPAction extends ToolUtil implements Action {
    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("==========店柜闭幕申请表信息传给ERP================Begin");

        RequestManager requestManager = requestInfo.getRequestManager();
        final String requestid = Util.null2String(requestInfo.getRequestid());// 获取请求ID
        final String workflowid = Util.null2String(requestInfo.getWorkflowid());// 获取流程ID
        final String userid = requestManager.getCreater() + "";// 流程创建人
        final int formid = requestManager.getFormid();


        String app_key = getSystemParamValue("erp_appkey");
        String password = getSystemParamValue("erp_password");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String timeStamp = format.format((new Date()));
        String url=getSystemParamValue("erp_url");

        writeLog("requestid:" + requestid + ",workflowid:" + workflowid + ",userid:" + userid + ",formid:" + formid);

        RecordSet rs = new RecordSet();
        try {
            String store_sql = "select * from formtable_main_" + Math.abs(formid) + "  where requestid = '" + requestid + "'";
            writeLog("闭幕申请表信息查询Sql:" + store_sql);
            rs.executeQuery(store_sql);

            String storeid = "";
            if (rs.next()) {
                storeid = Util.null2String(rs.getString("storeid"));
            }
            String itcode_sql = "select * from uf_dgxxbd where id =" + storeid;
            String itcode = "";

            rs.executeQuery(itcode_sql);
            if(rs.next()){
                itcode = Util.null2String(rs.getString("mddm"));
            }

            List columns = new ArrayList();
            columns.add("ID");

            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("table", "C_STORE");
            params.put("columns", columns);
            HashMap<String, String> conditions = new HashMap<String, String>();
            conditions.put("column", "CODE");
//            conditions.put("condition","%3D"+itcode);
            conditions.put("condition", "%3D"+"123");
            params.put("params", conditions);

            HashMap<String, Object> content = new HashMap<String, Object>();
            content.put("id", "112");
            content.put("command", "Query");
            content.put("params",params);

            UtilJason utilJason = new UtilJason();
            String sip_timestamp = utilJason.buildMD5Sign(app_key, password, timeStamp);
            String executeParams = utilJason.buildExecuteParams(app_key, timeStamp, sip_timestamp, content);

            String returnValue = utilJason.httpPostToERP(executeParams, url);
            writeLog(returnValue);

            JSONArray jsonArray = new JSONArray(returnValue);
            String erp_store_id = "";

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObj = jsonArray.getJSONObject(i);
                String code = jsonObj.getString("code");
                if(!"0".equals(code)){
                    String message=jsonObj.getString("message");
                    requestManager.setMessageid("520" + requestid);
                    requestManager.setMessagecontent(message);
                    return Action.FAILURE_AND_CONTINUE;
                }
                JSONArray a1 = jsonObj.getJSONArray("rows");
                erp_store_id = a1.getJSONArray(0).getString(0);
            }
            if(erp_store_id == ""){
                requestManager.setMessageid("530" + requestid);
                requestManager.setMessagecontent("未获取到ERP店柜id");
                return Action.FAILURE_AND_CONTINUE;
            }
            writeLog("erp_store_id:" + erp_store_id);



        } catch (Exception e1) {
            writeLog("异常:" + e1.getMessage() + ">>>>>" + e1.toString());
            requestInfo.getRequestManager().setMessageid("512" + requestid);
            requestInfo.getRequestManager().setMessagecontent("系统异常,请联系管理员,表单信息查询异常");
            return Action.FAILURE_AND_CONTINUE;
        }


        writeLog("==========店柜闭幕申请表信息传给ERP================End");
        return Action.SUCCESS;
    }
}
