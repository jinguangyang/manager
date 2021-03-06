package com.manage.project.system.vending.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.manage.common.exception.vending.LaneProductCapacityException;
import com.manage.common.exception.vending.LaneProductNullException;
import com.manage.common.exception.vending.LaneProductWarnCapException;
import com.manage.common.exception.vending.SupplyVorderExistException;
import com.manage.common.exception.vending.VunderExistException;
import com.manage.common.exception.vending.WarnCapGreatThanCapacityException;
import com.manage.common.utils.DateUtils;
import com.manage.common.utils.StringUtils;
import com.manage.common.utils.bean.BeanUtils;
import com.manage.common.utils.poi.ExcelUtil;
import com.manage.common.utils.security.ShiroUtils;
import com.manage.framework.aspectj.lang.annotation.Log;
import com.manage.framework.aspectj.lang.constant.BusinessType;
import com.manage.framework.config.ManageConfig;
import com.manage.framework.web.controller.BaseController;
import com.manage.framework.web.domain.AjaxResult;
import com.manage.framework.web.page.TableDataInfo;
import com.manage.project.common.Constant;
import com.manage.project.common.vo.CommonVo;
import com.manage.project.system.base.domain.DictData;
import com.manage.project.system.base.domain.Dispatch;
import com.manage.project.system.base.domain.User;
import com.manage.project.system.base.service.IDictDataService;
import com.manage.project.system.statement.service.IOrderApplyService;
import com.manage.project.system.statement.vo.OrderVo;
import com.manage.project.system.stock.domain.StockInfo;
import com.manage.project.system.supply.domain.SupplyVorder;
import com.manage.project.system.supply.service.ISupplyVorderService;
import com.manage.project.system.supply.vo.SupplyOrderListVo;
import com.manage.project.system.supply.vo.SupplyOrderTmpVo;
import com.manage.project.system.util.BussinessCacheService;
import com.manage.project.system.util.SystemUtil;
import com.manage.project.system.vending.domain.Vending;
import com.manage.project.system.vending.domain.VendingCabinet;
import com.manage.project.system.vending.domain.VendingLane;
import com.manage.project.system.vending.domain.VendingLanep;
import com.manage.project.system.vending.domain.VendingLogfile;
import com.manage.project.system.vending.domain.VendingLsdiffer;
import com.manage.project.system.vending.domain.VendingModel;
import com.manage.project.system.vending.domain.VendingWarn;
import com.manage.project.system.vending.mapper.VendingLogfileMapper;
import com.manage.project.system.vending.service.IVendingCabinetService;
import com.manage.project.system.vending.service.IVendingLaneService;
import com.manage.project.system.vending.service.IVendingLanepService;
import com.manage.project.system.vending.service.IVendingLogfileService;
import com.manage.project.system.vending.service.IVendingLsdifferService;
import com.manage.project.system.vending.service.IVendingModelService;
import com.manage.project.system.vending.service.IVendingService;
import com.manage.project.system.vending.service.IVendingWarnService;
import com.manage.project.system.vending.vo.CabinetTypeSelectVo;
import com.manage.project.system.vending.vo.Cols;
import com.manage.project.system.vending.vo.RelationSelectCabinetVo;
import com.manage.project.system.vending.vo.RelationSelectResultVo;
import com.manage.project.system.vending.vo.VendingCabinetVo;
import com.manage.project.system.vending.vo.VendingLaneVo;
import com.manage.project.system.vending.vo.VendingLanepVo;
import com.manage.project.system.vending.vo.VendingListVo;
import com.manage.project.system.vending.vo.VendingRecordVo;
import com.manage.project.system.vending.vo.VendingViewVo;
import com.manage.project.system.vending.vo.VendingVo;
import com.manage.project.system.vending.vo.ViewCols;
import com.manage.project.system.vendingPoint.domain.VendingLine;
import com.manage.project.system.vendingPoint.domain.VendingPoint;
import com.manage.project.system.vendingPoint.service.IVendingPointService;

/**
 * ??????????????????????????? ??????????????????
 * 
 * @author xufeng
 * @date 2018-08-31
 */
@Controller
@RequestMapping("/system/vending")
public class VendingController extends BaseController
{
	private Logger log=LoggerFactory.getLogger(VendingController.class);
	@Autowired
	private IVendingService vendingService;
	
	@Autowired
    private IDictDataService dictDataService;
	
	@Autowired
	private IVendingPointService vendingPointService;
	
	@Autowired
	private IVendingCabinetService vendingCabinetService;
	
	@Autowired
	private IVendingLaneService vendingLaneService;
	@Autowired
	private IOrderApplyService orderApplyService;
	@Autowired
	private IVendingWarnService vendingWarnService;
	@Autowired
	private IVendingLanepService vendingLanepService;
	@Autowired
	private IVendingModelService vendingModelService;
	
	@Autowired
	private ISupplyVorderService supplyVorderService;
	@Autowired
	private IVendingLsdifferService vendingLsdifferService;
	@Autowired
	private IVendingLogfileService vendingLogfileService;
	@Autowired
	private BussinessCacheService bussinessCacheService;
	
	/**
	 * ???????????????????????????????????????
	 */
	@GetMapping("/list")
	@ResponseBody
	public AjaxResult list(Vending vending)
	{
		startPage();
		if (SystemUtil.isZhilai()) {
			vending.setCorpId("");
		} else {
			vending.setCorpId(ShiroUtils.getUser().getCorpId());
		}
        List<Vending> list = vendingService.selectVendingList(vending);
        for (Vending site : list) {
        	site.setPointName(SystemUtil.getVendingPointNameFromCache(site.getPointId()));
		}
		return AjaxResult.success(getDataTable(list));
	}
	
	@PostMapping("/register")
	@ResponseBody
	public AjaxResult register(@RequestBody Vending vending)
	{		
        Vending site = vendingService.selectVendingBySiteId(vending.getSiteId());
        if(site==null) {
        	return AjaxResult.error("????????????????????????");
        }
        String result = "";
        try {
        	result = vendingService.registerVending(site);
            if(!"0000".equals(result)) {
    			log.info("????????????????????????????????????"+vending.getSiteId()+"??????,?????????:"+result+",??????"+DateUtils.getTime()+"");
    			if("1014".equals(result)) {
					return AjaxResult.error("????????????????????????,?????????????????????");
				}else {
					return AjaxResult.error("????????????");
				}
    		}else {
    			return AjaxResult.success();
    		}
        }catch (Exception e) {
        	log.info("????????????????????????????????????"+vending.getSiteId()+"??????,?????????:"+result+",??????"+DateUtils.getTime()+"");
        	return AjaxResult.error("????????????");
		}
        
	}
	
	/**
	 * ???????????????????????????????????????
	 */
//	@Log(title = "???????????????????????????", action = BusinessType.INSERT)
//	@PostMapping("/add")
//	@ResponseBody
//	public AjaxResult addSave(@RequestBody Vending vending)
//	{		
//		return toAjax(vendingService.insertVending(vending));
//	}
	
