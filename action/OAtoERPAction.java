package weaver.lyf.action;

import org.json.JSONArray;
import org.json.JSONObject;
import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestManager;
import weaver.zwl.common.ToolUtil;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OAtoERPAction extends ToolUtil implements Action {

    private String IS_REJECT;

    private String doctype;

    private String PRODUCTAPPLY_ID;

    @Override
    public String execute(RequestInfo request) {

        writeLog("==========OA与伯俊ERP系统接口对接================Beigin");

        RequestManager requestManager = request.getRequestManager();

        final String requestid = Util.null2String(request.getRequestid());// 获取请求ID

        final String workflowid = Util.null2String(request.getWorkflowid());// 获取流程ID

        int formid = requestManager.getFormid();

        final String requestname = getReqString(Util.null2String(requestManager
            .getRequestname())); // 获取请求名称

        final String userid = requestManager.getCreater() + "";// 流程创建人

        writeLog("requestid:" + requestid + ",workflowid:" + workflowid + ",requestname:"
            + requestname + ",userid:" + userid + ",formid:" + formid);

        RecordSet rs = new RecordSet();

        try{
            //获取主表数据
            String querySql = "select * from formtable_main_" + Math.abs(formid) + "  where requestid = '" + requestid + "'";
            writeLog("查询流程信息sql:" + querySql);
            rs.executeQuery(querySql);

            String mainid = "";//流程主id

            String doctype="";

            String num="";

            while (rs.next()) {
                mainid = Util.null2String(rs.getString("id"));
                doctype = Util.null2String(rs.getString(this.doctype));
                num = Util.null2String(rs.getString(this.PRODUCTAPPLY_ID));
            }

            writeLog("mainid:" + mainid);

            String tableid=getSystemParamValue("erp_tableid");

            String isReject=this.IS_REJECT;

            String remark= requestManager.getRemark();

            remark=ReplaceRemark(remark);

            writeLog("tableid:"+tableid+",doctype:"+doctype+",PRODUCTAPPLY_ID:"+num+",isReject:"+isReject+",remark:"+remark);

            HashMap<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("table", tableid);
            hashMap.put("DOCTYPE", doctype);
            hashMap.put("M_PRODUCTAPPLY_ID", num);
            hashMap.put("IS_REJECT", this.IS_REJECT);
            hashMap.put("OAREMARK", remark);

            HashMap<String, Object> hashObj = new HashMap<String, Object>();
            hashObj.put("command", "ObjectCreate");
            hashObj.put("id", "112");
            hashObj.put("params", hashMap);
            JSONArray sendjsonArray = new JSONArray();
            sendjsonArray.put(hashObj);
            //String content = "[{'command':'ObjectCreate','params':{'table':"+tableid+",'DOCTYPE':'"+doctype+"','M_PRODUCTAPPLY_ID__NAME':'"+num+"','IS_REJECT':'"+isReject+"','OAREMARK':'"+remark+"'}}]";
            httpUtilForLyf util=new httpUtilForLyf();
            String returnValue=util.httpPostForbj(sendjsonArray.toString());
            writeLog("returnValue:"+returnValue);
            JSONArray jsonArray = new JSONArray(returnValue);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObj = jsonArray.getJSONObject(i);
                String code = jsonObj.getString("code");
                if(!"0".equals(code)){
                    String message=jsonObj.getString("message");
                    request.getRequestManager().setMessageid("520" + requestid);
                    request.getRequestManager().setMessagecontent(message);
                    return Action.FAILURE_AND_CONTINUE;
                }
            }
        }catch (Exception e) {
            writeLog("异常:" + e.getMessage() + ">>>>>" + e.toString());
            request.getRequestManager().setMessageid("512" + requestid);
            request.getRequestManager().setMessagecontent("系统异常,请联系管理员");
            return Action.FAILURE_AND_CONTINUE;
        }
        return Action.SUCCESS;
    }

    private String getReqString(String arg) {
        String infoString = "";
        Pattern pattern = Pattern.compile("[\\s\\\\/:\\*\\?\\\"<>\\|]");
        Matcher matcher = pattern.matcher(arg);
        infoString = matcher.replaceAll("");
        return infoString;
    }

    // 截取签字意见删除html标签 只保留节点操作者输入的意见
    private String ReplaceRemark(String remark) {
        // 定义正则表达式 匹配html标签 和 空格 并置空
        Pattern p = Pattern.compile("<.+?>");
        Pattern p2 = Pattern.compile("&nbsp;");
        Matcher m = p.matcher(remark); // 获得匹配器对象
        String mindStr = m.replaceAll("");
        Matcher m2 = p2.matcher(mindStr);
        String result = m2.replaceAll("");
        result=ReplaceKeyWords(result);
        return result;
    }

    // 屏蔽掉相关词汇信息 关联流程，关联文档，上传图片，上传附件
    private String ReplaceKeyWords(String str) {
        String result = "";
        Pattern pattern = null;
        Matcher matcher = null;
        pattern = Pattern.compile("关联流程");
        matcher = pattern.matcher(str);
        result = matcher.replaceAll("");
        pattern = Pattern.compile("关联文档");
        matcher = pattern.matcher(result);
        result = matcher.replaceAll("");
        pattern = Pattern.compile("上传图片");
        matcher = pattern.matcher(result);
        result = matcher.replaceAll("");
        pattern = Pattern.compile("上传附件");
        matcher = pattern.matcher(result);
        result = matcher.replaceAll("");
        pattern = Pattern.compile("来自iPhone客户端");
        matcher = pattern.matcher(result);
        result = matcher.replaceAll("");
        pattern = Pattern.compile("来自android客户端");
        matcher = pattern.matcher(result);
        result = matcher.replaceAll("");
        pattern = Pattern.compile("来自ipad客户端");
        matcher = pattern.matcher(result);
        result = matcher.replaceAll("");
        pattern = Pattern.compile("此节点已审批通过");
        matcher = pattern.matcher(result);
        result = matcher.replaceAll("");
        pattern = Pattern.compile("来自web客户端");
        matcher = pattern.matcher(result);
        result = matcher.replaceAll("");
        pattern = Pattern.compile("来自androidpad客户端");
        matcher = pattern.matcher(result);
        result = matcher.replaceAll("");
        return result;
    }

    public String getIS_REJECT() {
        return IS_REJECT;
    }

    public void setIS_REJECT(String IS_REJECT) {
        this.IS_REJECT = IS_REJECT;
    }

    public String getDoctype() {
        return doctype;
    }

    public void setDoctype(String doctype) {
        this.doctype = doctype;
    }

    public String getPRODUCTAPPLY_ID() {
        return PRODUCTAPPLY_ID;
    }

    public void setPRODUCTAPPLY_ID(String PRODUCTAPPLY_ID) {
        this.PRODUCTAPPLY_ID = PRODUCTAPPLY_ID;
    }
}
