package com.manage.project.system.advert.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manage.project.system.advert.mapper.AdvertConfigMapper;
import com.manage.project.system.advert.mapper.AdvertDeviceMapper;
import com.manage.project.system.advert.mapper.AdvertMaterialMapper;
import com.manage.project.system.advert.mapper.AdvertMstrategyMapper;
import com.manage.project.system.advert.mapper.AdvertStrategyMapper;
import com.manage.project.common.Constant;
import com.manage.project.common.vo.CommonVo;
import com.manage.project.system.advert.domain.AdvertConfig;
import com.manage.project.system.advert.domain.AdvertDevice;
import com.manage.project.system.advert.domain.AdvertMaterial;
import com.manage.project.system.advert.domain.AdvertMstrategy;
import com.manage.project.system.advert.domain.AdvertStrategy;
import com.manage.project.system.advert.service.IAdvertConfigService;
import com.manage.project.system.advert.vo.Adevice;
import com.manage.project.system.advert.vo.AdvertPlat;
import com.manage.project.system.advert.vo.AdvertSaveVo;
import com.manage.project.system.advert.vo.EditAdvertDeviceVo;
import com.manage.project.system.advert.vo.Mstrategy;
import com.manage.project.system.advert.vo.Strategy;
import com.manage.project.system.util.SystemUtil;
import com.manage.project.system.vending.domain.VendingCmd;
import com.manage.project.system.vending.domain.VendingUpgradeTask;
import com.manage.project.system.vending.service.IVendingCmdService;

import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.MultimediaInfo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.manage.common.exception.CmdNoticeDeviceFailedException;
import com.manage.common.exception.advert.AdvertNotInvalidException;
import com.manage.common.exception.advert.AdvertPlayTimeIsNull;
import com.manage.common.exception.advert.AdvertPlayTimeLessThanZero;
import com.manage.common.exception.advert.AdvertPlayTimesIsNull;
import com.manage.common.exception.advert.AdvertPlayTimesLessThanZero;
import com.manage.common.exception.advert.AdvertStrategyPlayETimeEmptyException;
import com.manage.common.exception.advert.AdvertStrategyPlaySTimeEarlyThanNowException;
import com.manage.common.exception.advert.AdvertStrategyPlaySTimeEmptyException;
import com.manage.common.exception.advert.AdvertStrategyPlaySTimeLaterThanPlayETimeException;
import com.manage.common.exception.advert.AdvertStrategyPointNameNotExit;
import com.manage.common.exception.advert.AdvertStrategyTypeIsEmptyException;
import com.manage.common.exception.advert.MstrategyNotExitException;
import com.manage.common.exception.advert.StrategyNotExistExceprion;
import com.manage.common.exception.advert.VideoPlayTimeException;
import com.manage.common.support.Convert;
import com.manage.common.utils.DateUtils;
import com.manage.common.utils.StringUtils;
import com.manage.common.utils.bean.BeanUtils;
import com.manage.common.utils.security.ShiroUtils;
import com.manage.framework.config.ManageConfig;

/**
 * ???????????? ???????????????
 * 
 * @author xufeng
 * @date 2018-08-31
 */
@Service
public class AdvertConfigServiceImpl implements IAdvertConfigService 
{
	private Logger log = LoggerFactory.getLogger(AdvertConfigServiceImpl.class);
	
	@Autowired
	private AdvertConfigMapper advertConfigMapper;
	
	@Autowired
	private AdvertStrategyMapper advertStrategyMapper;
	
	@Autowired
	private AdvertMstrategyMapper advertMstrategyMapper;
	
	@Autowired
	private AdvertDeviceMapper advertDeviceMapper;
	
	@Autowired
	private AdvertMaterialMapper advertMaterialMapper;
	
	@Autowired
	private IVendingCmdService vendingCmdService;
	
	@Autowired
	private ManageConfig manageConfig;

	/**
     * ????????????????????????
     * 
     * @param logid ????????????ID
     * @return ??????????????????
     */
    @Override
	public AdvertConfig selectAdvertConfigById(String logid)
	{
	    return advertConfigMapper.selectAdvertConfigById(logid);
	}
	
	/**
     * ????????????????????????
     * 
     * @param advertConfig ??????????????????
     * @return ??????????????????
     */
	@Override
	public List<AdvertConfig> selectAdvertConfigList(AdvertConfig advertConfig)
	{
	    return advertConfigMapper.selectAdvertConfigList(advertConfig);
	}
	