	@Log(title = "??????????????????????????????????????????????????????", action = BusinessType.INSERT)
	@PostMapping("/add")
	@ResponseBody
	public AjaxResult addSave(@RequestBody VendingVo vendingVo)
	{	
		if (vendingVo.getSiteCode().length()>30) {
			return AjaxResult.error("?????????????????????????????????");
		}
		if (vendingVo.getSiteName().length()>50) {
			return AjaxResult.error("?????????????????????????????????");
		}
		String corpId = vendingVo.getCorpId();
		if(StringUtils.isEmpty(corpId)) {
			corpId = ShiroUtils.getUser().getCorpId();
			vendingVo.setCorpId(corpId);
		}
		
		String pointId = vendingVo.getPointId();
		if (StringUtils.isEmpty(pointId)) {
			return AjaxResult.error("?????????????????????");
		}
		Vending sel = new Vending();
		sel.setPointId(pointId);
		sel.setCorpId(corpId);
		List<Vending> vendingList = vendingService.selectVendingList(sel);
		if (vendingList != null && !vendingList.isEmpty()) {
			for (Vending vending : vendingList) {
				if(!"3".equals(vending.getCurState())) {
					return AjaxResult.error("?????????????????????????????????????????????????????????????????????");
				}
			}
		}
		
		
		//????????????????????????
		String code = vendingVo.getSiteCode();
		if(StringUtils.isEmpty(code)) {
			return AjaxResult.error("??????????????????");
		}else {
			if(checkCodeExist(code, corpId)) {
				return AjaxResult.error("????????????,?????????????????????");
			}
		}		
		List<String> payTypeList = vendingVo.getPayTypeList();
		if (payTypeList != null && !payTypeList.isEmpty()) {
			String payType = String.join(",", payTypeList);
			vendingVo.setPayType(payType);
		}
		try {
			Vending vending = vendingService.insertVending(vendingVo);
			//???????????????
			String r = vendingService.registerVending(vending);
			if(!"0000".equals(r)) {
				log.info("????????????????????????????????????"+vending.getSiteId()+"??????,?????????:"+r+",??????"+DateUtils.getTime()+"");
				if("1014".equals(r)) {
					return AjaxResult.error("????????????????????????,?????????????????????");
				}else {
					return AjaxResult.error("????????????");
				}
				
			}
		}catch (LaneProductCapacityException e) {
			return AjaxResult.error("?????????????????????????????????1");
		}catch (LaneProductWarnCapException e) {
			return AjaxResult.error("???????????????????????????1");
		}catch (WarnCapGreatThanCapacityException e) {
			return AjaxResult.error("??????????????????????????????");
		}catch(LaneProductNullException e) {
			return AjaxResult.error("???????????????????????????");
		}catch (Exception e) {
			log.info("?????????????????????:??????"+DateUtils.getTime(),e);
			return AjaxResult.error("?????????????????????");
		}
		return AjaxResult.success();
	}
	
	/**
	 * ???????????????
	 * @param vendingVo
	 * @return
	 */
	@Log(title = "??????????????????????????????????????????????????????", action = BusinessType.UPDATE)
	@PostMapping("/edit")
	@ResponseBody
	public AjaxResult updateVending(@RequestBody VendingVo vendingVo) {
		Vending vending = vendingService.selectVendingById(vendingVo.getLogid());
		String code = vending.getSiteCode();
		String updateCode = vendingVo.getSiteCode();
		//???????????????????????????
		if(StringUtils.isNotEmpty(updateCode)&&!updateCode.equals(code)) {
			//????????????????????????
			if(checkCodeExist(updateCode, vending.getCorpId())) {
				return AjaxResult.error("????????????,?????????????????????");
			}
		}
		List<String> payTypeList = vendingVo.getPayTypeList();
		if (payTypeList != null && !payTypeList.isEmpty()) {
			String payType = String.join(",", payTypeList);
			vendingVo.setPayType(payType);
		}
		int result=0;
		try {
			result = vendingService.updateVending(vendingVo); 
		}catch (LaneProductCapacityException e) {
			return AjaxResult.error("?????????????????????????????????1");
		}catch (LaneProductWarnCapException e) {
			return AjaxResult.error("???????????????????????????1");
		}catch (WarnCapGreatThanCapacityException e) {
			return AjaxResult.error("??????????????????????????????");
		}catch (SupplyVorderExistException e) {
			return AjaxResult.error("?????????????????????????????????????????????????????????");
		}catch (VunderExistException e) {
			return AjaxResult.error("????????????????????????????????????????????????");
		}catch (Exception e) {
			return AjaxResult.error("?????????????????????");
		}
		return toAjax(result);
	}
	
	/**
	 * ???????????????????????????????????????
	 */
	@Log(title = "???????????????????????????", action = BusinessType.DELETE)
	@PostMapping("/remove")
	@ResponseBody
	@Transactional
	public AjaxResult remove(@RequestBody CommonVo ids)
	{		
		String id = ids.getIds();
		String i[] = id.split(",");
		int r = 0;
		for (String a : i) {
			Vending vending = vendingService.selectVendingById(a);
			if (vending.getCurState().equals(Constant.VENDING_CURSTATE_DELETE)) {
				return AjaxResult.error("???????????????????????????????????????????????????????????????");
			}
			List<VendingLanep> list = vendingLanepService.selectVendingProduct(vending.getSiteId());	
			if (list != null && !list.isEmpty()) {
				for (VendingLanep vendingLanep : list) {
					if(vendingLanep.getCurCap()!=null&&vendingLanep.getCurCap()!=0) {
						return AjaxResult.error("?????????????????????????????????????????????????????????");
					}
				}	
			}
			OrderVo vo = new OrderVo();
			vo.setSiteId(vending.getSiteId());
			vo.setOrderType("1");
			List<OrderVo> orderList = orderApplyService.selectOrderApplyList(vo);
			if (orderList != null && !orderList.isEmpty()) {
				return AjaxResult.error("????????????????????????????????????????????????????????????");
			}	
			Vending v = new Vending();
			v.setLogid(a);
			v.setCurState(Constant.VENDING_CURSTATE_DELETE);
			//r = vendingService.updateVending(v);
			r = vendingService.deleteByFlag(v);
			Vending v1 = vendingService.selectVendingById(a);
			if (v1 != null) {
				vendingService.updateSupply(v1.getSiteId(), v1.getLineId());
			}
			String result = vendingService.deleteVending(vending.getSiteId());
			if(!"0000".equals(result)) {
				log.info("????????????????????????????????????"+vending.getSiteId()+"?????????,?????????:"+r+",??????"+DateUtils.getTime());
			}
		}
		
		return toAjax(r);
	}
	
	/**
	 * ???????????????????????????????????????
	 */
	@Log(title = "???????????????????????????", action = BusinessType.DELETE)
	@PostMapping( "/delete")
	@ResponseBody
	public AjaxResult delete(@RequestBody CommonVo ids)
	{	
		if(StringUtils.isNotEmpty(ids.getIds())) {
			List<Vending> list = vendingService.selectVendingByIds(ids.getIds());
			//?????????????????????????????????????????????
			for (Vending vending : list) {
				if(!Constant.VENDING_CURSTATE_DELETE.equals(vending.getCurState())) {
					return AjaxResult.error("?????????????????????????????????????????????");
				}
			}
			for (Vending vending : list) {
				log.info("???????????????:"+vending.toString());
			}
			return toAjax(vendingService.deleteVendingByIds(ids.getIds().split(",")));
		}else {
			return AjaxResult.error("?????????????????????");
		}
		
		
	}
	
	/**
	 * ???????????????????????????
	 * @param vending
	 * @return
	 */
	@GetMapping("/selectNVendingList")
	@ResponseBody
	public AjaxResult selectNVendingList(VendingListVo vendingListVo) {
		startPage();
		if (SystemUtil.isZhilai()) {
			vendingListVo.setCorpId("");
		} else {
			vendingListVo.setCorpId(ShiroUtils.getUser().getCorpId());
		}
        List<VendingListVo> list = vendingService.selectNVendingList(vendingListVo);
		return AjaxResult.success(getDataTable(list));
	}
	

