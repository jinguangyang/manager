package com.manage.project.system.product.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.manage.project.system.product.mapper.ProductClassifyMapper;
import com.manage.project.system.product.mapper.ProductInfoMapper;
import com.manage.project.system.product.mapper.ProductOnlineMapper;
import com.manage.project.common.CommonController;
import com.manage.project.common.Constant;
import com.manage.project.system.product.domain.ProductClassify;
import com.manage.project.system.product.domain.ProductInfo;
import com.manage.project.system.product.domain.ProductOnline;
import com.manage.project.system.product.service.IProductInfoService;
import com.manage.project.system.product.vo.ImportProductVo;
import com.manage.project.system.product.vo.PicJsonVo;
import com.manage.project.system.stock.domain.StockInfo;
import com.manage.project.system.stock.domain.StockProduct;
import com.manage.project.system.stock.domain.StockWarehouse;
import com.manage.project.system.stock.mapper.StockInfoMapper;
import com.manage.project.system.stock.mapper.StockProductMapper;
import com.manage.project.system.stock.mapper.StockWarehouseMapper;
import com.manage.project.system.stock.service.StockInfoServiceImpl;
import com.manage.project.system.util.BussinessCacheService;
import com.manage.project.system.util.SystemUtil;
import com.manage.project.system.vending.domain.VendingLanep;
import com.manage.project.system.vending.domain.VendingPlane;
import com.manage.project.system.vending.domain.VendingStock;
import com.manage.project.system.vending.mapper.VendingLanepMapper;
import com.manage.project.system.vending.mapper.VendingPlaneMapper;
import com.manage.project.system.vending.mapper.VendingStockMapper;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.manage.common.support.Convert;
import com.manage.common.utils.DateUtils;
import com.manage.common.utils.PicUtils;
import com.manage.common.utils.StringUtils;
import com.manage.common.utils.bean.BeanUtils;
import com.manage.common.utils.file.FileUploadUtils;
import com.manage.common.utils.security.ShiroUtils;
import com.manage.common.utils.spring.SpringUtils;
import com.manage.framework.config.ManageConfig;
import com.manage.framework.web.domain.AjaxResult;

/**
 * ???????????? ???????????????
 * 
 * @author xufeng
 * @date 2018-08-31
 */
@Service
public class ProductInfoServiceImpl implements IProductInfoService 
{
	private Logger log=LoggerFactory.getLogger(ProductInfoServiceImpl.class);
	@Autowired
	private ProductInfoMapper productInfoMapper;
	@Autowired
	private ProductClassifyMapper productClassifyMapper;
	@Autowired
	private BussinessCacheService bussinessCacheService;
	@Autowired
	private VendingLanepMapper vendingLanepMapper;
	@Autowired
	private VendingPlaneMapper vendingPlaneMapper;
	@Autowired
	private VendingStockMapper vendingStockMapper;
	@Autowired
	private ProductOnlineMapper productOnlineMapper;
	@Autowired
	private StockProductMapper stockProductMapper;
	@Autowired
	private StockWarehouseMapper stockWarehouseMapper;
	@Autowired
	private StockInfoMapper stockInfoMapper;
	@Autowired
    private ManageConfig manageConfig;
	@Value("${pic.product.siteGreat.width}")
	private int siteGreatWidth;
	@Value("${pic.product.siteGreat.height}")
	private int siteGreatHeight;
	//@Value("${pic.product.siteGreat.os}")
	private String siteGreatOs="03";
	@Value("${pic.product.siteLittle.width}")
	private int siteLittleWidth;
	@Value("${pic.product.siteLittle.height}")
	private int siteLittleHeight;
	//@Value("${pic.product.siteLittle.os}")
	private String siteLittleOs="03";
	@Value("${pic.product.wechatPublic.width}")
	private int wechatPublicWidth;
	@Value("${pic.product.wechatPublic.height}")
	private int wechatPublicHeight;
	//@Value("${pic.product.wechatPublic.os}")
	private String wechatPublicOs="02";
	@Value("${pic.product.supply.width}")
	private int supplyWidth;
	@Value("${pic.product.supply.height}")
	private int supplyHeight;
	//@Value("${pic.product.supply.os}")
	private String supplyOs="04";

