package com.manage.project.system.supply.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.manage.common.utils.security.ShiroUtils;
import com.manage.framework.aspectj.lang.annotation.Log;
import com.manage.framework.aspectj.lang.constant.BusinessType;
import com.manage.framework.web.controller.BaseController;
import com.manage.framework.web.domain.AjaxResult;
import com.manage.project.common.Constant;
import com.manage.project.system.base.domain.User;
import com.manage.project.system.base.service.IUserService;
import com.manage.project.system.stock.domain.StockInfo;
import com.manage.project.system.stock.service.IStockPinboundService;
import com.manage.project.system.supply.domain.SupplyOrder;
import com.manage.project.system.supply.domain.SupplyProduct;
import com.manage.project.system.supply.service.ISupplyOrderService;
import com.manage.project.system.supply.service.ISupplyProductService;
import com.manage.project.system.supply.vo.SupplyOutDetailVo;
import com.manage.project.system.supply.vo.SupplyOutListVo;
import com.manage.project.system.supply.vo.SupplyOutVo;
import com.manage.project.system.supply.vo.SupplyProductUpdateVo;
import com.manage.project.system.util.SystemUtil;

@Controller
@RequestMapping("/system/supply/supplyOut")
public class SupplyOutController extends BaseController{
	
	private static final Logger log = LoggerFactory.getLogger(SupplyOutController.class);

	private String prefix = "module/supplyOut";
	@Autowired
	private ISupplyOrderService supplyOrderService;
	@Autowired
	private ISupplyProductService supplyProductService;
	@Autowired
	private IStockPinboundService stockPinboundService;
	@Autowired
	private IUserService userService;
	
	@RequiresPermissions("supply:supplyOut:view")
	@GetMapping()
	public String supplyOut()
	{
	    return prefix + "/supplyOut";
	}
	
	/*
	 * ??????????????????
	 */
	@GetMapping("/list")
	@ResponseBody
	public AjaxResult list(SupplyOrder supplyOrder){
		startPage();
		if (SystemUtil.isZhilai()) {
			supplyOrder.setCorpId("");
		} else {
			supplyOrder.setCorpId(ShiroUtils.getUser().getCorpId());
		}
		List<SupplyOrder> list = supplyOrderService.selectSupplyOrderList(supplyOrder);
        List<SupplyOutListVo> supplyOutListVoList=new ArrayList<>();
		SupplyOutListVo supplyOutListVo=null;
		for(SupplyOrder supplyOrderR:list){
			supplyOutListVo=new SupplyOutListVo();
			supplyOutListVo.setWmId(supplyOrderR.getWmId());
			StockInfo StockInfo = SystemUtil.getStockInfo(supplyOrderR.getWmId());
			if(StockInfo != null){
				supplyOutListVo.setWmName(StockInfo.getStockName());
			}else {
				supplyOutListVo.setWmName("");
			}
			User user = SystemUtil.getUserByLoginId(supplyOrder.getCorpId(), supplyOrderR.getSupplierId());
			if(user != null){
				supplyOutListVo.setSupplierName(user.getUserName());
			}else {
				supplyOutListVo.setSupplierName("");
			}
			
			supplyOutListVo.setLineId(supplyOrderR.getLineId());
			supplyOutListVo.setCreateTime(supplyOrderR.getCreateTime());
			supplyOutListVo.setsOrderId(supplyOrderR.getSOrderId());
			float totalPrice = 0;
			int outNum = 0;
			List<SupplyProduct> supplyProductList = supplyProductService.selectListBySOrderId(supplyOrderR.getSOrderId());
			for(SupplyProduct supplyProductR : supplyProductList){
				int productOutNum=0;
				if(supplyProductR.getStockOutNum()==null || "".equals(supplyProductR.getStockOutNum())){
					//supplyOutListVo.setOutNum(0);
					productOutNum=0;
				}else{
					//supplyOutListVo.setOutNum(supplyProductR.getOutNum());
					productOutNum=supplyProductR.getStockOutNum();
				}
				//outNum = supplyOutListVo.getOutNum() + outNum;
				//totalPrice = supplyProductR.getBuyPrice() * supplyOutListVo.getOutNum() + totalPrice;
				outNum += productOutNum;
				totalPrice += supplyProductR.getBuyPrice() * productOutNum;
			}
			BigDecimal totalPriceDecimal = new BigDecimal(totalPrice);
			supplyOutListVo.setOutNum(outNum);
			supplyOutListVo.setTotalPrice(totalPriceDecimal.setScale(2,BigDecimal.ROUND_HALF_UP).floatValue());
			supplyOutListVo.setStockState(SystemUtil.parseStockState(supplyOrderR.getStockState()));
			supplyOutListVoList.add(supplyOutListVo);
		}
		return AjaxResult.success(getDataTable(supplyOutListVoList));
	}
	