	/**
	 * ??????????????????????????????
	 * @param logid	?????????logid
	 * @return	
	 */
	@GetMapping("/getVendingView")
	@ResponseBody
	public AjaxResult getVendingView(String logid) {
		// ?????????????????????
		Vending vending = vendingService.selectVendingById(logid);
		// ????????????????????????
		VendingViewVo vo = new VendingViewVo();	// ?????????????????????
		vo.setLogid(logid);
		vo.setSiteId(vending.getSiteId());
		vo.setSiteCode(vending.getSiteCode());
		vo.setSiteName(vending.getSiteName());
		vo.setCorpId(vending.getCorpId());
		vo.setCorpName(SystemUtil.getCorpNameById(vending.getCorpId()));		
		// ????????????
		VendingPoint vendingPoint = SystemUtil.getVendingPointFromCache(vending.getPointId());
		vo.setAddress(vendingPoint.getAdderss());
		vo.setAllAddress(vendingPoint.getAddresses());
		vo.setPointId(vendingPoint.getPointId());
		vo.setPointName(vendingPoint.getName());
		
		vo.setLineId(vending.getLineId());
		vo.setLineName(SystemUtil.getVendingLineNameFromCache(vending.getLineId()));
		vo.setDistrictId(vending.getDistrictId());
		vo.setDistrictName(SystemUtil.getVendingDistrictNameFromCache(vending.getDistrictId()));
		vo.setCabinetType(vending.getCabinetType());
		vo.setCabinetTypeName(vending.getCabinetTypeName());
		vo.setFactoryId(vending.getFactoryId());
		vo.setFactoryName(vending.getFactoryName());
		vo.setDeviceId(vending.getDeviceId());
		VendingModel model = vendingModelService.selectVendingModelById(vending.getDeviceId());
		if(model!=null) {
			vo.setDevice(model.getDeviceId());
		}
		
		vo.setNetWork(vending.getNetWork());
		vo.setNetWorkName(vending.getNetWorkName());
		vo.setPayType(vending.getPayType());
		vo.setPayTypeName(vending.getPayTypeName());
		if (!StringUtils.isEmpty(vending.getPayType())) {
			List<String> payTypeList = Arrays.asList(vending.getPayType().split(","));
			vo.setPayTypeList(payTypeList);
		}
		vo.setSellState(vending.getSellState());
		vo.setSellStateName(vending.getSellStateName());
		vo.setNetSate(vending.getNetSate());
		vo.setNetSateName(vending.getNetSateName());
		if (vending.getStateTime() != null && !vending.getStateTime().equals("")) {
			Date d1 = DateUtils.parseStrToDate(vending.getStateTime());
			vo.setStateTime(vending.getStateTime());
			vo.setContinueTime(DateUtils.subTime(DateUtils.getNowDate(), d1));
		}
		
		// ?????????????????????????????????
		VendingCabinet vendingCabinet = new VendingCabinet();
		vendingCabinet.setSiteId(vending.getSiteId());
		vendingCabinet.setCorpId(vending.getCorpId());
		vendingCabinet.setHangType(Constant.HANG_TYPE_TRUE);
		List<VendingCabinet> cabinetList = vendingCabinetService.selectVendingCabinetList(vendingCabinet);
		vo.setCabinets(cabinetList);
		return AjaxResult.success(vo);
	}
	
	/**
	 * ???????????????????????????
	 * @param siteId	???????????????
	 * @return	
	 */
	@GetMapping("/getVendingRecordBySiteId")
	@ResponseBody
	public AjaxResult getVendingRecordBySiteId(String siteId) {
		VendingRecordVo vo = new VendingRecordVo();
		SupplyVorder supplyVorder = new SupplyVorder();
		supplyVorder.setSiteId(siteId);
		List<SupplyVorder> supplyVorderList = supplyVorderService.selectSupplyVorderList(supplyVorder);
		if(StringUtils.isEmpty(supplyVorderList)) {
			vo.setSupplyNum(0);
		}else {
			vo.setSupplyNum(supplyVorderList.size());
		}
		Map<String, BigDecimal> capMap = vendingService.selectVendingCap(siteId);
		BigDecimal pcurNum = capMap.get("pcurNum");
		if (pcurNum == null) {
			pcurNum = new BigDecimal(0);
		}
		vo.setpCurNum(pcurNum.toBigInteger().intValue());
		BigDecimal pmaxNum = capMap.get("pmaxNum");
		if (pmaxNum == null) {
			pmaxNum = new BigDecimal(0);
		}
		vo.setpMaxNum(pmaxNum.toBigInteger().intValue());
		OrderVo orderVo = new OrderVo();
		orderVo.setSiteId(siteId);
		List<OrderVo> orderApplyList = orderApplyService.selectOrderApplyList(orderVo);
		if(StringUtils.isEmpty(orderApplyList)) {
			vo.setOrderNum(0);
		}else {
			vo.setOrderNum(orderApplyList.size());
		}
		VendingWarn vendingWarn = new VendingWarn();
		vendingWarn.setSiteId(siteId);
		List<VendingWarn> vendingWarnList = vendingWarnService.selectVendingWarnList(vendingWarn);
		if(StringUtils.isEmpty(vendingWarnList)) {
			vo.setWarnNum(0);
		}else {
			vo.setWarnNum(vendingWarnList.size());
		}
		//???????????????
		VendingLsdiffer vendingLsdiffer = new VendingLsdiffer();
		vendingLsdiffer.setSiteId(siteId);
		List<VendingLsdiffer> vendingLsdifferList = vendingLsdifferService.selectVendingLsdifferList(vendingLsdiffer);
		if(StringUtils.isEmpty(vendingLsdifferList)) {
			vo.setLsdifferNum(0);
		}else {
			vo.setLsdifferNum(vendingLsdifferList.size());
		}
		//??????????????????????????????
		VendingLogfile vendingLogfile = new VendingLogfile();
		vendingLogfile.setSiteId(siteId);
		List<VendingLogfile> vendingLogfileList = vendingLogfileService.selectVendingLogfileList(vendingLogfile);
		if(StringUtils.isEmpty(vendingLogfileList)) {
			vo.setLogfileNum(0);
		}else {
			vo.setLogfileNum(vendingLogfileList.size());
		}
		return AjaxResult.success(vo);
	}

	
	/**
	 * ???????????????????????????
	 */
	@GetMapping("/getSupplyOrderBySiteId")
	@ResponseBody
	public AjaxResult getSupplyOrderBySiteId(String siteId,String supplyFTime, String supplierId) {
		startPage();
		List<SupplyOrderTmpVo> list = supplyVorderService.selectSupplyVorderBySiteId(siteId, supplyFTime, supplierId);
		TableDataInfo dataTable = getDataTable(list);
		List<SupplyOrderListVo> supplyOrderListVoList=new ArrayList<SupplyOrderListVo>();
		
		SupplyOrderListVo supplyOrderListVo=null;
		for(SupplyOrderTmpVo supplyOrderTmpVo:list){
			supplyOrderListVo=new SupplyOrderListVo();
			supplyOrderListVo.setWmId(supplyOrderTmpVo.getWmId());
			StockInfo stock = SystemUtil.getStockInfo(supplyOrderTmpVo.getWmId());
			if(stock!=null) {
				supplyOrderListVo.setWmName(stock.getStockName());
			}
			User user = SystemUtil.getUserByLoginId(supplyOrderTmpVo.getCorpId(), supplyOrderTmpVo.getSupplierId());
			supplyOrderListVo.setSupplierName(user.getUserName());
			VendingLine vendingLine=SystemUtil.getVendingLineFromCache(supplyOrderTmpVo.getLineId());
			if(vendingLine!=null)
			    supplyOrderListVo.setLineName(vendingLine.getName());
			else
				supplyOrderListVo.setLineName("");
			supplyOrderListVo.setLineId(supplyOrderTmpVo.getLineId());
			supplyOrderListVo.setCreateTime(supplyOrderTmpVo.getCreateTime());
			supplyOrderListVo.setCurState(supplyOrderTmpVo.getCurState());
			if (supplyOrderTmpVo.getCurState().equals("1")) {
				supplyOrderListVo.setCurStateName("????????????");
			} else if (supplyOrderTmpVo.getCurState().equals("2")) {
				supplyOrderListVo.setCurStateName("????????????");
			}
			supplyOrderListVo.setVorderId(supplyOrderTmpVo.getVorderId());
			supplyOrderListVo.setsOrderId(supplyOrderTmpVo.getsOrderId());
			supplyOrderListVo.setStockState(supplyOrderTmpVo.getStockState());
			supplyOrderListVo.setStockStateName(SystemUtil.parseStockState(supplyOrderTmpVo.getStockState()));
			supplyOrderListVo.setSupplyNum(supplyOrderTmpVo.getSupplyNum());
			supplyOrderListVoList.add(supplyOrderListVo);
		}
		dataTable.setRows(supplyOrderListVoList);
		return AjaxResult.success(dataTable);
	}
	
