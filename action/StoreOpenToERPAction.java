package weaver.lyf.action;

import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import weaver.workflow.request.RequestManager;
import weaver.zwl.common.ToolUtil;

public class StoreOpenToERPAction extends ToolUtil implements Action{
    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("==========店柜开幕申请表信息传给ERP================Beigin");

        RequestManager requestManager = requestInfo.getRequestManager();
        final String requestid = Util.null2String(requestInfo.getRequestid());// 获取请求ID
        final String workflowid = Util.null2String(requestInfo.getWorkflowid());// 获取流程ID
        final String userid = requestManager.getCreater() + "";// 流程创建人
        final int formid = requestManager.getFormid();

        writeLog("requestid:" + requestid + ",workflowid:" + workflowid + ",userid:" + userid + ",formid:" + formid);

        RecordSet rs = new RecordSet();
        try {
            String store_sql = "select * from formtable_main_" + Math.abs(formid) + "  where requestid = '" + requestid + "'";
            writeLog("开店申请表信息查询Sql:" + store_sql);
            rs.executeQuery(store_sql);

            String itcode = "";
            if(rs.next()){
                itcode = Util.null2String(rs.getString("itcode"));
            }


        }catch (Exception e1){
            writeLog("异常:" + e1.getMessage() + ">>>>>" + e1.toString());
            requestInfo.getRequestManager().setMessageid("512" + requestid);
            requestInfo.getRequestManager().setMessagecontent("系统异常,请联系管理员,表单信息查询异常");
            return Action.FAILURE_AND_CONTINUE;
        }










        writeLog("==========店柜开幕申请表信息传给ERP================Beigin");
        return Action.SUCCESS;
    }
}
