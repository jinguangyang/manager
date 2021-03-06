package com.manage.project.system.statement.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.manage.common.utils.DateUtils;
import com.manage.common.utils.StringUtils;
import com.manage.common.utils.excel.ExcelData;
import com.manage.common.utils.excel.ExportExcelUtils;
import com.manage.common.utils.poi.ExcelUtil;
import com.manage.common.utils.security.ShiroUtils;
import com.manage.framework.aspectj.lang.annotation.Log;
import com.manage.framework.aspectj.lang.constant.BusinessType;
import com.manage.framework.web.controller.BaseController;
import com.manage.project.common.Constant;
import com.manage.project.system.report.vo.TotalVo;
import com.manage.project.system.server.domain.ServerMessage;
import com.manage.project.system.server.service.IServerService;
import com.manage.project.system.statement.domain.OrderApply;
import com.manage.project.system.statement.service.IOrderApplyService;
import com.manage.project.system.statement.vo.OrderApplyVo;
import com.manage.project.system.statement.vo.OrderBoxDownParamVo;
import com.manage.project.system.statement.vo.OrderBoxExcelVo;
import com.manage.project.system.statement.vo.OrderBoxVo;
import com.manage.project.system.statement.vo.OrderApplyDetailVo;
import com.manage.project.system.statement.vo.OrderProductVo;
import com.manage.project.system.statement.vo.OrderVo;
import com.manage.project.system.supply.domain.SupplyProduct;
import com.manage.project.system.util.SystemUtil;
import com.manage.project.system.vending.domain.VendingLanep;
import com.manage.project.system.vending.service.IVendingLanepService;
import com.manage.framework.web.domain.AjaxResult;
import com.manage.framework.web.page.TableDataInfo;

/**
 * ??????????????????????????? ??????????????????
 *
 * @author ?????????
 * @date 2018-10-17
 */
@Controller
@RequestMapping("/system/orderApply")
public class OrderApplyController extends BaseController{

	private static final Logger log = LoggerFactory.getLogger(OrderApplyController.class);

	@Autowired
	private IOrderApplyService orderApplyService;

	@Autowired
	private IServerService serverService;
	
	@Autowired
	private IVendingLanepService vendingLanepService;