	/**
     * ????????????????????????
     * 
     * @param logid ????????????ID
     * @return ??????????????????
     */
    @Override
	public ProductInfo selectProductInfoById(String logid)
	{
	    return productInfoMapper.selectProductInfoById(logid);
	}
	
	/**
     * ????????????????????????
     * 
     * @param productInfo ??????????????????
     * @return ??????????????????
     */
	@Override
	public List<ProductInfo> selectProductInfoList(ProductInfo productInfo)
	{
	    return productInfoMapper.selectProductInfoList(productInfo);
	}
	
    /**
     * ??????????????????
     * 
     * @param productInfo ??????????????????
     * @return ??????
     */
	@Override
	@Transactional
	public int insertProductInfo(ProductInfo productInfo)
	{		
		productInfo.setLogid(UUID.randomUUID().toString());
		String corpId = ShiroUtils.getUser().getCorpId();
		String productId = SystemUtil.getSeqId(corpId, "as_product_info");
		productInfo.setProductId(productId);
		productInfo.setCorpId(corpId);
		productInfo.setCreateTime(DateUtils.getTime());		
		String picJson = createPic(productInfo);
		productInfo.setPicJson(picJson);
	    int r = productInfoMapper.insertProductInfo(productInfo);
	    // ????????????????????????
	    StockProduct stockProduct = new StockProduct();
	    stockProduct.setLogid(UUID.randomUUID().toString());
	    stockProduct.setPstockId(SystemUtil.getSeqId(corpId, "as_stock_product"));
	    stockProduct.setCreateTime(DateUtils.getTime());
	    stockProduct.setProductId(productId);
	    stockProduct.setProductName(productInfo.getName());
	    stockProduct.setTotalNum(0);
	    stockProduct.setOverNum(0);
	    stockProduct.setCorpId(corpId);
	    stockProduct.setCurNum(0);
	    stockProductMapper.insertStockProduct(stockProduct);
	    // ??????as_stock_warehouse??????????????????,???????????????????????????????????????????????????????????????
	    StockInfo stockInfo = new StockInfo();
	    stockInfo.setCorpId(corpId);
	    List<StockInfo> stockList = stockInfoMapper.selectStockInfoList(stockInfo);	// ?????????????????????
	    if (stockList != null) {
	    	for (StockInfo si : stockList) {
	    		StockWarehouse stockWarehouse = new StockWarehouse();
	    		stockWarehouse.setLogid(UUID.randomUUID().toString());
	    		stockWarehouse.setWstockId(SystemUtil.getSeqId(corpId, "as_stock_warehouse"));
	    		stockWarehouse.setStockId(si.getStockId());
	    		stockWarehouse.setStokcName(si.getStockName());
	    		stockWarehouse.setProductId(productId);
	    		stockWarehouse.setProductName(productInfo.getName());
	    		stockWarehouse.setTotalNum(0);
	    		stockWarehouse.setCurNum(0);
	    		stockWarehouse.setOverNum(0);
	    		stockWarehouse.setWarnNum(0);
	    		stockWarehouse.setCorpId(corpId);
	    		stockWarehouse.setCreateTime(DateUtils.getTime());
	    		stockWarehouseMapper.insertStockWarehouse(stockWarehouse);
	    	}
	    }
	    
	    bussinessCacheService.initProduct();	// ?????????????????????
	    return r;
	}
	
