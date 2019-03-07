package weaver.lyf.schedual;


import weaver.conn.RecordSet;
import weaver.general.StaticObj;
import weaver.general.TimeUtil;
import weaver.general.Util;
import weaver.interfaces.datasource.DataSource;
import weaver.interfaces.schedule.BaseCronJob;
import weaver.zwl.common.ToolUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


/**
 *按定时任务,从伯俊ERP同步省,市数据资料
 *(按伯俊表里的最后更新日期判断是否需要更新)
 * @author Jason_min
 */
public class SynErpData extends BaseCronJob {

    ToolUtil toolUtil=new ToolUtil();
    public void execute(){
        toolUtil.writeLog("SynERPData=========================Begin");
        updateProvinces();
        updateCities();

        //更新系统设置字段
        RecordSet rs_sysconfig = new RecordSet();
        String update_systemconfig = "update uf_systemconfig set paramValue = '"+ TimeUtil.getCurrentTimeString()+"' where uuid='last_update_time'";
        if(rs_sysconfig.executeUpdate(update_systemconfig)){
            toolUtil.writeLog("上次更新时间刷新");
        }
        toolUtil.writeLog("SynERPData=========================End");
    }


    //更新省
    public void updateProvinces(){
        toolUtil.writeLog("UpdateProvinces=========================Begin");

        //获取ERP数据源配置
        DataSource ds = (DataSource) StaticObj.getServiceByFullname("datasource.ERP",DataSource.class);
        Connection conn = ds.getConnection();
        PreparedStatement pstm=null;
        ResultSet rs = null;

        //查询晚于上一次更新时间的数据
        String last_update_time = toolUtil.getSystemParamValue("last_update_time");
        String sql = "select id,name,modifieddate,isactive from C_PROVINCE where modifieddate>to_date('"+last_update_time+"','yyyy-mm-dd hh24:mi:ss')";
        toolUtil.writeLog("sql:"+sql);

        RecordSet rs_weaver = new RecordSet();
        RecordSet rs_update = new RecordSet();


        try {
            pstm = conn.prepareStatement(sql);
            rs = pstm.executeQuery();

            while (rs.next()){
                String tempId= Util.null2String(rs.getString("id"));
                String tempName = Util.null2String(rs.getString("name"));
                String tempDate = Util.null2String(rs.getString("modifieddate")).substring(0,10);
                String tempIsActive = Util.null2String(rs.getString("isactive"));
                String weaver_select = "select * from uf_provinces where code='" + tempId +"'";
                rs_weaver.executeQuery(weaver_select);
                if(rs_weaver.next()){
                    String weaver_update = "update uf_provinces set name='"+tempName+"',modifyDate='"+tempDate+"',isactive='"+tempIsActive+"' where code='"+tempId+"'";
                    rs_update.executeUpdate(weaver_update);
                }else {
                    String weaver_insert = "insert into uf_provinces (code,name,modifyDate,isactive) values ('"+tempId+"','"+tempName+"','"+tempDate+"','"+tempIsActive+"')";
                    rs_update.executeUpdate(weaver_insert);
                }
            }


        }catch (Exception e1){
            e1.printStackTrace();
            toolUtil.writeLog("更新Provinces数据异常===================");
        }finally {
            try {
                if(rs!=null){
                    try {
                        rs.close();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                if(pstm!=null){
                    try{
                        pstm.close();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                if(conn!=null){
                    try{
                        conn.close();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }catch (Exception e2){
                toolUtil.writeLog("关闭连接异常===================");
            }

        }

        toolUtil.writeLog("UpdateProvinces=========================End");
    }


    public void updateCities(){
        toolUtil.writeLog("UpdateCities=========================Begin");
        //获取ERP数据源配置
        DataSource ds = (DataSource) StaticObj.getServiceByFullname("datasource.ERP",DataSource.class);
        Connection conn = ds.getConnection();
        PreparedStatement pstm=null;
        ResultSet rs = null;

        //查询晚于上一次更新时间的数据
        String last_update_time = toolUtil.getSystemParamValue("last_update_time");
        String sql = "select id,name,c_province_id,modifieddate,isactive from C_CITY where modifieddate>to_date('"+last_update_time+"','yyyy-mm-dd hh24:mi:ss')";
        toolUtil.writeLog("sql:"+sql);

        RecordSet rs_weaver = new RecordSet();
        RecordSet rs_update = new RecordSet();

        try {
            pstm = conn.prepareStatement(sql);
            rs = pstm.executeQuery();

            while (rs.next()){
                String tempId= Util.null2String(rs.getString("id"));
                String tempName = Util.null2String(rs.getString("name"));
                String tempDate = Util.null2String(rs.getString("modifieddate")).substring(0,10);
                String tempProvinceCode = Util.null2String(rs.getString("c_province_id"));
                String tempIsActive = Util.null2String(rs.getString("isactive"));
                String weaver_select = "select * from uf_cities where code='" + tempId +"'";
                rs_weaver.executeQuery(weaver_select);
                if(rs_weaver.next()){
                    String weaver_update = "update uf_cities set name='"+tempName+"',modifyDate='"+tempDate+"',isactive='"+tempIsActive+"',provinceCode='"+tempProvinceCode+"' where code='"+tempId+"'";
                    rs_update.executeUpdate(weaver_update);
                }else {
                    String weaver_insert = "insert into uf_cities (code,name,modifyDate,isactive,provinceCode) values ('"+tempId+"','"+tempName+"','"+tempDate+"','"+tempIsActive+"','"+tempProvinceCode+"')";
                    rs_update.executeUpdate(weaver_insert);
                }
            }

        }catch (Exception e1){
            e1.printStackTrace();
            toolUtil.writeLog("更新Cities数据异常===================");
        }finally {
            try {
                rs.close();
                pstm.close();
                conn.close();
            }catch (Exception e2){
                toolUtil.writeLog("关闭连接异常===================");
            }
        }


        toolUtil.writeLog("UpdateCities=========================End");
    }

}
