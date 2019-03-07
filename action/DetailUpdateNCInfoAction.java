package weaver.lyf.action;

import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestManager;
import weaver.zwl.common.ToolUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DetailUpdateNCInfoAction extends ToolUtil implements Action{

    @Override
    public String execute(RequestInfo request) {
        writeLog("更新传NC数据=========================Begin");

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

            double amountTotalWithTaxCNY = 0;//人民币总含税金额
            double totalTaxCNY = 0;//人民币总增值税额
            double amountTotalWithTaxUSD = 0;//美元总含税金额
            double totalTaxUSD = 0;//美元总增值税额

            //查询明细一中的数据
            String dt1 = "select * from formtable_main_" + Math.abs(formid) + "_dt1 where mainid=" + mainid;
            rs.executeQuery(dt1);
            while (rs.next()){
                int currencyId = Util.getIntValue(rs.getString("currency"));//获取币种
                int fplx = Util.getIntValue(rs.getString("fplx"));//获取发票类型
                if(currencyId==0){
                    amountTotalWithTaxCNY = amountTotalWithTaxCNY + Util.getDoubleValue(rs.getString("hsje"));
                    if(fplx==0){
                        totalTaxCNY = totalTaxCNY + Util.getDoubleValue(rs.getString("se"));
                    }
                }else{
                    amountTotalWithTaxUSD = amountTotalWithTaxUSD + Util.getDoubleValue(rs.getString("hsje"));
                    if(fplx==0){
                        totalTaxUSD = totalTaxUSD + Util.getDoubleValue(rs.getString("se"));
                    }
                }

            }

            //判断含税金额是否为0
            if(amountTotalWithTaxCNY <= 0 && amountTotalWithTaxUSD <=0){
                request.getRequestManager().setMessageid("555555" + requestid);
                request.getRequestManager().setMessagecontent("接口调用失败，失败原因[ 费用明细金额有误 ]！");
                return Action.FAILURE_AND_CONTINUE;
            }

            writeLog("amountTotalWithTaxCNY:" + amountTotalWithTaxCNY + ", totalTaxCNY:" + totalTaxCNY);
            writeLog("amountTotalWithTaxUSD:" + amountTotalWithTaxUSD + ", totalTaxUSD:" + totalTaxUSD);
            double toNCSumCNY = amountTotalWithTaxCNY - totalTaxCNY;
            double tempCNY = 0;
            double toNCSumUSD = amountTotalWithTaxUSD - totalTaxUSD;
            double tempUSD = 0;


            //查询明细二中的数据
            String dt2_CNY = "select * from formtable_main_" + Math.abs(formid) + "_dt2 where currency=0 and mainid= " + mainid;
            rs.executeQuery(dt2_CNY);

            RecordSet rsUpdate = new RecordSet();

//            更新人民币明细
            while(rs.next()){
                int detailId = Util.getIntValue(rs.getString("id"));
                double cdje = Util.getDoubleValue(rs.getString("cdje"));
                double tonc = 0;
                if(rs.next()){
                    rs.previous();
                    tonc = (double)Math.round((cdje/amountTotalWithTaxCNY) * toNCSumCNY * 100)/100;
                }else {
                    tonc = toNCSumCNY - tempCNY;
                }
                tempCNY = tempCNY + tonc;

                String update_tonc = "update formtable_main_" + Math.abs(formid) + "_dt2 set tonc =" + tonc + ",local_tonc = " + tonc + " where id = " + detailId;
                boolean issuccess = rsUpdate.executeUpdate(update_tonc);
                if(!issuccess){
                    request.getRequestManager().setMessageid("555555" + requestid);
                    request.getRequestManager().setMessagecontent("更新明细2中,传NC原币金额字段错误");
                    return Action.FAILURE_AND_CONTINUE;
                }
            }

            String dt2_USD = "select * from formtable_main_" + Math.abs(formid) + "_dt2 where currency=1 and mainid= " + mainid;
            rs.executeQuery(dt2_USD);

//            更新美元明细
            while(rs.next()){
                int detailId = Util.getIntValue(rs.getString("id"));
                double cdje = Util.getDoubleValue(rs.getString("cdje"));
                double currrate = Util.getDoubleValue(rs.getString("currrate"));
                double tonc = 0;
                if(rs.next()){
                    rs.previous();
                    tonc = (double)Math.round((cdje/amountTotalWithTaxUSD) * toNCSumUSD * 100)/100;
                }else {
                    tonc = toNCSumUSD - tempUSD;
                }
                double local_tonc = (double)Math.round(tonc * currrate * 100)/100;
                tempUSD = tempUSD + tonc;

                String update_tonc = "update formtable_main_" + Math.abs(formid) + "_dt2 set tonc =" + tonc + ",local_tonc = "+ local_tonc +" where id = " + detailId;
                boolean issuccess = rsUpdate.executeUpdate(update_tonc);
                if(!issuccess){
                    request.getRequestManager().setMessageid("555555" + requestid);
                    request.getRequestManager().setMessagecontent("更新明细2中,传NC原币金额字段错误");
                    return Action.FAILURE_AND_CONTINUE;
                }
            }

            writeLog("更新传NC数据=========================End" );

        }catch (Exception e){
            writeLog("异常:" + e.getMessage() + ">>>>>" + e.toString());
            request.getRequestManager().setMessageid("555555" + requestid);
            request.getRequestManager().setMessagecontent("");
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


}