	/**
	 * ????????????
	 * 
	 * @param productInfo ????????????
	 * @return picJson?????????
	 */
	private String createPic(ProductInfo productInfo) {
		String productId=productInfo.getProductId();
		return SpringUtils.getBean(PicUtils.class).picHandler(productId, Constant.TYPE_PRODUCT, productInfo.getPicJson());
	}
//	private String createPic(ProductInfo productInfo) {
//		String productId=productInfo.getProductId();
//		try {
//			String pic = productInfo.getPicJson();
//			if(StringUtils.isEmpty(pic)) {
//				return "";
//			}
//			String[] picSplit = productInfo.getPicJson().split("/");
//			String picJson = picSplit[picSplit.length-1];
//			List<PicJsonVo> picJsonList = new ArrayList<PicJsonVo>();			
//			// ??????????????????
//			String filePath = ManageConfig.getProfile()+"product/";
//			//????????????
//			String prefix = manageConfig.getImgProfile();
//			//????????????
//			String suffix = ".png";	
//			File srcImg = new File(filePath+picJson);
//			//???????????????
//			PicJsonVo productSrcPic = PicUtils.productSrcPicRename(productId, picJson);
//			picJsonList.add(productSrcPic);
//			//???????????????
//			String siteGreat = PicUtils.productPicRename(siteGreatOs, productId, siteGreatWidth, siteGreatHeight, "1",suffix);
//			PicUtils.resizePng(srcImg, new File(filePath+siteGreat), siteGreatWidth, siteGreatHeight, false);
//			//???????????????
//			String siteLittle = PicUtils.productPicRename(siteLittleOs, productId, siteLittleWidth, siteLittleHeight, "1",suffix);
//			PicUtils.resizePng(srcImg, new File(filePath+siteLittle), siteLittleWidth, siteLittleHeight, false);
//			PicJsonVo siteJson = new PicJsonVo();
//			siteJson.setOs(siteGreatOs);
//			siteJson.setPic(siteGreat+"#"+siteLittle);
//			StringBuilder sb = new StringBuilder();
//			sb.append(siteGreatWidth).append("X").append(siteGreatHeight).append("#")
//			.append(siteLittleWidth).append("X").append(siteLittleHeight);
//			siteJson.setPx(sb.toString());
//			siteJson.setType("1");
//			picJsonList.add(siteJson);
//			//?????????????????????
//			String wechatPublic = PicUtils.productPicRename(wechatPublicOs, productId, wechatPublicWidth, wechatPublicHeight, "1",suffix);
//			PicUtils.resizePng(srcImg, new File(filePath+wechatPublic), wechatPublicWidth, wechatPublicHeight, false);
//			PicJsonVo wechatPublicJson = new PicJsonVo();
//			wechatPublicJson.setOs(wechatPublicOs);
//			wechatPublicJson.setPic(wechatPublic);
//			wechatPublicJson.setPx(wechatPublicWidth+"X"+wechatPublicHeight);
//			wechatPublicJson.setType("1");
//			picJsonList.add(wechatPublicJson);
//			//?????????????????????
//			String supply = PicUtils.productPicRename(supplyOs, productId, supplyWidth, supplyHeight, "1",suffix);
//			PicUtils.resizePng(srcImg, new File(filePath+supply), supplyWidth, supplyHeight, false);
//			PicJsonVo supplyJson = new PicJsonVo();
//			supplyJson.setOs(supplyOs);
//			supplyJson.setPic(supply);
//			supplyJson.setPx(supplyWidth+"X"+supplyHeight);
//			supplyJson.setType("1");
//			picJsonList.add(supplyJson);
//			return JSONObject.toJSONString(picJsonList);
//		}catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
	