    /**
     * ??????????????????
     * 
     * @param advertConfig ??????????????????
     * @return ??????
     * @throws Exception 
     */
	@Override
	@Transactional
	public int insertAdvertConfig(AdvertSaveVo advertSaveVo) throws Exception
	{
		List<Strategy> handleAdvertStrategys = handleAdvertStrategys(advertSaveVo);
		List<Strategy> strategiesList = advertSaveVo.getStrategies();
		//??????????????????????????????????????????
		if(StringUtils.isEmpty(strategiesList)) {
			return 12;
		}
		strategiesList.addAll(handleAdvertStrategys);
		int checkResult = checkAdvertConfig(advertSaveVo);
		if(checkResult!=1) {
			return checkResult;
		}
		// ??????as_advert_config
		AdvertConfig advertConfig = new AdvertConfig();
		BeanUtils.copyBeanProp(advertConfig, advertSaveVo);
		advertConfig.setLogid(UUID.randomUUID().toString());
		String corpId = ShiroUtils.getCorpId();
		String advertId = SystemUtil.getSeqId(corpId, "as_advert_config");
		advertConfig.setAdvertId(advertId);
		advertConfig.setCorpId(corpId);
		advertConfig.setCreateTime(DateUtils.getTime());
		//????????????,???????????????????????????
		if(Constant.ADVERT_DILIVERYTYPE_NOW.equals(advertConfig.getDeliveryType())) {
			advertConfig.setLazyTime("");
		}
		// ????????????
		int materialNum = 0;
		List<Strategy> strategies = advertSaveVo.getStrategies();
		if (strategies != null) {
			for (Strategy sg : strategies) {
				if (sg.getMstrategies() != null) {
					materialNum = materialNum + sg.getMstrategies().size();
				}
			}
		}
		//?????????????????????
		if(materialNum==0) {
			return 11;
		}
		advertConfig.setMaterialNum(materialNum);
		if (advertSaveVo.getAdevices() != null) {
			advertConfig.setPlayerNum(advertSaveVo.getAdevices().size());
		} else {
			advertConfig.setPlayerNum(0);
		}
		advertConfig.setCurState(Constant.ADVERT_CUS_STATE_WAIT);
		advertConfigMapper.insertAdvertConfig(advertConfig);
		// ??????as_advert_strategy
		if (strategies != null) {
			int strategiesSeq=1;
			for (Strategy sg : strategies) {
				AdvertStrategy advertStrategy = new AdvertStrategy();
				BeanUtils.copyBeanProp(advertStrategy, sg);
				advertStrategy.setCorpId(corpId);
				advertStrategy.setLogid(UUID.randomUUID().toString());
				String strategyId = SystemUtil.getSeqId(corpId, "as_advert_strategy");
				advertStrategy.setStrategyId(strategyId);
				advertStrategy.setAdvertId(advertId);
				advertStrategy.setCreateTime(DateUtils.getTime());
				advertStrategy.setSeqId(strategiesSeq);
				strategiesSeq++;
				if(StringUtils.isEmpty(advertStrategy.getStrategyPointName())) {
					throw new AdvertStrategyPointNameNotExit();
				}
				List<Mstrategy> mstrategies = sg.getMstrategies();
				if (mstrategies != null &&!mstrategies.isEmpty()) {
					advertStrategy.setMaterialNum(sg.getMstrategies().size());
				} else {
					throw new MstrategyNotExitException();
				}
				String playSTime = advertStrategy.getPlaySTime();
				String playEtime = advertStrategy.getPlayEtime();
				if(StringUtils.isEmpty(advertStrategy.getStrategyType())) {
					throw new AdvertStrategyTypeIsEmptyException();
				}
				if(StringUtils.isEmpty(playSTime)) {
					throw new AdvertStrategyPlaySTimeEmptyException();
				}
				if(StringUtils.isEmpty(playEtime)) {
					throw new AdvertStrategyPlayETimeEmptyException();
				}
				if(playSTime.compareToIgnoreCase(playEtime)>=0) {
					throw new AdvertStrategyPlaySTimeLaterThanPlayETimeException();
				}
				//???????????????????????????????????????????????????????????????
				if("2".equals(advertStrategy.getStrategyType())) {
					if(playSTime.compareToIgnoreCase(DateUtils.getTime())<=0) {
						throw new AdvertStrategyPlaySTimeEarlyThanNowException();
					}
				}
				// ??????as_advert_mstrategy				
				if (mstrategies != null) {
					int sumPlayerTime=0;
					int mstrategiesSeq=1;
					for (Mstrategy mg : mstrategies) {
						AdvertMstrategy advertMstrategy = new AdvertMstrategy();
//						BeanUtils.copyBeanProp(advertMstrategy, mg);
						AdvertMaterial ad = advertMaterialMapper.selectAdvertMaterialById(mg.getMaterialId());
						advertMstrategy.setMediaName(ad.getMediaName());
						advertMstrategy.setMediaType(ad.getMediaType());
						advertMstrategy.setMediaUrl(ad.getMediaUrl());
						advertMstrategy.setMediaSUrl(ad.getMediaSUrl());
						advertMstrategy.setMaterialId(mg.getMaterialId());
						advertMstrategy.setSeqId(mstrategiesSeq);
						mstrategiesSeq++;
						advertMstrategy.setLogid(UUID.randomUUID().toString());
						advertMstrategy.setMstrategyId(SystemUtil.getSeqId(corpId, "as_advert_mstrategy"));
						advertMstrategy.setStrategyId(strategyId);
						advertMstrategy.setAdvertId(advertId);
						advertMstrategy.setCorpId(corpId);
						advertMstrategy.setPlayerTime(mg.getPlayerTime());
						advertMstrategy.setPlayerTimes(mg.getPlayerTimes());
						//??????????????????
						Integer playerTimes = advertMstrategy.getPlayerTimes();
						if(playerTimes==null) {
							throw new AdvertPlayTimesIsNull();
						}
						if(playerTimes<=0) {
							throw new AdvertPlayTimesLessThanZero();
						}
						//???????????????,????????????????????????
						if(Constant.MEDIA_TYPE_VIDEO.equals(advertMstrategy.getMediaType())) {
							try {
								String mediaSUrl = advertMstrategy.getMediaSUrl();
								File file = new File(ManageConfig.getUploadPrefix()+mediaSUrl);
								Encoder encoder = new Encoder();
								MultimediaInfo video = encoder.getInfo(file);
								advertMstrategy.setPlayerTime((int)video.getDuration()/1000);
							}catch (Exception e) {
								log.error("????????????????????????,????????????:"+DateUtils.getTime(),e);
								throw new VideoPlayTimeException();
							}
						}else {
							Integer playerTime = advertMstrategy.getPlayerTime();
							if(playerTime==null) {
								throw new AdvertPlayTimeIsNull();
							}
							if(playerTime<=0) {
								throw new AdvertPlayTimeLessThanZero();
							}
						}
						sumPlayerTime+=advertMstrategy.getPlayerTime()*playerTimes;
						advertMstrategy.setCreateTime(DateUtils.getTime());
						advertMstrategyMapper.insertAdvertMstrategy(advertMstrategy);
					}
					advertStrategy.setPlayerTime(sumPlayerTime);
					advertStrategyMapper.insertAdvertStrategy(advertStrategy);
				}
			}
		}
		
		// ??????as_advert_device
		List<Adevice> adevices = advertSaveVo.getAdevices();
		List<AdvertDevice> advertDevices = new ArrayList<AdvertDevice>();
		if (adevices != null) {
			for (Adevice adevice : adevices) {
				AdvertDevice advertDevice = new AdvertDevice();
				BeanUtils.copyBeanProp(advertDevice, adevice);
				advertDevice.setLogid(UUID.randomUUID().toString());
				advertDevice.setAdvDeviceId(SystemUtil.getSeqId(corpId, "as_advert_device"));
				advertDevice.setCorpId(corpId);
				advertDevice.setAdvertId(advertId);
				advertDevice.setStateTime(DateUtils.getTime());
				advertDevice.setCreateTime(DateUtils.getTime());
				advertDevice.setPlayerPlat(advertSaveVo.getPlayerPlat());
				advertDevice.setCurState(Constant.ADVERT_CUS_STATE_WAIT);
				advertDevices.add(advertDevice);
				advertDeviceMapper.insertAdvertDevice(advertDevice);
			}
		}
		//?????????????????????,????????????
		if(Constant.ADVERT_DILIVERYTYPE_NOW.equals(advertSaveVo.getDeliveryType())) {
			for (AdvertDevice adevice : advertDevices) {
				//?????????????????????,???????????????????????????????????????
				if(Constant.ADVERT_OPER_TYPE_FULL.equals(advertSaveVo.getOperType())) {
					invalidAdvertDeviceCoverExist(adevice.getDeviceId(),advertId);
				}
				int result = insertVendingCmd(adevice.getDeviceId(),advertId,adevice.getAdvDeviceId(),ShiroUtils.getCorpId());
				if(result!=1) {
					throw new CmdNoticeDeviceFailedException();
				}
			}	
		}
	    return 1;
	}
	