	private List<Cols> getCols(List<VendingLane> lanes) {
		List<Cols> result = new ArrayList<Cols>();		
		
		// VendingLane???????????????VendingLaneVo??????
		List<VendingLaneVo> laneList = new ArrayList<VendingLaneVo>();
		for (VendingLane vl : lanes) {
			VendingLaneVo vv = new VendingLaneVo();
			BeanUtils.copyBeanProp(vv, vl);
			VendingLanepVo lanep = new VendingLanepVo();
			BeanUtils.copyBeanProp(lanep, vl);
			
			lanep.setLaneSId(vl.getLaneId());
			lanep.setLaneEId(vl.getLaneId());
			//????????????????????????????????????,????????????????????????
			if(StringUtils.isEmpty(lanep.getProductId())&"1".equals(lanep.getLaneSate())) {
				lanep.setAllowDel("1");
			}else {
				lanep.setAllowDel("2");
			}
			
			if (vl.getPlogid() == null || vl.getPlogid().equals("")) {
				lanep.setLogid("");
			} else {
				lanep.setLogid(vl.getPlogid());
			}
			vv.setLanep(lanep);
			laneList.add(vv);
		}
				
		// ???????????????????????????Cols??????
		int rowNum = 1;	// ?????????
		List<VendingLaneVo> rows = new ArrayList<VendingLaneVo>();
		for (int j = 0; j < laneList.size(); j++) {
			VendingLaneVo lane = laneList.get(j);
			if (lane.getRow() == null) {	// ??????????????????
				continue;
			}
			if (lane.getRow().intValue() == rowNum) {
				rows.add(lane);
				if (j == laneList.size() - 1) {	// ????????????
					Cols cols = new Cols();					
					cols.setCols(rows);
					result.add(cols);
				}
			} else {
				++rowNum;
				Cols cols = new Cols();					
				cols.setCols(rows);
				result.add(cols);
				
				rows = new ArrayList<VendingLaneVo>();
				rows.add(lane);
				if (j == laneList.size() - 1) {	// ????????????
					Cols cols1 = new Cols();					
					cols1.setCols(rows);
					result.add(cols1);
				}
			}
		}
		return result;
	}
	
	/**
	 * ???????????????????????????????????????
	 * @param pointId	??????id
	 * @return
	 */
	@GetMapping("/getCabinetByPointId")
	@ResponseBody
	public AjaxResult getCabinetByPointId(String pointId) {		
		Vending vending = new Vending();
		vending.setPointId(pointId);
		//vending.setCurState(Constant.VENDING_CURSTATE_DELETE);
		List<Vending> list = vendingService.selectNeverDelVendingList(vending);
		if (list == null || list.isEmpty()) {
			return AjaxResult.error("??????????????????");
		}
		Vending v= list.get(0);
		List<VendingCabinetVo> cabinets = this.getCabinetsVo(v.getSiteId());		

		return AjaxResult.success(cabinets);
	}
	
	/**
	 * ???????????????id??????????????????
	 * @param siteId
	 * @return
	 */
	@GetMapping("/getCabinetBySiteId")
	@ResponseBody
	public AjaxResult getCabinetBySiteId(String siteId) {
		if (StringUtils.isEmpty(siteId)) {
			return AjaxResult.error("params is null");
		}
		List<VendingCabinetVo> cabinets = this.getCabinetsVo(siteId);		

		return AjaxResult.success(cabinets);
	} 
	
	/**
	 * ???????????????id????????????????????????
	 * @param siteId
	 * @return
	 */
	private List<VendingCabinetVo> getCabinetsVo(String siteId) {
		VendingCabinet vendingCabinet = new VendingCabinet();
		vendingCabinet.setSiteId(siteId);
		List<VendingCabinet> cabinetList = vendingCabinetService.selectVendingCabinetList(vendingCabinet);
		
		List<VendingCabinetVo> cabinets = new ArrayList<VendingCabinetVo>();
		for (VendingCabinet vc : cabinetList) {
			VendingCabinetVo c = this.getCabinetLane(vc);
			cabinets.add(c);
		}
		return cabinets;
	}
	
	/**
	 * ????????????id????????????????????????
	 * @param vendingCabinet ????????????
	 * @return
	 */
	private VendingCabinetVo getCabinetLane(VendingCabinet vendingCabinet) {
		VendingCabinetVo vo = new VendingCabinetVo();
		BeanUtils.copyBeanProp(vo, vendingCabinet);
		List<VendingLane> lanes = vendingLaneService.selectVendingLaneByCabinetId(vo.getCabinetId());
		
		List<Cols> cols = this.getCols(lanes);
		vo.setLanes(cols);
		
		return vo;
	}
	
