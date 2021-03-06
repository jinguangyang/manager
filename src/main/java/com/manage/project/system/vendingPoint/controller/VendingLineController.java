package com.manage.project.system.vendingPoint.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFPictureData;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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
import org.springframework.web.multipart.MultipartFile;

import com.manage.common.utils.DateUtils;
import com.manage.common.utils.StringUtils;
import com.manage.common.utils.poi.ExcelUtil;
import com.manage.common.utils.security.ShiroUtils;
import com.manage.framework.aspectj.lang.annotation.Log;
import com.manage.framework.aspectj.lang.constant.BusinessType;
import com.manage.framework.config.ManageConfig;
import com.manage.project.common.Constant;
import com.manage.project.common.vo.CommonVo;
import com.manage.project.system.product.domain.ProductClassify;
import com.manage.project.system.product.domain.ProductInfo;
import com.manage.project.system.supply.domain.SupplyConfig;
import com.manage.project.system.supply.service.ISupplyConfigService;
import com.manage.project.system.util.BussinessCacheService;
import com.manage.project.system.util.SystemUtil;
import com.manage.project.system.vendingPoint.domain.VendingDistrict;

import com.manage.project.system.util.SystemUtil;
import com.manage.project.system.vendingPoint.domain.VendingDistrict;
import com.manage.project.system.util.SystemUtil;

import com.manage.project.system.vendingPoint.domain.VendingLine;
import com.manage.project.system.vendingPoint.domain.VendingPoint;
import com.manage.project.system.vendingPoint.service.IVendingDistrictService;
import com.manage.project.system.vendingPoint.service.IVendingLineService;
import com.manage.project.system.vendingPoint.service.IVendingPointService;
import com.manage.framework.web.controller.BaseController;
import com.manage.framework.web.domain.AjaxResult;

/**
 * ??????????????? ??????????????????
 * 
 * @author xufeng
 * @date 2018-08-31
 */
@Controller
@RequestMapping("/system/vendingLine")
public class VendingLineController extends BaseController
{
	@Autowired
	private IVendingLineService vendingLineService;
	@Autowired
	private IVendingPointService vendingPointService;
	@Autowired
	private ISupplyConfigService supplyConfigService;
	@Autowired
	private IVendingDistrictService vendingDistrictService;
	@Autowired
	private BussinessCacheService bussinessCacheService;
	
	private Logger log = LoggerFactory.getLogger(VendingLineController.class);
	
	/**
	 * ???????????????????????????
	 */
	@GetMapping("/list")
	@ResponseBody
	public AjaxResult list(VendingLine vendingLine)
	{
		startPage();
		if (SystemUtil.isZhilai()) {
			vendingLine.setCorpId("");
		} else {
			vendingLine.setCorpId(ShiroUtils.getUser().getCorpId());
		}
		String corpId = ShiroUtils.getUser().getCorpId();
		if (corpId.equals(Constant.ZHILAI_CORP_ID)) {	// ??????????????????????????????
			vendingLine.setCorpId("");
		} else {
			vendingLine.setCorpId(corpId);
		}
        List<VendingLine> list = vendingLineService.selectVendingLineList(vendingLine);
        for (VendingLine vendingLine2 : list) {
        	vendingLine2.setCorpName(SystemUtil.getCorpNameById(corpId));
		}
        return AjaxResult.success(getDataTable(list));
	}
	
