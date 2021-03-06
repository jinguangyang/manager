package com.manage.project.system.vending.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manage.project.system.vending.mapper.VendingMapper;
import com.manage.project.system.vending.mapper.VendingModelMapper;
import com.manage.project.system.vending.mapper.VendingStateMapper;
import com.manage.project.system.vending.mapper.VendingStatisticMapper;
import com.manage.project.system.vending.mapper.VendingStockMapper;
import com.manage.project.common.Constant;
import com.manage.project.system.base.domain.DictData;
import com.manage.project.system.base.mapper.DictDataMapper;
import com.manage.project.system.product.domain.ProductInfo;
import com.manage.project.system.product.domain.ProductLunder;
import com.manage.project.system.product.domain.ProductOnline;
import com.manage.project.system.product.domain.ProductUnder;
import com.manage.project.system.product.domain.ProductVunder;
import com.manage.project.system.product.mapper.ProductInfoMapper;
import com.manage.project.system.product.mapper.ProductLunderMapper;
import com.manage.project.system.product.mapper.ProductOnlineMapper;
import com.manage.project.system.product.mapper.ProductUnderMapper;
import com.manage.project.system.product.mapper.ProductVunderMapper;
import com.manage.project.system.product.service.IProductUnderService;
import com.manage.project.system.product.vo.ProductLunderVo;
import com.manage.project.system.server.service.IServerService;
import com.manage.project.system.statement.mapper.OrderApplyMapper;
import com.manage.project.system.statement.vo.OrderVo;
import com.manage.project.system.stock.domain.StockProduct;
import com.manage.project.system.stock.domain.StockWarehouse;
import com.manage.project.system.stock.mapper.StockProductMapper;
import com.manage.project.system.stock.mapper.StockWarehouseMapper;
import com.manage.project.system.supply.domain.SupplyConfig;
import com.manage.project.system.supply.domain.SupplyVending;
import com.manage.project.system.supply.domain.SupplyVorder;
import com.manage.project.system.supply.mapper.SupplyConfigMapper;
import com.manage.project.system.supply.mapper.SupplyVendingMapper;
import com.manage.project.system.util.BussinessCacheService;
import com.manage.project.system.util.SystemUtil;
import com.manage.project.system.vending.domain.Vending;
import com.manage.project.system.vending.service.IVendingService;
import com.manage.project.system.vending.vo.CabinetTypeSelectVo;
import com.manage.project.system.vending.vo.Cols;
import com.manage.project.system.vending.vo.RelationSelectResultVo;
import com.manage.project.system.vending.vo.VendingCabinetVo;
import com.manage.project.system.vending.vo.VendingLaneVo;
import com.manage.project.system.vending.vo.VendingLanepVo;
import com.manage.project.system.vending.vo.VendingListVo;
import com.manage.project.system.vending.vo.VendingVo;
import com.manage.project.system.vendingPoint.domain.VendingPoint;
import com.manage.project.system.vendingPoint.mapper.VendingPointMapper;
import com.manage.project.system.supply.mapper.SupplyVorderMapper;

import com.manage.project.system.vending.domain.VendingCabinet;
import com.manage.project.system.vending.domain.VendingCmd;
import com.manage.project.system.vending.domain.VendingLane;
import com.manage.project.system.vending.domain.VendingLanep;
import com.manage.project.system.vending.domain.VendingLogfile;
import com.manage.project.system.vending.domain.VendingModel;
import com.manage.project.system.vending.domain.VendingState;
import com.manage.project.system.vending.domain.VendingStatistic;
import com.manage.project.system.vending.domain.VendingStock;
import com.manage.project.system.vending.mapper.VendingCabinetMapper;
import com.manage.project.system.vending.mapper.VendingLaneMapper;
import com.manage.project.system.vending.mapper.VendingLanepMapper;
import com.alibaba.fastjson.JSONObject;
import com.manage.common.exception.vending.LaneProductCapacityException;
import com.manage.common.exception.vending.LaneProductNullException;
import com.manage.common.exception.vending.LaneProductWarnCapException;
import com.manage.common.exception.vending.SupplyVorderExistException;
import com.manage.common.exception.vending.VunderExistException;
import com.manage.common.exception.vending.WarnCapGreatThanCapacityException;
import com.manage.common.support.Convert;
import com.manage.common.utils.DateUtils;
import com.manage.common.utils.StringUtils;
import com.manage.common.utils.bean.BeanUtils;
import com.manage.common.utils.http.HttpClientUtils;
import com.manage.common.utils.security.ShiroUtils;
import com.manage.framework.config.ManageConfig;
import com.manage.framework.web.domain.AjaxResult;

/**
 * ??????????????????????????? ???????????????
 * 
 * @author xufeng
 * @date 2018-08-31
 */
@Service
public class VendingServiceImpl implements IVendingService 
{
	private Logger logger = LoggerFactory.getLogger(VendingServiceImpl.class);;
	
	@Autowired
	private VendingMapper vendingMapper;
	
	@Autowired
	private VendingPointMapper vendingPointMapper;
	
	@Autowired
	private VendingModelMapper vendingModelMapper;
	
	@Autowired
	private VendingCabinetMapper vendingCabinetMapper;
	
	@Autowired
	private VendingLaneMapper vendingLaneMapper;
	
	@Autowired
	private VendingLanepMapper vendingLanepMapper;
	
	@Autowired
	private BussinessCacheService bussinessCacheService;
	
	@Autowired
	private VendingStateMapper vendingStateMapper;
	
	@Autowired
	private VendingStockMapper vendingStockMapper;
	
	@Autowired
	private VendingStatisticMapper vendingStatisticMapper;
	
	@Autowired
	private ProductOnlineMapper productOnlineMapper;
	
	@Autowired
	private SupplyVendingMapper supplyVendingMapper;
	
	@Autowired
	private SupplyConfigMapper supplyConfigMapper;
	
	@Autowired
	private IProductUnderService productUnderService;
	
	@Autowired
	private SupplyVorderMapper supplyVorderMapper;
	
	@Autowired
    private ManageConfig manageConfig;
	
	@Autowired
	private ProductVunderMapper productVunderMapper;
	
	@Autowired
	private ProductInfoMapper productInfoMapper;
	
	@Autowired
	private ProductLunderMapper productLunderMapper;
	
	@Autowired
	private StockWarehouseMapper stockWarehouseMapper;
	
	@Autowired
	private StockProductMapper stockProductMapper;
	
	@Autowired
	private ProductUnderMapper productUnderMapper;
	
	@Autowired
	private OrderApplyMapper orderApplyMapper;
	
	@Autowired
	private DictDataMapper dictDataMapper;
	@Autowired
	private IServerService serverService;
	
	@Autowired
	private IVendingCmdService vendingCmdService;
	
	/**
     * ???????????????????????????????????????
     * 
     * @param logid ???????????????????????????ID
     * @return ?????????????????????????????????
     */
    @Override
	public Vending selectVendingById(String logid)
	{
	    return vendingMapper.selectVendingById(logid);
	}
	
	/**
     * ???????????????????????????????????????
     * 
     * @param vending ?????????????????????????????????
     * @return ?????????????????????????????????
     */
	@Override
	public List<Vending> selectVendingList(Vending vending)
	{
	    return vendingMapper.selectVendingList(vending);
	}
	
	/**
     * ?????????????????????????????????
     * 
     * @param vending ?????????????????????????????????
     * @return ??????
     */
	@Override
	public int updateVending(Vending vending)
	{
	    int r = vendingMapper.updateVending(vending);
	    // ???????????????????????????
	 	bussinessCacheService.initVendingNum();
	 	// ????????????????????????????????????
	 	bussinessCacheService.initVending();
	 	return r;
	}

	/**
     * ???????????????????????????????????????
     * 
     * @param ids ?????????????????????ID
     * @return ??????
     */
	@Override
	public int deleteVendingByIds(String ids)
	{
		int r = vendingMapper.deleteVendingByIds(Convert.toStrArray(ids));
		// ???????????????????????????
		bussinessCacheService.initVendingNum();
		// ????????????????????????????????????
		bussinessCacheService.initVending();
		return r;
	}
	
	/**
	 * ???????????????????????????
	 * @param vending
	 * @return
	 */
	@Override
	public List<VendingListVo> selectNVendingList(VendingListVo vendingListVo) {
		if (SystemUtil.isZhilai()) {	// ????????????????????????????????????
			vendingListVo.setCorpId("");
		}
		return vendingMapper.selectNVendingList(vendingListVo);
	}