	@GetMapping("/getVendingViewByLogid")
	@ResponseBody
	public AjaxResult getVendingViewByLogid(String logid) {
		VendingVo vo = new VendingVo();	// rsult
		// ?????????????????????
		Vending vending = vendingService.selectVendingById(logid);
		if (vending == null) {
			return AjaxResult.error("??????????????????.");
		}
		vo.setLogid(logid);
		vo.setSiteId(vending.getSiteId());
		vo.setSiteCode(vending.getSiteCode());
		vo.setSiteName(vending.getSiteName());
		vo.setCorpId(vending.getCorpId());
		vo.setCorpName(SystemUtil.getCorpNameById(vending.getCorpId()));		
		// ????????????
		VendingPoint vendingPoint = SystemUtil.getVendingPointFromCache(vending.getPointId());
		if (vendingPoint != null) {
			vo.setAddress(vendingPoint.getAdderss());
			vo.setAllAddress(vendingPoint.getAddresses());
			vo.setPointId(vendingPoint.getPointId());
			vo.setPointName(vendingPoint.getName());
		}
		
		vo.setLineId(vending.getLineId());
		vo.setLineName(SystemUtil.getVendingLineNameFromCache(vending.getLineId()));
		vo.setDistrictId(vending.getDistrictId());
		vo.setDistrictName(SystemUtil.getVendingDistrictNameFromCache(vending.getDistrictId()));
		vo.setCabinetType(vending.getCabinetType());
		vo.setCabinetTypeName(vending.getCabinetTypeName());
		vo.setFactoryId(vending.getFactoryId());
		vo.setFactoryName(vending.getFactoryName());
		vo.setDeviceId(vending.getDeviceId());
		VendingModel model = vendingModelService.selectVendingModelById(vending.getDeviceId());
		if(model!=null) {
			vo.setDevice(model.getDeviceId());
		}
		vo.setNetWork(vending.getNetWork());
		vo.setNetWorkName(vending.getNetWorkName());
		vo.setPayType(vending.getPayType());
		if (!StringUtils.isEmpty(vending.getPayType())) {
			List<String> payTypeList = Arrays.asList(vending.getPayType().split(","));
			vo.setPayTypeList(payTypeList);
		}
		vo.setPayTypeName(vending.getPayTypeName());
		vo.setSellState(vending.getSellState());
		vo.setSellStateName(vending.getSellStateName());
		vo.setNetSate(vending.getNetSate());
		vo.setNetSateName(vending.getNetSateName());
		vo.setMediaType(vending.getMediaType());
		if (vending.getStateTime() != null && !vending.getStateTime().equals("")) {
			Date d1 = DateUtils.parseStrToDate(vending.getStateTime());
			vo.setStateTime(vending.getStateTime());
			vo.setContinueTime(DateUtils.subTime(DateUtils.getNowDate(), d1));
		}
		// ?????????????????????????????????
		VendingCabinet vendingCabinet = new VendingCabinet();
		vendingCabinet.setSiteId(vending.getSiteId());
		vendingCabinet.setCorpId(vending.getCorpId());
		List<VendingCabinet> cabinetList = vendingCabinetService.selectVendingCabinetList(vendingCabinet);
		List<VendingCabinetVo> cabinetVoList = new ArrayList<VendingCabinetVo>();
		
		
		for (VendingCabinet v : cabinetList) {
			VendingCabinetVo vc = new VendingCabinetVo();
			BeanUtils.copyBeanProp(vc, v);
			vc.setDis(true);// ???????????????????????????????????????
			VendingModel m = vendingModelService.selectVendingModelById(vc.getDeviceId());
			if(m!=null) {
				vc.setDevice(m.getDeviceId());
				vc.setDeviceName(m.getDeviceId());
			}
			cabinetVoList.add(vc);
		}
		
		int z = -1;
		for (int i = 0; i < cabinetVoList.size(); i++) {
			VendingCabinetVo vc = cabinetVoList.get(i);
			List<VendingLane> lanes = vendingLaneService.selectVendingLaneByCabinetId(vc.getCabinetId());
			List<Cols> cols = this.getCols(lanes);
			vc.setLanes(cols);
			
			if (vc.getHangType().equals(Constant.HANG_TYPE_FALSE)) {// ??????
				vo.setCabinetId(vc.getCabinetId());
				vo.setCabinetLogid(vc.getLogid());
				vo.setViewName(vc.getViewName());
				vo.setLanes(cols);
				z = i;
			}
			
			// ??????isShowTemplate??????,????????????????????????,?????????????????????false
			if (lanes != null) {
				for (VendingLane vl1 : lanes) {
					if (vl1.getProductId() != null && !vl1.getProductId().equals("")) {
						//????????????????????????????????????
						if (vc.getHangType().equals(Constant.HANG_TYPE_FALSE)) {// ??????
							vo.setIsShowTemplate(false);
						} else {	// ??????
							vc.setIsShowTemplate(false);
						}
						//break;
					}else {
					}
				}
			}
		}
		if (z != -1) {
			cabinetVoList.remove(z);
		}
		vo.setCabinets(cabinetVoList);
		vo.setDis(true);// ???????????????????????????????????????
		
		return AjaxResult.success(vo); 
	}
	/**
	 * ?????????????????????????????????
	 * @param logid	?????????logid
	 * @return	
	 */
	@GetMapping("/getVendingViewByLogid1")
	@ResponseBody
	public AjaxResult getVendingViewByLogid1(String logid) {
		// ?????????????????????
		Vending vending = vendingService.selectVendingById(logid);
		// ????????????????????????
		VendingViewVo vo = new VendingViewVo();	// ?????????????????????
		vo.setLogid(logid);
		vo.setSiteId(vending.getSiteId());
		vo.setSiteCode(vending.getSiteCode());
		vo.setSiteName(vending.getSiteName());
		vo.setCorpId(vending.getCorpId());
		vo.setCorpName(SystemUtil.getCorpNameById(vending.getCorpId()));		
		// ????????????
		VendingPoint vendingPoint = SystemUtil.getVendingPointFromCache(vending.getPointId());
		vo.setAddress(vendingPoint.getAdderss());
		vo.setAllAddress(vendingPoint.getAddresses());
		vo.setPointId(vendingPoint.getPointId());
		vo.setPointName(vendingPoint.getName());
		
		vo.setLineId(vending.getLineId());
		vo.setLineName(SystemUtil.getVendingLineNameFromCache(vending.getLineId()));
		vo.setDistrictId(vending.getDistrictId());
		vo.setDistrictName(SystemUtil.getVendingDistrictNameFromCache(vending.getDistrictId()));
		vo.setCabinetType(vending.getCabinetType());
		vo.setCabinetTypeName(vending.getCabinetTypeName());
		vo.setFactoryId(vending.getFactoryId());
		vo.setFactoryName(vending.getFactoryName());
		vo.setDeviceId(vending.getDeviceId());
		vo.setNetWork(vending.getNetWork());
		vo.setNetWorkName(vending.getNetWorkName());
		vo.setPayType(vending.getPayType());
		if (!StringUtils.isEmpty(vending.getPayType())) {
			List<String> payTypeList = Arrays.asList(vending.getPayType().split(","));
			vo.setPayTypeList(payTypeList);
		}
		vo.setPayTypeName(vending.getPayTypeName());
		vo.setSellState(vending.getSellState());
		vo.setSellStateName(vending.getSellStateName());
		vo.setNetSate(vending.getNetSate());
		vo.setNetSateName(vending.getNetSateName());
		if (vending.getStateTime() != null && !vending.getStateTime().equals("")) {
			Date d1 = DateUtils.parseStrToDate(vending.getStateTime());
			vo.setStateTime(vending.getStateTime());
			vo.setContinueTime(DateUtils.subTime(DateUtils.getNowDate(), d1));
		}
		
		
		// ?????????????????????????????????
		VendingCabinet vendingCabinet = new VendingCabinet();
		vendingCabinet.setSiteId(vending.getSiteId());
		vendingCabinet.setCorpId(vending.getCorpId());
//		vendingCabinet.setHangType(Constant.HANG_TYPE_1);
		List<VendingCabinet> cabinetList = vendingCabinetService.selectVendingCabinetList(vendingCabinet);
		int z = -1;
		for (int i = 0; i < cabinetList.size(); i++) {
			VendingCabinet v = cabinetList.get(i);
			if (v.getHangType().equals(Constant.HANG_TYPE_FALSE)) {// ??????
				vo.setCabinetId(v.getCabinetId());
				vo.setCabinetLogid(v.getLogid());
				vo.setViewName(v.getViewName());
				List<VendingLane> lanes = vendingLaneService.selectVendingLaneByCabinetId(v.getCabinetId());
				List<ViewCols> colsList = new ArrayList<ViewCols>();	// ???????????????
				ViewCols cols = new ViewCols();
				List<VendingLane> rows = new ArrayList<VendingLane>();
				int rowNum = 1;	// ?????????
				
				// ???????????????????????????ViewCols??????
				for (int j = 0; j < lanes.size(); j++) {
					VendingLane lane = lanes.get(j);
					if (lane.getRow().intValue() == rowNum) {
						rows.add(lane);
						if (j == lanes.size() - 1) {	// ????????????
							cols.setCols(rows);
							colsList.add(cols);
						}
					} else {
						++rowNum;
						cols.setCols(rows);
						colsList.add(cols);
						rows = new ArrayList<VendingLane>();
						rows.add(lane);
						if (j == lanes.size() - 1) {	// ????????????
							cols.setCols(rows);
							colsList.add(cols);
						}
					}
				}
				vo.setLanes(colsList);
				z = i;
			} else {	// ??????
				List<VendingLane> lanes = vendingLaneService.selectVendingLaneByCabinetId(v.getCabinetId());
				List<ViewCols> colsList = new ArrayList<ViewCols>();	// ???????????????
				ViewCols cols = new ViewCols();
				List<VendingLane> rows = new ArrayList<VendingLane>();
				int rowNum = 1;	// ?????????
				
				// ???????????????????????????ViewCols??????
				for (int j = 0; j < lanes.size(); j++) {
					VendingLane lane = lanes.get(j);
					if (lane.getRow().intValue() == rowNum) {
						rows.add(lane);
						if (j == lanes.size() - 1) {	// ????????????
							cols.setCols(rows);
							colsList.add(cols);
						}
					} else {
						++rowNum;
						cols.setCols(rows);
						colsList.add(cols);
						rows = new ArrayList<VendingLane>();
						rows.add(lane);
						if (j == lanes.size() - 1) {	// ????????????
							cols.setCols(rows);
							colsList.add(cols);
						}
					}
				}
				
				v.setLanes(colsList);
			}
		}
		if (z != -1) {
			cabinetList.remove(z);
		}
		
		vo.setCabinets(cabinetList);
		return AjaxResult.success(vo);
	}
	
