package com.huiminpay.merchant.service;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huiminpay.common.cache.domain.BusinessException;
import com.huiminpay.common.cache.domain.CommonErrorCode;
import com.huiminpay.common.cache.util.PhoneUtil;
import com.huiminpay.common.cache.util.StringUtil;
import com.huiminpay.merchant.api.MerchantService;
import com.huiminpay.merchant.convert.MerchantConvert;
import com.huiminpay.merchant.convert.StaffConvert;
import com.huiminpay.merchant.convert.StoreConvert;
import com.huiminpay.merchant.dto.MerchantDTO;
import com.huiminpay.merchant.dto.StaffDTO;
import com.huiminpay.merchant.dto.StoreDTO;
import com.huiminpay.merchant.entity.Merchant;
import com.huiminpay.merchant.entity.Staff;
import com.huiminpay.merchant.entity.Store;
import com.huiminpay.merchant.entity.StoreStaff;
import com.huiminpay.merchant.mapper.MerchantMapper;
import com.huiminpay.merchant.mapper.StaffMapper;
import com.huiminpay.merchant.mapper.StoreMapper;
import com.huiminpay.merchant.mapper.StoreStaffMapper;
import com.huiminpay.user.api.TenantService;
import com.huiminpay.user.api.dto.tenant.CreateTenantRequestDTO;
import com.huiminpay.user.api.dto.tenant.TenantDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 服务实现类
 */