	/**
	 * ??????????????????
	 */
	@GetMapping("/list")
	@ResponseBody
	public AjaxResult list(OrderVo vo){
		if (SystemUtil.isZhilai()) {
			vo.setCorpId("");
		} else {
			vo.setCorpId(ShiroUtils.getUser().getCorpId());
		}
		//??????????????????,?????????????????????
		if(StringUtils.isEmpty(vo.getStartDate())&&StringUtils.isEmpty(vo.getEndDate())) {
			vo.setStartDate(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, (DateUtils.getPastDate(-1))));
		}
		startPage();
		List<OrderVo> list = orderApplyService.selectOrderApplyList(vo);
		TableDataInfo dataTable = getDataTable(list);
		for (OrderVo orderVo : list) {
			String pointId = orderVo.getPointId();
			if(StringUtils.isNotEmpty(pointId)) {
				String pointName = SystemUtil.getVendingPointNameFromCache(pointId);
				orderVo.setPointName(pointName);
			}
			orderVo.setPayTypeName(SystemUtil.parsePayType(orderVo.getPayType()));
			orderVo.setPayStateName(SystemUtil.parsePayState(orderVo.getPayState()));
			orderVo.setReturnTypeName(SystemUtil.parseReturnType(orderVo.getReturnType()));
			orderVo.setCurStateName(SystemUtil.parseOrderCurState(orderVo.getCurState()));
			if(!Constant.PAYSTATESUCCESS.equals(orderVo.getPayState())) {
				orderVo.setProfitMoney(0F);
			}
		}
		return AjaxResult.success(dataTable);
	}

	/**
	 * ??????????????????
	 */
	@GetMapping("/selectOrderList")
	@ResponseBody
	public AjaxResult selectOrderList(OrderVo vo){
		if (SystemUtil.isZhilai()) {
			vo.setCorpId("");
		} else {
			vo.setCorpId(ShiroUtils.getUser().getCorpId());
		}
		startPage();
		List<OrderVo> list = orderApplyService.selectOrderList(vo);
		TableDataInfo dataTable = getDataTable(list);
		for (OrderVo orderVo : list) {
			String pointId = orderVo.getPointId();
			if(StringUtils.isNotEmpty(pointId)) {
				String pointName = SystemUtil.getVendingPointNameFromCache(pointId);
				orderVo.setPointName(pointName);
			}
			orderVo.setPayTypeName(SystemUtil.parsePayType(orderVo.getPayType()));
			orderVo.setPayStateName(SystemUtil.parsePayState(orderVo.getPayState()));
			orderVo.setReturnTypeName(SystemUtil.parseReturnType(orderVo.getReturnType()));
			orderVo.setCurStateName(SystemUtil.parseOrderCurState(orderVo.getCurState()));
			if(!Constant.PAYSTATESUCCESS.equals(orderVo.getPayState())) {
				orderVo.setProfitMoney(0F);
			}
		}
		return AjaxResult.success(dataTable);
	}


	/**
	 * ??????????????????
	 */
	@GetMapping("/detail")
	@ResponseBody
	public AjaxResult detail(OrderApply orderApply){
		OrderApply orderApplyR=this.orderApplyService.selectOrderByOrderId(orderApply.getOrderId());
		if(orderApplyR==null){
			return AjaxResult.error("?????????");
		}
		if (!"2".equals(orderApplyR.getPayState())) {	// ?????????????????????????????????0
			orderApplyR.setProfitMoney(0f);
		}
		Map<String,String> productIdMap=new HashMap<String,String>();

//		OrderVo orderVoP=new OrderVo();
//		orderVoP.setOrderId(orderApplyR.getOrderId());
		List<OrderProductVo> orderProductVoList=orderApplyService.selectProductList(orderApply.getOrderId());
		List<OrderBoxVo> orderBoxVoList=orderApplyService.selectBoxList(orderApply.getOrderId());
		for (OrderBoxVo orderBoxVo : orderBoxVoList) {
			orderBoxVo.setOutState(SystemUtil.parseBoxState(orderBoxVo.getOutState()));
		}
		List<OrderApplyVo> orderApplyVolist=new ArrayList<>();
		OrderApplyVo orderApplyVo=new OrderApplyVo();
		orderApplyVo.setOrderId(orderApplyR.getOrderId());
		orderApplyVo.setSiteId(orderApplyR.getSiteId());
		orderApplyVo.setSiteName(orderApplyR.getSiteName());
		orderApplyVo.setpNum(orderApplyR.getPNum());
		orderApplyVo.setSalePrice(orderApplyR.getSalePrice());
		orderApplyVo.setPayPrice(orderApplyR.getPayPrice());
		orderApplyVo.setFavPrice(orderApplyR.getFavPrice());
		orderApplyVo.setProfitMoney(orderApplyR.getProfitMoney());
		orderApplyVo.setReturnType(SystemUtil.parseReturnType(orderApplyR.getReturnType()));
		orderApplyVo.setReturnMoney(orderApplyR.getReturnMoney());
		orderApplyVo.setPayType(SystemUtil.parsePayType(orderApplyR.getPayType()));
		orderApplyVo.setPayState(SystemUtil.parsePayState(orderApplyR.getPayState()));
		orderApplyVo.setCreateTime(orderApplyR.getCreateTime());
		orderApplyVo.setpTradeNo(orderApplyR.getPTradeNo());
		orderApplyVo.setAbnomarlState(orderApplyR.getAbnomarlState());
		orderApplyVolist.add(orderApplyVo);
		OrderApplyDetailVo orderApplyDetailVo = new OrderApplyDetailVo();
		orderApplyDetailVo.setOrderApplyInfo(orderApplyVolist);
		orderApplyDetailVo.setOrderProductInfo(orderProductVoList);
		orderApplyDetailVo.setOrderBoxInfo(orderBoxVoList);
		return AjaxResult.success(orderApplyDetailVo);
	}

	/**
	 * ??????????????????
	 */
	@GetMapping("/getOrderPayState")
	@ResponseBody
	public AjaxResult getOrderPayState(String orderId){
		OrderVo vo = orderApplyService.selectOrderByOrderId(orderId);
		if(vo==null) {
			return AjaxResult.error("???????????????");
		}else {
			return AjaxResult.success(SystemUtil.parsePayState(vo.getPayState()));
		}
	}

	/**
	 * ??????
	 */