	/*
	 * ????????????????????????
	 */
	@GetMapping("/detail")
	@ResponseBody
	public AjaxResult detail(SupplyOrder supplyOrder){
		//?????????????????????
		SupplyOrder supplyOrderR=this.supplyOrderService.selectSupplyOrderById(supplyOrder.getSOrderId());
		if(supplyOrderR==null){
			return AjaxResult.error("?????????");
		}
//		if("1".equals(supplyOrderR.getCurState())) {
//			SupplyConfig supplyConfig = supplyConfigService.selectSupplyConfigBySupplyId(supplyOrderR.getSupplyId());			
//			try {
//				supplyOrderService.getLineThreadByLineId(supplyConfig.getLineId()).createSupplyOrderBySupplyConfig(supplyConfig, null, true);
//			}catch (Exception e) {
//				log.error("???????????????????????????:????????????"+supplyOrderR.getsOrderId()+"??????"+DateUtils.getTime(),e);
//			}
//			
//		}

//		Map<String,String> productIdMap=new HashMap<String,String>();
		//???????????????????????????
		SupplyProduct supplyProductP=new SupplyProduct();
		supplyProductP.setSOrderId(supplyOrderR.getSOrderId());
        List<SupplyProduct> supplyProductList=this.supplyProductService.selectSupplyProductList(supplyProductP);
        //???????????????????????????
  		List<SupplyOutVo> supplyOutVoList=new ArrayList<>();
  		SupplyOutVo supplyOutVo=null;
  		for(SupplyProduct supplyProductPP:supplyProductList){
  			supplyOutVo=new SupplyOutVo();
  			supplyOutVo.setProductId(supplyProductPP.getProductId());
  			supplyOutVo.setProductName(SystemUtil.getProductNameById(supplyProductPP.getProductId())); 			
  			//??????????????????????????????????????????
//  			if(Constant.supplyOrderCur_wait.equals(supplyOrderR.getCurState())) {
//  				supplyOutVo.setOutNum(supplyProductPP.getSupplyNum());
//  			}else {
//  				supplyOutVo.setOutNum(supplyProductPP.getStockOutNum());
//  			}
  			supplyOutVo.setOutNum(supplyProductPP.getStockOutNum());
  			//???????????????,???????????????????????????
  			if(Constant.supplyConfigCheck_noCheck.equals(supplyOrderR.getStockState())) {
  				Float buyPrice = stockPinboundService.selectBuyPriceRecentlyByProductId(supplyProductPP.getProductId());
  				supplyOutVo.setBuyPrice(buyPrice);
  			}else {
  				supplyOutVo.setBuyPrice(supplyProductPP.getBuyPrice());
  			}
  			if(supplyProductPP.getStockOutNum()==null) {
  				supplyProductPP.setStockOutNum(0);
  			}
  			BigDecimal priceDecimal = new BigDecimal(supplyProductPP.getStockOutNum() * supplyProductPP.getBuyPrice());
  			supplyOutVo.setTotalPrice(priceDecimal.setScale(2,BigDecimal.ROUND_HALF_UP).floatValue());
  			supplyOutVo.setSupplyrNum(supplyProductPP.getSupplyrNum());
  			supplyOutVo.setSupplynNum(supplyProductPP.getSupplynNum());
  			supplyOutVo.setSupplydNum(supplyProductPP.getSupplydNum());
  			supplyOutVo.setSupplylNum(supplyProductPP.getSupplylNum());
  			supplyOutVoList.add(supplyOutVo);
  		}
  		
  		
  		List<SupplyOutListVo> supplyOutListVolist=new ArrayList<>();
  		SupplyOutListVo supplyOutListVo=new SupplyOutListVo();
  		supplyOutListVo.setSupplydType(supplyOrderR.getSupplydType());
		supplyOutListVo.setsOrderId(supplyOrderR.getSOrderId());
  		supplyOutListVo.setLineId(supplyOrderR.getLineId());
  		supplyOutListVo.setWmId(supplyOrderR.getWmId());
  		
  		supplyOutListVo.setSupplyrNum(supplyOrderR.getSupplyrNum());
  		supplyOutListVo.setSupplynNum(supplyOrderR.getSupplynNum());
  		supplyOutListVo.setSupplydNum(supplyOrderR.getSupplydNum());
  		supplyOutListVo.setSupplylNum(supplyOrderR.getSupplylNum());
  		StockInfo StockInfo = SystemUtil.getStockInfo(supplyOrderR.getWmId());
  		if(StockInfo == null){
			return AjaxResult.error("???????????????");
		}
		supplyOutListVo.setWmName(StockInfo.getStockName());
  		supplyOutListVo.setStockState(SystemUtil.parseStockState(supplyOrderR.getStockState()));
		User user = userService.selectUserByLoginName(supplyOrderR.getSupplierId());
  		if(user == null){
			return AjaxResult.error("??????????????????");
		}
		supplyOutListVo.setSupplierName(user.getUserName());
		float totalPrice = 0;
		int outNum = 0;
  		for(SupplyProduct supplyProductPP:supplyProductList){
  			/*outNum = supplyProductPP.getOutNum() + outNum;
  			totalPrice = supplyProductPP.getBuyPrice() * supplyProductPP.getOutNum() + totalPrice;		*/
  			int productOutNum=0;
			if(supplyProductPP.getStockOutNum()==null || "".equals(supplyProductPP.getStockOutNum())){
				productOutNum=0;
			}else{
				productOutNum=supplyProductPP.getStockOutNum();
			}
			outNum += productOutNum;
			totalPrice += supplyProductPP.getBuyPrice() * productOutNum;
  		}
  		supplyOutListVo.setOutNum(outNum);
  		BigDecimal totalPriceDecimal = new BigDecimal(totalPrice);
  		supplyOutListVo.setTotalPrice(totalPriceDecimal.setScale(2,BigDecimal.ROUND_HALF_UP).floatValue());
  		supplyOutListVo.setCreateTime(supplyOrderR.getCreateTime());
  		supplyOutListVolist.add(supplyOutListVo);
  		
  		SupplyOutDetailVo supplyOutDetailVo = new SupplyOutDetailVo();
  		supplyOutDetailVo.setSupplyOutProductInfo(supplyOutVoList);
  		supplyOutDetailVo.setSupplyOut(supplyOutListVolist);
  		return AjaxResult.success(supplyOutDetailVo);
	}
	
