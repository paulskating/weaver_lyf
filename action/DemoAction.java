package weaver.lyf.action;

import weaver.conn.RecordSet;
import weaver.general.StaticObj;
import weaver.general.TimeUtil;
import weaver.general.Util;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;
import weaver.zwl.common.ToolUtil;
import weaver.interfaces.datasource.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DemoAction extends ToolUtil implements Action {
    @Override
    public String execute(RequestInfo requestInfo) {
//        writeLog("DemoAction=========================Begin");
//        //获取ERP数据源配置
//        DataSource ds = (DataSource)StaticObj.getServiceByFullname("datasource.ERP",DataSource.class);
//        Connection conn = ds.getConnection();
//        PreparedStatement pstm=null;
//        ResultSet rs = null;
//
//
//        //查询晚于上一次更新时间的数据
//        String last_update_time = getSystemParamValue("last_update_time");
////        String last_update_time = "2019-02-22 16:54:00";
//        String sql = "select id,name,modifieddate from C_PROVINCE where modifieddate>to_date('"+last_update_time+"','yyyy-mm-dd hh24:mi:ss')";
//        writeLog("sql:"+sql);
//
//        RecordSet rs_weaver = new RecordSet();
//        RecordSet rs_update = new RecordSet();
//
//        try {
//            pstm = conn.prepareStatement(sql);
//            rs = pstm.executeQuery();
//
//            while (rs.next()){
//                String tempId=Util.null2String(rs.getString("id"));
//                String tempName = Util.null2String(rs.getString("name"));
//                String tempDate = Util.null2String(rs.getString("modifieddate")).substring(0,10);
//                String weaver_select = "select * from uf_provinces where code='" + tempId +"'";
//                rs_weaver.executeQuery(weaver_select);
//                if(rs_weaver.next()){
//                    String weaver_update = "update uf_provinces set name='"+tempName+"',modifyDate='"+tempDate+"' where code='"+tempId+"'";
//                    rs_update.executeQuery(weaver_update);
//                }else {
//                    String weaver_insert = "insert into uf_provinces (code,name,modifyDate) values ('"+tempId+"','"+tempName+"','"+tempDate+"')";
//                    rs_update.executeQuery(weaver_insert);
//                }
//            }
//
//            //更新系统设置字段
//            String update_systemconfig = "update uf_systemconfig set paramValue = '"+TimeUtil.getCurrentTimeString()+"' where uuid='last_update_time'";
//            rs_weaver.executeQuery(update_systemconfig);
//        }catch (Exception e1) {
//            e1.printStackTrace();
//            writeLog("ERP数据库查询异常===================");
//            return Action.FAILURE_AND_CONTINUE;
//        } finally {
//            try {
//                rs.close();
//                pstm.close();
//                conn.close();
//            }catch (Exception e2){
//                writeLog("EPR数据库连接关闭异常===================");
//                return Action.FAILURE_AND_CONTINUE;
//            }
//
//        }
//
        return Action.SUCCESS;
    }


}