	/**
     * ??????????????????
     * 
     * @param productInfo ??????????????????
     * @return ??????
     */
	@Override
	@Transactional
	public int updateProductInfo(ProductInfo productInfo)
	{
		//????????????????????????????????????
		ProductInfo product = productInfoMapper.selectProductInfoById(productInfo.getLogid());
		String productId = product.getProductId();
		String srcName = product.getName();
		Float srcSalePrice = product.getSalePrice();
		String srcPicJson = product.getPic();
		String srcFullName = product.getFullName();
		String srcTypeId = product.getTypeId();
		String srcBagType = product.getBagType();
		String srcFactoryName = product.getFactoryName();
		//???????????????????????????
		String name = productInfo.getName();
		Float salePrice = productInfo.getSalePrice();
		String pic = productInfo.getPicJson();
		String fullName = productInfo.getFullName();
		String typeId = productInfo.getTypeId();
		String bagType = productInfo.getBagType();
		String factoryName = productInfo.getFactoryName();
		//????????????????????????
		boolean nameFlag = !StringUtils.equals(srcName, name);
		if(srcSalePrice==null) {
			srcSalePrice=0F;
		}
		if(salePrice==null) {
			salePrice=0F;
		}
		boolean salePriceFlag = !(Math.abs(srcSalePrice-salePrice)<0.0001);
		boolean fullNameFlag = !StringUtils.equals(srcFullName, fullName);
		boolean typeIdFlag = !StringUtils.equals(srcTypeId, typeId);
		boolean bagTypeFlag = !StringUtils.equals(srcBagType, bagType);
		boolean factoryNameFlag = !StringUtils.equals(srcFactoryName, factoryName);
		boolean picFlag = !StringUtils.equals(srcPicJson, pic);
		String picJson=null;
		if(picFlag) {
			picJson = createPic(productInfo);
		}else {
			picJson="";
		}
		productInfo.setPicJson(picJson);
		log.info("????????????,??????"+DateUtils.getTime()+",?????????:"+JSONObject.toJSONString(product)+",???????????????:"+JSONObject.toJSONString(productInfo));
		int r = productInfoMapper.updateProductInfo(productInfo);
		//???????????????????????????
		//if(picFlag||salePriceFlag) {
			VendingLanep vendingLanep = new VendingLanep();
			vendingLanep.setProductId(productId);
			vendingLanep.setProductPic(picJson);
			vendingLanep.setSalePrice(salePrice);
			vendingLanepMapper.updateVendingLanepByProductId(vendingLanep);
		//}
		//?????????????????????????????????
		//if(picFlag||salePriceFlag) {
			VendingPlane vendingPlane = new VendingPlane();
			vendingPlane.setProductId(productId);
			vendingPlane.setProductPic(picJson);
			vendingPlane.setSalePrice(salePrice);
			vendingPlaneMapper.updateVendingPlaneByProductId(vendingPlane);
		//}
		//????????????????????????????????????
		//if(picFlag||salePriceFlag||nameFlag||typeIdFlag) {
			VendingStock vendingStock = new VendingStock();
			vendingStock.setProductId(productId);
			vendingStock.setPicUrl(picJson);
			vendingStock.setSalePrice(salePrice);
			vendingStock.setProductName(name);
			vendingStock.setTypeId(typeId);
			vendingStockMapper.updateVendingStockByProductId(vendingStock);
		//}
		//????????????????????????
		//if(salePriceFlag||nameFlag||typeIdFlag||bagTypeFlag||fullNameFlag||factoryNameFlag) {
			ProductOnline productOnline = new ProductOnline();
			productOnline.setProductId(productId);
			productOnline.setSalePrice(salePrice);
			productOnline.setName(name);
			productOnline.setTypeId(typeId);
			productOnline.setBagType(bagType);
			productOnline.setFactoryName(factoryName);
			productOnline.setFullName(fullName);
			productOnlineMapper.updateProductOnlineByProductId(productOnline);
		//}
		//?????????????????????
		//if(nameFlag) {
			StockProduct stockProduct = new StockProduct();
			stockProduct.setProductId(productId);
			stockProduct.setProductName(name);
			stockProductMapper.updateStockProductByProductId(stockProduct);
		//}
		//?????????????????????????????????
		//if(nameFlag) {
			StockWarehouse stockWarehouse = new StockWarehouse();
			stockWarehouse.setProductId(productId);
			stockWarehouse.setProductName(name);
			stockWarehouseMapper.updateStockWarehouseByProductId(stockWarehouse);
		//}
		bussinessCacheService.initProduct();
		return r;
	}