	@Override
	@Transactional
	public Vending insertVending(VendingVo vendingVo) throws LaneProductNullException, LaneProductWarnCapException, LaneProductCapacityException {
		Vending vending = new Vending();
		List<VendingLane> saveLanes = new ArrayList<VendingLane>();	// ???????????????????????????
		List<VendingLanep> saveLaneps = new ArrayList<VendingLanep>();	// ?????????????????????????????????
		BeanUtils.copyBeanProp(vending, vendingVo);
		vending.setLogid(UUID.randomUUID().toString());
		String corpId = ShiroUtils.getCorpId();
		vending.setCorpId(corpId);
		String siteId = SystemUtil.getSeqIdShort(corpId, "as_vending");
		vending.setSiteId(siteId);
		vending.setCurState(Constant.VENDING_CURSTATE_NOTREGISTER);
		VendingPoint vp = SystemUtil.getVendingPointFromCache(vending.getPointId());
		vending.setLongitude(vp.getLongitude());
		vending.setLatitude(vp.getLatitude());
		vending.setAddress(vp.getAdderss());
		vending.setNetSate("1");	// ????????????
		vending.setOnlineTime(DateUtils.getTime());
		vending.setInitTime(DateUtils.getTime());
		vending.setStateTime(DateUtils.getTime());
		vending.setNetTime(DateUtils.getTime());
		//??????????????????,?????????????????????????????????
		if("04".equals(vending.getCabinetType())) {
			vending.setPlatType(Constant.PLATTYPE_SINGLECHIP);
		}else {
			//????????????????????????
			vending.setPlatType(Constant.PLATTYPE_BASE);
		}
		
		if (vp != null) {
			vending.setLineId(vp.getLineId());
			vending.setDistrictId(vp.getDistrictId());
		}
		int laneNum = 1;	// ????????????
		
		// ????????????
		VendingCabinet zhCabinet = new VendingCabinet();
		BeanUtils.copyBeanProp(zhCabinet, vendingVo);
		zhCabinet.setSiteId(siteId);
		zhCabinet.setLogid(UUID.randomUUID().toString());
		zhCabinet.setCorpId(corpId);
		zhCabinet.setHangType(Constant.HANG_TYPE_FALSE);
		String zhuseqId = SystemUtil.getSeqId(corpId, "as_vending_cabinet");
		zhCabinet.setCabinetId(zhuseqId);
//		zhCabinet.setViewName("1");
		zhCabinet.setSeqId(1);
		zhCabinet.setVmcId("");
		zhCabinet.setCreateTime(DateUtils.getTime());
		zhCabinet.setDescription("???");
		zhCabinet.setPointCode("");
		zhCabinet.setReportTime(DateUtils.getTime());
//		zhCabinet.setCabinetName("??????");
		vendingCabinetMapper.insertVendingCabinet(zhCabinet);		
		// ??????????????????
		List<Cols> colsList = vendingVo.getLanes();
		List<VendingLaneVo> zhlanes = new ArrayList<VendingLaneVo>();
		if (colsList != null && !colsList.isEmpty()) {
			for (Cols col : colsList) {
				zhlanes.addAll(col.getCols());
			}
		}
		
		int x = 1;
		for (VendingLaneVo laneVo : zhlanes) {
			VendingLane vendingLane = new VendingLane();
			BeanUtils.copyBeanProp(vendingLane, laneVo);
			vendingLane.setLogid(UUID.randomUUID().toString());
			vendingLane.setCorpId(corpId);
			vendingLane.setCabinetId(zhuseqId);
			String slaneId = SystemUtil.getSeqId(corpId, "as_vending_lane");
			vendingLane.setSlaneId(slaneId);
			vendingLane.setSiteId(zhCabinet.getSiteId());
			vendingLane.setSiteName(zhCabinet.getSiteName());
			vendingLane.setArrange(1);
			vendingLane.setSiteName(zhCabinet.getSiteName());
			vendingLane.setCabinetType(zhCabinet.getCabinetType());
			vendingLane.setDeviceId(zhCabinet.getDeviceId());
			vendingLane.setLaneCode(x++);
			vendingLane.setStateTime(DateUtils.getTime());
			vendingLane.setCreateTime(DateUtils.getTime());			
			vendingLane.setLaneId(laneNum++);
			saveLanes.add(vendingLane);
//			vendingLaneMapper.insertVendingLane(vendingLane);
			
			// ??????????????????
//			if (laneVo.getLanep() != null && laneVo.getLanep().getProductId() != null && !laneVo.getLanep().getProductId().equals("")) {
			// ????????????????????????????????????????????????
//			if (laneVo.getLanep() == null || laneVo.getLanep().getProductId() == null || laneVo.getLanep().getProductId().equals("")) {
//				if (laneVo.getLanep() != null) {
//					if (!laneVo.getLanep().getLaneSate().equals("2")) {
//						throw new LaneProductNullException();
//					}
//				}
//			}
			VendingLanepVo vendingLanepVo = laneVo.getLanep();
			VendingLanep vendingLanep = new VendingLanep();
			BeanUtils.copyBeanProp(vendingLanep, vendingLanepVo);
			vendingLanep.setSiteId(vendingLane.getSiteId());
			vendingLanep.setSlaneId(vendingLane.getSlaneId());
			vendingLanep.setSiteName(vendingLane.getSiteName());
			vendingLanep.setCorpId(corpId);
			vendingLanep.setLogid(UUID.randomUUID().toString());
			vendingLanep.setLSateTime(DateUtils.getTime());
			vendingLanep.setPStateTime(DateUtils.getTime());
			
			vendingLanep.setLaneSId(vendingLane.getLaneCode());
			vendingLanep.setLaneEId(vendingLane.getLaneCode());
			vendingLanep.setCreateTime(DateUtils.getTime());
			if (!StringUtils.isEmpty(vendingLanepVo.getProductId())) {
				ProductInfo product = SystemUtil.getProductById(vendingLanepVo.getProductId());
				if (product != null) {
					vendingLanep.setProductPic(product.getPicJson());
					vendingLanep.setSalePrice(product.getSalePrice());
					vendingLanep.setProductName(product.getName());
				} else {
					vendingLanep.setProductPic("");
					vendingLanep.setSalePrice(0f);
					vendingLanep.setProductName("");
				}
			} else {
				vendingLanep.setProductPic("");
				vendingLanep.setSalePrice(0f);
				vendingLanep.setProductName("");
			}
			saveLaneps.add(vendingLanep);
			//???????????????????????????
			if(StringUtils.isNotEmpty(vendingLanep.getProductId())) {
				if(vendingLanep.getWarnCap()==null||vendingLanep.getWarnCap()<1) {
					throw new LaneProductWarnCapException();
				}
				if(vendingLanep.getCapacity()==null||vendingLanep.getCapacity()<1){
					throw new LaneProductCapacityException();
				}
				if(vendingLanep.getWarnCap()>vendingLanep.getCapacity()) {
					throw new WarnCapGreatThanCapacityException();
				}
			}	
			vendingLanepMapper.insertVendingLanep(vendingLanep);
//			}
		}
		
		// ????????????
		List<VendingCabinetVo> cabinets = vendingVo.getCabinets();
		for (VendingCabinetVo cabinetVo : cabinets) {
			VendingCabinet vendingCabinet = new VendingCabinet();
			BeanUtils.copyBeanProp(vendingCabinet, cabinetVo);
			vendingCabinet.setSiteId(siteId);
			vendingCabinet.setLogid(UUID.randomUUID().toString());
			vendingCabinet.setCorpId(corpId);
			String cabinetId = SystemUtil.getSeqId(corpId, "as_vending_cabinet");
			vendingCabinet.setCabinetId(cabinetId);
			vendingCabinet.setSiteName(vendingVo.getSiteName());
			vendingCabinet.setVmcId(" ");
			if (cabinetVo.getDescription() == null || cabinetVo.getDescription().equals("")) {
				vendingCabinet.setDescription("???");
			}
			vendingCabinet.setReportTime(DateUtils.getTime());
			if (cabinetVo.getViewName() != null && !cabinetVo.getViewName().equals("")) {
				vendingCabinet.setSeqId(Integer.valueOf(cabinetVo.getViewName()));
			}
			vendingCabinet.setCreateTime(DateUtils.getTime());
			vendingCabinet.setVmcId("");
			vendingCabinetMapper.insertVendingCabinet(vendingCabinet);
			
			// ??????????????????
			List<Cols> fcolsList = cabinetVo.getLanes();
			List<VendingLaneVo> lanes = new ArrayList<VendingLaneVo>();
			if (fcolsList != null && !fcolsList.isEmpty()) {
				for (Cols col : fcolsList) {
					lanes.addAll(col.getCols());
				}
			}
//			int xh = 1;
			for (VendingLaneVo laneVo : lanes) {
				VendingLane vendingLane = new VendingLane();
				BeanUtils.copyBeanProp(vendingLane, laneVo);
				vendingLane.setLogid(UUID.randomUUID().toString());
				vendingLane.setCorpId(corpId);
				vendingLane.setCabinetId(cabinetId);
				String slaneId = SystemUtil.getSeqId(corpId, "as_vending_lane");
				vendingLane.setSlaneId(slaneId);
				vendingLane.setSiteId(vendingCabinet.getSiteId());
				vendingLane.setSiteName(vendingCabinet.getSiteName());
				vendingLane.setCabinetType(vendingCabinet.getCabinetType());
				vendingLane.setDeviceId(vendingCabinet.getDeviceId());
				vendingLane.setLaneCode(x++);
				vendingLane.setStateTime(DateUtils.getTime());
				vendingLane.setArrange(1);
				vendingLane.setCreateTime(DateUtils.getTime());
				vendingLane.setLaneId(laneNum++);
				saveLanes.add(vendingLane);
//				vendingLaneMapper.insertVendingLane(vendingLane);
				
				// ??????????????????
//				if (laneVo.getLanep() != null && laneVo.getLanep().getProductId() != null && !laneVo.getLanep().getProductId().equals("")) {
					VendingLanepVo vendingLanepVo = laneVo.getLanep();
					VendingLanep vendingLanep = new VendingLanep();
					BeanUtils.copyBeanProp(vendingLanep, vendingLanepVo);
					vendingLanep.setSiteId(vendingLane.getSiteId());
					vendingLanep.setSlaneId(vendingLane.getSlaneId());
					vendingLanep.setLaneSId(vendingLane.getLaneId());
					vendingLanep.setSiteName(vendingLane.getSiteName());
					vendingLanep.setCorpId(corpId);
					vendingLanep.setLogid(UUID.randomUUID().toString());
					vendingLanep.setLSateTime(DateUtils.getTime());
					vendingLanep.setPStateTime(DateUtils.getTime());
					
					vendingLanep.setLaneSId(vendingLane.getLaneId());
					vendingLanep.setLaneEId(vendingLane.getLaneId());
					vendingLanep.setCreateTime(DateUtils.getTime());
					if (!StringUtils.isEmpty(vendingLanepVo.getProductId())) {
						ProductInfo product = SystemUtil.getProductById(vendingLanepVo.getProductId());
						if (product != null) {
							vendingLanep.setProductPic(product.getPicJson());
							vendingLanep.setSalePrice(product.getSalePrice());
							vendingLanep.setProductName(product.getName());
						} else {
							vendingLanep.setProductPic("");
							vendingLanep.setSalePrice(0f);
							vendingLanep.setProductName("");
						}
					} else {
						vendingLanep.setProductPic("");
						vendingLanep.setSalePrice(0f);
						vendingLanep.setProductName("");
					}
					saveLaneps.add(vendingLanep);
					//???????????????????????????
					if(StringUtils.isNotEmpty(vendingLanep.getProductId())) {
						if(vendingLanep.getWarnCap()==null||vendingLanep.getWarnCap()<1) {
							throw new LaneProductWarnCapException();
						}
						if(vendingLanep.getCapacity()==null||vendingLanep.getCapacity()<1){
							throw new LaneProductCapacityException();
						}
					}	
					vendingLanepMapper.insertVendingLanep(vendingLanep);
//				}
			}
		}
		
		vending.setLaneNum(laneNum);
		vending.setPmaxNum(0);
		vending.setPcurNum(0);
		vending.setStockLevel(0);
		vending.setSellTime(DateUtils.getTime());
		vending.setCreateTime(DateUtils.getTime());
		if (vendingVo.getDescription() == null || vendingVo.getDescription().equals("")) {
			vending.setDescription("???");
		}
		vending.setConfigFile(manageConfig.getVendingXmlPath()+vending.getSiteId()+".xml");
		// ???????????????
		int result = vendingMapper.insertVending(vending);		
		// ????????????
		vendingLaneMapper.insertVendingLaneBatch(saveLanes);
		// ??????????????????
//		vendingLanepMapper.insertVendingLanepBatch(saveLaneps);
		// ???????????????????????????
		bussinessCacheService.initVendingNum();
		// ????????????????????????????????????
		bussinessCacheService.initVending();
		// ????????????????????????????????????
		createVendingXml(siteId);
		
		// ??????????????????
		this.saveVendingState(siteId, vending.getSiteName());
		// ????????????????????????????????????
		this.saveVendingStock(saveLaneps, siteId, vending.getSiteName());
		// ??????????????????????????????
		this.saveVendingStatistic(vending, saveLaneps);
		// ?????????????????????????????????
		this.saveOnlineProduct(saveLaneps);
		// ??????????????????
		this.updateSupplyAddVending(siteId, vending.getSiteName(), vending.getLineId(), corpId);
		logger.info("??????????????????"+vending.toString());
	    return vending;
	}
	
	/**
	 * ???????????????
	 * 
	 * @param vending
	 */
	public String registerVending(Vending vending) {
		String appKey="";
		if("04".equals(vending.getCabinetType())) {
			appKey="c2886f3c207c11e998f3e86a64025eb8";
		}else {
			appKey=UUID.randomUUID().toString().replaceAll("-", "");
		}
		StringBuilder sb = new StringBuilder();
		sb.append("siteId=").append(vending.getSiteId()).append("&deviceType=").append(vending.getCabinetType())
		.append("&siteName=").append(vending.getSiteName()).append("&apiUrl=").append(manageConfig.getVendingNoticeUrl()).append("&appKey=").append(appKey).append("&linkUrl=").append(manageConfig.getLinkUrl());
		return HttpClientUtils.httpGet(manageConfig.getMessageServerUrl()+"/api/register?", sb.toString(),false);
	}
	
	/**
	 * ???????????????
	 * 
	 * @param vending
	 */
	public String deleteVending(String siteId) {
		StringBuilder sb = new StringBuilder();
		sb.append("siteId=").append(siteId);
		return HttpClientUtils.httpGet(manageConfig.getMessageServerUrl()+"/api/register?", sb.toString(),false);
	}

