package com.manage.project.system.base.domain;

import com.manage.framework.aspectj.lang.annotation.Excel;
import com.manage.framework.web.domain.BaseEntity;

/**
 * 字典类型对象 sys_dict_type
 * 
 */
public class DictType extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 字典主键 */
    @Excel(name = "字典主键")
    private Long dictId;

    /** 字典名称 */
    @Excel(name = "字典名称")
    private String dictName;

    /** 字典类型 */
    @Excel(name = "字典类型 ")
    private String dictType;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态")
    private String status;
    // 状态名称
    private String statusName;
    
    public String getStatusName() {
    	if (status == null) {
    		return "";
    	}
    	if (status.equals("0")) {
    		return "正常";
    	} 
    	if (status.equals("1")) {
    		return "停用";
    	}
		return "";
	}

	public Long getDictId()
    {
        return dictId;
    }

    public void setDictId(Long dictId)
    {
        this.dictId = dictId;
    }

    public String getDictName()
    {
        return dictName;
    }

    public void setDictName(String dictName)
    {
        this.dictName = dictName;
    }

    public String getDictType()
    {
        return dictType;
    }

    public void setDictType(String dictType)
    {
        this.dictType = dictType;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @Override
    public String toString()
    {
        return "DictType [dictId=" + dictId + ", dictName=" + dictName + ", dictType=" + dictType + ", status=" + status
                + "]";
    }

}