	/**
	 * ?????????????????????
	 * @return
	 */
	@GetMapping("/getVendingNum")
	@ResponseBody
	public AjaxResult getVendingNum() {
		/*Object o = CacheUtils.get(Constant.VENDING_NUM_CACHE_+ShiroUtils.getCorpId());
		if (o == null) {
			Map<String, Integer> map = new HashMap<String, Integer>();
			map.put("allNum", 0);
			map.put("onlineNum", 0);
			map.put("outlineNum", 0);
			map.put("wzNum", 0);
			map.put("delNum", 0);
			return AjaxResult.success(map);
		}
		Map<String, Integer> map = (Map<String, Integer>)o;*/
		String corpId = SystemUtil.getCorpId();
		Map<String,Integer> map = vendingService.selectVendingNum(corpId);
		return AjaxResult.success(map);
	}
	
	/**
	 * ?????????????????????
	 */
	@GetMapping("/listAll")
	@ResponseBody
	public AjaxResult listAll(Vending vending)
	{
        List<Vending> list = vendingService.selectVendingList(vending);
		return AjaxResult.success(getDataTable(list));
	}
	
	/**
	 * ???????????????id?????????????????????
	 * @param name
	 * @return
	 */
	@GetMapping("/selectVendingByCommon")
	@ResponseBody
	public AjaxResult selectVendingByCommon(String name)
	{
		startPage();
        List<Vending> list = vendingService.selectVendingByCommon(name, SystemUtil.getCorpId());
		return AjaxResult.success(getDataTable(list));
	}
	
	/**
	 * ?????????????????????????????????????????????
	 * @param cabinetType
	 * @return
	 */
	@GetMapping("/selectRelationVending")
	@ResponseBody
	public AjaxResult selectRelationVending(String cabinetType, String name)
	{
		if (StringUtils.isEmpty(cabinetType)) {
			AjaxResult.error("????????????????????????");
		}
		startPage();
		String corpId = SystemUtil.getCorpId();
        List<RelationSelectResultVo> list = vendingService.selectNeverDelVendingByCabinetType(cabinetType, corpId, name);	//.selectVendingByCommon(name, SystemUtil.getCorpId());
        TableDataInfo table = getDataTable(list);
        if (list != null&&list.size()!=0) {
        	Iterator<RelationSelectResultVo> iterator = list.iterator();
        	while(iterator.hasNext()) {
        		RelationSelectResultVo rs = iterator.next();
        		//rs.getSiteId()
        		//if()
        		VendingCabinet vendingCabinet = new VendingCabinet();
        		vendingCabinet.setCabinetType(cabinetType);
        		vendingCabinet.setCorpId(corpId);
        		vendingCabinet.setSiteId(rs.getSiteId());
        		List<VendingCabinet> cabinetList = vendingCabinetService.selectVendingCabinetList(vendingCabinet);
        		
        		List<RelationSelectCabinetVo> cabinets = new ArrayList<RelationSelectCabinetVo>();
        		if (cabinetList != null) {
        			for (VendingCabinet vc : cabinetList) {
        				//?????????????????????????????????????????????,???????????????????????????????????????????????????
        				List<VendingLanep> vendingLanepList = vendingLanepService.selectVendingLanepByCabinetId(vc.getCabinetId());
        				for (VendingLanep vendingLanep : vendingLanepList) {
							if(vendingLanep!=null&&vendingLanep.getCurCap()!=null&&vendingLanep.getCurCap()>0) {
								continue;
							}
						}
        				RelationSelectCabinetVo rsc = new RelationSelectCabinetVo();
        				BeanUtils.copyBeanProp(rsc, vc);
        				cabinets.add(rsc);
        			}
        		}
        		//???????????????????????????????????????
        		if(!cabinets.isEmpty()) {
        			rs.setCabinets(cabinets);
        		}else {
        			//???????????????????????????,???????????????
        			iterator.remove();
        		}	
        	}
//        	for (RelationSelectResultVo rs : list) {
//        		VendingCabinet vendingCabinet = new VendingCabinet();
//        		vendingCabinet.setCabinetType(cabinetType);
//        		vendingCabinet.setCorpId(corpId);
//        		vendingCabinet.setSiteId(rs.getSiteId());
//        		List<VendingCabinet> cabinetList = vendingCabinetService.selectVendingCabinetList(vendingCabinet);
//        		
//        		List<RelationSelectCabinetVo> cabinets = new ArrayList<RelationSelectCabinetVo>();
//        		if (cabinetList != null) {
//        			for (VendingCabinet vc : cabinetList) {
//        				List<VendingLanep> vendingLanepList = vendingLanepService.selectVendingLanepByCabinetId(vc.getCabinetId());
//        				for (VendingLanep vendingLanep : vendingLanepList) {
//							if(vendingLanep!=null&&vendingLanep.getCurCap()!=null&&vendingLanep.getCurCap()>0) {
//								continue;
//							}
//						}
//        				RelationSelectCabinetVo rsc = new RelationSelectCabinetVo();
//        				BeanUtils.copyBeanProp(rsc, vc);
//        				cabinets.add(rsc);
//        			}
//        		}
//        		rs.setCabinets(cabinets);
//        	}
        }
        
        return AjaxResult.success(table);
	} 
	
	
	/**
	 * ???????????????????????????????????????
	 * @param pointId	????????????
	 * @return	
	 */
	@GetMapping("/getVendingViewByPoint")
	@ResponseBody
	public AjaxResult getVendingViewByPoint(String pointId) {
		Vending vending = vendingService.selectVendingByPointId(pointId);
		return getVendingView(vending.getLogid());
	}
	
	/**
	 * ???????????????????????????
	 * @param siteId	????????????
	 * @return	
	 */
	@GetMapping("/getVendingRecordByPoint")
	@ResponseBody
	public AjaxResult getVendingRecordByPoint(String pointId) {
		Vending vending = vendingService.selectVendingByPointId(pointId);
		return getVendingRecordBySiteId(vending.getSiteId());
	}
	
	/**
	 * ??????????????????????????????
	 * @param factoryId
	 * @return
	 */
	@GetMapping("/selectCabinetTypeByFactoryId")
	@ResponseBody
	public AjaxResult selectCabinetTypeByFactoryId(String factoryId) {
		/*if (StringUtils.isEmpty(factoryId)) {
			return AjaxResult.success();
		}*/
		List<CabinetTypeSelectVo> list = vendingService.selectCabinetTypeByFactoryId(factoryId);
		return AjaxResult.success(list);
	}
	
	/**
	 * ???????????????????????????
	 * @param cabinetType
	 * @return
	 */
	@GetMapping("/selectDeviceByCabinetType")
	@ResponseBody
	public AjaxResult selectDeviceByCabinetType(String cabinetType) {
		/*if (StringUtils.isEmpty(cabinetType)) {
			return AjaxResult.success();
		}*/ 
		List<VendingModel> list = vendingService.selectDeviceByCabinetType(cabinetType,SystemUtil.getCorpId());
		return AjaxResult.success(list);
	}
	
	/**
	 * ????????????????????????
	 * 
	 * @param code ??????
	 * @param corpId ????????????
	 * @return ????????????true,???????????????false
	 */
	public boolean checkCodeExist(String code,String corpId) {
		Vending vending = new Vending();
		vending.setSiteCode(code);
		vending.setCorpId(corpId);
		List<Vending> list = vendingService.selectVendingList(vending);
		return StringUtils.isNotEmpty(list);
	}
	