	/**
	 * ?????????????????????????????????
	 * @param list
	 * @return
	 */
	private int saveOnlineProduct(List<VendingLanep> list) {
		Map<String,List<VendingLanep>> map = this.transLanep(list);
		String corpId = ShiroUtils.getCorpId();
		int n = 0;
		for (String productId : map.keySet()) {
			if (StringUtils.isEmpty(productId)) {
				continue;
			}
			ProductOnline productOnline = productOnlineMapper.selectProductOnlineByProductId(productId);
			if (productOnline != null) {
				continue;
			}
			productOnline = new ProductOnline();
			productOnline.setLogid(UUID.randomUUID().toString());
			productOnline.setProductId(productId);
			//ProductInfo productInfo = SystemUtil.getProductById(productId);
			ProductInfo productInfo = productInfoMapper.selectProductInfoByProductId(productId);
			if (productInfo != null) {
				productOnline.setName(productInfo.getName());
				productOnline.setFullName(productInfo.getFullName());
				productOnline.setTypeId(productInfo.getTypeId());
				productOnline.setSalePrice(productInfo.getSalePrice());
				if (productInfo.getTypeId() == null) {
					productOnline.setTypeId("");
				} else {
					productOnline.setTypeId(productInfo.getTypeId());
				}
				if (productInfo.getFactoryName() == null) {
					productOnline.setFactoryName("");
				} else {
					productOnline.setFactoryName(productInfo.getFactoryName());
				}
				if (productInfo.getBagType() == null) {
					productOnline.setBagType("");
				} else {
					productOnline.setBagType(productInfo.getBagType());
				}
				productOnline.setCorpId(corpId);
				productOnline.setCreateTime(DateUtils.getTime());
			}
			n = n + productOnlineMapper.insertProductOnline(productOnline);
		}
		return n;		
	}
	
	
	/**
	 * ??????????????????????????????
	 * @param vending
	 * @param list
	 * @return
	 */
	private int saveVendingStatistic(Vending vending, List<VendingLanep> list) {
		VendingStatistic vendingStatistic = new VendingStatistic();
		String corpId = ShiroUtils.getCorpId();
		vendingStatistic.setLogid(UUID.randomUUID().toString());
		vendingStatistic.setRptdate(DateUtils.getTime());
		vendingStatistic.setDistrictId(vending.getDistrictId());
		vendingStatistic.setLineId(vending.getLineId());
		vendingStatistic.setPointId(vending.getPointId());
		vendingStatistic.setSiteId(vending.getSiteId());
		vendingStatistic.setSiteName(vending.getSiteName());
		vendingStatistic.setSupplyNum(0);
		vendingStatistic.setOrderNum(0);
		vendingStatistic.setOfflineNum(0);
		Map<String,List<VendingLanep>> map = this.transLanep(list);
		vendingStatistic.setProTNum(map.entrySet().size());
		vendingStatistic.setWarnNum(0);
		vendingStatistic.setProfitMoney(0f);
		vendingStatistic.setCorpId(corpId);
		vendingStatistic.setCreateTime(DateUtils.getTime());
		return vendingStatisticMapper.insertVendingStatistic(vendingStatistic);
	}
	
	/**
	 * ???????????????????????????????????????????????????????????????
	 * @param list
	 * @return
	 */
	private Map<String,List<VendingLanep>> transLanep(List<VendingLanep> list) {
		Map<String,List<VendingLanep>> saveLaneps = new HashMap<String,List<VendingLanep>>();
		for (VendingLanep vl : list) {
			List<VendingLanep> vls = saveLaneps.get(vl.getProductId());
			if (vls == null) {
				vls = new ArrayList<VendingLanep>();
				saveLaneps.put(vl.getProductId(), vls);
			} else {
				vls.add(vl);
			}
		}
		return saveLaneps;
	}
	
	/**
	 * ????????????????????????????????????
	 */
	private int saveVendingStock(List<VendingLanep> list, String siteId, String siteName) {
		Map<String,List<VendingLanep>> saveLaneps = this.transLanep(list);
		
		// ???????????????????????????
		vendingStockMapper.deleteVendingStockBySiteId(siteId);
		Set<String> productIds = saveLaneps.keySet();
		String corpId = ShiroUtils.getCorpId();
		int n = 0;
		for (String productId : productIds) {
			if (StringUtils.isEmpty(productId)) {
				continue;
			}
			VendingStock vendingStock = new VendingStock();
			vendingStock.setLogid(UUID.randomUUID().toString());
			vendingStock.setCorpId(corpId);
			String wproductId = SystemUtil.getSeqId(corpId, "as_vending_stock");
			vendingStock.setWproductId(wproductId);
			if (!StringUtils.isEmpty(productId)) {
				ProductInfo productInfo = SystemUtil.getProductById(productId);
				vendingStock.setProductName(productInfo.getName());
				vendingStock.setPicUrl(productInfo.getPicJson());
				vendingStock.setSalePrice(productInfo.getSalePrice());
				vendingStock.setProductId(productId);
				vendingStock.setTypeId(productInfo.getTypeId());
			} else {
				vendingStock.setProductName("");
				vendingStock.setPicUrl("");
				vendingStock.setSalePrice(0f);
				vendingStock.setProductId("");
				vendingStock.setTypeId("");
			}			
			vendingStock.setSiteId(siteId);
			vendingStock.setSiteName(siteName);
			List<VendingLanep> laneps = saveLaneps.get(productId);
			int num = 0;
			for (VendingLanep lanep : laneps) {
				num = num + lanep.getCurCap();
			}
			vendingStock.setNum(String.valueOf(num));
			vendingStock.setRecoveryNum("0");
			vendingStock.setOverdueNum("0");			
			vendingStock.setCreateTime(DateUtils.getTime());
			n = n + vendingStockMapper.insertVendingStock(vendingStock);
		}
		return n;
	}
	
	/**
	 * ???????????????????????????????????????
	 * @param siteId
	 * @param siteName
	 * @return
	 */
	private int saveVendingState(String siteId, String siteName) {
		VendingState vendingState = vendingStateMapper.selectVendingStateById(siteId);
		if (vendingState != null) {
			return 0;
		}
		vendingState = new VendingState();
		vendingState.setSiteId(siteId);
		vendingState.setLogid(UUID.randomUUID().toString());
		vendingState.setCorpId(ShiroUtils.getCorpId());
		vendingState.setSiteName(siteName);
		vendingState.setContime(0);
		vendingState.setIccid("");
		vendingState.setIpAddress("");
		vendingState.setLoseContime(0);
		vendingState.setNetSate("0");
		vendingState.setReportTime("");
		vendingState.setResoution("");
		vendingState.setScreenType("");
		vendingState.setSeqId("");
		vendingState.setSignalValue(0);
		vendingState.setVAndroid("");
		vendingState.setVBaseband("");
		vendingState.setVFirmware("");
		vendingState.setVVCS("");
		return vendingStateMapper.insertVendingState(vendingState);
	}
	
	/**
	 * ???????????????configxml
	 * @param siteId
	 * @return
	 */
	@SuppressWarnings("finally")
	private void createVendingXml(String siteId) {
		// ???????????????
		Vending vending = vendingMapper.selectVendingBySiteId(siteId);
		// ????????????
		VendingCabinet vendingCabinet = new VendingCabinet();
		vendingCabinet.setSiteId(siteId);
		List<VendingCabinet> cabinets = vendingCabinetMapper.selectVendingCabinetList(vendingCabinet);
		
		Document document = DocumentHelper.createDocument();
        Element root = document.addElement("config");
        if (cabinets == null) {
        	try {
				this.writer(document, siteId);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage());
			} finally {
				return;
			}
        }
        