	/**
	 * ?????????????????????????????????
	 * 
	 * @param deviceId
	 * @param advertId
	 */
	public void invalidAdvertDeviceCoverExist(String deviceId, String advertId) {
		//??????????????????????????????
		AdvertDevice advertDevice = new AdvertDevice();
		advertDevice.setDeviceId(deviceId);
		List<AdvertDevice> advertDevices = advertDeviceMapper.selectAdvertDevicesNotiInvalid(advertDevice);
		Map<String, AdvertDevice> map = new HashMap<String,AdvertDevice>();
		//????????????????????????????????????????????????map
		for (AdvertDevice device : advertDevices) {
			if(device.getAdvertId().equals(advertId)) {
				continue;
			}
			if(map.get(device.getAdvertId())==null) {
				map.put(device.getAdvertId(), device);
			}
		}
		Set<String> advertIds = map.keySet();
		log.info("?????????????????????????????????,??????:"+DateUtils.getTime());
		advertDeviceMapper.invalidAdvertDeviceCoverExist(deviceId,advertId);
		for (String id : advertIds) {
			//?????????????????????????????????????????????????????????
			List<EditAdvertDeviceVo> advertDeviceList = advertDeviceMapper.selectAdvertDeviceByAdvertId(id);
			boolean invalidDeviceflag=true;
			for (EditAdvertDeviceVo advertDeviceVo : advertDeviceList) {
				String curState = advertDeviceVo.getCurState();
				if(!Constant.ADVERT_CUS_STATE_INVALID.equals(curState)&&!Constant.ADVERT_CUS_STATE_DELETE.equals(curState)) {
					invalidDeviceflag=false;
					break;
				}
			}
			//?????????????????????????????????,?????????????????????
			if(invalidDeviceflag) {
				AdvertConfig advertConfig = new AdvertConfig();
				advertConfig.setAdvertId(id);
				advertConfig.setCurState(Constant.ADVERT_CUS_STATE_INVALID);
				advertConfigMapper.updateAdvertConfig(advertConfig);
			}
		}
	}
	
