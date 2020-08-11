package com.manage.project.system.base.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.manage.project.system.base.domain.User;

/**
 * 用户表 数据层
 * 
 */
public interface UserMapper
{

	/**
	 * 根据姓名进行模糊查询
	 * @param userName
	 * @param roleId 
	 * @return
	 */
	public List<User> selectUserByUserName(@Param("userName") String userName, @Param("corpId") String corpId,@Param("roleId")  String roleId);
	
	public List<String> selectPermsByUserId(@Param("userId") Long userId);
	
	public List<Long> selectRolesByUserId(@Param("userId") Long userId);
	
	/**
	 * 根据用户id查用户信息
	 * @param userId
	 * @return
	 */
	public User selectByUserId(@Param("userId") Long userId);
	
    /**
     * 根据条件分页查询用户对象
     * 
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    public List<User> selectUserList(User user);

    /**
     * 通过用户名查询用户
     * 
     * @param userName 用户名
     * @return 用户对象信息
     */
    public User selectUserByLoginName(String userName);

    /**
     * 通过手机号码查询用户
     * 
     * @param phoneNumber 手机号码
     * @return 用户对象信息
     */
    public List<User> selectUserByPhoneNumber(String phoneNumber);

    /**
     * 通过邮箱查询用户
     * 
     * @param email 邮箱
     * @return 用户对象信息
     */
    public User selectUserByEmail(String email);

    /**
     * 通过用户ID查询用户
     * 
     * @param userId 用户ID
     * @return 用户对象信息
     */
    public User selectUserById(Long userId);

    /**
     * 通过用户ID删除用户
     * 
     * @param userId 用户ID
     * @return 结果
     */
    public int deleteUserById(Long userId);

    /**
     * 批量删除用户信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteUserByIds(Long[] ids);

    /**
     * 修改用户信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    public int updateUser(User user);

    /**
     * 新增用户信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    public int insertUser(User user);

    /**
     * 校验用户名称是否唯一
     * 
     * @param loginName 登录名称
     * @return 结果
     */
    public int checkLoginNameUnique(String loginName);

    /**
     * 校验手机号码是否唯一
     *
     * @param phonenumber 手机号码
     * @return 结果
     */
    public User checkPhoneUnique(String phonenumber);

    /**
     * 校验email是否唯一
     *
     * @param email 用户邮箱
     * @return 结果
     */
    public User checkEmailUnique(String email);
    
    /**
     * 根据登录名取得用户姓名
     * @param loginName
     * @return
     */
    public List<String> selectUserNameByLoginName(String loginName);

    /**
     * 通过角色编号查询权限码
     * 
     * @param roleIds
     * @return
     */
	public List<String> selectPermsByRoleId(Long roleIds);
}