        for (VendingCabinet cabinet : cabinets) {
        	Element cab = root.addElement("cab");
        	// ???????????????
        	if (cabinet.getHangType().equals(Constant.HANG_TYPE_FALSE)) {
        		cab.setName("mian");
        	} else {	// ??????
        		cab.setName("attach");
        	}
        	cab.addElement("seq").addText(cabinet.getSeqId().toString());
        	// ??????????????????
        	Element cabinetInfo = cab.addElement("cabinetInfo");
        	cabinetInfo.addElement("deviceId").addText(cabinet.getDeviceId());
        	if (StringUtils.isEmpty(cabinet.getVmcId())) {
        		cabinetInfo.addElement("vmcId").addText("");
        	} else {
        		cabinetInfo.addElement("vmcId").addText(cabinet.getVmcId());
        	}
        	cabinetInfo.addElement("hangType").addText(cabinet.getHangType());
        	cabinetInfo.addElement("pointCode").addText(cabinet.getPointCode());
        	cabinetInfo.addElement("factoryId").addText(cabinet.getFactoryId());
        	cabinetInfo.addElement("cabinetType").addText(cabinet.getCabinetType());
        	cabinetInfo.addElement("payType").addText(vending.getPayType());
        	cabinetInfo.addElement("mediaType").addText(vending.getMediaType());
        	cabinetInfo.addElement("netWork").addText(vending.getNetWork());
        	cabinetInfo.addElement("mediaIp").addText(manageConfig.getIp());
        	cabinetInfo.addElement("payIp").addText(SystemUtil.getPayUrl("2"));
        	cabinetInfo.addElement("upLoadUrl").addText(manageConfig.getIp()+"/system/logfile/upload");
        	// ?????????????????????
        	Element laneInfo = cab.addElement("laneInfo");
        	// ??????????????????
        	List<VendingLane> lanes = vendingLaneMapper.selectVendingLaneByCabinetId(cabinet.getCabinetId());
        	if (lanes != null) {
        		for (VendingLane vl : lanes) {
        			Element lane = laneInfo.addElement("lane");
        			lane.addElement("laneId").setText(vl.getLaneId().toString());
        			lane.addElement("row").setText(vl.getRow().toString());
        			lane.addElement("col").setText(vl.getCol().toString());
        			lane.addElement("arrange").setText(vl.getArrange().toString());
        		}
        	}
        	// ??????????????????
        	Element productInfo = cab.addElement("productInfo");
        	List<VendingLanep> laneps = vendingLanepMapper.selectVendingLanepByCabinetId(cabinet.getCabinetId());
        	if (laneps != null) {
        		for (VendingLanep vlp : laneps) {
        			Element product = productInfo.addElement("product");
        			product.addElement("productId").setText(vlp.getProductId());
        			ProductInfo pi = SystemUtil.getProductById(vlp.getProductId());
        			if (pi != null) {
        				product.addElement("productCode").setText(pi.getProductCode());
        				product.addElement("typeName").setText(pi.getTypeName());
        			} else {
        				product.addElement("productCode").setText("");
        				product.addElement("typeName").setText("");
        			}
        			if (StringUtils.isEmpty(vlp.getProductName())) {
        				product.addElement("productName").setText("");
        			} else {
        				product.addElement("productName").setText(vlp.getProductName());
        			}
        			if (vlp.getLaneSId() == null) {
        				product.addElement("laneSId").setText("");
        			} else {
        				product.addElement("laneSId").setText(vlp.getLaneSId().toString());
        			}
        			
        			if (vlp.getLaneEId() == null) {
        				product.addElement("laneEId").setText("");
        			} else {
        				product.addElement("laneEId").setText(vlp.getLaneEId().toString());
        			}
        			
        			if (StringUtils.isEmpty(vlp.getPic())) {
        				product.addElement("picUrl").setText("");
        			} else {
        				product.addElement("picUrl").setText(vlp.getPic());
        			}
        			if (vlp.getSalePrice() == null) {
        				product.addElement("salePrice").setText("0.0");
        			} else {
        				product.addElement("salePrice").setText(vlp.getSalePrice().toString());
        			}
        			if (vlp.getCapacity() == null) {
        				product.addElement("capacity").setText("");
        			} else {
        				product.addElement("capacity").setText(vlp.getCapacity().toString());
        			}
        		}
        	}
        }
        try {
			this.writer(document, siteId);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		} finally {
			return;
		}
	}
	
	/**
     * ???document????????????????????????
     * 
     * @param document
     * @throws Exception
     */
    private void writer(Document document, String siteId) throws Exception {
        // ???????????????
        // OutputFormat format = OutputFormat.createCompactFormat();
        // ?????????????????????
        OutputFormat format = OutputFormat.createPrettyPrint();
        // ????????????
        format.setEncoding("UTF-8");
        // ??????XMLWriter??????,????????????????????????????????????
        // XMLWriter writer = new XMLWriter(new FileWriter(new
        // File("src//a.xml")),format);
        XMLWriter writer = new XMLWriter(
                new OutputStreamWriter(new FileOutputStream(new File(ManageConfig.getUploadPrefix()+"file/"+siteId+".xml")), "UTF-8"), format);
        // ??????
        writer.write(document);
        // ????????????
        writer.flush();
        // ????????????
        writer.close();
    }
    
    /**
     * ??????????????????
     * @param siteId
     * @param newLanepList
     */
    private List<VendingLanep> getUnderProduct(String siteId, List<VendingLanep> newLanepList) throws SupplyVorderExistException {
    	VendingLanep vendingLanep = new VendingLanep();
    	vendingLanep.setSiteId(siteId);
    	List<VendingLanep> lanepList = vendingLanepMapper.selectVendingLanepList(vendingLanep);	// ?????????????????????????????????
    	// ??????????????????????????????????????????????????????????????????????????????????????????
    	List<VendingLanep> underList = new ArrayList<VendingLanep>();	// ??????????????????
    	for (VendingLanep old : lanepList) {
    		if (old.getProductId() == null || old.getProductId().equals("")) {	// ????????????????????????
    			continue;
    		}
    		for (VendingLanep newLanep : newLanepList) {
    			if (newLanep.getSlaneId() == null || newLanep.getSlaneId().equals("")) {	// ??????????????????????????????,???????????????
    				continue;
    			}
    			if (old.getSlaneId().equals(newLanep.getSlaneId())) {
    				if (newLanep.getProductId() == null || newLanep.getProductId().equals("") || !old.getProductId().equals(newLanep.getProductId())) {
    					underList.add(old);	// ??????????????????????????????????????????
    				}
    				continue;
    			}
    		}
    	}
    	
    	// ????????????????????????????????????????????????????????????????????????
//    	SupplyVorder supplyVorder = new SupplyVorder();
//    	supplyVorder.setSiteId(siteId);
//    	supplyVorder.setCurState("1");
//    	List<SupplyVorder> vorderList = supplyVorderMapper.selectSupplyVorderList(supplyVorder);
//    	if (vorderList != null && !vorderList.isEmpty()) {
//    		throw new SupplyVorderExistException();
//    	}
//    	int r = 0;
//    	if (!underList.isEmpty()) {
//    		r = productUnderService.insertProductUnderBatch(underList);	// ??????????????????
//    	}
    	return underList;
    }
    
    /**
     * ??????????????????????????????
     * @param vendingVo
     * @return
     */
    private List<VendingLanep> findAllLanep(VendingVo vendingVo) {
    	// ????????????????????????
    	List<Cols> colsList = vendingVo.getLanes();
		List<VendingLaneVo> zhlanes = new ArrayList<VendingLaneVo>();
		if (colsList != null && !colsList.isEmpty()) {
			for (Cols col : colsList) {
				zhlanes.addAll(col.getCols());
			}
		}
		List<VendingLanep> result = new ArrayList<VendingLanep>();
		for (VendingLaneVo laneVo : zhlanes) {
			VendingLanepVo lanepVo = laneVo.getLanep();
			VendingLanep vendingLanep = new VendingLanep();
			BeanUtils.copyBeanProp(vendingLanep, lanepVo);
			result.add(vendingLanep);
		}
		// ????????????????????????
		List<VendingCabinetVo> cabinets = vendingVo.getCabinets();
		if (cabinets != null && !cabinets.isEmpty()) {
			for (VendingCabinetVo cabinetVo : cabinets) {
				List<Cols> fcolsList = cabinetVo.getLanes();
				if (fcolsList != null && !fcolsList.isEmpty()) {
					for (Cols col : fcolsList) {
						List<VendingLaneVo> lanevoList = col.getCols();
						for (VendingLaneVo laneVo : lanevoList) {
							VendingLanepVo lanepVo = laneVo.getLanep();
							VendingLanep vendingLanep = new VendingLanep();
							BeanUtils.copyBeanProp(vendingLanep, lanepVo);
							result.add(vendingLanep);
						}
					}
				}
			}
		}
		return result;
    }
	
	/**
	 * ???????????????
	 * @param vendingVo
	 * @return
	 */
	@Override
	@Transactional
	public int updateVending(VendingVo vendingVo) throws SupplyVorderExistException, VunderExistException {
		// ?????????????????????????????????sid
		Integer maxsid = vendingLanepMapper.selectLanepMaxSidBySiteId(vendingVo.getSiteId());
		if (maxsid == null) {
			maxsid = 0;
		}
		
		
		
		List<VendingLanep> newVendingLanepList = this.findAllLanep(vendingVo);	// ??????????????????????????????
		List<VendingLanep> underProductList = this.getUnderProduct(vendingVo.getSiteId(), newVendingLanepList);
		if(underProductList!=null&&underProductList.size()!=0) {
			// ??????????????????????????????????????????????????????????????????
			ProductVunder productVunder = new ProductVunder();
			productVunder.setCurState("1");	// ????????????
			productVunder.setSiteId(vendingVo.getSiteId());
			List<ProductVunder> productVunderList = productVunderMapper.selectProductVunderList(productVunder);
			if (productVunderList != null && !productVunderList.isEmpty()) {
				throw new VunderExistException();
			}
			
			// ????????????????????????????????????????????????????????????????????????
			SupplyVorder supplyVorder = new SupplyVorder();
	    	supplyVorder.setSiteId(vendingVo.getSiteId());
	    	supplyVorder.setCurState("1");
	    	List<SupplyVorder> vorderList = supplyVorderMapper.selectSupplyVorderList(supplyVorder);
	    	if (vorderList != null && !vorderList.isEmpty()) {
	    		throw new SupplyVorderExistException();
	    	}
		}
				
		String corpId = ShiroUtils.getCorpId();
		// ?????????????????????????????????????????????????????????
		VendingCabinet ve = new VendingCabinet();
		ve.setHangType(Constant.HANG_TYPE_TRUE);
		ve.setSiteId(vendingVo.getSiteId());
		ve.setCorpId(corpId);
		List<VendingCabinet> cabs = vendingCabinetMapper.selectVendingCabinetList(ve);	// ????????????
		
		Vending vending = new Vending();
		BeanUtils.copyBeanProp(vending, vendingVo);
		
		VendingPoint vp = SystemUtil.getVendingPointFromCache(vending.getPointId());
		vending.setLongitude(vp.getLongitude());
		vending.setLatitude(vp.getLatitude());
		vending.setAddress(vp.getAdderss());
		if (vp != null) {
			vending.setLineId(vp.getLineId());
			vending.setDistrictId(vp.getDistrictId());
		}
		
		int result = vendingMapper.updateVending(vending);
		// ????????????
		VendingCabinet zhCabinet = new VendingCabinet();
		BeanUtils.copyBeanProp(zhCabinet, vendingVo);
		zhCabinet.setLogid(vendingVo.getCabinetLogid());	// ?????????logid
		vendingCabinetMapper.updateVendingCabinet(zhCabinet);
		// ??????????????????
		List<Cols> colsList = vendingVo.getLanes();
		List<VendingLaneVo> zhlanes = new ArrayList<VendingLaneVo>();
		if (colsList != null && !colsList.isEmpty()) {
			for (Cols col : colsList) {
				zhlanes.addAll(col.getCols());
			}
		}
//		List<VendingLaneVo> zhlanes = vendingVo.getLanes();
		List<VendingLanep> saveLaneps = new ArrayList<VendingLanep>();
		for (VendingLaneVo laneVo : zhlanes) {
			VendingLane vendingLane = new VendingLane();
			BeanUtils.copyBeanProp(vendingLane, laneVo);
			// ????????????
			if (vendingLane.getLogid() == null || vendingLane.getLogid().equals("")) {
				vendingLane.setLogid(UUID.randomUUID().toString());
				vendingLane.setCorpId(corpId);
				vendingLane.setCabinetId(zhCabinet.getCabinetId());
				vendingLane.setSlaneId(SystemUtil.getSeqId(corpId, "as_vending_lane"));
				vendingLane.setSiteId(zhCabinet.getSiteId());
				vendingLane.setSiteName(zhCabinet.getSiteName());
				vendingLane.setLaneId(++maxsid);
				vendingLane.setDeviceId(vendingVo.getDeviceId());
				vendingLaneMapper.insertVendingLane(vendingLane);
			} else {	// ????????????
				vendingLaneMapper.updateVendingLane(vendingLane);
			}
			// ??????????????????
			VendingLanepVo vendingLanepVo = laneVo.getLanep();
			
			if (vendingLanepVo != null) {
				VendingLanep vendingLanep = new VendingLanep();
				BeanUtils.copyBeanProp(vendingLanep, vendingLanepVo);
				vendingLanep.setLaneSId(vendingLane.getLaneId());
				vendingLanep.setLaneEId(vendingLane.getLaneId());
				// ??????????????????
				if (vendingLanep.getLogid() == null || vendingLanep.getLogid().equals("")) {
					if (vendingLanep.getProductId() != null && !vendingLanep.getProductId().equals("")) {
						vendingLanep.setSiteId(vendingLane.getSiteId());
						vendingLanep.setSlaneId(vendingLane.getSlaneId());
						vendingLanep.setSiteName(vendingLane.getSiteName());
						vendingLanep.setCorpId(corpId);
						vendingLanep.setLogid(UUID.randomUUID().toString());
						
						//???????????????????????????
						if(StringUtils.isNotEmpty(vendingLanep.getProductId())) {
							if(vendingLanep.getWarnCap()==null||vendingLanep.getWarnCap()<1) {
								throw new LaneProductWarnCapException();
							}
							if(vendingLanep.getCapacity()==null||vendingLanep.getCapacity()<1){
								throw new LaneProductCapacityException();
							}
							if(vendingLanep.getWarnCap()>vendingLanep.getCapacity()) {
								throw new WarnCapGreatThanCapacityException();
							}
						} else {
							vendingLanep.setCapacity(0);
							vendingLanep.setWarnCap(0);
						}
						vendingLanepMapper.insertVendingLanep(vendingLanep);
					}
				} else {
					//???????????????????????????
					if(StringUtils.isNotEmpty(vendingLanep.getProductId())) {
						if(vendingLanep.getWarnCap()==null||vendingLanep.getWarnCap()<1) {
							throw new LaneProductWarnCapException();
						}
						if(vendingLanep.getCapacity()==null||vendingLanep.getCapacity()<1){
							throw new LaneProductCapacityException();
						}
					} else {
						vendingLanep.setCapacity(0);
						vendingLanep.setWarnCap(0);
					}
					vendingLanepMapper.updateVendingLanep(vendingLanep);
				}
				saveLaneps.add(vendingLanep);
			}
		}
		
		// ????????????
		List<VendingCabinetVo> cabinets = vendingVo.getCabinets();
		List<String> cabLogids = new ArrayList<String>();
		
		int seqId = 2;	// ??????seqid?????????
		if (cabs != null && !cabs.isEmpty()) {
			Integer seq = cabs.get(cabs.size()-1).getSeqId();
			if (seq!= null) {
				seqId = seq+1;
			}
		}
		
		for (VendingCabinetVo cabinetVo : cabinets) {
			VendingCabinet vendingCabinet = new VendingCabinet();
			BeanUtils.copyBeanProp(vendingCabinet, cabinetVo);
			
			// ????????????
			if (vendingCabinet.getLogid() == null || vendingCabinet.getLogid().equals("")) {
				vendingCabinet.setSiteId(vending.getSiteId());
				vendingCabinet.setSiteName(vending.getSiteName());
				vendingCabinet.setLogid(UUID.randomUUID().toString());
				vendingCabinet.setCorpId(corpId);
				if (StringUtils.isEmpty(cabinetVo.getVmcId())) {
					vendingCabinet.setVmcId("");
				}
				vendingCabinet.setCabinetId(SystemUtil.getSeqId(corpId, "as_vending_cabinet"));
				vendingCabinet.setCreateTime(DateUtils.getTime());
				vendingCabinet.setReportTime(DateUtils.getTime());
				vendingCabinet.setDescription("???");
				// ??????seqid?????????????????????seqId?????????+1				
				vendingCabinet.setSeqId(seqId++);
				
				vendingCabinetMapper.insertVendingCabinet(vendingCabinet);
			} else {	// ??????
				cabLogids.add(vendingCabinet.getLogid());
				vendingCabinetMapper.updateVendingCabinet(vendingCabinet);
			}
			
//			// ??????????????????
//			List<VendingLaneVo> lanes = cabinetVo.getLanes();
			// ??????????????????
			List<Cols> fcolsList = cabinetVo.getLanes();
			List<VendingLaneVo> lanes = new ArrayList<VendingLaneVo>();
			if (fcolsList != null && !fcolsList.isEmpty()) {
				for (Cols col : fcolsList) {
					lanes.addAll(col.getCols());
				}
			}
			for (VendingLaneVo laneVo : lanes) {
				VendingLane vendingLane = new VendingLane();
				BeanUtils.copyBeanProp(vendingLane, laneVo);
				// ????????????
				if (vendingLane.getLogid() == null || vendingLane.getLogid().equals("")) {
					vendingLane.setLogid(UUID.randomUUID().toString());
					vendingLane.setCorpId(corpId);
					vendingLane.setCabinetId(vendingCabinet.getCabinetId());
					vendingLane.setSlaneId(SystemUtil.getSeqId(corpId, "as_vending_lane"));
					vendingLane.setSiteId(vendingCabinet.getSiteId());
					vendingLane.setSiteName(vendingCabinet.getSiteName());
					vendingLane.setCabinetType(vendingCabinet.getCabinetType());
					vendingLane.setDeviceId(vendingCabinet.getDeviceId());
					vendingLane.setStateTime(DateUtils.getTime());
					vendingLane.setLaneId(++maxsid);
					vendingLane.setDeviceId(vendingVo.getDeviceId());
					vendingLane.setLaneCode(vendingLane.getLaneId());
					vendingLaneMapper.insertVendingLane(vendingLane);
					VendingLanepVo vendingLanepVo = laneVo.getLanep();
					if (vendingLanepVo != null) {
						VendingLanep vendingLanep = new VendingLanep();
						BeanUtils.copyBeanProp(vendingLanep, vendingLanepVo);
						// ??????????????????
						vendingLanep.setSiteId(vendingLane.getSiteId());
						vendingLanep.setSlaneId(vendingLane.getSlaneId());
						vendingLanep.setSiteName(vendingLane.getSiteName());
						vendingLanep.setCorpId(corpId);
						vendingLanep.setLogid(UUID.randomUUID().toString());
						vendingLanep.setLaneSId(maxsid);
						vendingLanep.setLaneEId(maxsid);
						vendingLanep.setCreateTime(DateUtils.getTime());
						
						//???????????????????????????
						if(StringUtils.isNotEmpty(vendingLanep.getProductId())) {
							if(vendingLanep.getWarnCap()==null||vendingLanep.getWarnCap()<1) {
								throw new LaneProductWarnCapException();
							}
							if(vendingLanep.getCapacity()==null||vendingLanep.getCapacity()<1){
								throw new LaneProductCapacityException();
							}
						} else {
							vendingLanep.setCapacity(0);
							vendingLanep.setWarnCap(0);
						}
						saveLaneps.add(vendingLanep);
						vendingLanepMapper.insertVendingLanep(vendingLanep);
					}
				} else {
					vendingLaneMapper.updateVendingLane(vendingLane);
					VendingLanepVo vendingLanepVo = laneVo.getLanep();
					if (vendingLanepVo != null) {
						VendingLanep vendingLanep = new VendingLanep();
						BeanUtils.copyBeanProp(vendingLanep, vendingLanepVo);
						vendingLanep.setLaneSId(vendingLane.getLaneId());
						vendingLanep.setLaneEId(vendingLane.getLaneId());
						
						//???????????????????????????
						if(StringUtils.isNotEmpty(vendingLanep.getProductId())) {
							if(vendingLanep.getWarnCap()==null||vendingLanep.getWarnCap()<1) {
								throw new LaneProductWarnCapException();
							}
							if(vendingLanep.getCapacity()==null||vendingLanep.getCapacity()<1){
								throw new LaneProductCapacityException();
							}
						} else {
							vendingLanep.setCapacity(0);
							vendingLanep.setWarnCap(0);
						}	
						saveLaneps.add(vendingLanep);
						vendingLanepMapper.updateVendingLanep(vendingLanep);	
					}
				}				
			}
		}
		
		// ?????????????????????????????????????????????????????????		
		if (cabs != null && !cabs.isEmpty()) {
			List<String> diff = new ArrayList<String>();
			for (VendingCabinet vc : cabs) {
				if(!cabLogids.contains(vc.getLogid())) {
					diff.add(vc.getLogid());           
				} 
			}
			if (!diff.isEmpty()) {
				for (String logid : diff) {
					vendingLanepMapper.deleteVendingLanepByCabId(logid);	// ??????????????????
					VendingLane vendingLane = new VendingLane();
					vendingLane.setLogid(logid);
					vendingLane.setCorpId(corpId);
					vendingLaneMapper.deleteVendingLaneByCabId(vendingLane);	// ????????????
				}
				
				vendingCabinetMapper.deleteVendingCabinetByIds(diff.toArray(new String[diff.size()]));
			}
		}
		
		// ????????????????????????????????????
		bussinessCacheService.initVending();
		
		// ????????????????????????????????????
		this.saveVendingStock(saveLaneps, vending.getSiteId(), vending.getSiteName());
		// ?????????????????????????????????
		this.saveOnlineProduct(saveLaneps);
		// ????????????xml??????
		this.createVendingXml(vendingVo.getSiteId());
		// ??????supplyConfig???supplyVending??????????????????
		this.updateSupplyUpdateVending(vendingVo.getSiteId(), vending.getLineId());		
		logger.info("??????????????????"+vending.toString());
	    return result;
	}
	
	
	public Vending selectVendingBySiteId(String siteId){
		return vendingMapper.selectVendingBySiteId(siteId);
	}

	@Override
	public List<Vending> selectVendingByCommon(String name, String corpId) {
		return vendingMapper.selectVendingByCommon(name, corpId);
	}

	@Override
	public List<RelationSelectResultVo> selectVendingByCabinetType(String cabinetType, String curState, String corpId, String name) {
		return vendingMapper.selectVendingByCabinetType(cabinetType, curState, corpId, name);
	}

	/**
	 * ???????????????????????????
	 * 
	 * @param pointId ????????????
	 * @return ???????????????
	 */
	@Override
	public Vending selectVendingByPointId(String pointId) {
		return vendingMapper.selectVendingByPointId(pointId);
	}

	@Override
	public List<CabinetTypeSelectVo> selectCabinetTypeByFactoryId(String factoryId) {
		return vendingMapper.selectCabinetTypeByFactoryId(factoryId);
	}

	@Override
	public List<VendingModel> selectDeviceByCabinetType(String cabinetType, String corpId) {
		return vendingMapper.selectDeviceByCabinetType(cabinetType,corpId);
	}

	/**
	 * ?????????????????????
	 * 
	 * @param corpId ????????????
	 * @return
	 */
	@Override
	public Map<String, Integer> selectVendingNum(String corpId) {
		int allNum=vendingMapper.selectVendingAllNum(corpId);
		int onlineNum=vendingMapper.selectVendingOnlineNum(corpId);
		int outlineNum=vendingMapper.selectVendingOutlineNum(corpId);
		int wzNum=vendingMapper.selectVendingWzNum(corpId);
		int delNum=vendingMapper.selectVendingDelNum(corpId);
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("allNum", allNum);
		map.put("onlineNum", onlineNum);
		map.put("outlineNum", outlineNum);
		map.put("wzNum", wzNum);
		map.put("delNum", delNum);
		return map;
	}

	/**
	 * ?????????????????????????????????????????????
	 * 
	 * @param pointIds
	 * @return
	 */
	@Override
	public Integer selectVendingNumByPointIds(String[] pointIds) {
		return vendingMapper.selectVendingNumByPointIds(pointIds);
	}

	@Override
	public Map<String, BigDecimal> selectVendingCap(String siteId) {
		return vendingMapper.selectVendingCap(siteId);
	}
	
	/**
	 * ????????????????????????????????????
	 */
	@Override
	public void updateSupply(String siteId, String lineId) {
		// ???????????? as_supply_config???????????????????????????????????????
		supplyVendingMapper.deleteSupplyVendingBySiteId(siteId);	// ??????as_Supply_Vending???
		
		SupplyConfig supplyConfig = new SupplyConfig();
		supplyConfig.setLineId(lineId);
		List<SupplyConfig> configList = supplyConfigMapper.selectSupplyConfigList(supplyConfig);
		if (configList == null||configList.isEmpty()) {
			return;
		}
		supplyConfig = configList.get(0);
		// ????????????????????????
		int num = supplyConfig.getNum();
		if (num > 0) {
			supplyConfig.setNum(num-1);
		} else {
			supplyConfig.setNum(0);
		}
		// ????????????????????????CurPNum
		Map<String, BigDecimal> capMap = this.selectVendingCap(siteId);
		BigDecimal pcurNum = capMap.get("pcurNum");
		if (pcurNum == null) {
			pcurNum = new BigDecimal(0);
		}
		int curNum = pcurNum.toBigInteger().intValue();
		if (supplyConfig.getCurPNum() - curNum > 0) {
			supplyConfig.setCurPNum(supplyConfig.getCurPNum() - curNum);
		} else {
			supplyConfig.setCurPNum(0);
		}
		// ?????????????????????pMaxNum
		BigDecimal pmaxNum = capMap.get("pmaxNum");
		if (pmaxNum == null) {
			pmaxNum = new BigDecimal(0);
		}
		int maxNum = pmaxNum.toBigInteger().intValue();
		if (supplyConfig.getMaxPNum() - maxNum > 0) {
			supplyConfig.setMaxPNum(supplyConfig.getMaxPNum() - maxNum);
		} else {
			supplyConfig.setMaxPNum(0);
		}
		supplyConfigMapper.updateSupplyConfig(supplyConfig);
	}
	
	/**
	 * ??????????????????????????????????????????????????????
	 * @param siteId
	 * @return
	 */
	private Map<String, Integer> getCurNumMaxNum(String siteId) {
		Map<String, BigDecimal> capMap = this.selectVendingCap(siteId);
		BigDecimal pcurNum = capMap.get("pcurNum");
		if (pcurNum == null) {
			pcurNum = new BigDecimal(0);
		}
		int curNum = pcurNum.toBigInteger().intValue();
		// ?????????????????????pMaxNum
		BigDecimal pmaxNum = capMap.get("pmaxNum");
		if (pmaxNum == null) {
			pmaxNum = new BigDecimal(0);
		}
		int maxNum = pmaxNum.toBigInteger().intValue();
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("curNum", curNum);
		map.put("maxNum", maxNum);
		return map;
	}
	
	/**
	 * ??????supplyConfig???supplyVending????????????????????????????????????????????????
	 * @param siteId
	 * @param lineId
	 */
	private void updateSupplyUpdateVending(String siteId, String lineId) {
		SupplyConfig supplyConfig = new SupplyConfig();
		supplyConfig.setLineId(lineId);
		List<SupplyConfig> configList = supplyConfigMapper.selectSupplyConfigList(supplyConfig);
		if (configList == null||configList.size()==0) {
			return;
		}
		supplyConfig = configList.get(0);
		
		SupplyVending supplyVending = new SupplyVending();
		supplyVending.setSiteId(siteId);
		SupplyVending sv = supplyVendingMapper.selectSupplyVendingBySiteId(siteId);
		if (sv == null) {
			return;
		}
		Integer oldMax = sv.getMaxPNum();	// ???????????????
		if (oldMax == null) {
			oldMax = 0;
		}
				
		Map<String, Integer> map = this.getCurNumMaxNum(siteId);
		Integer maxNum = map.get("maxNum");	// ???????????????
		sv.setMaxPNum(maxNum);
		// ??????supplyVending???maxNum
		supplyVendingMapper.updateSupplyVendingMax(sv);
		// ??????supplyConfig???maxNum
		Integer m = supplyConfig.getMaxPNum();
		if (m == null) {
			return;
		}
		supplyConfig.setMaxPNum(m - oldMax + maxNum);
		supplyConfigMapper.updateSupplyConfigMax(supplyConfig);
	}
	
	/**
	 * ???????????????????????????????????????
	 * @param siteId
	 * @param lineId
	 */
	private void updateSupplyAddVending(String siteId, String siteName, String lineId, String corpId) {
		// 1. ??????as_supply_vending???    2.??????as_supply_config???
		SupplyConfig supplyConfig = new SupplyConfig();
		supplyConfig.setLineId(lineId);
		List<SupplyConfig> configList = supplyConfigMapper.selectSupplyConfigList(supplyConfig);
		if (configList == null||configList.size()==0) {
			return;
		}
		supplyConfig = configList.get(0);
		
		SupplyVending supplyVending = new SupplyVending();
		supplyVending.setLogid(UUID.randomUUID().toString());
		String supplyId = supplyConfig.getSupplyId();
		supplyVending.setSupplyId(supplyId);
		supplyVending.setSiteId(siteId);
		supplyVending.setSiteName(siteName);
		supplyVending.setName(supplyConfig.getName());
		supplyVending.setCorpId(corpId);
		supplyVending.setLineId(lineId);
		Map<String, Integer> map = this.getCurNumMaxNum(siteId);
		int curNum = map.get("curNum");
		supplyVending.setCurPNum(curNum);
		int maxNum = map.get("maxNum");
		supplyVending.setMaxPNum(maxNum);
		supplyVending.setStoryLevel(1);
		supplyVending.setFristlevel(supplyConfig.getFristlevel());
		supplyVending.setTwoLevel(supplyConfig.getTwolevel());
		supplyVending.setThreeLevel(supplyConfig.getThreelevel());
		supplyVending.setCreateTime(DateUtils.getTime());
		supplyVendingMapper.insertSupplyVending(supplyVending);
		// ??????as_supply_config???		
		Integer num = supplyConfig.getNum();
		
		if (num == null) {
			num = 0;
		}
		supplyConfig.setNum(num + 1);
		// ?????????????????????pMaxNum
		Integer maxPNum = supplyConfig.getMaxPNum();
		if (maxPNum == null) {
			maxPNum = 0;
		}
		supplyConfig.setMaxPNum(maxPNum + maxNum);
		supplyConfigMapper.updateSupplyConfig(supplyConfig);
	}

	/**
	 * ?????????????????????????????????
	 * 
	 * @param ids
	 * @return
	 */
	@Override
	public List<Vending> selectVendingByIds(String ids) {
		return vendingMapper.selectVendingByIds(ids.split(","));
	}

	/**
	 * ?????????????????????
	 * 
	 * @param ids
	 * @return
	 */
	@Override
	@Transactional
	public int deleteVendingByIds(String[] ids) {
		List<Vending> list = vendingMapper.selectVendingByIds(ids);
		if(list!=null&&!list.isEmpty()) {
			for (Vending vending : list) {
				String siteId=vending.getSiteId();
				logger.info("?????????????????????:??????:"+DateUtils.getTime()+",?????????:"+JSONObject.toJSONString(vending));
				//???????????????????????????
				vendingStockMapper.deleteVendingStockBySiteId(siteId);
				//???????????????????????????
				vendingCabinetMapper.deleteVendingCabinetBySiteId(siteId);
				//???????????????????????????
				vendingLaneMapper.deleteVendingLaneBySiteId(siteId);
				//?????????????????????????????????
				vendingLanepMapper.deleteVendingLanepBySiteId(siteId);
				//???????????????????????????
				vendingStateMapper.deleteVendingStateById(siteId);
				//???????????????????????????
				vendingStatisticMapper.deleteVendingStatisticBySiteId(siteId);
			}
		}
		
		int r = vendingMapper.deleteVendingByIds(ids);
		bussinessCacheService.initVending();
		return r;
	}

	/**
	 * ?????????????????????????????????????????????
	 * 
	 * @param cabinetType
	 * @param corpId
	 * @param name
	 * @return
	 */
	@Override
	public List<RelationSelectResultVo> selectNeverDelVendingByCabinetType(String cabinetType, String corpId,
			String name) {
		return vendingMapper.selectNeverDelVendingByCabinetType(cabinetType,corpId,name);
	}

	/**
	 * ?????????????????????
	 * 
	 * @param v
	 */
	@Override
	@Transactional
	public int deleteByFlag(Vending v) {
		int result = vendingMapper.updateVending(v);
		Vending vending = vendingMapper.selectVendingById(v.getLogid());
		// ???????????????????????????
	 	bussinessCacheService.initVendingNum();
	 	// ????????????????????????????????????
	 	bussinessCacheService.initVending();
	 	updateSupply(vending.getSiteId(), vending.getLineId());
		return result;
	}

	/**
	 * ???????????????
	 * 
	 * @param vendingLanep
	 * @return
	 */
	@Override
	@Transactional
	public int updateVendingLanep(VendingLanep vendingLanep) {
		String siteId=vendingLanep.getSiteId();
		String productId=vendingLanep.getProductId();
		Integer laneSId = vendingLanep.getLaneSId();
		Integer laneEId = vendingLanep.getLaneEId();
		String slaneId = vendingLanep.getSlaneId();
		Integer capacity = vendingLanep.getCapacity();
		Integer curCap = vendingLanep.getCurCap();
		Integer warnCap = vendingLanep.getWarnCap();
		String siteName="";
		String corpId=ShiroUtils.getCorpId();
		Vending vending = SystemUtil.getVendingBase(siteId);
		if(vending!=null) {
			siteName=vending.getSiteName();
			corpId=vending.getCorpId();
		}
		//????????????
		if(StringUtils.isEmpty(vendingLanep.getProductId())) {
			return 2;
		}
		VendingLanep srcLanep = vendingLanepMapper.selectVendingLanepBySlaneId(slaneId);
		ProductInfo productInfo = productInfoMapper.selectProductInfoByProductId(productId);
		//?????????????????????
		if(srcLanep==null) {
			return 3;
		}
		//??????????????????????????????
		if(StringUtils.isEmpty(srcLanep.getProductId())) {
			return 4;
		}
		//????????????????????????????????????
		if(srcLanep.getProductId().equals(productId)) {
			return 5;
		}
		//??????????????????????????????0
		if(curCap<0) {
			return 6;
		}
		//??????????????????????????????????????????
		if(curCap>srcLanep.getCapacity()) {
			return 7;
		}
		//??????????????????????????????0
		if(capacity<=0) {
			return 8;
		}
		//????????????????????????0
		if(warnCap<=0) {
			return 9;
		}
		//??????????????????????????????
		if(warnCap>capacity) {
			return 10;
		}
		
		//???????????????????????????????????????
		SupplyVorder supplyVorder = new SupplyVorder();
		supplyVorder.setSiteId(siteId);
		supplyVorder.setCurState(Constant.SUPPLY_ORDER_CUR_STATE_WAIT);
		List<SupplyVorder> supplyVorderList = supplyVorderMapper.selectSupplyVorderList(supplyVorder);
		if(supplyVorderList!=null&&!supplyVorderList.isEmpty()) {
			return 11;
		}
		//????????????????????????????????????
		ProductLunder productLunder = new ProductLunder();
		productLunder.setSiteId(siteId);
		productLunder.setLaneSId(laneSId);
		productLunder.setLaneEId(laneEId);
		productLunder.setCurState(Constant.UNDER_STATE_WAIT);
		List<ProductLunderVo> productLunderList = productLunderMapper.selectProductLunderList(productLunder);
		if(productLunderList!=null&&!productLunderList.isEmpty()) {
			return 12;
		}
		//????????????????????????????????????
		OrderVo orderVo = new OrderVo();
		orderVo.setSiteId(siteId);
		orderVo.setOrderType("1");
		List<OrderVo> orderApplyList = orderApplyMapper.selectOrderApplyList(orderVo);
		if(orderApplyList!=null&&!orderApplyList.isEmpty()) {
			return 13;
		}
		
		//????????????????????????,????????????????????????0
		VendingLanep vendingLanepUpdate = new VendingLanep();
		vendingLanepUpdate.setSlaneId(vendingLanep.getSlaneId());
		vendingLanepUpdate.setLogid(vendingLanep.getLogid());
		vendingLanepUpdate.setSiteId(siteId);
		vendingLanepUpdate.setSiteName(siteName);
		vendingLanepUpdate.setLaneSId(laneSId);
		vendingLanepUpdate.setLaneEId(laneEId);
		vendingLanepUpdate.setProductId(productId);
		vendingLanepUpdate.setLaneSate(vendingLanep.getLaneSate());
		vendingLanepUpdate.setPStateTime(vendingLanep.getPStateTime());
		vendingLanepUpdate.setLSateTime(vendingLanep.getLSateTime());
		vendingLanepUpdate.setProductName(productInfo.getName());
		vendingLanepUpdate.setProductPic(productInfo.getPicJson());
		vendingLanepUpdate.setSalePrice(productInfo.getSalePrice());
		vendingLanepUpdate.setCurCap(0);
		vendingLanepUpdate.setCapacity(capacity);
		vendingLanepUpdate.setWarnCap(warnCap);
		vendingLanepUpdate.setProductSate("1");
		vendingLanepMapper.updateVendingLanep(vendingLanepUpdate);
		//???????????????????????????
		VendingLanep vendingLanepSelect = new VendingLanep();
		vendingLanepSelect.setSiteId(siteId);
		List<VendingLanep> vendingLanepList = vendingLanepMapper.selectVendingLanepList(vendingLanepSelect);
		// ????????????????????????????????????
		this.saveVendingStock(vendingLanepList, siteId, siteName);
		
		if(curCap>0) {
			//???????????????
			srcLanep.setCurCap(curCap);
			this.vendingProductUnder(srcLanep);
			//??????????????????
			SupplyConfig supplyConfig = supplyConfigMapper.selectSupplyConfigByLineId(vending.getLineId());
			if(supplyConfig!=null) {
				String stockId = supplyConfig.getWmId();
				StockWarehouse stockWarehouse = stockWarehouseMapper.selectStockWarehouseByProductAndStock(srcLanep.getProductId(), stockId, corpId);
				Integer curNum = stockWarehouse.getCurNum();
				stockWarehouse.setCurNum(curNum+curCap);
				stockWarehouseMapper.updateStockWarehouse(stockWarehouse);
			}
			//??????????????????
			StockProduct stockProduct = stockProductMapper.selectStockProductByCorpIdAndProdcutId(corpId, srcLanep.getProductId());
			stockProduct.setCurNum(stockProduct.getCurNum()+curCap);
			stockProductMapper.updateStockProduct(stockProduct);
		}
		
		// ??????supplyConfig???supplyVending??????????????????
		this.updateSupplyUpdateVending(siteId, vending.getLineId());
		//??????xml????????????
		this.createVendingXml(siteId);
		//????????????????????????
		this.saveOnlineProduct(vendingLanepList);
		return 1;
	}
	
	/**
	 * ???????????????
	 * 
	 * @param vendingLanep
	 * @return
	 */
	public void vendingProductUnder(VendingLanep vendingLanep) {
		String siteId = vendingLanep.getSiteId();
		Vending vending = vendingMapper.selectVendingBySiteId(siteId);
		String corpId=vending.getCorpId();
		String lineId=vending.getLineId();
		Integer underNum = vendingLanep.getCurCap();
		String vUnderId = SystemUtil.getSeqId(corpId, "as_product_vunder");
		String underId = SystemUtil.getSeqId(corpId, "as_product_under");
		String lunderId = SystemUtil.getSeqIdLong(corpId, "product_lunder_id");
		String productId = vendingLanep.getProductId();
		ProductVunder productVunder = new ProductVunder();
		//????????????????????????
		productVunder.setLogid(UUID.randomUUID().toString());
		productVunder.setvUnderId(vUnderId);		
		productVunder.setCorpId(corpId);
		productVunder.setSiteId(siteId);
		productVunder.setLineId(lineId);
		//??????????????????????????????
		SupplyConfig supplyConfig = supplyConfigMapper.selectSupplyConfigByLineId(vending.getLineId());
		if(supplyConfig==null||StringUtils.isEmpty(supplyConfig.getSupplierId())) {
			productVunder.setLoginId("");
		}else {
			productVunder.setLoginId(supplyConfig.getSupplierId());
		}
		productVunder.setCurState(Constant.UNDER_STATE_FINISH);
		productVunder.setStateTime(DateUtils.getTime());
		productVunder.setCreateTime(DateUtils.getTime());
		productVunder.setUnderNum(underNum);
		List<ProductVunder> productVunderList = new ArrayList<ProductVunder>();
		productVunderList.add(productVunder);
		productVunderMapper.insertProductVunderBatch(productVunderList);
		//??????????????????????????????
		ProductUnder productUnder = new ProductUnder();
		productUnder.setLogid(UUID.randomUUID().toString());
		productUnder.setUnderId(underId);
		productUnder.setvUnderId(vUnderId);
		productUnder.setUnderNum(underNum);
		productUnder.setDistrictId(vending.getDistrictId());
		productUnder.setLineId(vending.getLineId());
		productUnder.setPointId(vending.getPointId());
		productUnder.setSiteId(vending.getSiteId());
		productUnder.setSiteName(vending.getSiteName());
		ProductInfo productInfo = productInfoMapper.selectProductInfoByProductId(productId);
		productUnder.setProductId(productId);
		if(productInfo!=null) {
			productUnder.setName(productInfo.getName());
			productUnder.setFullName(productInfo.getFullName());
		}
		productUnder.setCurState(Constant.UNDER_STATE_FINISH);
		productUnder.setStateTime(DateUtils.getTime());
		productUnder.setUnderType("1");
		productUnder.setCorpId(vending.getCorpId());
		productUnder.setCreateTime(DateUtils.getTime());
		productUnderMapper.insertProductUnder(productUnder);
		//????????????????????????????????????
		ProductLunder productLunder = new ProductLunder();
		productLunder.setLogid(UUID.randomUUID().toString());						
		productLunder.setLunderId(lunderId);
		productLunder.setUnderId(underId);
		productLunder.setvUnderId(vUnderId);
		productLunder.setDistrictId(vending.getDistrictId());
		productLunder.setLineId(vending.getLineId());
		productLunder.setPointId(vending.getPointId());
		productLunder.setSiteId(vending.getSiteId());
		productLunder.setSiteName(vending.getSiteName());
		productLunder.setProductId(vendingLanep.getProductId());
		productLunder.setUnderNum(underNum);
		productLunder.setLaneSId(vendingLanep.getLaneSId());
		productLunder.setLaneEId(vendingLanep.getLaneEId());
		productLunder.setCurState(Constant.UNDER_STATE_FINISH);
		productLunder.setCorpId(corpId);
		productLunder.setCreateTime(DateUtils.getTime());
		productLunderMapper.insertProductLunder(productLunder);
	}

	/**
	 * ????????????????????????
	 * 
	 * @param vending
	 * @return
	 */
	@Override
	public List<Vending> selectNeverDelVendingList(Vending vending) {
		return vendingMapper.selectNeverDelVendingList(vending);
	}

	/**
	 * ?????????????????????
	 * 
	 * @param vending
	 * @return
	 */
	@Override
	@Transactional
	public AjaxResult saveImportVending(Vending vending) {
		AjaxResult result = checkVending(vending);
		if(!result.isSuccess()) {
			return result;
		}
		vending.setLogid(UUID.randomUUID().toString());
		String corpId = ShiroUtils.getCorpId();
		vending.setCorpId(corpId);
		String siteId = SystemUtil.getSeqIdShort(corpId, "as_vending");
		String siteName = vending.getSiteName();
		vending.setSiteId(siteId);
		vending.setCurState(Constant.VENDING_CURSTATE_NOTREGISTER);
		VendingPoint vp = SystemUtil.getVendingPointFromCache(vending.getPointId());
		vending.setLongitude(vp.getLongitude());
		vending.setLatitude(vp.getLatitude());
		vending.setAddress(vp.getAdderss());
		vending.setNetSate("1");	// ????????????
		vending.setOnlineTime(DateUtils.getTime());
		vending.setInitTime(DateUtils.getTime());
		vending.setStateTime(DateUtils.getTime());
		vending.setNetTime(DateUtils.getTime());
		//??????????????????,?????????????????????????????????
		if("04".equals(vending.getCabinetType())) {
			vending.setPlatType(Constant.PLATTYPE_SINGLECHIP);
		}else {
			//????????????????????????
			vending.setPlatType(Constant.PLATTYPE_BASE);
		}
		
		if (vp != null) {
			vending.setLineId(vp.getLineId());
			vending.setDistrictId(vp.getDistrictId());
		}
		
		// ????????????
		VendingCabinet cabinet = new VendingCabinet();
		cabinet.setSiteId(siteId);
		cabinet.setSiteName(siteName);
		cabinet.setLogid(UUID.randomUUID().toString());
		cabinet.setCorpId(corpId);
		cabinet.setHangType(Constant.HANG_TYPE_FALSE);
		String cabinetId = SystemUtil.getSeqId(corpId, "as_vending_cabinet");
		cabinet.setCabinetId(cabinetId);;
//		zhCabinet.setViewName("1");
		cabinet.setSeqId(1);
		cabinet.setDeviceId(vending.getDeviceId());
		cabinet.setVmcId("");
		cabinet.setCreateTime(DateUtils.getTime());
		cabinet.setDescription("");
		cabinet.setPointCode("");
		cabinet.setFactoryId(vending.getFactoryId());
		String cabinetType = vending.getCabinetType();
		cabinet.setCabinetType(cabinetType);
		cabinet.setReportTime(DateUtils.getTime());
//		zhCabinet.setCabinetName("??????");
		vendingCabinetMapper.insertVendingCabinet(cabinet);	
		
		//??????????????????
		String deviceId = vending.getDeviceId();
		VendingModel vendingModel = vendingModelMapper.selectVendingModelById(deviceId);
		Integer laneNum = vendingModel.getLaneNum();
		Integer row = vendingModel.getRow();
		Integer col = vendingModel.getCol();
		int laneId=1;
		List<VendingLane> laneList = new ArrayList<VendingLane>();
		List<VendingLanep> lanepList = new ArrayList<VendingLanep>();
		//?????????????????????????????????
		for (int i = 1; i <= row; i++) {
			for (int j = 1; j <= col; j++) {
				VendingLane vendingLane = new VendingLane();
				VendingLanep vendingLanep = new VendingLanep();
				String slaneId = SystemUtil.getSeqId(corpId, "as_vending_lane");
				vendingLane.setLogid(UUID.randomUUID().toString());
				vendingLane.setSlaneId(slaneId);
				vendingLane.setSiteId(siteId);
				vendingLane.setSiteName(siteName);
				vendingLane.setLaneId(laneId);
				vendingLane.setCabinetType(cabinetType);
				vendingLane.setCabinetId(cabinetId);
				vendingLane.setDeviceId(deviceId);
				vendingLane.setLaneCode(laneId);
				vendingLane.setCol(j);
				vendingLane.setRow(i);
				vendingLane.setArrange(1);
				vendingLane.setCurState(Constant.VENDING_LANESTATE_NORMAL);
				vendingLane.setStateTime(DateUtils.getTime());
				vendingLane.setCreateTime(DateUtils.getTime());
				vendingLane.setCorpId(corpId);
				
				vendingLanep.setLogid(UUID.randomUUID().toString());
				vendingLanep.setSlaneId(slaneId);
				vendingLanep.setSiteId(siteId);
				vendingLanep.setSiteName(siteName);
				vendingLanep.setLaneSId(laneId);
				vendingLanep.setLaneEId(laneId);
				vendingLanep.setProductId("");
				vendingLanep.setProductPic("");
				vendingLanep.setSalePrice(0.0F);
				Integer capacity = vendingModel.getCapacity();
				if(capacity==null) {
					capacity=0;
				}
				Integer warnCap = vendingModel.getWarnCap();
				if(warnCap==null) {
					warnCap=0;
				}
				vendingLanep.setCapacity(capacity);
				vendingLanep.setWarnCap(warnCap);
				vendingLanep.setCurCap(0);
				vendingLanep.setProductSate("1");
				vendingLanep.setPStateTime("");
				vendingLanep.setLaneSate(Constant.VENDING_LANESTATE_NORMAL);
				vendingLanep.setLSateTime("");;
				vendingLanep.setCreateTime(DateUtils.getTime());
				vendingLanep.setCorpId(corpId);
				laneList.add(vendingLane);
				lanepList.add(vendingLanep);
				laneId++;
				vendingLanepMapper.insertVendingLanep(vendingLanep);
			}
		}
		
		
		vending.setLaneNum(laneNum);
		vending.setPmaxNum(0);
		vending.setPcurNum(0);
		vending.setStockLevel(0);
		vending.setSellTime(DateUtils.getTime());
		vending.setCreateTime(DateUtils.getTime());
		vending.setDescription("");

		vending.setConfigFile(manageConfig.getVendingXmlPath()+vending.getSiteId()+".xml");
		// ???????????????
		vendingMapper.insertVending(vending);		
		// ????????????
		vendingLaneMapper.insertVendingLaneBatch(laneList);
//		vendingLanepMapper.insertVendingLanepBatch(lanepList);
		// ??????????????????
//		vendingLanepMapper.insertVendingLanepBatch(saveLaneps);
		// ????????????????????????????????????
		createVendingXml(siteId);
		
		// ??????????????????
		this.saveVendingState(siteId, vending.getSiteName());
//		// ????????????????????????????????????
//		this.saveVendingStock(saveLaneps, siteId, vending.getSiteName());
		// ??????????????????????????????
		this.saveVendingStatistic(vending, lanepList);
//		// ?????????????????????????????????
//		this.saveOnlineProduct(saveLaneps);
		// ??????????????????
		this.updateSupplyAddVending(siteId, vending.getSiteName(), vending.getLineId(), corpId);
		logger.info("??????????????????"+vending.toString());
		String registResult = registerVending(vending);
		if(!"0000".equals(registResult)) {
			logger.info("????????????????????????????????????"+vending.getSiteId()+"??????,?????????:"+registResult+",??????"+DateUtils.getTime()+"");
			if("1014".equals(registResult)) {
				return AjaxResult.error("????????????????????????,?????????????????????");
			}else {
				return AjaxResult.error("????????????");
			}
		}
		return AjaxResult.success();
	}
	
	/**
	 * ????????????
	 * 
	 * @param vending
	 * @return
	 */
	private AjaxResult checkVending(Vending vending) {
		String siteName = vending.getSiteName();
		String siteCode = vending.getSiteCode();
		String factoryName = vending.getFactoryName();
		String cabinetTypeName = vending.getCabinetTypeName();
		String deviceCode = vending.getDeviceCode();
		String pointName = vending.getPointName();
		String netWorkName = vending.getNetWorkName();
		String sellStateName = vending.getSellStateName();
		String payTypeName = vending.getPayTypeName();
		String mediaTypeName = vending.getMediaTypeName();
		if(StringUtils.isEmpty(siteName)) {
			return AjaxResult.error("???????????????????????????");
		}
		if(siteName.length()>50) {
			return AjaxResult.error("???????????????????????????50?????????");
		}
		Vending vendingName = new Vending();
		vendingName.setSiteName(siteName);
		vendingName.setCorpId(ShiroUtils.getCorpId());
		Vending nameExist = vendingMapper.selectVendingExist(vendingName);
		if(nameExist!=null) {
			return AjaxResult.error("?????????????????????");
		}
		if(StringUtils.isEmpty(siteCode)) {
			return AjaxResult.error("???????????????????????????");
		}
		if(siteCode.length()>30) {
			return AjaxResult.error("???????????????????????????50?????????");
		}
		Vending vendingCode = new Vending();
		vendingCode.setSiteCode(siteCode);
		vendingCode.setCorpId(ShiroUtils.getCorpId());
		Vending codeExist = vendingMapper.selectVendingExist(vendingCode);
		if(codeExist!=null) {
			return AjaxResult.error("?????????????????????");
		}
		if(StringUtils.isEmpty(factoryName)) {
			return AjaxResult.error("????????????????????????");
		}
		DictData dictData = new DictData();
		dictData.setDictType("sys_factory");
		dictData.setDictLabel(factoryName);
        List<DictData> factoryList = dictDataMapper.selectDictList(dictData);
        if(StringUtils.isEmpty(factoryList)) {
        	return AjaxResult.error("?????????????????????");
        }
        vending.setFactoryId(factoryList.get(0).getDictValue());
        if(StringUtils.isEmpty(cabinetTypeName)) {
        	return AjaxResult.error("????????????????????????");
        }
        String cabinetType = getCode(cabinetTypeName);
        dictData = new DictData();
		dictData.setDictType("sys_cabinet_type");
		dictData.setDictValue(cabinetType);
        List<DictData> cabinetList = dictDataMapper.selectDictList(dictData);
        if(StringUtils.isEmpty(cabinetList)) {
        	return AjaxResult.error("?????????????????????");
        }
        vending.setCabinetType(cabinetType);
        if(StringUtils.isEmpty(deviceCode)) {
        	return AjaxResult.error("??????????????????");
        }
        String[] split = deviceCode.split("--");
        String deviceId = split[split.length-1];
        VendingModel vendingModel = vendingModelMapper.selectVendingModelById(deviceId);
        if(vendingModel==null) {
        	return AjaxResult.error("???????????????");
        }
        vending.setDeviceId(deviceId);
        if(StringUtils.isEmpty(pointName)) {
        	return AjaxResult.error("??????????????????");
        }
        String pointId = getCodeBySeparate(pointName);
        VendingPoint vendingPoint = vendingPointMapper.selectVendingPointByPointId(pointId);
        if(vendingPoint==null) {
        	return AjaxResult.error("???????????????");
        }
        VendingPoint point = new VendingPoint();
        point.setPointId(pointId);
        Vending vendingBind = new Vending();
        vendingBind.setPointId(pointId);
        List<Vending> vendingList = vendingMapper.selectNeverDelVendingList(vendingBind);
        if(StringUtils.isNotEmpty(vendingList)) {
        	return AjaxResult.error("??????????????????????????????");
        }
        vending.setPointId(pointId);
        if(StringUtils.isEmpty(netWorkName)) {
        	return AjaxResult.error("????????????????????????");
        }
        String netWork = getCodeBySeparate(netWorkName);
        dictData=new DictData();
        dictData.setDictType("sys_net_work");
        dictData.setDictValue(netWork);
        List<DictData> netWorkList = dictDataMapper.selectDictList(dictData);
        if(StringUtils.isEmpty(netWorkList)) {
        	return AjaxResult.error("?????????????????????");
        }
        vending.setNetWork(netWork);
        
        if(StringUtils.isEmpty(sellStateName)) {
        	return AjaxResult.error("????????????????????????");
        }
        String sellState = getCodeBySeparate(sellStateName);
        dictData=new DictData();
        dictData.setDictType("sys_sell_state");
        dictData.setDictValue(sellState);
        List<DictData> sellStateList = dictDataMapper.selectDictList(dictData);
        if(StringUtils.isEmpty(sellStateList)) {
        	return AjaxResult.error("?????????????????????");
        }
        vending.setSellState(sellState);
        String payType = payTypeName.trim();
        if(StringUtils.isEmpty(payType)) {
        	return AjaxResult.error("????????????????????????");
        }
        if("01".equals(payType)||"02".equals(payType)||"01,02".equals(payType)) {
        	vending.setPayType(payType);
        }else {
        	return AjaxResult.error("??????????????????");
        }
        if(StringUtils.isEmpty(mediaTypeName)) {
        	return AjaxResult.error("????????????????????????");
        }
        String mediaType = getCodeBySeparate(mediaTypeName);
        if("1".equals(mediaType)||"2".equals(mediaType)) {
        	vending.setMediaType(mediaType);
        }else {
        	return AjaxResult.error("?????????????????????");
        }
        return AjaxResult.success();
	}
	
	/**
	 * ????????????????????????
	 * 
	 * @param item
	 * @return
	 */
	private String getCode(String item) {
		//?????????????????????????????????
		Matcher matcher = Pattern.compile("[0-9]").matcher(item);
		matcher.find();
		int start = matcher.start();
		//????????????
		return item.substring(start);
	}
	
	/**
	 * ????????????????????????
	 * 
	 * @param item ??????????????????
	 * @param regex ?????????
	 * @return
	 */
	private String getCodeBySeparate(String item,String regex) {
		String[] split = item.split(regex);
		return split[split.length-1];
	}
	
	/**
	 * ????????????????????????
	 * 
	 * @param item
	 * @return
	 */
	private String getCodeBySeparate(String item) {
		return getCodeBySeparate(item,"--");
	}

	
	/**
	 * ????????????
	 * 
	 * @param vendingLanep
	 * @return
	 */
	@Override
	public AjaxResult lockLane(VendingLanep vendingLanep) {
		VendingLanep lanep = vendingLanepMapper.selectVendingLanepBySlaneId(vendingLanep.getSlaneId());
		String laneSate = lanep.getLaneSate();
		//??????????????????????????????????????????????????????
		switch (laneSate) {
			case Constant.VENDING_LANESTATE_NORMAL:
				
			case Constant.VENDING_LANESTATE_WAIT_LOCK:
				VendingLane vendingLane = new VendingLane();
				vendingLane.setSlaneId(vendingLanep.getSlaneId());
				vendingLane.setCurState(Constant.VENDING_LANESTATE_WAIT_LOCK);
				vendingLaneMapper.updateVendingLaneBySlaneId(vendingLane);
				lanep.setLaneSate(Constant.VENDING_LANESTATE_WAIT_LOCK);
				vendingLanepMapper.updateVendingLanepBySlaneId(lanep);
				int result = insertVendingCmd(lanep,Constant.CMD_VENDING_LANE_LOCK);
				if(result==1) {
					return AjaxResult.success();
				}else {
					return AjaxResult.error("??????????????????????????????");
				}
			case Constant.VENDING_LANESTATE_LOCKED:
				return AjaxResult.error("????????????????????????,?????????????????????");
			case Constant.VENDING_LANESTATE_WAIT_UNLOCK:
				return AjaxResult.error("??????????????????????????????,???????????????????????????");
			case Constant.VENDING_LANESTATE_DAMAGE:
				return AjaxResult.error("????????????????????????,??????????????????????????????????????????????????????");
			default:
				return AjaxResult.error("??????????????????,??????????????????");
		}
	}

	/**
	 * ????????????
	 * 
	 * @param vendingLanep
	 * @return
	 */
	@Override
	@Transactional
	public AjaxResult unlockLane(VendingLanep vendingLanep) {
		VendingLanep lanep = vendingLanepMapper.selectVendingLanepBySlaneId(vendingLanep.getSlaneId());
		String laneSate = lanep.getLaneSate();
		//????????????,????????????????????????????????????????????????
		switch (laneSate) {
			case Constant.VENDING_LANESTATE_NORMAL:
				return AjaxResult.error("????????????????????????,????????????");
			case Constant.VENDING_LANESTATE_WAIT_LOCK:
				return AjaxResult.error("??????????????????????????????,?????????????????????????????????????????????");
			case Constant.VENDING_LANESTATE_LOCKED:
				
			case Constant.VENDING_LANESTATE_WAIT_UNLOCK:

			case Constant.VENDING_LANESTATE_DAMAGE:
				VendingLane vendingLane = new VendingLane();
				vendingLane.setSlaneId(vendingLanep.getSlaneId());
				vendingLane.setCurState(Constant.VENDING_LANESTATE_WAIT_UNLOCK);
				vendingLaneMapper.updateVendingLaneBySlaneId(vendingLane);
				lanep.setLaneSate(Constant.VENDING_LANESTATE_WAIT_UNLOCK);
				vendingLanepMapper.updateVendingLanepBySlaneId(lanep);
				int result = insertVendingCmd(lanep,Constant.CMD_VENDING_LANE_UNLOCK);
				if(result==1) {
					return AjaxResult.success();
				}else {
					return AjaxResult.error("??????????????????????????????");
				}
			default:
				return AjaxResult.error("??????????????????,??????????????????");
		}
	}
	
	/**
	 * ??????????????????
	 * 
	 * @param vendingUpgrade
	 * @return
	 */
	public int insertVendingCmd(VendingLanep vendingLanep,String cmd) {
		VendingCmd vendingCmd = new VendingCmd();
		vendingCmd.setCmdCode(vendingLanep.getProductId());
		vendingCmd.setCmdType(Constant.VENDING_CMD_TYPE_PRODUCT);
		vendingCmd.setCmd(cmd);
		Map<String, String> map = new HashMap<String,String>();
		map.put("LaneSId", vendingLanep.getLaneSId().toString());
		map.put("LaneEId", vendingLanep.getLaneEId().toString());
		map.put("ProductId", vendingLanep.getProductId());
		vendingCmd.setCont(JSONObject.toJSONString(map));
		return vendingCmdService.insertVendingCmdQuick(vendingLanep.getSiteId(),vendingCmd,vendingLanep.getCorpId()); 
	}

}
