/**
 * 
 */
package com.manage.project.system.base.domain;

/**
 * 系统参数 sys_parameter
 * 
 * @author zhangjiawei
 * @date 2018年10月29日
 * 
 */
public class Parameter {
	/**记录编号*/
	private String logid;
	/**参数代码*/
	private String paraCode;
	/**参数名称*/
	private String name;
	/**参数值*/
	private String value;
	/**参数描述*/
	private String description;
	
	public String getLogid() {
		return logid;
	}
	public void setLogid(String logid) {
		this.logid = logid;
	}
	public String getParaCode() {
		return paraCode;
	}
	public void setParaCode(String paraCode) {
		this.paraCode = paraCode;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	
}
