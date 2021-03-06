package com.redmoon.oa.officeequip.validator;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormValidator;
import com.redmoon.oa.basic.TreeSelectDb;
import com.redmoon.oa.flow.FormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.officeequip.OfficeStocktakingDb;

public class OfficePdFormValidator implements IFormValidator {

	@Override
	public String getExtraData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isUsed() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void onWorkflowFinished(WorkflowDb wf, WorkflowActionDb lastAction)
			throws ErrMsgException {
		FormDb fd = new FormDb();
		// 新建一个Leaf对象
		Leaf lf = new Leaf();
		lf = lf.getLeaf(wf.getTypeCode());
		fd = fd.getFormDb(lf.getFormCode());
		// 新建一个FormD对象
		FormDAO fdao = new FormDAO(wf.getId(), fd);
		fdao.load();
		String officeName = fdao.getFieldValue("officeName");

		String storageCount = fdao.getFieldValue("realCount");

		if (wf.getStatus() == WorkflowDb.STATUS_FINISHED) {

			// 增加盘点表记录
			OfficeStocktakingDb officeStocktakingDb = new OfficeStocktakingDb();
			officeStocktakingDb.setRealNum(Integer.parseInt(storageCount));
			officeStocktakingDb.setEquipmentCode(officeName);
			officeStocktakingDb.setFlowid(wf.getId());

			officeStocktakingDb.updateStock();
			officeStocktakingDb.insertStock();
		}
	}

	@Override
	public void setExtraData(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIsUsed(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean validate(HttpServletRequest request, FileUpload fu,
			int flowId, Vector fields) throws ErrMsgException {
		int actionId = StrUtil.toInt(fu.getFieldValue("actionId"));
		WorkflowActionDb wa = new WorkflowActionDb();
		wa = wa.getStartAction(flowId);
		if (wa.getId() == actionId) {
			String code = fu.getFieldValue("officeName");
			TreeSelectDb tsd = new TreeSelectDb(code);
			if (tsd == null || !tsd.isLoaded() || tsd.getChildCount() > 0) {
				throw new ErrMsgException("请选择办公用品！");
			}
			if(tsd.getChildCount() > 0)
			{
				throw new ErrMsgException("请选择子节点的办公用品！");
			}
			OfficeStocktakingDb officeStocktakingDb = new OfficeStocktakingDb();
			if (officeStocktakingDb.hasEquipCode(code) == -1) {
				throw new ErrMsgException("该办公用品不存在，请先入库！");
			}
		}
		return true;
	}

	@Override
	public void onActionFinished(HttpServletRequest arg0, int arg1,
			FileUpload arg2) {
		// TODO Auto-generated method stub

	}
}
