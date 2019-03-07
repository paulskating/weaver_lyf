package weaver.lyf.action;

import com.weaver.general.Util;
import weaver.conn.RecordSet;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;
import weaver.zwl.common.ToolUtil;

import java.util.Map;

/**
 * 丽婴房
 * 员工报销Action
 * @author bleach
 *
 */
public class Employee_Reimburse_Action extends ToolUtil implements Action {
	//是否退回
	private String ISReject = "";
	
	/**
	 * 实现父类方法
	 */
	@Override
	public String execute(RequestInfo request) {
		// TODO Auto-generated method stub
		this.writeLog("----------------------------进入 Employee_Reimburse_Action -------------------------------");
		//获取当前流程workflowid
		String workflowid = request.getWorkflowid();
		//获取当前流程requestid
		String requestid = request.getRequestid();
		//获取当前流程formid
		int formid = request.getRequestManager().getFormid();
		
		this.writeLog("workflowid:[" + workflowid + "],requestid:[" + requestid + "],formid:[" + formid + "]");
		
		RecordSet rs = new RecordSet();
		
		//获取当前流程的字段配置集合
		Map<String,String> param_map = this.getSystemParamValueMap("Reimburse_" + workflowid + "_");
		
		//获取是否充预支的字段名称
		String isadvance_fieldname = "";
		
		if(param_map.containsKey("Reimburse_" + workflowid + "_IsAdvance")){
			isadvance_fieldname = param_map.get("Reimburse_" + workflowid + "_IsAdvance");
		}
		
		//获取相关预支单的字段名称
		String advancebill_fieldname = "";
		if(param_map.containsKey("Reimburse_" + workflowid + "_AdvanceBill")){
			advancebill_fieldname = param_map.get("Reimburse_" + workflowid + "_AdvanceBill");
		}
		
		//获取本次冲销金额的字段名称
		String writeoff_fieldname = "";
		if(param_map.containsKey("Reimburse_" + workflowid + "_WriteOffAmount")){
			writeoff_fieldname = param_map.get("Reimburse_" + workflowid + "_WriteOffAmount");
		}
		
		//获取报销金额合计的字段名称
		String reimburse_fieldname = "";
		if(param_map.containsKey("Reimburse_" + workflowid + "_ReimburseAmount")){
			reimburse_fieldname = param_map.get("Reimburse_" + workflowid + "_ReimburseAmount");
		}
		
		this.writeLog("isadvance_fieldname:[" + isadvance_fieldname + "],advancebill_fieldname:[" + advancebill_fieldname + "],writeoff_fieldname:[" + writeoff_fieldname + "],reimburse_fieldname:[" + reimburse_fieldname + "]");
		
		//是否预支
		int isadvance_value = -1;
		//冲销金额
		String writeoff_value = "0.0";
		//报销金额
		double reimburse_value = 0.0;
		//预支单ID
		int advancebill_value = -1;
		
		//获取流程数据
		String select_sql = "select * from formtable_main_" + Math.abs(formid) + " where requestid = ?";
		
		if(rs.executeQuery(select_sql, requestid)){
			if(rs.next()){
				//获取是否充预支
				isadvance_value = Util.getIntValue(rs.getString(isadvance_fieldname),-1);
				
				writeoff_value = Util.null2String(rs.getString(writeoff_fieldname));
				
				advancebill_value = Util.getIntValue(rs.getString(advancebill_fieldname),-1);
				
				reimburse_value = Util.getDoubleValue(rs.getString(reimburse_fieldname),0.0);
			}
		}
		
		boolean issuccess = true;
		
		if(isadvance_value == 0 && advancebill_value > 0){//表示为预支
			
			if(!"1".equals(this.ISReject)){
				if(Util.getDoubleValue(writeoff_value,0.0) > reimburse_value){
					request.getRequestManager().setMessageid("11111" + requestid + "22222");
					request.getRequestManager().setMessagecontent("本次冲销金额大于报销金额合计，请重新填写！");
					
					return Action.FAILURE_AND_CONTINUE;
				}
				
				//再次判断本次冲销金额与剩余金额进行比较
				String select_exsit = "select 1 from uf_gryz where id = " + advancebill_value + " and syje > " + writeoff_value;
				if(rs.equals(select_exsit)){
					if(rs.next()){
						request.getRequestManager().setMessageid("11111" + requestid + "22222");
						request.getRequestManager().setMessagecontent("本次冲销金额大于预支剩余金额，请重新填写！");
						
						return Action.FAILURE_AND_CONTINUE;
					}
				}
				
				//修改剩余金额
				issuccess = rs.executeUpdate("update uf_gryz set syje = isnull(syje,0.0) - " + writeoff_value + ",cxje = isnull(cxje,0.0) + " + writeoff_value + " where id = ?", advancebill_value);
			}else{//表示当前操作为退回
				//修改剩余金额
				issuccess = rs.executeUpdate("update uf_gryz set syje = isnull(syje,0.0) + " + writeoff_value + ",cxje = isnull(cxje,0.0) - " + writeoff_value + "  where id = ?", advancebill_value);
			}
		}
		
		if(!issuccess){
			request.getRequestManager().setMessageid("11111" + requestid + "22222");
			request.getRequestManager().setMessagecontent("预支剩余金额更新失败，请联系系统管理员！");
			
			return Action.FAILURE_AND_CONTINUE;
		}
		
		this.writeLog("----------------------------退出 Employee_Reimburse_Action -------------------------------");
	
		return Action.SUCCESS;
	}

	/**
	 * @return the iSReject
	 */
	public String getISReject() {
		return ISReject;
	}

	/**
	 * @param iSReject the iSReject to set
	 */
	public void setISReject(String iSReject) {
		ISReject = iSReject;
	}
}