	/**
	 * ???????????????????????????
	 */
	@Log(title = "???????????????", action = BusinessType.INSERT)
	@PostMapping("/add")
	@ResponseBody
	public AjaxResult addSave(@RequestBody VendingLine vendingLine)
	{		
		String corpId = vendingLine.getCorpId();
		if(StringUtils.isEmpty(corpId)) {
			corpId = ShiroUtils.getUser().getCorpId();
			vendingLine.setCorpId(corpId);
		}
		//????????????????????????
		String code = vendingLine.getCode();
		if(StringUtils.isEmpty(code)) {
			return AjaxResult.error("??????????????????");
		}else {
			if(checkCodeExist(code, corpId)) {
				return AjaxResult.error("????????????,?????????????????????");
			}
		}
		if(vendingLine.getCode().length() > 30){
			return AjaxResult.error("??????????????????????????????");
		}
		if(StringUtils.isEmpty(vendingLine.getName())){
			return AjaxResult.error("????????????????????????");
		}
		if(vendingLine.getName().length() > 50){
			return AjaxResult.error("??????????????????????????????");
		}
		if(StringUtils.isEmpty(vendingLine.getDistrictId())){
			return AjaxResult.error("????????????????????????");
		}
		if (StringUtils.isEmpty(vendingLine.getDescription())) {
			vendingLine.setDescription("???");
		}
		return toAjax(vendingLineService.insertVendingLine(vendingLine));
	}

	
	/**
	 * ???????????????????????????
	 */
	@Log(title = "???????????????", action = BusinessType.UPDATE)
	@PostMapping("/edit")
	@ResponseBody
	public AjaxResult editSave(@RequestBody VendingLine vendingLine)
	{	
		VendingLine line = vendingLineService.selectVendingLineByLineId(vendingLine.getLineId());
		String code = line.getCode();
		String updateCode = vendingLine.getCode();
		//???????????????????????????
		if(StringUtils.isNotEmpty(updateCode)&&!updateCode.equals(code)) {
			//????????????????????????
			if(checkCodeExist(updateCode, line.getCorpId())) {
				return AjaxResult.error("????????????,?????????????????????");
			}
		}
		if(vendingLine.getCode().length() > 30){
			return AjaxResult.error("??????????????????????????????");
		}
		if(vendingLine.getName().length() > 50){
			return AjaxResult.error("??????????????????????????????");
		}
		if (StringUtils.isEmpty(vendingLine.getDescription())) {
			vendingLine.setDescription("???");
		}
		return toAjax(vendingLineService.updateVendingLine(vendingLine));
	}
	
	/**
	 * ?????????????????????
	 */
	@Log(title = "???????????????", action = BusinessType.DELETE)
	@PostMapping( "/remove")
	@ResponseBody
	public AjaxResult remove(@RequestBody CommonVo ids)
	{		
		String[] idArr = ids.getIds().split(",");
		for (String lineId : idArr) {
			VendingPoint point = new VendingPoint();
			point.setLineId(lineId);
			List<VendingPoint> list = vendingPointService.selectVendingPointList(point);
			if(StringUtils.isNotEmpty(list)) {
				return AjaxResult.error("????????????????????????,????????????");
			}
			//????????????????????????????????????
			SupplyConfig supplyConfigSelect = new SupplyConfig();
			supplyConfigSelect.setLineId(lineId);
			List<SupplyConfig> supplyConfigList = supplyConfigService.selectSupplyConfigList(supplyConfigSelect);
			if(supplyConfigList!=null&&supplyConfigList.size()!=0) {
				return AjaxResult.error("????????????????????????????????????,????????????");
			}
		}
		return toAjax(vendingLineService.deleteVendingLineByIds(ids.getIds()));
	}
	
	/**
	 * ???????????????????????????
	 */
	@GetMapping("/listAll")
	@ResponseBody
	public AjaxResult listAll(VendingLine vendingLine)
	{
//		VendingLine vendingLine = new VendingLine();
		vendingLine.setCorpId(SystemUtil.getCorpId());
        List<VendingLine> list = vendingLineService.selectVendingLineList(vendingLine);
        return AjaxResult.success(getDataTable(list));
	}
	
	/**
	 * ????????????????????????
	 * 
	 * @param code ??????
	 * @param corpId ????????????
	 * @return ????????????true,???????????????false
	 */
	public boolean checkCodeExist(String code,String corpId) {
		VendingLine line = new VendingLine();
		line.setCode(code);
		line.setCorpId(corpId);
		List<VendingLine> list = vendingLineService.selectVendingLineList(line);
		return StringUtils.isNotEmpty(list);
	}
	
