package weaver.lyf.schedual;

import weaver.conn.RecordSet;
import weaver.general.TimeUtil;
import weaver.general.Util;
import weaver.interfaces.schedule.BaseCronJob;
import weaver.soa.workflow.request.RequestService;
import weaver.zwl.common.ToolUtil;

/**
 *按定时任务配置频率对流程进行删除操作
 *
 * @author ywg
 */
public class deleteConfigFlow extends BaseCronJob {

    ToolUtil toolUtil=new ToolUtil();

    public void execute() {

        String nowdate=TimeUtil.getCurrentDateString();

        toolUtil.writeNewLog("deleteConfigFlow","=====定时修改任务和计划任务====="+nowdate);

        String wfid = toolUtil.getSystemParamValue("deletewfid");

        RecordSet rs=new RecordSet();

        //查询配置的workflowid是否在首节点和是否是退回的流程

        String querySql="select requestid from workflow_Requestbase where workflowid in("+wfid+") and currentnodetype=0";

        toolUtil.writeNewLog("deleteConfigFlow","=====查询sql====="+querySql);

        rs.executeQuery(querySql);

        while(rs.next()){
            String requestid = Util.null2String(rs.getString("requestid"));
            //根据requestid删除此流程
            RequestService rqs = new RequestService();
            Boolean deleteRequest =rqs.deleteRequest(Integer.valueOf(requestid));
            if(deleteRequest){
                toolUtil.writeNewLog("deleteConfigFlow","删除"+requestid+"成功");
            }else{
                toolUtil.writeNewLog("deleteConfigFlow","删除"+requestid+"失败");
            }
        }
    }
}
