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

import com.google.common.io.Files;
import com.manage.common.utils.DateUtils;
import com.manage.common.utils.StringUtils;
import com.manage.common.utils.poi.ExcelUtil;
import com.manage.common.utils.security.ShiroUtils;
import com.manage.framework.aspectj.lang.annotation.Log;
import com.manage.framework.aspectj.lang.constant.BusinessType;
import com.manage.framework.config.ManageConfig;
import com.manage.framework.web.controller.BaseController;
import com.manage.framework.web.domain.AjaxResult;
import com.manage.project.common.Constant;
import com.manage.project.common.vo.CommonVo;
import com.manage.project.system.product.domain.ProductClassify;
import com.manage.project.system.product.domain.ProductInfo;
import com.manage.project.system.util.BussinessCacheService;
import com.manage.project.system.util.SystemUtil;
import com.manage.project.system.vendingPoint.domain.VendingDistrict;
import com.manage.project.system.vendingPoint.domain.VendingLine;
import com.manage.project.system.vendingPoint.service.IVendingDistrictService;
import com.manage.project.system.vendingPoint.service.IVendingLineService;

/**
 * ????????????????????? ??????????????????
 * 
 * @author xufeng
 * @date 2018-08-31
 */
@Controller
@RequestMapping("/system/vendingDistrict")
public class VendingDistrictController extends BaseController
{
	@Autowired
	private IVendingDistrictService vendingDistrictService;
	@Autowired
	private IVendingLineService vendingLineService;
	@Autowired
	private BussinessCacheService bussinessCacheService;
	
	private Logger log = LoggerFactory.getLogger(VendingDistrictController.class);
	
	/**
	 * ?????????????????????????????????
	 */
	@GetMapping("/list")
	@ResponseBody
	public AjaxResult list(VendingDistrict vendingDistrict)
	{
		startPage();
		if (SystemUtil.isZhilai()) {
			vendingDistrict.setCorpId("");
		} else {
			vendingDistrict.setCorpId(ShiroUtils.getUser().getCorpId());
		}
		vendingDistrict.setCorpId(SystemUtil.getCorpId());
        List<VendingDistrict> list = vendingDistrictService.selectVendingDistrictList(vendingDistrict);
		return AjaxResult.success(getDataTable(list));
	}
	
	/**
	 * ?????????????????????????????????
	 */
	@Log(title = "?????????????????????", action = BusinessType.INSERT)
	@PostMapping("/add")
	@ResponseBody
	public AjaxResult addSave(@RequestBody VendingDistrict vendingDistrict)
	{	
		vendingDistrict.setLogid(UUID.randomUUID().toString());
		vendingDistrict.setCreateTime(DateUtils.getTime());
		if (SystemUtil.isZhilai()) {
			vendingDistrict.setCorpId("");
		} else {
			vendingDistrict.setCorpId(ShiroUtils.getUser().getCorpId());
		}
		String corpId = vendingDistrict.getCorpId();
		if(StringUtils.isEmpty(corpId)) {
			corpId = ShiroUtils.getUser().getCorpId();
			vendingDistrict.setCorpId(corpId);
		}
		//????????????????????????
		String code = vendingDistrict.getCode();
		if(StringUtils.isEmpty(code)) {
			return AjaxResult.error("??????????????????");
		}else {
			if(checkCodeExist(code, corpId)) {
				return AjaxResult.error("????????????,?????????????????????");
			}
		}	
		if(vendingDistrict.getCode().length() > 30){
			return AjaxResult.error("??????????????????????????????");
		}
		//???????????????????????????????????????
		if(!Pattern.matches("^[A-Za-z0-9]+$", vendingDistrict.getCode())) {
		return AjaxResult.error("????????????????????????????????????");
		}
		if(StringUtils.isEmpty(vendingDistrict.getName())){
			return AjaxResult.error("????????????????????????");
		}
		if(vendingDistrict.getName().length() > 50){
			return AjaxResult.error("??????????????????????????????");
		}
		//??????????????????????????????????????????????????????
		if(!Pattern.matches("^[\u4E00-\u9FA5A-Za-z0-9]+$", vendingDistrict.getName())) {
		return AjaxResult.error("???????????????????????????????????????????????????");
		}
		if(StringUtils.isEmpty(vendingDistrict.getCorpId())){
			return AjaxResult.error("????????????????????????");
		}
		if (StringUtils.isEmpty(vendingDistrict.getDescription())) {
			vendingDistrict.setDescription("???");
		}
		if (StringUtils.isEmpty(vendingDistrict.getManager())) {
			vendingDistrict.setManager("???");
		}
		if (StringUtils.isEmpty(vendingDistrict.getMobile())) {
			vendingDistrict.setMobile("???");
		}
		vendingDistrict.setDistrictId(SystemUtil.getSeqId(corpId, "as_vending_district"));
//		vendingDistrict.setCid(ShiroUtils.getUser().getCorpId());
		return toAjax(vendingDistrictService.insertVendingDistrict(vendingDistrict));
	}
	
