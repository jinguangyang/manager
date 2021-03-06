package com.manage.project.system.supply.controller;

import com.manage.common.utils.CacheUtils;
import com.manage.common.utils.poi.ExcelUtil;
import com.manage.common.utils.security.ShiroUtils;
import com.manage.framework.aspectj.lang.annotation.Log;
import com.manage.framework.aspectj.lang.constant.BusinessType;
import com.manage.framework.web.controller.BaseController;
import com.manage.framework.web.domain.AjaxResult;
import com.manage.framework.web.page.TableDataInfo;
import com.manage.project.common.Constant;
import com.manage.project.system.base.domain.Dispatch;
import com.manage.project.system.base.domain.User;
import com.manage.project.system.product.domain.ProductInfo;
import com.manage.project.system.stock.domain.StockInfo;
import com.manage.project.system.supply.domain.SupplyConfig;
import com.manage.project.system.supply.domain.SupplyVending;
import com.manage.project.system.supply.service.ISupplyConfigService;
import com.manage.project.system.supply.service.ISupplyVendingService;
import com.manage.project.system.supply.vo.*;
import com.manage.project.system.util.SystemUtil;
import com.manage.project.system.vending.domain.Vending;
import com.manage.project.system.vending.domain.VendingLanep;
import com.manage.project.system.vending.service.IVendingLanepService;
import com.manage.project.system.vending.service.IVendingService;
import com.manage.project.system.vendingPoint.domain.VendingDistrict;
import com.manage.project.system.vendingPoint.domain.VendingLine;
import com.manage.project.system.vendingPoint.domain.VendingPoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ?????????????????????????????????
 * 
 * @author wusijie
 * @date 2018-09-02
 */
@Controller
@RequestMapping("/system/supply/supplyWarnPoint")
public class SupplyWarnPointController extends BaseController
{
    private String prefix = "supply/supplyWarnPoint";
	@Autowired
	private ISupplyConfigService supplyConfigService;
	@Autowired
	private ISupplyVendingService supplyVendingService;
	@Autowired
	private IVendingService vendingService;
	@Autowired
	private IVendingLanepService vendingLanepService;

	@GetMapping()
	public String supplyWarnLine()
	{
	    return prefix + "/supplyWarnPoint";
	}
	
	/**
	 * ????????????????????????
	 */
	@GetMapping("/list")
	@ResponseBody
	public AjaxResult list(SupplyVending supplyVending)
	{
		startPage();
		if (SystemUtil.isZhilai()) {
			supplyVending.setCorpId("");
		} else {
			supplyVending.setCorpId(ShiroUtils.getUser().getCorpId());
		}
        List<SupplyVending> list = supplyVendingService.selectSupplyVendingList(supplyVending);
		TableDataInfo tableDataInfo=getDataTable(list);
        List<WarnPointVo> warnPointVoList=new ArrayList<>();
		WarnPointVo warnPointVo=null;
        for(SupplyVending supplyVendingR:list){
			//Vending vending=this.vendingService.selectVendingBySiteId(supplyVendingR.getSiteId());
        	Vending vending=SystemUtil.getVendingBase(supplyVendingR.getSiteId());
        	warnPointVo=new WarnPointVo();
        	if(supplyVendingR.getMaxPNum()==null) {
        		supplyVendingR.setMaxPNum(0);
        	}
        	if(supplyVendingR.getCurPNum()==null) {
        		supplyVendingR.setCurPNum(0);
        	}
			warnPointVo.setSupplyNum(supplyVendingR.getMaxPNum()-supplyVendingR.getCurPNum());
			warnPointVo.setStoryLevel(supplyVendingR.getStoryLevel());
			warnPointVo.setSiteName(vending.getSiteName());
			warnPointVo.setSiteId(supplyVendingR.getSiteId());
			warnPointVo.setPointId(vending.getPointId());
			warnPointVo.setAddress(vending.getAddress());
			VendingPoint vendingPoint = SystemUtil.getVendingPointFromCache(vending.getPointId());
			if(vendingPoint!=null) {
				warnPointVo.setPointName(vendingPoint.getName());
			}
			String vendingDistrict = SystemUtil.getVendingDistrictNameFromCache(vending.getDistrictId());
			if(vendingDistrict!=null) {
				warnPointVo.setDistrictName(vendingDistrict);
			}
			int storyPercent=(int)((double)supplyVendingR.getCurPNum()/(double)supplyVendingR.getMaxPNum()*100);
			warnPointVo.setStoryPercent(storyPercent);
			warnPointVoList.add(warnPointVo);
		}
		tableDataInfo.setRows(warnPointVoList);
		return AjaxResult.success(tableDataInfo);
	}
	/**
	 * ????????????????????????
	 */
	@GetMapping("/detail")
	@ResponseBody
	public AjaxResult detail(SupplyVending supplyVending)
	{
		//?????????????????????
		SupplyVending supplyVendingP = supplyVendingService.selectSupplyVendingBySiteId(supplyVending.getSiteId());
		//????????????????????????????????????
		SupplyConfig supplyConfigR = supplyConfigService.selectSupplyConfigBySupplyId(supplyVendingP.getSupplyId());
		//??????????????????????????????
		List<VendingLanep> vendingLanepList=null;
		//????????????????????????????????????
		if(supplyConfigR.getType().equals(Constant.supplyType_yuzhi)){
			vendingLanepList=vendingLanepService.selectSupplyPThreshold(supplyVendingP.getSiteId());
		}else{//?????????????????????????????????
			vendingLanepList=vendingLanepService.selectSupplyProduct(supplyVendingP.getSiteId());
		}
        //??????
		List<SupplyProductVo> supplyProductVoList=new ArrayList<>();
		SupplyProductVo supplyProductVo=null;
		for(VendingLanep vendingLanep:vendingLanepList){
			supplyProductVo=new SupplyProductVo();
			supplyProductVo.setProductId(vendingLanep.getProductId());
			supplyProductVo.setProductName(vendingLanep.getProductName());
			supplyProductVo.setSupplyNum(String.valueOf(vendingLanep.getCurCap()));
			supplyProductVoList.add(supplyProductVo);
		}
		return AjaxResult.success(supplyProductVoList);
	}
	