/*	@PostMapping("/returnMoney")
	@ResponseBody
	public AjaxResult returnMoney(@RequestBody OrderApply order){
		//??????????????????
		Map<String, String> zhead = new HashMap<String,String>();
		zhead.put("BCode", "01");
		zhead.put("TCode", "1004");
		zhead.put("Version", "01");
		Map<String, String> zbody = new HashMap<String,String>();
		zbody.put("OrderId", order.getOrderId());
		Map<String, Map<String, String>> zmsg = new HashMap<String,Map<String, String>>();
		zmsg.put("ZHEAD", zhead);
		zmsg.put("ZBODY", zbody);
		Map<String, Map> map = new HashMap<String,Map>();
		map.put("ZMSG", zmsg);
		String jsonString = JSONObject.toJSONString(map);
		HttpClient client = HttpClients.createDefault();
		try {
			//????????????
			String encodeParam = URLEncoder.encode(jsonString,"UTF-8");
			String urlStr=url+encodeParam;
			HttpGet get = new HttpGet(urlStr);
            HttpResponse response = client.execute(get);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "UTF-8");
            JSONObject resultObject = JSONObject.parseObject(result);
            JSONObject zmsgResult = resultObject.getJSONObject("ZMSG");
            JSONObject zheadResult = zmsgResult.getJSONObject("ZHEAD");
            String retCode = zheadResult.getString("RetCode");
            if("0000".equals(retCode)){
            	return success();
            }else {
            	return error("????????????");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return error("????????????");
        }
	}*/

	/**
	 * ??????
	 */
	@PostMapping("/returnMoney")
	@ResponseBody
	public AjaxResult returnMoney(@RequestBody OrderApply order){
		log.info("???????????? time:"+DateUtils.getTime());
		ServerMessage resultMessage = serverService.returnMoney1008(order.getOrderId(), ShiroUtils.getLoginName(), ShiroUtils.getUser().getUserName());
		if(resultMessage==null) {
			log.error("????????????:?????????:"+order.getOrderId()+",??????"+DateUtils.getTime());
			return error("????????????");
		}else {
			if(resultMessage.success()) {
				return success("????????????");
			}else {
				log.error("????????????:?????????:"+order.getOrderId()+","+JSONObject.toJSONString(resultMessage)+",??????"+DateUtils.getTime());
				return error("????????????");
			}
		}
	}

	/**
	 * ????????????????????????
	 */
	@GetMapping("/selectTotalSale")
	@ResponseBody
	public AjaxResult selectTotalSale(OrderVo vo){
		TotalVo totalVo = orderApplyService.selectTotalSale(vo);
		return AjaxResult.success(totalVo);
	}

	/**
	 * ????????????
	 */
	@GetMapping("/getOrderBox")
	@ResponseBody
	public AjaxResult getOrderBox(OrderApply orderApply){
		OrderApply orderApplyR=this.orderApplyService.selectOrderByOrderId(orderApply.getOrderId());
		if(orderApplyR==null){
			return AjaxResult.error("?????????");
		}
		Map<String,String> productIdMap=new HashMap<String,String>();

		OrderVo orderVoP=new OrderVo();
		orderVoP.setOrderId(orderApplyR.getOrderId());
		List<OrderVo> orderVoList=this.orderApplyService.selectOrderProductList(orderVoP);
		List<OrderBoxVo> orderBoxVoList=new ArrayList<>();
		OrderBoxVo orderBoxVo=null;
		for(OrderVo orderVoPP:orderVoList){
			orderBoxVo=new OrderBoxVo();
			orderBoxVo.setProductId(orderVoPP.getProductId());
			orderBoxVo.setProductName(orderVoPP.getProductName());
			orderBoxVo.setLaneSId(orderVoPP.getLaneSId());
			orderBoxVo.setLaneEId(orderVoPP.getLaneEId());
			orderBoxVo.setOutState(SystemUtil.parseBoxState(orderVoPP.getOutState()));
			orderBoxVoList.add(orderBoxVo);
		}
		List<OrderProductVo> orderProductVoList=new ArrayList<>();
		OrderProductVo orderProductVo=null;
		for(OrderVo orderVoPP:orderVoList){
			orderProductVo=new OrderProductVo();
			orderProductVo.setProductId(orderVoPP.getProductId());
			orderProductVo.setProductName(orderVoPP.getProductName());
			orderProductVo.setSalePrice(orderVoPP.getSalePrice());
			orderProductVo.setSaleNum(orderVoPP.getSaleNum());
			orderProductVoList.add(orderProductVo);
		}
		List<OrderApplyVo> orderApplyVolist=new ArrayList<>();
		OrderApplyVo orderApplyVo=new OrderApplyVo();
		orderApplyVo.setOrderId(orderApplyR.getOrderId());
		orderApplyVo.setSiteId(orderApplyR.getSiteId());
		orderApplyVo.setSiteName(orderApplyR.getSiteName());
		orderApplyVo.setpNum(orderApplyR.getPNum());
		orderApplyVo.setSalePrice(orderApplyR.getSalePrice());
		orderApplyVo.setPayPrice(orderApplyR.getPayPrice());
		orderApplyVo.setFavPrice(orderApplyR.getFavPrice());
		
		orderApplyVo.setProfitMoney(orderApplyR.getProfitMoney());
		orderApplyVo.setReturnType(SystemUtil.parseReturnType(orderApplyR.getReturnType()));
		orderApplyVo.setReturnMoney(orderApplyR.getReturnMoney());
		orderApplyVo.setPayType(SystemUtil.parsePayType(orderApplyR.getPayType()));
		orderApplyVo.setPayState(SystemUtil.parsePayState(orderApplyR.getPayState()));
		orderApplyVo.setCreateTime(orderApplyR.getCreateTime());
		orderApplyVolist.add(orderApplyVo);
		OrderApplyDetailVo orderApplyDetailVo = new OrderApplyDetailVo();
		orderApplyDetailVo.setOrderApplyInfo(orderApplyVolist);
		orderApplyDetailVo.setOrderProductInfo(orderProductVoList);
		orderApplyDetailVo.setOrderBoxInfo(orderBoxVoList);
		return AjaxResult.success(orderApplyDetailVo);
	}
	
	/**
	 * ????????????
	 */
	@PostMapping("/export")
	@Log(title="???????????????",action=BusinessType.EXPORT)
	@ResponseBody
	public AjaxResult export(@RequestBody OrderBoxDownParamVo param){
		try {
			if(StringUtils.isEmpty(param.getStartDate())) {
				return AjaxResult.error("????????????????????????");
			}
			if(StringUtils.isEmpty(param.getEndDate())) {
				return AjaxResult.error("????????????????????????");
			}
			param.setCorpId(SystemUtil.getCorpId());
			ExcelUtil<OrderBoxExcelVo> util = new ExcelUtil<OrderBoxExcelVo>(OrderBoxExcelVo.class);
			//??????????????????
			List<OrderBoxExcelVo> list = orderApplyService.selectOrderBoxExcelList(param);
			for (OrderBoxExcelVo orderBoxExcelVo : list) {
				orderBoxExcelVo.setPayType(SystemUtil.parsePayType(orderBoxExcelVo.getPayType()));
				orderBoxExcelVo.setPayState(SystemUtil.parsePayState(orderBoxExcelVo.getPayState()));
				orderBoxExcelVo.setOutState(SystemUtil.parseBoxState(orderBoxExcelVo.getOutState()));
				orderBoxExcelVo.setReturnType(SystemUtil.parseReturnType(orderBoxExcelVo.getReturnType()));
				orderBoxExcelVo.setSysState(orderBoxExcelVo.getSysStateName());
			}
            return util.exportExcel(list, "Sheet1");
			
		} catch (Exception e) {
			log.error("??????????????????:"+DateUtils.getTime(),e);
			return error("??????Excel????????????????????????????????????");
		}
		
		
	}
	
	/**
	 * ????????????
	 * 
	 */
	@PostMapping("/reOrderApply")
	@Log(title="????????????",action=BusinessType.INSERT)
	@ResponseBody
	public AjaxResult reOrderApply(@RequestBody VendingLanep vendinglanep){
		try {
			String siteId = vendinglanep.getSiteId();
			Integer laneSId = vendinglanep.getLaneSId();
			Integer laneEId = vendinglanep.getLaneEId();
			String productId = vendinglanep.getProductId();
			Integer outNum = vendinglanep.getOutNum();
			VendingLanep vendingLanepSelect = new VendingLanep();
			vendingLanepSelect.setSiteId(siteId);
			vendingLanepSelect.setLaneSId(laneSId);
			vendingLanepSelect.setLaneEId(laneEId);
			List<VendingLanep> lanepList = vendingLanepService.selectVendingLanepList(vendingLanepSelect);
			if(StringUtils.isEmpty(lanepList)) {
				return AjaxResult.error("???????????????");
			}
			VendingLanep lanep = lanepList.get(0);
			String laneSate = lanep.getLaneSate();
			if(!Constant.VENDING_LANESTATE_NORMAL.equals(laneSate)) {
				return AjaxResult.error("???????????????????????????????????????");
			}
			Integer curCap = lanep.getCurCap();
			if(outNum==null) {
				return AjaxResult.error("????????????????????????");
			}
			if(outNum<=0) {
				return AjaxResult.error("????????????????????????0");
			}
			if(curCap==null||curCap<outNum) {
				return AjaxResult.error("????????????????????????????????????");
			}
			//????????????
			Map<String, Object> orderMap = new HashMap<String,Object>();
			orderMap.put("SiteId", siteId);
			orderMap.put("LoginId", ShiroUtils.getLoginName());
			orderMap.put("LoginName", ShiroUtils.getLoginName());
			orderMap.put("SaleChannel", "01");
			orderMap.put("ApplyTime", DateUtils.getTime());
			orderMap.put("PayType", "99");
			List<Map<String, String>> productList = new ArrayList<Map<String,String>>();
			Map<String, String> product = new HashMap<String,String>();
			product.put("ProductId", productId);
			product.put("LaneSId", laneSId.toString());
			product.put("LaneEId", laneEId.toString());
			product.put("Num", outNum.toString());
			productList.add(product);
			orderMap.put("ProductInfo", productList);
//			map.put("WechatPayInfo", map.get(""));
			ServerMessage serverMessage = serverService.orderApply1011(orderMap);
			if(serverMessage.success()) {
				//????????????
				String orderId = (String)serverMessage.getZbody("OrderId");
				Map<String, Object> payMap = new HashMap<String,Object>();
				payMap.put("TradeNo", orderId);
				payMap.put("FeeType", "01");
				payMap.put("Price", ":0");
				payMap.put("OutTradeNo", "");
				payMap.put("TradeType", "01");
				payMap.put("PayState", "1");
				payMap.put("GenTime", DateUtils.getTime());
				ServerMessage message = serverService.noticeOrderPay2502(payMap);
				if(message.success()) {
					return AjaxResult.success();
				}else {
					log.error("????????????2502??????,????????????:"+message.toJson()+",??????:"+DateUtils.getTime());
					return AjaxResult.error("??????????????????");
				}
			}else {
				log.error("????????????1011??????,????????????:"+serverMessage.toJson()+",??????:"+DateUtils.getTime());
				return AjaxResult.error("??????????????????");
			}
		} catch (Exception e) {
			log.error("??????????????????,??????:"+DateUtils.getTime(),e);
			return AjaxResult.error("??????????????????");
		}
		
		
	}

}