	/**
	 * ????????????????????????
	 * 
	 * @param code ??????
	 * @param corpId ????????????
	 * @return ????????????true,???????????????false
	 */
	public boolean checkCodeExist(String code,String corpId) {
		VendingDistrict vendingDistrict = new VendingDistrict();
		vendingDistrict.setCode(code);
		vendingDistrict.setCorpId(corpId);
		List<VendingDistrict> list = vendingDistrictService.selectVendingDistrictList(vendingDistrict);
		return StringUtils.isNotEmpty(list);
	}

	/**
	 * ?????????????????????????????????
	 */
	@Log(title = "?????????????????????", action = BusinessType.UPDATE)
	@PostMapping("/edit")
	@ResponseBody
	public AjaxResult editSave(@RequestBody VendingDistrict vendingDistrict)
	{			
		VendingDistrict district = vendingDistrictService.selectVendingDistrictById(vendingDistrict.getDistrictId());
		String code = district.getCode();
		String updateCode = vendingDistrict.getCode();
		//???????????????????????????
		if(StringUtils.isNotEmpty(updateCode)&&!updateCode.equals(code)) {
			//????????????????????????
			if(checkCodeExist(updateCode, district.getCorpId())) {
				return AjaxResult.error("????????????,?????????????????????");
			}
		}
		if(vendingDistrict.getCode().length() > 30){
			return AjaxResult.error("??????????????????????????????");
		}
		//???????????????????????????????????????
		if(!Pattern.matches("^[A-Za-z0-9]+$", vendingDistrict.getCode())) {
		return AjaxResult.error("????????????????????????????????????");
		}
		if(vendingDistrict.getName().length() > 50){
			return AjaxResult.error("??????????????????????????????");
		}
		//??????????????????????????????????????????????????????
		if(!Pattern.matches("^[\u4E00-\u9FA5A-Za-z0-9]+$", vendingDistrict.getName())) {
		return AjaxResult.error("???????????????????????????????????????????????????");
		}
		if (StringUtils.isEmpty(vendingDistrict.getDescription())) {
			vendingDistrict.setDescription("???");
		}
		if (StringUtils.isEmpty(vendingDistrict.getManager())) {
			vendingDistrict.setManager("???");
		}
		if (StringUtils.isEmpty(vendingDistrict.getMobile())) {
			vendingDistrict.setMobile("???");
		}
		return toAjax(vendingDistrictService.updateVendingDistrict(vendingDistrict));
	}
	
	/**
	 * ???????????????????????????
	 */
	@Log(title = "?????????????????????", action = BusinessType.DELETE)
	@PostMapping( "/remove")
	@ResponseBody
	public AjaxResult remove(@RequestBody CommonVo ids)
	{	
		String[] idArr = ids.getIds().split(",");
		for (String districtId : idArr) {
			VendingLine line = new VendingLine();
			line.setDistrictId(districtId);
			List<VendingLine> list = vendingLineService.selectVendingLineList(line);
			if(StringUtils.isNotEmpty(list)) {
				return AjaxResult.error("????????????????????????,????????????");
			}
		}
		//List<VendingPoint> list = vendingPointService.selectVendingPointListByDistrictIds(idArr);
		return toAjax(vendingDistrictService.deleteVendingDistrictByIds(ids.getIds()));
	}
	
