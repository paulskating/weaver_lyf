package weaver.lyf.action;

import weaver.conn.RecordSet;
import weaver.general.TimeUtil;
import weaver.general.Util;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestManager;
import weaver.zwl.common.ToolUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class detailAddMxAction extends ToolUtil implements Action {
    @Override
    public String execute(RequestInfo request) {

        writeLog("==========明细表生成Action================Beigin");

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

        try {

            //获取主表数据
            String querySql = "select * from formtable_main_" + Math.abs(formid) + "  where requestid = '" + requestid + "'";
            writeLog("查询流程信息sql:" + querySql);
            rs.executeQuery(querySql);

            String mainid = "";//流程主id
            while (rs.next()) {
                mainid = Util.null2String(rs.getString("id"));
            }
            writeLog("mainid:" + mainid);

            //根据workflowid配置信息往明细表中写入数据
            List<Map<String, Object>> main_field_list = new ArrayList<Map<String,Object>>();

            main_field_list.clear();

            int config_main_key=0;

            String detailTable="";

            String select_main_config_sql = "select * from uf_detailadd_config where  workflowid in (select id from workflow_base where activeVersionID in (select activeVersionID  from workflow_base where id = ?) or id = ?)";

            if(rs.executeQuery(select_main_config_sql,workflowid,workflowid)){
                if(rs.next()){
                    config_main_key = Util.getIntValue(rs.getString("id"),0);

                    detailTable = Util.null2String(rs.getString("detailTable"));
                }
                writeLog("config_main_key:"+config_main_key+",detailTable:"+detailTable);

                if(config_main_key>0){
                    //获取当前流程明细字段配置信息
                    String select_detail_config_sql = "select dt.*,wb.fieldname,wb.viewtype,wb.detailtable from uf_detailadd_config_dt1 dt left join workflow_billfield wb on dt.fieldid = wb.id where dt.mainid = ?";

                    if(rs.executeQuery(select_detail_config_sql,config_main_key)){
                        while(rs.next()) {
                            //OA流程字段
                            String fieldname = Util.null2String(rs.getString("fieldname"));
                            //OA流程字段所属
                            int viewtype = Util.getIntValue(rs.getString("viewtype"), 0);
                            //转换规则
                            int changerule = Util.getIntValue(rs.getString("changerule"), 0);
                            //自定义转换规则
                            String cussql = Util.null2String(rs.getString("cussql"));

                            if (!"".equals(fieldname)) {
                                Map<String, Object> detail_map = new HashMap<String, Object>();
                                detail_map.put("fieldname", fieldname);
                                detail_map.put("viewtype", viewtype);
                                detail_map.put("changerule", changerule);
                                detail_map.put("cussql", cussql);
                                main_field_list.add(detail_map);
                            }
                        }
                    }
                    if(main_field_list!=null&&main_field_list.size()>0){
                        String field="mainid,";
                        String fieldV="'"+mainid+"',";
                        for(Map<String,Object> field_map : main_field_list){
                            String fieldname = Util.null2String(field_map.get("fieldname").toString());
                            String fieldValue = getFieldValue(field_map,requestid,mainid,detailTable);
                            field=field+fieldname+",";
                            fieldV=fieldV+"'"+fieldValue+"',";
                        }
                        if(field.endsWith(",")){
                            field=field.substring(0,field.length()-1);
                            fieldV=fieldV.substring(0,fieldV.length()-1);
                        }

                        String  insertSql="insert into "+detailTable+"("+field+") values ("+fieldV+")";
                        RecordSet rsInsert=new RecordSet();
                        if(rsInsert.executeUpdate(insertSql)){
                            writeLog("明细表添加成功");
                        };
                    }
                }
            }
        }catch (Exception e) {
            writeLog("异常:" + e.getMessage() + ">>>>>" + e.toString());
            request.getRequestManager().setMessageid("512" + requestid);
            request.getRequestManager().setMessagecontent("");
            return Action.FAILURE_AND_CONTINUE;
        }
        return Action.SUCCESS;
    }

    private String getFieldValue(Map<String,Object> configMap,String requestid,String mainid,String detailTable){
        //流程字段名称
        String wffieldname = Util.null2String(configMap.get("fieldname").toString());
        //转换规则
        int changerule = Util.getIntValue(configMap.get("changerule").toString(),0);
        //自定义规则
        String cussql = Util.null2String(configMap.get("cussql").toString());

        String wffieldvalue = "";

        if(changerule == 0){//表示固定值
            wffieldvalue = cussql;
        }else if(changerule == 1){//当前日期
            wffieldvalue = TimeUtil.getCurrentDateString();
        }else if(changerule == 2){//求和
            String querySql="select sum(?) from "+detailTable+" where mainid='"+mainid+"'";
            wffieldvalue = getValueByChangeRule_SingleParam(querySql,wffieldname);
        }else if(changerule == 3){//自定义转换
            wffieldvalue = getValueByChangeRule(cussql,"",requestid);
        }
        return wffieldvalue;
    }

    private String getReqString(String arg) {
        String infoString = "";
        Pattern pattern = Pattern.compile("[\\s\\\\/:\\*\\?\\\"<>\\|]");
        Matcher matcher = pattern.matcher(arg);
        infoString = matcher.replaceAll("");
        return infoString;
    }
}