	@Log(title="??????????????????", action = BusinessType.UPDATE)
	@PostMapping("/edit")
	@ResponseBody
	public AjaxResult editSave(@RequestBody SupplyProductUpdateVo supplyProductUpdateVo){
		SupplyOrder supplyOrder = supplyOrderService.selectSupplyOrderById(supplyProductUpdateVo.getsOrderId());
		if(supplyOrder==null) {
			return AjaxResult.error("??????????????????");
		}
		if("2".equals(supplyOrder.getStockState())) {
			return AjaxResult.error("?????????????????????");
		}
		int flag=0;
		try {
			flag = supplyProductService.updateSupplyProduct(supplyProductUpdateVo);
		} catch (Exception e) {
			e.printStackTrace();
			return AjaxResult.error("????????????");
		}
		switch (flag) {
			case 1:
				return toAjax(flag);
			case 2:
				return AjaxResult.error("????????????????????????????????????");
			case 3:
				return AjaxResult.error("????????????????????????????????????");
			case 4:
				return AjaxResult.error("???????????????????????????");
			default:
				return AjaxResult.error("????????????");
		}
		
	}
	
	@Log(title="????????????", action = BusinessType.UPDATE)
	@PostMapping("/repeatInbound")
	@ResponseBody
	public AjaxResult repeatInbound(@RequestBody SupplyProductUpdateVo supplyProductUpdateVo){
		int flag=0;
		try {
			flag = supplyProductService.repeatInbound(supplyProductUpdateVo);
		} catch (Exception e) {
			e.printStackTrace();
			return AjaxResult.error("????????????");
		}
		switch (flag) {
			case 1:
				return toAjax(flag);
			case 2:
				return AjaxResult.error("??????????????????");
			case 3:
				return AjaxResult.error("?????????????????????????????????");
			case 4:
				return AjaxResult.error("??????????????????????????????????????????????????????????????????????????????????????????????????????");
			default:
				return AjaxResult.error("????????????");
		}
		
	}
	
	/**
	 * ????????????????????????
	 * @return
	 */
	@GetMapping("/listAll")
	@ResponseBody
	public AjaxResult listAll(SupplyProduct supplyProduct)
	{
//		SupplyProduct supplyProduct = new SupplyProduct();
		supplyProduct.setCorpId(SystemUtil.getCorpId());
        List<Float> list = supplyProductService.selectSupplyProductPriceList(supplyProduct);
        for (int i = 0; i < list.size(); i++) {
			if(list.get(i)==null) {
				list.set(i, 0F);
			}
		}
		return AjaxResult.success(getDataTable(list));
	}
	
	/**
	 * ????????????
	 * 
	 * @param supplyProductUpdateVo
	 * @return
	 */
	@Log(title="????????????", action = BusinessType.UPDATE)
	@PostMapping("/extraOut")
	@ResponseBody
	public AjaxResult extraOut(@RequestBody SupplyProductUpdateVo supplyProductUpdateVo){
		SupplyOrder supplyOrder = supplyOrderService.selectSupplyOrderById(supplyProductUpdateVo.getsOrderId());
		if(supplyOrder==null) {
			return AjaxResult.error("??????????????????");
		}
		if(Constant.STOCK_STATE_WAIT.equals(supplyOrder.getStockState())) {
			return AjaxResult.error("?????????????????????????????????????????????");
		}
		int flag=0;
		try {
			flag = supplyProductService.extraOut(supplyProductUpdateVo);
		} catch (Exception e) {
			e.printStackTrace();
			return AjaxResult.error("????????????");
		}
		switch (flag) {
			case 1:
				return toAjax(flag);
			case 2:
				return AjaxResult.error("????????????????????????????????????");
			case 3:
				return AjaxResult.error("????????????????????????????????????");
			case 4:
				return AjaxResult.error("???????????????????????????");
			default:
				return AjaxResult.error("????????????");
		}
		
	}
	
}