	/**
     * ??????????????????
     * 
     * @param advertConfig ??????????????????
     * @return ??????
     */
	@Override
	@Transactional
	public int updateAdvertConfig(AdvertSaveVo advertSaveVo) {
		List<Strategy> handleAdvertStrategys = handleAdvertStrategys(advertSaveVo);
		List<Strategy> strategiesList = advertSaveVo.getStrategies();
		//??????????????????????????????????????????
		if(StringUtils.isEmpty(strategiesList)) {
			return 12;
		}
		strategiesList.addAll(handleAdvertStrategys);
		int checkResult = checkAdvertConfig(advertSaveVo);
		if(checkResult!=1) {
			return checkResult;
		}
		// ??????as_advert_config
		AdvertConfig advertConfig = new AdvertConfig();
		BeanUtils.copyBeanProp(advertConfig, advertSaveVo);
		String corpId = ShiroUtils.getCorpId();
		String advertId = advertSaveVo.getAdvertId();
		advertConfig.setAdvertId(advertId);
		//????????????,???????????????????????????
		if(Constant.ADVERT_DILIVERYTYPE_NOW.equals(advertConfig.getDeliveryType())) {
			advertConfig.setLazyTime("");
		}
		// ????????????
		int materialNum = 0;
		List<Strategy> strategies = advertSaveVo.getStrategies();
		if (strategies != null) {
			for (Strategy sg : strategies) {
				if (sg.getMstrategies() != null) {
					materialNum = materialNum + sg.getMstrategies().size();
				}
			}
		}
		//?????????????????????
		if(materialNum==0) {
			return 11;
		}
		advertConfig.setMaterialNum(materialNum);
		if (advertSaveVo.getAdevices() != null) {
			advertConfig.setPlayerNum(advertSaveVo.getAdevices().size());
		} else {
			advertConfig.setPlayerNum(0);
		}
		advertConfig.setCurState(Constant.ADVERT_CUS_STATE_WAIT);
		advertConfigMapper.updateAdvertConfig(advertConfig);
		//?????????????????????
		advertStrategyMapper.deleteAdvertStrategyByAdvertId(advertId);
		advertMstrategyMapper.deleteAdvertMstrategyByAdvertId(advertId);
		advertDeviceMapper.deleteAdvertDeviceByAdvertId(advertId);
		// ??????as_advert_strategy
		if (strategies != null) {
			int strategiesSeq=1;
			for (Strategy sg : strategies) {
				AdvertStrategy advertStrategy = new AdvertStrategy();
				BeanUtils.copyBeanProp(advertStrategy, sg);
				advertStrategy.setCorpId(corpId);
				advertStrategy.setLogid(UUID.randomUUID().toString());
				String strategyId = SystemUtil.getSeqId(corpId, "as_advert_strategy");
				advertStrategy.setStrategyId(strategyId);
				advertStrategy.setAdvertId(advertId);
				advertStrategy.setSeqId(strategiesSeq);
				strategiesSeq++;
				advertStrategy.setCreateTime(DateUtils.getTime());
				if(StringUtils.isEmpty(advertStrategy.getStrategyPointName())) {
					throw new AdvertStrategyPointNameNotExit();
				}
				List<Mstrategy> mstrategies = sg.getMstrategies();
				if (mstrategies != null &&!mstrategies.isEmpty()) {
					advertStrategy.setMaterialNum(sg.getMstrategies().size());
				} else {
					new MstrategyNotExitException();
				}
				String playSTime = advertStrategy.getPlaySTime();
				String playEtime = advertStrategy.getPlayEtime();
				if(StringUtils.isEmpty(advertStrategy.getStrategyType())) {
					throw new AdvertStrategyTypeIsEmptyException();
				}
				if(StringUtils.isEmpty(playSTime)) {
					throw new AdvertStrategyPlaySTimeEmptyException();
				}
				if(StringUtils.isEmpty(playEtime)) {
					throw new AdvertStrategyPlayETimeEmptyException();
				}
				if(playSTime.compareToIgnoreCase(playEtime)>=0) {
					throw new AdvertStrategyPlaySTimeLaterThanPlayETimeException();
				}
				//???????????????????????????????????????????????????????????????
				if("2".equals(advertStrategy.getStrategyType())) {
					if(playSTime.compareToIgnoreCase(DateUtils.getTime())<=0) {
						throw new AdvertStrategyPlaySTimeEarlyThanNowException();
					}
				}
				int sumPlayerTime=0;
				// ??????as_advert_mstrategy				
				if (mstrategies != null) {
					int mstrategiesSeq=1;
					for (Mstrategy mg : mstrategies) {
						AdvertMstrategy advertMstrategy = new AdvertMstrategy();
//						BeanUtils.copyBeanProp(advertMstrategy, mg);
						AdvertMaterial ad = advertMaterialMapper.selectAdvertMaterialById(mg.getMaterialId());
						advertMstrategy.setMediaName(ad.getMediaName());
						advertMstrategy.setMediaType(ad.getMediaType());
						advertMstrategy.setMediaUrl(ad.getMediaUrl());
						advertMstrategy.setMediaSUrl(ad.getMediaSUrl());
						advertMstrategy.setMaterialId(mg.getMaterialId());
						advertMstrategy.setSeqId(mstrategiesSeq);
						mstrategiesSeq++;
						advertMstrategy.setLogid(UUID.randomUUID().toString());
						advertMstrategy.setMstrategyId(SystemUtil.getSeqId(corpId, "as_advert_mstrategy"));
						advertMstrategy.setStrategyId(strategyId);
						advertMstrategy.setAdvertId(advertId);
						advertMstrategy.setCorpId(corpId);
						advertMstrategy.setPlayerTime(mg.getPlayerTime());
						advertMstrategy.setPlayerTimes(mg.getPlayerTimes());
						//??????????????????
						Integer playerTimes = advertMstrategy.getPlayerTimes();
						if(playerTimes==null) {
							throw new AdvertPlayTimesIsNull();
						}
						if(playerTimes<=0) {
							throw new AdvertPlayTimesLessThanZero();
						}
						//???????????????,????????????????????????
						if(Constant.MEDIA_TYPE_VIDEO.equals(advertMstrategy.getMediaType())) {
							try {
								String mediaSUrl = advertMstrategy.getMediaSUrl();
								File file = new File(ManageConfig.getUploadPrefix()+mediaSUrl);
								Encoder encoder = new Encoder();
								MultimediaInfo video = encoder.getInfo(file);
								advertMstrategy.setPlayerTime((int)video.getDuration()/1000);
							}catch (Exception e) {
								log.error("????????????????????????,????????????:"+DateUtils.getTime(),e);
								throw new VideoPlayTimeException();
							}
						}else {
							Integer playerTime = advertMstrategy.getPlayerTime();
							if(playerTime==null) {
								throw new AdvertPlayTimeIsNull();
							}
							if(playerTime<=0) {
								throw new AdvertPlayTimeLessThanZero();
							}
						}
						sumPlayerTime+=advertMstrategy.getPlayerTime()*playerTimes;
						advertMstrategy.setCreateTime(DateUtils.getTime());
						advertMstrategyMapper.insertAdvertMstrategy(advertMstrategy);
					}
				}
				advertStrategy.setPlayerTime(sumPlayerTime);
				advertStrategyMapper.insertAdvertStrategy(advertStrategy);
			}
		}
		
		// ??????as_advert_device
		List<Adevice> adevices = advertSaveVo.getAdevices();
		if(StringUtils.isEmpty(adevices)) {
			throw new RuntimeException("??????????????????????????????");
		}
		List<AdvertDevice> advertDevices = new ArrayList<AdvertDevice>();
		if (adevices != null) {
			for (Adevice adevice : adevices) {
				AdvertDevice advertDevice = new AdvertDevice();
				BeanUtils.copyBeanProp(advertDevice, adevice);
				advertDevice.setLogid(UUID.randomUUID().toString());
				advertDevice.setAdvDeviceId(SystemUtil.getSeqId(corpId, "as_advert_device"));
				advertDevice.setCorpId(corpId);
				advertDevice.setAdvertId(advertId);
				advertDevice.setStateTime(DateUtils.getTime());
				advertDevice.setCreateTime(DateUtils.getTime());
				advertDevice.setCurState(Constant.ADVERT_CUS_STATE_WAIT);
				advertDevice.setPlayerPlat(advertSaveVo.getPlayerPlat());
				advertDevices.add(advertDevice);
				advertDeviceMapper.insertAdvertDevice(advertDevice);
			}
		}
		//?????????????????????,????????????
		if(Constant.ADVERT_DILIVERYTYPE_NOW.equals(advertSaveVo.getDeliveryType())) {
			for (AdvertDevice adevice : advertDevices) {
				//?????????????????????,???????????????????????????????????????
				if(Constant.ADVERT_OPER_TYPE_FULL.equals(advertSaveVo.getOperType())) {
					invalidAdvertDeviceCoverExist(adevice.getDeviceId(),advertId);
				}
				int result = insertVendingCmd(adevice.getDeviceId(),advertId,adevice.getAdvDeviceId(),ShiroUtils.getCorpId());
				if(result!=1) {
					throw new CmdNoticeDeviceFailedException();
				}
			}	
		}
	    return 1;
	}
	