@Slf4j
@Service
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    private MerchantMapper merchantMapper;
    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private StaffMapper staffMapper;
    @Autowired
    private StoreStaffMapper storeStaffMapper;
    @Reference
    private TenantService tenantService;


    /**
     * 根据ID查询商户信息
     *
     * @param merchantId
     * @return
     */
    @Override
    public MerchantDTO queryMerchantById(Long merchantId) {
        Merchant byId = merchantMapper.selectById(merchantId);
        MerchantDTO merchantDTO = new MerchantDTO();
        BeanUtils.copyProperties(byId,merchantDTO);
        return merchantDTO;
    }

    /**
     * 注册商户
     *
     * @param merchantDTO 商户
     * @return 商户信息
     */
    @Override
    public MerchantDTO createMerchant(MerchantDTO merchantDTO) throws BusinessException {
        //1.校验
        //注册信息非空校验
        if (merchantDTO == null){
            throw new BusinessException(CommonErrorCode.E_100108);//参数为空
        }
        //手机号非空校验
        if (StringUtil.isBlank(merchantDTO.getMobile())){
            throw new BusinessException(CommonErrorCode.E_100112);//手机号为空
        }
        //手机号的合法性
        if (!PhoneUtil.isMatches(merchantDTO.getMobile())){
            throw new BusinessException(CommonErrorCode.E_100109);//手机号格式不正确
        }
        //用户名非空校验
        if (StringUtil.isBlank(merchantDTO.getUsername())){
            throw new BusinessException(CommonErrorCode.E_100110);//用户名为空
        }
        //密码非空校验
        if (StringUtil.isBlank(merchantDTO.getPassword())){
            throw new BusinessException(CommonErrorCode.E_100111);//密码为空
        }
        //手机号是否已存在
        Integer count = merchantMapper.selectCount(new LambdaQueryWrapper<Merchant>().eq(Merchant::getMobile, merchantDTO.getMobile()));
        if (count > 0){
            throw new BusinessException(CommonErrorCode.E_100113);//手机号已存在
        }

        //2.调用SaaS接口 添加 租户并绑定和门店 员工 商户之间的关系
        CreateTenantRequestDTO createTenantRequestDTO = new CreateTenantRequestDTO();
        //设置基本信息
        createTenantRequestDTO.setMobile(merchantDTO.getMobile());//手机号
        createTenantRequestDTO.setUsername(merchantDTO.getUsername());//账号
        createTenantRequestDTO.setPassword(merchantDTO.getPassword());//密码
        createTenantRequestDTO.setBundleCode("huimin-merchant");//租户类型
        createTenantRequestDTO.setTenantTypeCode("huimin-merchant");//租户类型编码
        createTenantRequestDTO.setName(merchantDTO.getUsername());//租户名称和用户名一样
        //创建租户 如果已存在租户则返回租户信息，否则新增租户、新增租户管理员，同时初始化权限
        TenantDTO tenantAndAccount = tenantService.createTenantAndAccount(createTenantRequestDTO);
        //校验租户是否存在
        if (tenantAndAccount == null || tenantAndAccount.getId() == null) {
            throw new BusinessException(CommonErrorCode.E_200012);//租户不存在
        }
        //获取租户id
        Long tenantId = tenantAndAccount.getId();
        //因为租户id在商户表中是唯一的，校验商户中租户id是否已经存在
        Integer selectCount = merchantMapper.selectCount(new LambdaQueryWrapper<Merchant>().eq(Merchant::getTenantId, tenantId));
        if (selectCount > 0){
            throw new BusinessException(CommonErrorCode.E_200017);//商户在当前租户下已经注册，不可重复注册
        }
        //3.设置租户 和 商户 绑定关系
        //DTO转entity
        Merchant merchant = MerchantConvert.INSTANCE.dto2Entity(merchantDTO);
        //设置默认审核状态为0 ，0‐未申请,1‐已申请待审核,2‐审核通过,3‐审核拒绝
        merchant.setAuditStatus("0");
        //设置租户id
        merchant.setTenantId(tenantId);
        //商户注册
        merchantMapper.insert(merchant);
        //获取商户id
        Long merchantId = merchant.getId();
        //4.商户注册成功后，进行新增门店
        StoreDTO store = new StoreDTO();
        store.setMerchantId(merchantId);//商户id
        store.setStoreStatus(true);//默认为启用
        //其他数据后期用户进行修改的时候添加，创建时只添加已有数据
        //新增门店
        StoreDTO storeDTO = createStore(store);
        //5.新增员工
        StaffDTO staff = new StaffDTO();
        staff.setMerchantId(merchantDTO.getId());//商户id
        staff.setUsername(merchantDTO.getUsername());//账号
        staff.setMobile(merchantDTO.getMobile());//手机号
        staff.setStoreId(storeDTO.getId());//门店id
        staff.setLastLoginTime(LocalDateTime.now());//最后登录时间
        staff.setStaffStatus(true);//默认启用
        //其他数据后期用户进行修改的时候添加，创建时只添加已有数据
        //新增员工
        StaffDTO staffDTO = createStaff(staff);
        //6.绑定员工和门店
        bindStaffToStore(storeDTO.getId(), staffDTO.getId());

        //对象转换
        MerchantDTO merchantDTONew = MerchantConvert.INSTANCE.entity2Dto(merchant);
        return merchantDTONew;
    }

    /**
     * 资质申请接口
     *
     * @param merchantId  商人id
     * @param merchantDTO 资质申请的信息
     * @throws BusinessException 业务异常
     */
    @Override
    @Transactional
    public void applyMerchant(Long merchantId, MerchantDTO merchantDTO) throws BusinessException {
        //校验传入对象是否为空
        if (merchantId== null || merchantDTO == null) {
            throw new BusinessException(CommonErrorCode.E_100108);//传入对象为空
        }
        Merchant merchant = merchantMapper.selectById(merchantId);
        //校验商户非空
        if (merchant == null) {
            throw new BusinessException(CommonErrorCode.E_200002);//商户不存在
        }
        //将dto转entity
        Merchant entity = MerchantConvert.INSTANCE.dto2Entity(merchantDTO);
        //设置必要参数
        entity.setId(merchant.getId());//主键id
        entity.setMobile(merchant.getMobile());//因为资质申请的时候手机号不让改，还使用数据库中原来的手机号
        entity.setAuditStatus("1");//修改审核状态为1—已申请带审核
        entity.setTenantId(merchant.getTenantId());
        //更新商户数据
        merchantMapper.updateById(entity);
    }

    /**
     * 商户下添加门店
     *
     * @param storeDTO 商店dto
     * @return 商店dto
     * @throws BusinessException 业务异常
     */
    @Override
    public StoreDTO createStore(StoreDTO storeDTO) throws BusinessException {
        //对象转换
        Store store = StoreConvert.INSTANCE.dto2entity(storeDTO);
        //添加数据
        storeMapper.insert(store);
        //返回有主键的dto对象
        return StoreConvert.INSTANCE.entity2dto(store);
    }

    /**
     * 商户新增员工
     *
     * @param staffDTO 员工dto
     * @return 员工dto
     * @throws BusinessException 业务异常
     */
    @Override
    public StaffDTO createStaff(StaffDTO staffDTO) throws BusinessException {
        //校验手机号格式及存在
        String mobile = staffDTO.getMobile();
        if (StringUtil.isBlank(mobile)) {
            throw new BusinessException(CommonErrorCode.E_100112);//手机号为空
        }
        if (!PhoneUtil.isMatches(mobile)) {
            throw new BusinessException(CommonErrorCode.E_100109);//手机号格式不正确
        }
        //根据商户id和手机号校验唯一性
        if (isExistStaffByMobile(mobile,staffDTO.getMerchantId())) {
            throw new BusinessException(CommonErrorCode.E_100113);//手机号已存在
        }
        //校验用户名是否为空
        if(StringUtil.isBlank(staffDTO.getUsername())){
            throw new BusinessException(CommonErrorCode.E_100110);//用户名为空
        }
        //根据商户id和账号校验唯一性
        if (isExistStaffByUserName(staffDTO.getUsername(),staffDTO.getMerchantId())){
            throw new BusinessException(CommonErrorCode.E_100114);
        }

        //对象转换
        Staff staff = StaffConvert.INSTANCE.dto2entity(staffDTO);
        //数据添加
        staffMapper.insert(staff);

        return StaffConvert.INSTANCE.entity2dto(staff);
    }

    /**
     * 根据账号判断员工是否已在指定商户存在
     * @param username   用户名
     * @param merchantId 商人id
     * @return boolean
     */
    private boolean isExistStaffByUserName(String username, Long merchantId) {
        Integer count = staffMapper.selectCount(new LambdaQueryWrapper<Staff>()
                .eq(Staff::getUsername, username)
                .eq(Staff::getMerchantId, merchantId));
        return count > 0;
    }

    /**
     * 根据手机号判断员工是否已经在指定的商户中存在
     *
     * @param mobile     手机号
     * @param merchantId
     * @return boolean
     */
    private boolean isExistStaffByMobile(String mobile,Long merchantId){
        Integer count = staffMapper.selectCount(new LambdaQueryWrapper<Staff>()
                .eq(Staff::getMobile, mobile)
                .eq(Staff::getMerchantId,merchantId));
        return count > 0;
    }
    /**
     * 绑定管理员和门店
     *
     * @param storeId 存储id
     * @param staffId 员工id
     * @throws BusinessException 业务异常
     */
    @Override
    public void bindStaffToStore(Long storeId, Long staffId) throws BusinessException {
        StoreStaff storeStaff = new StoreStaff();
        storeStaff.setStoreId(storeId);
        storeStaff.setStaffId(staffId);
        storeStaffMapper.insert(storeStaff);
    }

    /**
     * 根据租户id查询商户的信息
     *
     * @param tenantId 租户id
     * @return 商人dto
     */
    @Override
    public MerchantDTO queryMerchantByTenantId(Long tenantId) {
        Merchant merchant = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>().eq(Merchant::getTenantId, tenantId));
        return MerchantConvert.INSTANCE.entity2Dto(merchant);
    }

}