	/**
     * ????????????????????????
     * 
     * @param ids ?????????????????????ID
     * @return ??????
     */
	@Override
	@Transactional
	public int deleteProductInfoByIds(String ids)
	{
		List<ProductInfo> list = productInfoMapper.selectProductInfoByIds(ids.split(","));
		for (ProductInfo productInfo : list) {
			//??????????????????????????????????????????
			StockProduct stockProduct = new StockProduct();
			stockProduct.setProductId(productInfo.getProductId());
			List<StockProduct> stockProductList = stockProductMapper.selectStockProductCurNumNotNull(stockProduct);
			if(stockProductList!=null&&stockProductList.size()!=0) {
				return 2;
			}
			//???????????????????????????????????????
			StockWarehouse stockWarehouse = new StockWarehouse();
			stockWarehouse.setProductId(productInfo.getProductId());
			List<StockWarehouse> stockWarehouseList = stockWarehouseMapper.selectStockWarehouseCurNumNotNull(stockWarehouse);
			if(stockWarehouseList!=null&&stockWarehouseList.size()!=0) {
				return 3;
			}
			//??????????????????????????????????????????
			VendingStock vendingStock = new VendingStock();
			vendingStock.setProductId(productInfo.getProductId());
			List<VendingStock> vendingStockList = vendingStockMapper.selectVendingStockCurNumNotNull(vendingStock);
			if(vendingStockList!=null&&vendingStockList.size()!=0) {
				return 4;
			}
			//?????????????????????????????????
			VendingLanep vendingLanep = new VendingLanep();
			vendingLanep.setProductId(productInfo.getProductId());
			List<VendingLanep> vendingLanepList = vendingLanepMapper.selectVendingLanepList(vendingLanep);
			if(vendingLanepList!=null&&vendingLanepList.size()!=0) {
				return 5;
			}
			//???????????????????????????????????????
			VendingPlane vendingPlane = new VendingPlane();
			vendingPlane.setProductId(productInfo.getProductId());
			List<VendingPlane> vendingPlaneList = vendingPlaneMapper.selectVendingPlaneList(vendingPlane);
			if(vendingPlaneList!=null&&vendingPlaneList.size()!=0) {
				return 6;
			}
		}
		try {
			productInfoMapper.deleteProductInfoByIds(Convert.toStrArray(ids));			
			for (ProductInfo productInfo : list) {
				log.info("????????????:"+DateUtils.getTime()+JSONObject.toJSONString(productInfo));
				productOnlineMapper.deleteProductOnlineByProductId(productInfo.getProductId());
				stockProductMapper.deleteStockProductByProductId(productInfo.getProductId());
				stockWarehouseMapper.deleteStockWarehouseByProductId(productInfo.getProductId());
				vendingStockMapper.deleteVendingStockByProductId(productInfo.getProductId());
			}
		}catch (Exception e) {
			log.error("??????????????????:"+DateUtils.getTime(),e);
			throw new RuntimeException();
		}
		
		bussinessCacheService.initProduct();
		return 1;		
	}
	
	/**
     * ??????????????????
     * 
     * @param productInfoList ????????????
     * @return ??????
     */
	@Override
	@Transactional
	public int insertProductInfoBatch(List<ProductInfo> productInfoList)
	{	int ret=0;
		for (ProductInfo productInfo:productInfoList) {
			productInfo.setLogid(UUID.randomUUID().toString());
			String corpId = ShiroUtils.getCorpId();
			productInfo.setCorpId(corpId);
			productInfo.setCreateTime(DateUtils.getTime());
			productInfo.setProductId(SystemUtil.getSeqId(corpId, "as_product_info"));
			log.info("????????????,??????"+DateUtils.getTime()+",??????:"+JSONObject.toJSONString(productInfo));
			productInfoMapper.insertProductInfo(productInfo);
			ret+=1;
			
			// ????????????????????????
		    StockProduct stockProduct = new StockProduct();
		    stockProduct.setLogid(UUID.randomUUID().toString());
		    stockProduct.setPstockId(SystemUtil.getSeqId(corpId, "as_stock_product"));
		    stockProduct.setCreateTime(DateUtils.getTime());
		    stockProduct.setProductId(productInfo.getProductId());
		    stockProduct.setProductName(productInfo.getName());
		    stockProduct.setTotalNum(0);
		    stockProduct.setOverNum(0);
		    stockProduct.setCorpId(corpId);
		    stockProduct.setCurNum(0);
		    stockProductMapper.insertStockProduct(stockProduct);
		    // ??????as_stock_warehouse??????????????????,???????????????????????????????????????????????????????????????
		    StockInfo stockInfo = new StockInfo();
		    stockInfo.setCorpId(corpId);
		    List<StockInfo> stockList = stockInfoMapper.selectStockInfoList(stockInfo);	// ?????????????????????
		    if (stockList != null) {
		    	for (StockInfo si : stockList) {
		    		StockWarehouse stockWarehouse = new StockWarehouse();
		    		stockWarehouse.setLogid(UUID.randomUUID().toString());
		    		stockWarehouse.setWstockId(SystemUtil.getSeqId(corpId, "as_stock_warehouse"));
		    		stockWarehouse.setStockId(si.getStockId());
		    		stockWarehouse.setStokcName(si.getStockName());
		    		stockWarehouse.setProductId(productInfo.getProductId());
		    		stockWarehouse.setProductName(productInfo.getName());
		    		stockWarehouse.setTotalNum(0);
		    		stockWarehouse.setCurNum(0);
		    		stockWarehouse.setOverNum(0);
		    		stockWarehouse.setWarnNum(0);
		    		stockWarehouse.setCorpId(corpId);
		    		stockWarehouse.setCreateTime(DateUtils.getTime());
		    		stockWarehouseMapper.insertStockWarehouse(stockWarehouse);
		    	}
		    }
			
		}
		bussinessCacheService.initProduct();
		return ret;
	}