	/**
     * ??????????????????
     * 
     * @param advertConfig ??????????????????
     * @return ??????
     */
//	@Override
//	@Transactional
//	public int updateAdvertConfig(AdvertSaveVo advertSaveVo)
//	{
//		//????????????
//		int checkResult = checkAdvertConfig(advertSaveVo);
//		if(checkResult!=1) {
//			return checkResult;
//		}
//		String corpId = ShiroUtils.getCorpId();
//		
//		// ??????as_advert_config
//		AdvertConfig advertConfig = new AdvertConfig();
//		BeanUtils.copyBeanProp(advertConfig, advertSaveVo);
//		// ????????????
//		int materialNum = 0;
//		List<Strategy> strategies = advertSaveVo.getStrategies();
//		if (strategies != null) {
//			for (Strategy sg : strategies) {
//				if (sg.getStrategyId() != null && !sg.getStrategyId().equals("") && sg.getIsDel() != null && sg.getIsDel().equals("1")) {	// ?????????????????????
//					// ???????????????????????????????????????
//					advertMstrategyMapper.deleteAdvertMstrategyByStrategyId(sg.getStrategyId());
//					advertStrategyMapper.deleteAdvertStrategyById(sg.getStrategyId());
//				} else if (sg.getStrategyId() != null && !sg.getStrategyId().equals("")) {// ??????????????????
//					List<Mstrategy> ms = sg.getMstrategies();
//					// ??????????????????????????????????????????
//					if (ms != null) {
//						for (Mstrategy m : ms) {
//							if (m.getMstrategyId() == null || m.getMstrategyId().equals("")) {	// ???????????????
//								AdvertMstrategy advertMstrategy = new AdvertMstrategy();
//								AdvertMaterial ad = advertMaterialMapper.selectAdvertMaterialById(m.getMaterialId());
//								advertMstrategy.setMediaName(ad.getMediaName());
//								advertMstrategy.setMediaType(ad.getMediaType());
//								advertMstrategy.setMaterialId(m.getMaterialId());
//								advertMstrategy.setSeqId(m.getSeqId());
//								advertMstrategy.setLogid(UUID.randomUUID().toString());
//								advertMstrategy.setMstrategyId(SystemUtil.getSeqId(corpId, "as_advert_mstrategy"));
//								advertMstrategy.setStrategyId(sg.getStrategyId());
//								advertMstrategy.setAdvertId(advertSaveVo.getAdvertId());
//								advertMstrategy.setCorpId(corpId);
//								advertMstrategy.setMediaUrl(ad.getMediaUrl());
//								advertMstrategy.setMediaSUrl(ad.getMediaSUrl());
//								advertMstrategy.setCreateTime(DateUtils.getTime());
//								advertMstrategyMapper.insertAdvertMstrategy(advertMstrategy);
//								materialNum++;
//							} else if (m.getIsDel() != null && m.getIsDel().equals("1")) {	// ??????????????????????????????
//								advertMstrategyMapper.deleteAdvertMstrategyById(m.getMstrategyId());
//							} else {	// ???????????????????????????????????????
//								materialNum++;
//							}
//						}
//					}
//					AdvertStrategy advertStrategy = new AdvertStrategy();
//					BeanUtils.copyBeanProp(advertStrategy, sg);
//					advertStrategyMapper.updateAdvertStrategy(advertStrategy);	// ????????????
//				} else {
//					// ????????????
//					AdvertStrategy advertStrategy = new AdvertStrategy();
//					BeanUtils.copyBeanProp(advertStrategy, sg);
//					advertStrategy.setCorpId(corpId);
//					advertStrategy.setLogid(UUID.randomUUID().toString());
//					String strategyId = SystemUtil.getSeqId(corpId, "as_advert_strategy");
//					advertStrategy.setStrategyId(strategyId);
//					advertStrategy.setAdvertId(advertSaveVo.getAdvertId());
//					advertStrategy.setCreateTime(DateUtils.getTime());
//					advertStrategyMapper.insertAdvertStrategy(advertStrategy);
//					
//					// ??????as_advert_mstrategy
//					List<Mstrategy> mstrategies = sg.getMstrategies();
//					if (mstrategies != null) {
//						for (Mstrategy mg : mstrategies) {
//							AdvertMstrategy advertMstrategy = new AdvertMstrategy();
////							BeanUtils.copyBeanProp(advertMstrategy, mg);
//							AdvertMaterial ad = advertMaterialMapper.selectAdvertMaterialById(mg.getMaterialId());
//							advertMstrategy.setMediaName(ad.getMediaName());
//							advertMstrategy.setMediaType(ad.getMediaType());
//							advertMstrategy.setMediaUrl(ad.getMediaUrl());
//							advertMstrategy.setMaterialId(mg.getMaterialId());
//							advertMstrategy.setSeqId(mg.getSeqId());
//							advertMstrategy.setLogid(UUID.randomUUID().toString());
//							advertMstrategy.setMstrategyId(SystemUtil.getSeqId(corpId, "as_advert_mstrategy"));
//							advertMstrategy.setStrategyId(strategyId);
//							advertMstrategy.setAdvertId(advertSaveVo.getAdvertId());
//							advertMstrategy.setCorpId(corpId);
//							advertMstrategy.setMediaSUrl(ad.getMediaUrl());
//							advertMstrategy.setCreateTime(DateUtils.getTime());
//							advertMstrategyMapper.insertAdvertMstrategy(advertMstrategy);
//							materialNum++;
//						}
//					}
//				}
//			}
//		}
//		advertConfig.setMaterialNum(materialNum);
//		if (advertSaveVo.getAdevices() != null) {
//			advertConfig.setPlayerNum(advertSaveVo.getAdevices().size());
//		} else {
//			advertConfig.setPlayerNum(0);
//		}
//		int r = advertConfigMapper.updateAdvertConfig(advertConfig);	//.insertAdvertConfig(advertConfig);
//		
//		// ????????????????????????,?????????as_advert_device
//		advertDeviceMapper.deleteAdvertDeviceByAdvertId(advertSaveVo.getAdvertId());
//		List<Adevice> adevices = advertSaveVo.getAdevices();
//		if (adevices != null) {
//			for (Adevice adevice : adevices) {
//				AdvertDevice advertDevice = new AdvertDevice();
//				BeanUtils.copyBeanProp(advertDevice, adevice);
//				advertDevice.setLogid(UUID.randomUUID().toString());
//				advertDevice.setAdvDeviceId(SystemUtil.getSeqId(corpId, "as_advert_device"));
//				advertDevice.setCorpId(corpId);
//				advertDevice.setAdvertId(advertSaveVo.getAdvertId());
//				advertDevice.setStateTime(DateUtils.getTime());
//				advertDevice.setCreateTime(DateUtils.getTime());
//				advertDeviceMapper.insertAdvertDevice(advertDevice);
//			}
//		}
//		
//	    return r;
//	}