	/**
	 * ??????????????????
	 * @return
	 */
	@GetMapping("/listAll")
	@ResponseBody
	public AjaxResult listAll()
	{
		VendingDistrict vendingDistrict = new VendingDistrict();
		vendingDistrict.setCorpId(SystemUtil.getCorpId());
        List<VendingDistrict> list = vendingDistrictService.selectVendingDistrictList(vendingDistrict);
		return AjaxResult.success(getDataTable(list));
	}
	
	/**
	 * ??????????????????
	 */
	@Log(title = "?????????????????????", action = BusinessType.IMPORT)
	@PostMapping( "/importExcel")
	@ResponseBody
	public AjaxResult importProductExcel(@RequestParam(value = "file", required = false) MultipartFile file) {
		FileOutputStream fos = null;
		try {
			
			ExcelUtil<VendingDistrict> util = new ExcelUtil<VendingDistrict>(VendingDistrict.class);
			List<VendingDistrict> vendingDistrictList = util.importExcel("????????????", file.getInputStream());// ??????
			HSSFWorkbook workbook = (HSSFWorkbook)WorkbookFactory.create(file.getInputStream());
            HSSFSheet sheet = workbook.getSheetAt(0);
            int row=1;
            //??????????????????
            for (VendingDistrict vendingDistrict : vendingDistrictList) {
            	AjaxResult ajaxResult = vendingDistrictService.saveImportVendingDistrict(vendingDistrict);
            	HSSFRow hssfRow = sheet.getRow(row);
        		HSSFCell cell = hssfRow.getCell(5);
        		if(cell==null) {
        			cell = hssfRow.createCell(5);
        		}
        		row++;
            	if(ajaxResult.isSuccess()) {
            		cell.setCellValue("??????");
            	}else {
            		cell.setCellValue(ajaxResult.getMsg());
            	}
			}
            bussinessCacheService.initVendingDistrict();
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
	@Log(title = "?????????????????????", action = BusinessType.EXPORT)
	@PostMapping("/exportExcel")
	@ResponseBody
	public AjaxResult exportExcel(@RequestBody VendingDistrict vendingDistrict) {
		try {
			ExcelUtil<VendingDistrict> util = new ExcelUtil<VendingDistrict>(VendingDistrict.class);
			vendingDistrict.setCorpId(SystemUtil.getCorpId());
			List<VendingDistrict> list = vendingDistrictService.selectVendingDistrictList(vendingDistrict);
			
            return util.exportExcel(list, "????????????");
		} catch (Exception e) {
			log.error("????????????????????????,??????:"+DateUtils.getTime(),e);
			return error("??????Excel????????????????????????????????????");
		}
	}
	
	/**
	 * ?????? ????????????????????????
	 */
	@Log(title = "?????????????????????", action = BusinessType.EXPORT)
	@PostMapping("/downLoadExcelModel")
	@ResponseBody
	public AjaxResult downLoadExcelModel() {
		String excelModelPath = SystemUtil.getExcelModelPath("district");
		if(StringUtils.isNotEmpty(excelModelPath)) {
			String path="file/excel/model/"+ShiroUtils.getCorpId()+"/????????????_"+DateUtils.dateTimeNow("yyyyMMddhhmmss")+".xls";
			try {
				Files.copy(new File(ManageConfig.getUploadPrefix()+excelModelPath), new File(ManageConfig.getUploadPrefix()+path));
				return AjaxResult.success(path);
			} catch (Exception e) {
				log.error("????????????????????????,??????:"+DateUtils.getTime(),e);
				return error("??????Excel??????????????????????????????????????????");
			}
		}else {
			log.error("????????????????????????,?????????????????????,??????:"+DateUtils.getTime());
			return error("??????Excel??????????????????????????????????????????");
		}
	}
}