	@Override
	@Transactional
	public int insertProduct(List<ImportProductVo> list) {
		if (list == null || list.isEmpty()) {
			return 0;
		}
		int r = 0;
		for (ImportProductVo vo : list) {
			if (vo.getProductCode() == null || vo.getProductCode().equals("")) {
				break;	// ?????????????????????
			}
			ProductInfo product = new ProductInfo();
			BeanUtils.copyBeanProp(product, vo);
			product.setLogid(UUID.randomUUID().toString());
			String corpId = ShiroUtils.getCorpId();
			product.setProductId(SystemUtil.getSeqId(corpId, "as_product_info"));
			product.setCorpId(corpId);
			product.setCreateTime(DateUtils.getTime());
			log.info("????????????,??????"+DateUtils.getTime()+",??????:"+JSONObject.toJSONString(product));
			r = r + productInfoMapper.insertProductInfo(product);
			
			// ????????????????????????
		    StockProduct stockProduct = new StockProduct();
		    stockProduct.setLogid(UUID.randomUUID().toString());
		    stockProduct.setPstockId(SystemUtil.getSeqId(corpId, "as_stock_product"));
		    stockProduct.setCreateTime(DateUtils.getTime());
		    stockProduct.setProductId(product.getProductId());
		    stockProduct.setProductName(product.getName());
		    stockProduct.setTotalNum(0);
		    stockProduct.setOverNum(0);
		    stockProduct.setCorpId(corpId);
		    stockProduct.setCurNum(0);
		    stockProductMapper.insertStockProduct(stockProduct);
		    // ??????as_stock_warehouse??????????????????,???????????????????????????????????????????????????????????????
		    StockInfo stockInfo = new StockInfo();
		    stockInfo.setCorpId(corpId);
		    List<StockInfo> stockList = stockInfoMapper.selectStockInfoList(stockInfo);	// ?????????????????????
		    if (stockList != null) {
		    	for (StockInfo si : stockList) {
		    		StockWarehouse stockWarehouse = new StockWarehouse();
		    		stockWarehouse.setLogid(UUID.randomUUID().toString());
		    		stockWarehouse.setWstockId(SystemUtil.getSeqId(corpId, "as_stock_warehouse"));
		    		stockWarehouse.setStockId(si.getStockId());
		    		stockWarehouse.setStokcName(si.getStockName());
		    		stockWarehouse.setProductId(product.getProductId());
		    		stockWarehouse.setProductName(product.getName());
		    		stockWarehouse.setTotalNum(0);
		    		stockWarehouse.setCurNum(0);
		    		stockWarehouse.setOverNum(0);
		    		stockWarehouse.setWarnNum(0);
		    		stockWarehouse.setCorpId(corpId);
		    		stockWarehouse.setCreateTime(DateUtils.getTime());
		    		stockWarehouseMapper.insertStockWarehouse(stockWarehouse);
		    	}
		    }
		    
		    
		}
		bussinessCacheService.initProduct();	// ?????????????????????
		return r;
	}