	/**
	 * ???????????????
	 */
	@Log(title = "???????????????", action = BusinessType.EXPORT)
	@PostMapping( "/exportExcel")
	@ResponseBody
	public AjaxResult exportExcel(@RequestBody SupplyVending supplyVending) {
		try {
			
			ExcelUtil<SupplyProductVo> util = new ExcelUtil<SupplyProductVo>(SupplyProductVo.class);
			//?????????????????????
			SupplyVending supplyVendingP = supplyVendingService.selectSupplyVendingBySiteId(supplyVending.getSiteId());
			//????????????????????????????????????
			SupplyConfig supplyConfigR = supplyConfigService.selectSupplyConfigBySupplyId(supplyVendingP.getSupplyId());
			//??????????????????????????????
			List<VendingLanep> vendingLanepList=null;
			//????????????????????????????????????
			if(supplyConfigR.getType().equals(Constant.supplyType_yuzhi)){
				vendingLanepList=vendingLanepService.selectSupplyPThreshold(supplyVendingP.getSiteId());
			}else{//?????????????????????????????????
				vendingLanepList=vendingLanepService.selectSupplyProduct(supplyVendingP.getSiteId());
			}
	        //??????
			List<SupplyProductVo> supplyProductVoList=new ArrayList<>();
			SupplyProductVo supplyProductVo=null;
			for(VendingLanep vendingLanep:vendingLanepList){
				supplyProductVo=new SupplyProductVo();
				supplyProductVo.setProductId(vendingLanep.getProductId());
				supplyProductVo.setProductName(vendingLanep.getProductName());
				supplyProductVo.setSupplyNum(String.valueOf(vendingLanep.getCurCap()));
				supplyProductVoList.add(supplyProductVo);
			}
            return util.exportExcel(supplyProductVoList, "Sheet1");
			
		} catch (Exception e) {
			return error("??????Excel????????????????????????????????????");
		}
	}
	
	/**
	 * ??????????????????????????????
	 */
	@GetMapping("/warnDetail")
	@ResponseBody
	public AjaxResult warnDetail(SupplyVending supplyVending){
		SupplyVending supply = supplyVendingService.selectSupplyVendingBySiteId(supplyVending.getSiteId());
		SupplyWarnVo vo = new SupplyWarnVo();
		String siteId = supply.getSiteId();
		vo.setSiteId(siteId);
		Vending vending = SystemUtil.getVendingBase(siteId);
		if(vending!=null) {
			vo.setSiteName(vending.getSiteName());
		}
		VendingPoint point = SystemUtil.getVendingPointFromCache(vending.getPointId());
		if(point!=null) {
			vo.setPointId(point.getPointId());
			vo.setPointName(point.getName());
			String district = point.getDistrict();
			//?????????????????????????????????
			Map<String, Dispatch> map = (Map<String, Dispatch>)CacheUtils.get(Constant.DISPATCH_CACHE);
			Dispatch dispatch = map.get(district);
			vo.setDispatchName(dispatch.getName());
		}
		VendingDistrict district = SystemUtil.getVendingDistrictFromCache(vending.getDistrictId());
		if(district!=null) {
			vo.setDistrictId(district.getDistrictId());
			vo.setDistrictName(district.getName());
		}
		vo.setWaitSPNum(supply.getMaxPNum()-supply.getCurPNum());
		String warnLevel = SystemUtil.parseWarnLevel(supply.getStoryLevel().toString());
		vo.setWarnLevelName(warnLevel);
		return AjaxResult.success(vo);
	}
}