	/**
	 * ????????????
	 */
	@Log(title = "???????????????", action = BusinessType.IMPORT)
	@PostMapping( "/importExcel")
	@ResponseBody
	public AjaxResult importLineExcel(@RequestParam(value = "file", required = false) MultipartFile file) {
		FileOutputStream fos = null;
		try {
			ExcelUtil<VendingLine> util = new ExcelUtil<VendingLine>(VendingLine.class);
			List<VendingLine> vendingLineList = util.importExcel("????????????", file.getInputStream());// ??????
			HSSFWorkbook workbook = (HSSFWorkbook)WorkbookFactory.create(file.getInputStream());
            HSSFSheet sheet = workbook.getSheetAt(0);
            int row=1;
            //??????????????????
            for (VendingLine vendingLine : vendingLineList) {
            	AjaxResult ajaxResult = vendingLineService.saveImportVendingLine(vendingLine);
            	HSSFRow hssfRow = sheet.getRow(row);
        		HSSFCell cell = hssfRow.getCell(4);
        		if(cell==null) {
        			cell = hssfRow.createCell(4);
        		}
        		row++;
            	if(ajaxResult.isSuccess()) {
            		cell.setCellValue("??????");
            	}else {
            		cell.setCellValue(ajaxResult.getMsg());
            	}
			}
            bussinessCacheService.initVendingLine();
            //?????????????????????excel???????????????
            String path="file/excel/model/"+ShiroUtils.getCorpId()+"/??????????????????_"+DateUtils.dateTimeNow("yyyyMMddhhmmss")+".xls";
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
			log.error("??????Excel??????,??????:"+DateUtils.getTime(),e);
			if(fos!=null) {
				try {
					fos.close();
				} catch (IOException exception) {
					log.error("??????Excel??????,??????????????????,??????:"+DateUtils.getTime(),exception);
				}
			}
			return error("??????Excel????????????????????????????????????");
		}
	}
	
	/**
	 * ????????????
	 */
	@Log(title = "???????????????", action = BusinessType.EXPORT)
	@PostMapping("/exportExcel")
	@ResponseBody
	public AjaxResult exportExcel(@RequestBody VendingLine vendingLine) {
		try {
			ExcelUtil<VendingLine> util = new ExcelUtil<VendingLine>(VendingLine.class);
			vendingLine.setCorpId(SystemUtil.getCorpId());
			List<VendingLine> list = vendingLineService.selectVendingLineList(vendingLine);
			for (VendingLine line : list) {
				line.setDistrictName(SystemUtil.getVendingDistrictNameFromCache(line.getDistrictId()));
			}
            return util.exportExcel(list, "????????????");
		} catch (Exception e) {
			log.error("????????????????????????,??????:"+DateUtils.getTime(),e);
			return error("??????Excel????????????????????????????????????");
		}
	}
	
	/**
	 * ????????????
	 */
	@Log(title = "???????????????", action = BusinessType.EXPORT)
	@PostMapping("/downLoadExcelModel")
	@ResponseBody
	public AjaxResult downLoadExcelModel() {
		FileInputStream inStream = null;
		FileOutputStream fos = null;
		try {
			String excelModelPath = SystemUtil.getExcelModelPath("line");		
            inStream = new FileInputStream(new File(ManageConfig.getUploadPrefix()+excelModelPath));
            HSSFWorkbook workbook = (HSSFWorkbook)WorkbookFactory.create(inStream);
			if(inStream!=null){
                inStream.close();
            }
            HSSFSheet sheet = workbook.getSheetAt(0);        
            //???????????????????????????
            VendingDistrict vendingDistrict = new VendingDistrict();
            vendingDistrict.setCorpId(SystemUtil.getCorpId());
            List<VendingDistrict> vendingDistrictList = vendingDistrictService.selectVendingDistrictList(vendingDistrict);
            String[] districtList = new String[1000];
            for (int i = 0; i < vendingDistrictList.size(); i++) {
            	VendingDistrict district = vendingDistrictList.get(i);
            	districtList[i]=district.getName()+"--"+district.getDistrictId();
			}
            //?????????????????????
            ExcelUtil.addDropDownList(workbook, sheet, districtList, 1, Constant.DROP_DOWN_LIST_ROWS, 2);
			//????????????
			File file = new File(ManageConfig.getExcelPath()+"model/"+ShiroUtils.getCorpId()+"/????????????_"+DateUtils.dateTimeNow("yyyyMMddhhmmss")+".xls");
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
            inStream=null;
            return AjaxResult.success("file/excel/model/"+ShiroUtils.getCorpId()+"/????????????_"+DateUtils.dateTimeNow("yyyyMMddhhmmss")+".xls");
		} catch (Exception e) {
			log.error("????????????????????????,??????:"+DateUtils.getTime(),e);
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
	                log.error("??????????????????,?????????????????????,??????:"+DateUtils.getTime(),exception);
	            }
			return error("??????Excel????????????????????????????????????");
		}
	}
}