	/**
     * ????????????????????????
     * 
     * @param productId ????????????
     * @return ??????????????????
     */
	@Override
	public ProductInfo selectProductInfoByProductId(String productId) {
		return productInfoMapper.selectProductInfoByProductId(productId);
	}

	/**
	 * ????????????????????????
	 * 
	 * @param productInfoList
	 * @return
	 */
	@Override
	@Transactional
	public AjaxResult saveImportProductInfo(ProductInfo productInfo) {
		AjaxResult ajaxResult = checkProduct(productInfo);
		if(!ajaxResult.isSuccess()) {
			return ajaxResult;
		}
		String corpId=ShiroUtils.getCorpId();
		productInfo.setLogid(UUID.randomUUID().toString());
		String productId = SystemUtil.getSeqId(corpId, "as_product_info");	
		productInfo.setProductId(productId);
		productInfo.setCorpId(corpId);
		productInfo.setCreateTime(DateUtils.getTime());
		if(StringUtils.isNotEmpty(productInfo.getPicJson())) {
			String picJson = createPic(productInfo);
		}
		String picJson = createPic(productInfo);
		productInfo.setPicJson(picJson);
	    int r = productInfoMapper.insertProductInfo(productInfo);
	    // ????????????????????????
	    StockProduct stockProduct = new StockProduct();
	    stockProduct.setLogid(UUID.randomUUID().toString());
	    stockProduct.setPstockId(SystemUtil.getSeqId(corpId, "as_stock_product"));
	    stockProduct.setCreateTime(DateUtils.getTime());
	    stockProduct.setProductId(productId);
	    stockProduct.setProductName(productInfo.getName());
	    stockProduct.setTotalNum(0);
	    stockProduct.setOverNum(0);
	    stockProduct.setCorpId(corpId);
	    stockProduct.setCurNum(0);
	    stockProductMapper.insertStockProduct(stockProduct);
	    // ??????as_stock_warehouse??????????????????,???????????????????????????????????????????????????????????????
	    StockInfo stockInfo = new StockInfo();
	    stockInfo.setCorpId(corpId);
	    List<StockInfo> stockList = stockInfoMapper.selectStockInfoList(stockInfo);	// ?????????????????????
	    if (stockList != null) {
	    	for (StockInfo si : stockList) {
	    		StockWarehouse stockWarehouse = new StockWarehouse();
	    		stockWarehouse.setLogid(UUID.randomUUID().toString());
	    		stockWarehouse.setWstockId(SystemUtil.getSeqId(corpId, "as_stock_warehouse"));
	    		stockWarehouse.setStockId(si.getStockId());
	    		stockWarehouse.setStokcName(si.getStockName());
	    		stockWarehouse.setProductId(productId);
	    		stockWarehouse.setProductName(productInfo.getName());
	    		stockWarehouse.setTotalNum(0);
	    		stockWarehouse.setCurNum(0);
	    		stockWarehouse.setOverNum(0);
	    		stockWarehouse.setWarnNum(0);
	    		stockWarehouse.setCorpId(corpId);
	    		stockWarehouse.setCreateTime(DateUtils.getTime());
	    		stockWarehouseMapper.insertStockWarehouse(stockWarehouse);
	    	}
	    }
		return AjaxResult.success();
	}
	