	/**
     * ????????????????????????
     * 
     * @param ids ?????????????????????ID
     * @return ??????
     */
	@Override
	@Transactional
	public int deleteAdvertConfigByIds(String ids)
	{
		String[] ds = Convert.toStrArray(ids);
		for (String advertId : ds) {
			//???????????????????????????????????????
			AdvertConfig config = advertConfigMapper.selectAdvertConfigByAdvertId(advertId);
			if(Constant.ADVERT_CUS_STATE_INVALID.equals(config.getCurState())) {
				AdvertConfig advertConfig = new AdvertConfig();
				advertConfig.setAdvertId(advertId);
				advertConfig.setCurState(Constant.ADVERT_CUS_STATE_DELETE);
				advertConfigMapper.updateAdvertConfig(advertConfig);
				advertDeviceMapper.deleteAdvertDeviceByAdvertIdWithFlag(advertId);
			}else {
				throw new AdvertNotInvalidException();
			}
		}
		return 1;
//		return advertConfigMapper.deleteAdvertConfigByIds(Convert.toStrArray(ids));
	}

	@Override
	public AdvertConfig selectAdvertConfigByAdvertId(String advertId) {
		return advertConfigMapper.selectAdvertConfigByAdvertId(advertId);
	}
	
	/**
	 * ????????????
	 * 
	 * @param advertConfig
	 * @return
	 */
	private int checkAdvertConfig(AdvertSaveVo advertSaveVo) {
		//?????????????????????????????????
		if(StringUtils.isNotEmpty(advertSaveVo.getAdvertId())){
			AdvertConfig config = advertConfigMapper.selectAdvertConfigByAdvertId(advertSaveVo.getAdvertId());
			if(!Constant.ADVERT_CUS_STATE_WAIT.equals(config.getCurState())) {
				return 2;
			}
		}
		String name = advertSaveVo.getName(); 
		//??????????????????????????????
		if(StringUtils.isEmpty(name)) {
			return 3;
		}
		//?????????????????????????????????
		AdvertConfig advertConfig = new AdvertConfig();
		advertConfig.setAdvertId(advertSaveVo.getAdvertId());
		advertConfig.setName(advertSaveVo.getName());
		advertConfig.setCorpId(ShiroUtils.getCorpId());
		AdvertConfig configExit = advertConfigMapper.selectAdvertConfigNameNotExist(advertConfig);
		if(configExit!=null) {
			return 4;
		}
		//????????????????????????
		if(StringUtils.isEmpty(advertSaveVo.getDeliveryType())) {
			return 5;
		}
		//????????????????????????
		if(StringUtils.isEmpty(advertSaveVo.getPlayerPlat())) {
			return 6;
		}
		//????????????????????????
		if(StringUtils.isEmpty(advertSaveVo.getOperType())) {
			return 7;
		}
		//??????????????????
		if(StringUtils.isEmpty(advertSaveVo.getStrategies())) {
			return 8;
		}
		//??????????????????????????????
		if(StringUtils.isEmpty(advertSaveVo.getAdevices())) {
			return 9;
		}
		if(Constant.ADVERT_DILIVERYTYPE_LATER.equals(advertSaveVo.getDeliveryType())) {
			//????????????????????????????????????
			if(StringUtils.isEmpty(advertSaveVo.getLazyTime())) {
				return 10;
			}
			//????????????????????????????????????????????????
			if(DateUtils.getTime().compareToIgnoreCase(advertSaveVo.getLazyTime())>=0) {
				return 13;
			}
		}
		return 1;
	}