	/**
	 * ???????????????
	 * 
	 * @param vendingLanep
	 * @return
	 */
	@PostMapping("updateProduct")
	@Log(title="?????????????????????",action=BusinessType.UPDATE)
	@ResponseBody
	public AjaxResult updateVendingLanep(@RequestBody VendingLanep vendingLanep) {
		try {
			int result = vendingService.updateVendingLanep(vendingLanep);
			if(result==1) {
				return AjaxResult.success();
			}else if(result==2) {
				return AjaxResult.error("??????????????????");
			}
			else if(result==3) {
				return AjaxResult.error("???????????????");
			}else if(result==4) {
				return AjaxResult.error("?????????????????????????????????");
			}else if(result==5) {
				return AjaxResult.error("????????????????????????????????????");
			}else if(result==6) {
				return AjaxResult.error("??????????????????????????????0");
			}else if(result==7) {
				return AjaxResult.error("??????????????????????????????????????????");
			}else if(result==8) {
				return AjaxResult.error("??????????????????????????????0");
			}else if(result==9) {
				return AjaxResult.error("????????????????????????0");
			}else if(result==10) {
				return AjaxResult.error("??????????????????????????????");
			}else if(result==11) {
				return AjaxResult.error("???????????????????????????????????????");
			}else if(result==12) {
				return AjaxResult.error("????????????????????????????????????");
			}else if(result==13) {
				return AjaxResult.error("????????????????????????????????????");
			}else {
				return AjaxResult.error("??????????????????");
			}
		}catch (Exception e) {
			log.error("??????????????????:"+DateUtils.getTime(),e);
			return AjaxResult.error("??????????????????");
		}
		
		
	}
	
	/**
	 * ???????????????
	 */
	@Log(title = "???????????????", action = BusinessType.IMPORT)
	@PostMapping( "/importExcel")
	@ResponseBody
	public AjaxResult importProductExcel(@RequestParam(value = "file", required = false) MultipartFile file) {
		FileOutputStream fos = null;
		try {
			ExcelUtil<Vending> util = new ExcelUtil<Vending>(Vending.class);
			List<Vending> vendingList = util.importExcel("????????????", file.getInputStream());// ??????
			HSSFWorkbook workbook = (HSSFWorkbook)WorkbookFactory.create(file.getInputStream());
            HSSFSheet sheet = workbook.getSheetAt(0);
            int row=1;
            //??????????????????
            for (Vending vending : vendingList) {
            	AjaxResult ajaxResult = vendingService.saveImportVending(vending);
            	HSSFRow hssfRow = sheet.getRow(row);
        		HSSFCell cell = hssfRow.getCell(10);
        		if(cell==null) {
        			cell = hssfRow.createCell(10);
        		}
        		row++;
            	if(ajaxResult.isSuccess()) {
            		cell.setCellValue("??????");
            	}else {
            		cell.setCellValue(ajaxResult.getMsg());
            	}
			}
            // ???????????????????????????
    		bussinessCacheService.initVendingNum();
    		// ????????????????????????????????????
    		bussinessCacheService.initVending();
            //?????????????????????excel???????????????
            String path="file/excel/model/"+ShiroUtils.getCorpId()+"/?????????????????????_"+DateUtils.dateTimeNow("yyyyMMddhhmmss")+".xls";
            //????????????
			File returnFile = new File(ManageConfig.getUploadPrefix()+path);
			if(returnFile.exists()) {
				returnFile.delete();
			}
			File dir = returnFile.getParentFile();
			if(!dir.exists()) {
				dir.mkdirs();
			}
            fos = new FileOutputStream(returnFile);
            workbook.write(fos);
            fos.close();
            return AjaxResult.success(path);
		} catch (Exception e) {
			log.error("???????????????Excel??????,??????:"+DateUtils.getTime(),e);
			if(fos!=null) {
				try {
					fos.close();
				} catch (IOException exception) {
					log.error("???????????????Excel??????,??????????????????,??????:"+DateUtils.getTime(),exception);
				}
			}
			return error("??????Excel????????????????????????????????????");
		}
	}
	