	/**
	 * ??????????????????
	 * 
	 * @param productInfo
	 * @return
	 */
	private AjaxResult checkProduct(ProductInfo productInfo) {
		String productCode = productInfo.getProductCode();
		String name = productInfo.getName();
		String fullName = productInfo.getFullName();
		String typeId = productInfo.getTypeId();
		Float salePrice = productInfo.getSalePrice();
		Integer validTime = productInfo.getValidTime();
		String bagType = productInfo.getBagType();
		String spec = productInfo.getSpec();
		String factoryName = productInfo.getFactoryName();
		String desOne = productInfo.getDesOne();
		String desTwo = productInfo.getDesTwo();
		
		if(StringUtils.isEmpty(productCode)) {
			return AjaxResult.error("????????????");
		}
		if(productCode.length()>30) {
			return AjaxResult.error("??????????????????");
		}
		ProductInfo codeSelect = new ProductInfo();
		codeSelect.setProductCode(productCode);
		codeSelect.setCorpId(ShiroUtils.getCorpId());
		ProductInfo codeExistProduct = productInfoMapper.selectProductInfoExist(codeSelect);
		if(codeExistProduct!=null) {
			return AjaxResult.error("????????????");
		}
		
		if(StringUtils.isEmpty(name)) {
			return AjaxResult.error("??????????????????");
		}
		if(name.length()>30) {
			return AjaxResult.error("????????????????????????");
		}
		ProductInfo nameSelect = new ProductInfo();
		nameSelect.setProductCode(productCode);
		nameSelect.setCorpId(ShiroUtils.getCorpId());
		ProductInfo nameExistProduct = productInfoMapper.selectProductInfoExist(nameSelect);
		if(nameExistProduct!=null) {
			return AjaxResult.error("??????????????????");
		}
		
		if(StringUtils.isEmpty(fullName)) {
			return AjaxResult.error("??????????????????");
		}
		if(fullName.length()>30) {
			return AjaxResult.error("????????????????????????");
		}
		ProductInfo fullNameSelect = new ProductInfo();
		fullNameSelect.setProductCode(productCode);
		fullNameSelect.setCorpId(ShiroUtils.getCorpId());
		ProductInfo fullNameExistProduct = productInfoMapper.selectProductInfoExist(fullNameSelect);
		if(fullNameExistProduct!=null) {
			return AjaxResult.error("??????????????????");
		}
		
		if(StringUtils.isEmpty(typeId)) {
			return AjaxResult.error("??????????????????");
		}
		//??????????????????
		String[] typeIdStrs = typeId.split("--");
		typeId = typeIdStrs[typeIdStrs.length-1];
    	productInfo.setTypeId(typeId);
    	productInfo.setTypeId(typeId);
		ProductClassify productClassify = productClassifyMapper.selectProductClassifyByClassifyId(typeId);
		if(productClassify==null) {
			return AjaxResult.error("?????????????????????");
		}
		
		if(salePrice==null) {
			return AjaxResult.error("???????????????");
		}
		if(salePrice<=0||salePrice>=1000000) {
			return AjaxResult.error("???????????????0-1000000??????");
		}
		
		if(validTime==null) {
			return AjaxResult.error("??????????????????");
		}
		if(validTime<=0||validTime>=10000) {
			return AjaxResult.error("??????????????????0-10000??????");
		}
		
		if(StringUtils.isEmpty(bagType)) {
			return AjaxResult.error("??????????????????");
		}
		List<String> bagTypeList = Arrays.asList(Constant.PRODUCT_BAG_TYPE_LIST);
		if(!bagTypeList.contains(bagType)) {
			return AjaxResult.error("?????????????????????");
		}
		//??????????????????
		String[] bagTypeStrs = bagType.split("--");
		bagType=bagTypeStrs[bagTypeStrs.length-1];
    	productInfo.setBagType(bagType);   	
		
		if(StringUtils.isEmpty(spec)) {
			return AjaxResult.error("???????????????");
		}
		if(spec.length()>50) {
			return AjaxResult.error("?????????????????????");
		}
		if(factoryName!=null&&factoryName.length()>30) {
			return AjaxResult.error("????????????????????????");
		}
		if(desOne!=null&&desOne.length()>150) {
			return AjaxResult.error("??????1????????????");
		}
		if(desTwo!=null&&desTwo.length()>150) {
			return AjaxResult.error("??????2????????????");
		}
		return AjaxResult.success();
	}

	/**
	 * ?????????????????????????????????
	 * 
	 * @param productInfo
	 * @return
	 */
	@Override
	public List<ProductInfo> selectReferenceProductInfoList(ProductInfo productInfo) {
		return productInfoMapper.selectReferenceProductInfoList(productInfo);
	}
}