	/**
	 * ????????????
	 * 
	 * @param ids
	 * @return
	 */
	@Override
	@Transactional
	public int invalid(String ids) {
		for (String advertId : ids.split(",")) {
			AdvertConfig config = advertConfigMapper.selectAdvertConfigByAdvertId(advertId);
			String curState = config.getCurState();
			//???????????????,?????????????????????
			if(Constant.ADVERT_CUS_STATE_INVALID.equals(curState)) {
				return 2;
			}
			//?????????????????????,????????????
			if(Constant.ADVERT_CUS_STATE_DELETE.equals(curState)) {
				return 3;
			}
			AdvertConfig advertConfig = new AdvertConfig();
			advertConfig.setAdvertId(advertId);
			advertConfig.setCurState(Constant.ADVERT_CUS_STATE_INVALID);
			advertConfigMapper.updateAdvertConfig(advertConfig);
			advertDeviceMapper.invalidAdvertDeviceByAdvertIdWithFlag(advertId);
			//????????????
			List<EditAdvertDeviceVo> devices = advertDeviceMapper.selectAdvertDeviceByAdvertId(advertId);
			for (EditAdvertDeviceVo editAdvertDeviceVo : devices) {
				int result = insertVendingCmd(editAdvertDeviceVo.getDeviceId(),advertId,editAdvertDeviceVo.getAdvDeviceId(),config.getCorpId());
				if(result!=1) {
					throw new CmdNoticeDeviceFailedException();
				}
			}
		}
		return 1;
	}
	