	/**
	 * ???????????????
	 */
	@Log(title = "???????????????", action = BusinessType.EXPORT)
	@PostMapping("/exportExcel")
	@ResponseBody
	public AjaxResult exportExcel(@RequestBody Vending vending) {
		try {
			ExcelUtil<Vending> util = new ExcelUtil<Vending>(Vending.class);
			vending.setCorpId(SystemUtil.getCorpId());
			List<Vending> list = vendingService.selectVendingList(vending);
			for (Vending site : list) {
				site.setLineName(SystemUtil.getVendingLineNameFromCache(site.getLineId()));
				site.setPointName(SystemUtil.getVendingPointNameFromCache(site.getPointId()));
				site.setFactoryName(SystemUtil.parseFactoryId(site.getFactoryId()));
				site.setCabinetTypeName(SystemUtil.parseCabinetType(site.getCabinetType()));
				site.setDeviceCode(SystemUtil.getDeviceCode(site.getDeviceId()));
				site.setNetWorkName(site.getNetWorkName());
				site.setSellStateName(site.getSellStateName());
				String mediaType = site.getMediaType();
				String mediaTypeName = "";
				if("1".equals(mediaType)) {
					mediaTypeName="??????";
				}
				if("2".equals(mediaType)) {
					mediaTypeName="?????????";
				}
				site.setMediaTypeName(mediaTypeName);
				String payTypes = site.getPayType();
				if(StringUtils.isNotEmpty(payTypes)) {
					String[] split = payTypes.split(",");
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < split.length; i++) {
						String payType = split[i];
						if(StringUtils.isNotEmpty(payType)) {
							sb.append(SystemUtil.parsePayType(payType)).append(",");
						}
					}
					sb.deleteCharAt(sb.length()-1);
					site.setPayTypeName(sb.toString());
				}
			}
            return util.exportExcel(list, "???????????????");
		} catch (Exception e) {
			log.error("???????????????????????????,??????:"+DateUtils.getTime(),e);
			return error("??????Excel????????????????????????????????????");
		}
	}
	
	/**
	 * ????????????
	 */
	@Log(title = "?????????????????????", action = BusinessType.EXPORT)
	@PostMapping("/downLoadExcelModel")
	@ResponseBody
	public AjaxResult downLoadExcelModel() {
		FileInputStream inStream = null;
		FileOutputStream fos = null;
		try {
			String excelModelPath = SystemUtil.getExcelModelPath("vending");		
            inStream = new FileInputStream(new File(ManageConfig.getUploadPrefix()+excelModelPath));
            HSSFWorkbook workbook = (HSSFWorkbook)WorkbookFactory.create(inStream);
			if(inStream!=null){
                inStream.close();
            }
            HSSFSheet sheet = workbook.getSheetAt(0);        
            //???????????????????????????????????????
            VendingPoint vendingPoint = new VendingPoint();
            vendingPoint.setCorpId(SystemUtil.getCorpId());
            List<VendingPoint> vendingPointList = vendingPointService.selectNotBindingVendingPointList(vendingPoint);
            String[] pointList = new String[1000];
            for (int i = 0; i < vendingPointList.size(); i++) {
            	VendingPoint point = vendingPointList.get(i);
            	pointList[i]=point.getName()+"--"+point.getPointId();
			}
            //?????????????????????
            ExcelUtil.addDropDownList(workbook, sheet, pointList, 1, Constant.DROP_DOWN_LIST_ROWS, 5);
			//????????????????????????
            createCascadeCell(workbook, sheet, 1, 2);
            //??????????????????
    		DictData dictData = new DictData();
        	dictData.setDictType("sys_net_work");
            List<DictData> networkList = dictDataService.selectDictDataList(dictData);
            String[] networkArr = new String[networkList.size()];
            for (int i = 0; i < networkList.size(); i++) {
            	DictData network = networkList.get(i);
            	networkArr[i]=network.getDictLabel()+"--"+network.getDictValue();
			}
            ExcelUtil.addDropDownList(workbook, sheet, networkArr, 1, Constant.DROP_DOWN_LIST_ROWS, 6);
            //??????????????????
        	dictData.setDictType("sys_sell_state");
            List<DictData> sellStateList = dictDataService.selectDictDataList(dictData);
            String[] sellStateArr = new String[sellStateList.size()];
            for (int i = 0; i < sellStateList.size(); i++) {
            	DictData sellState = sellStateList.get(i);
            	sellStateArr[i]=sellState.getDictLabel()+"--"+sellState.getDictValue();
			}
            ExcelUtil.addDropDownList(workbook, sheet, sellStateArr, 1, Constant.DROP_DOWN_LIST_ROWS, 7);
            //??????????????????????????????
            String[] mediaTypeArr =new String[] {"??????--1","?????????--2"};
            ExcelUtil.addDropDownList(workbook, sheet, mediaTypeArr, 1, Constant.DROP_DOWN_LIST_ROWS, 9);
			//????????????
			File file = new File(ManageConfig.getExcelPath()+"model/"+ShiroUtils.getCorpId()+"/???????????????_"+DateUtils.dateTimeNow("yyyyMMddhhmmss")+".xls");
			if(file.exists()) {
				file.delete();
			}
			File dir = file.getParentFile();
			if(!dir.exists()) {
				dir.mkdirs();
			}
			fos=new FileOutputStream(file);
			workbook.write(fos);
			fos.close();
            return AjaxResult.success("file/excel/model/"+ShiroUtils.getCorpId()+"/???????????????_"+DateUtils.dateTimeNow("yyyyMMddhhmmss")+".xls");
		} catch (Exception e) {
			log.error("???????????????????????????,??????:"+DateUtils.getTime(),e);
			 try {
	            	if(inStream!=null){
	                    inStream.close();
	                }
	                inStream=null;
	                if(fos!=null){
	                	fos.close();
	                }
	                fos=null;
	            } catch (Exception exception) {                
	                log.error("?????????????????????,?????????????????????,??????:"+DateUtils.getTime(),exception);
	            }
			return error("??????Excel????????????????????????????????????");
		}
	}
	
	private void createCascadeCell(HSSFWorkbook workbook,HSSFSheet sheet,int startRow,int startColumn) throws Exception {
		
		//????????????????????????
		DictData dictData = new DictData();
    	dictData.setDictType("sys_factory");
        List<DictData> factoryList = dictDataService.selectDictDataList(dictData);
				
		Sheet hideSheet = workbook.createSheet("site_sheet");
		workbook.setSheetHidden(workbook.getSheetIndex("site_sheet"), true);
		
		int rowId = 0;
		// ????????????????????????????????????
		Row factoryRow = hideSheet.createRow(rowId++);
		factoryRow.createCell(0).setCellValue("????????????");
		String[] factoryArr = new String[factoryList.size()];
		Map<String, DictData> factoryMap = new HashMap<String,DictData>();
		Map<String, CabinetTypeSelectVo> cabinetMap = new HashMap<String,CabinetTypeSelectVo>();
		for (int i = 0; i < factoryList.size(); i++) {
			Cell factoryCell = factoryRow.createCell(i + 1);
			DictData factory = factoryList.get(i);
			factoryCell.setCellValue(factory.getDictLabel());
			factoryArr[i]=factory.getDictLabel();
			//?????????????????????map
			factoryMap.put(factory.getDictValue(), factory);
		}
		//?????????????????????
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		//??????????????????????????????
		List<CabinetTypeSelectVo> cabinetTypeList = vendingService.selectCabinetTypeByFactoryId("");
		for (CabinetTypeSelectVo cabinetTypeSelectVo : cabinetTypeList) {
			DictData factory = factoryMap.get(cabinetTypeSelectVo.getFactoryId());
			//???????????????????????????map
			cabinetMap.put(cabinetTypeSelectVo.getCabinetType(), cabinetTypeSelectVo);
			if(factory==null) {
				continue;
			}else {
				List<String> cabinetList = map.get(factory.getDictLabel());
				if(cabinetList==null) {
					cabinetList = new ArrayList<String>();
				}
				cabinetList.add(cabinetTypeSelectVo.getCabinetTypeExcel());
				map.put(factory.getDictLabel(), cabinetList);
			}
		}
		//????????????????????????
		List<VendingModel> vendingModelList = vendingService.selectDeviceByCabinetType("",SystemUtil.getCorpId());
		for (VendingModel vendingModel : vendingModelList) {
			CabinetTypeSelectVo cabinetTypeSelectVo = cabinetMap.get(vendingModel.getCabinetType());
			if(cabinetTypeSelectVo==null) {
				continue;
			}else {
				List<String> modelList = map.get(cabinetTypeSelectVo.getCabinetTypeExcel());
				if(modelList==null) {
					modelList = new ArrayList<String>();
				}
				modelList.add(vendingModel.getDeviceId()+"--"+vendingModel.getDeviceCode());
				map.put(cabinetTypeSelectVo.getCabinetTypeExcel(), modelList);
			}
		}
		// ??????????????????????????????????????????????????????????????????????????????????????????
		Iterator<String> keyIterator = map.keySet().iterator();
		while (keyIterator.hasNext()) {
			String key = keyIterator.next();
			List<String> son = map.get(key);
			
			Row row = hideSheet.createRow(rowId++);
			row.createCell(0).setCellValue(key);
			for (int i = 0; i < son.size(); i++) {
				Cell cell = row.createCell(i + 1);
				cell.setCellValue(son.get(i));
			}
			
			// ?????????????????????
			String range = ExcelUtil.getRange(1, rowId, son.size());
			Name name = workbook.createName();
			name.setNameName(key);
			String formula = "site_sheet!" + range;
			name.setRefersToFormula(formula);
		}

		// ????????????
		DVConstraint provConstraint = DVConstraint.createExplicitListConstraint(factoryArr);
		CellRangeAddressList provRangeAddressList = new CellRangeAddressList(1, Constant.DROP_DOWN_LIST_ROWS, 2, 2);
		DataValidation provinceDataValidation = new HSSFDataValidation(provRangeAddressList, provConstraint);
		provinceDataValidation.createErrorBox("error", "????????????????????????");
		sheet.addValidationData(provinceDataValidation);
		
		for (int i = 1; i <= Constant.DROP_DOWN_LIST_ROWS; i++) {
		    ExcelUtil.setDataValidation(sheet,"C",i,3);
		    ExcelUtil.setDataValidation(sheet,"D",i,4);
		}
	}
	
	/**
	 * ?????????????????????
	 * 
	 * @param vendingLanep
	 * @return
	 */
	@Log(title = "?????????????????????", action = BusinessType.UPDATE)
	@PostMapping("/lockLane")
	@ResponseBody
	public AjaxResult lockLane(@RequestBody VendingLanep vendingLanep) {
		try {
			return vendingService.lockLane(vendingLanep);
		}catch (Exception e) {
			log.error("??????????????????,??????:"+DateUtils.getTime(),e);
			return AjaxResult.error("??????????????????");
		}
	}
	
	/**
	 * ?????????????????????
	 * 
	 * @param vendingLanep
	 * @return
	 */
	@Log(title = "?????????????????????", action = BusinessType.UPDATE)
	@PostMapping("/unlockLane")
	@ResponseBody
	public AjaxResult unlockLane(@RequestBody VendingLanep vendingLanep) {
		try {
			return vendingService.unlockLane(vendingLanep);
		}catch (Exception e) {
			log.error("??????????????????,??????:"+DateUtils.getTime(),e);
			return AjaxResult.error("??????????????????");
		}
	}
	
	
}
