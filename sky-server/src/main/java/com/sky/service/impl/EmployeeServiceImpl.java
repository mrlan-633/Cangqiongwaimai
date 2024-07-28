package com.sky.service.impl;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @Override
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();

        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 对前端传来的明文密码进行md5处理
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus().equals(StatusConstant.DISABLE)) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    @Override
    public void add(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        //BeanUtils 工具类 前者复制给后者
        BeanUtils.copyProperties(employeeDTO, employee);
        //采用全局变量 避免魔法值
        employee.setStatus(StatusConstant.ENABLE);
        //使用md5加密技术对密码进行加密 初始密码为123456
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        //设置记录时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());
        //因为局部线程变量需要手动结束

        employeeMapper.insert(employee);
    }

    //分页查询
    @Override
    public PageResult PageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        //开始分页查询
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        Page<Employee> page = employeeMapper.PageQuery(employeePageQueryDTO);
        long total = page.getTotal();
        List<Employee> records = page.getResult();
        return new PageResult(total, records);
    }

    @Override
    public void startOrstop(Integer status, Long id) {
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();

        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.update(employee);
    }

    @Override
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        employee.setPassword("****");
        return employee;
    }

    @Override
    public void update(EmployeeDTO employeeDTO) {

        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);

        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.update(employee);
    }

    @Override
    public void updatePassword(PasswordEditDTO passwordEditDTO) {
        passwordEditDTO.setOldPassword(DigestUtils.md5DigestAsHex(passwordEditDTO.getOldPassword().getBytes()));
        passwordEditDTO.setEmpId(BaseContext.getCurrentId());
       if(passwordEditDTO.getOldPassword().equals(employeeMapper.getById(passwordEditDTO.getEmpId()).getPassword()))  {
           Employee employee = new Employee();
        passwordEditDTO.setNewPassword(DigestUtils.md5DigestAsHex(passwordEditDTO.getNewPassword().getBytes()));
        employee.setPassword(passwordEditDTO.getNewPassword());
        employee.setUpdateUser(BaseContext.getCurrentId());
        employee.setUpdateTime(LocalDateTime.now());
        employee.setId(passwordEditDTO.getEmpId());
           employeeMapper.update(employee);
       }


    }

}