	/**
	 * ????????????
	 * 
	 * @param editAdvertDeviceVo
	 * @return
	 */
	public int insertVendingCmd(String siteId,String advertId,String AdvDeviceId,String corpId) {
		VendingCmd vendingCmd = new VendingCmd();
		vendingCmd.setCmdCode(siteId);
		vendingCmd.setCmdType(Constant.VENDING_CMD_TYPE_VENDING);
		vendingCmd.setCmd(Constant.CMD_ADVERT_UPDATE);
		Map<String, String> map = new HashMap<String,String>();
		map.put("AdvDeviceId", AdvDeviceId);
		map.put("AdvertId", advertId);
		vendingCmd.setCont(JSONObject.toJSONString(map));
		return vendingCmdService.insertVendingCmdQuick(siteId,vendingCmd,corpId);
	}
	
	/**
	 * ???????????????????????????????????????
	 * 
	 * @param advertSaveVo
	 * @return
	 */
	private List<Strategy> handleAdvertStrategys(AdvertSaveVo advertSaveVo){
		List<AdvertPlat> advertPlats = advertSaveVo.getAdvertPlats();
		List<Strategy> list = new ArrayList<Strategy>();
		int count=2;
		if(advertPlats!=null) {
			for (AdvertPlat advertPlat : advertPlats) {
				String strategyPoint="";
				if(count<10) {
					strategyPoint="010"+count;
				}else {
					strategyPoint="01"+count;
				}
				List<Strategy> strategies = advertPlat.getStrategies();
				if(StringUtils.isEmpty(strategies)) {
					throw new StrategyNotExistExceprion();
				}
				for (Strategy strategy : strategies) {
					strategy.setStrategyPoint(strategyPoint);
					list.add(strategy);
				}
				count++;
			}
		}
		return list;
	}

	/**
	 * ?????????????????????????????????
	 * 
	 * @param advertConfig
	 * @return
	 */
	@Override
	public List<AdvertConfig> selectNotExcutingAdvertConfigList(AdvertConfig advertConfig) {
		return advertConfigMapper.selectNotExcutingAdvertConfigList(advertConfig);
	}

	/**
	 * ????????????????????????
	 * 
	 * @param advertConfigSelect
	 * @return
	 */
	@Override
	public List<AdvertConfig> selectNotInvalidAdvertConfigList(AdvertConfig advertConfig) {
		return advertConfigMapper.selectNotInvalidAdvertConfigList(advertConfig);
	}
}
