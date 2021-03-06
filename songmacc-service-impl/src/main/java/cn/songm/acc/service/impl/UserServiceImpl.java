package cn.songm.acc.service.impl;

import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.songm.acc.dao.UserDao;
import cn.songm.acc.entity.User;
import cn.songm.acc.service.UserError;
import cn.songm.acc.service.UserService;
import cn.songm.common.service.ServiceException;
import cn.songm.common.utils.CodeUtils;
import cn.songm.common.utils.StringUtils;

@Service("userService")
public class UserServiceImpl implements UserService {

    private static String KEY_ACC = "songm";
    private static String KEY_NIC = "松美";

    @Autowired
    private UserDao userDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User register(String account, String password, String nickname,
            String sysVcode, String vcode) throws ServiceException {
        if (!vcode.equalsIgnoreCase(sysVcode)) {
            throw new ServiceException(UserError.ACC_116.getErrCode(), "验证码错误");
        }
        if (StringUtils.isEmptyOrNull(password)
                || StringUtils.isEmptyOrNull(nickname)) {
            throw new IllegalArgumentException();
        }

        if (!StringUtils.isEmptyOrNull(account)) {
            // 验证账号格式
            if (StringUtils.isEmptyOrNull(account) || !account.matches("^\\w{5,50}$")) {
                throw new ServiceException(UserError.ACC_105.getErrCode(), "账号格式错误");
            }
            account = account.toLowerCase();
            // 验证账号中的关键字
            verifyAccKey(account);
            // 验证账号是否重复
            if (this.verifyAccountRep(account)) {
                throw new ServiceException(UserError.ACC_101.getErrCode(), "账号已经被使用");
            }
        }

        // 验证昵称格式
        if (StringUtils.isEmptyOrNull(nickname) || !nickname.matches("^.{1,12}$")) {
            throw new ServiceException(UserError.ACC_106.getErrCode(), "昵称格式错误");
        }
        // 验证密码格式
        if (StringUtils.isEmptyOrNull(password) || !password.matches("^.{6,20}$")) {
            throw new ServiceException(UserError.ACC_107.getErrCode(), "密码格式错误");
        }
        // 验证昵称中的关键字
        verifyNicKey(nickname);
        // 验证昵称是否重复
        if (this.verifyNickRep(nickname)) {
            throw new ServiceException(UserError.ACC_102.getErrCode(), "昵称已经被使用");
        }

        User user = new User();
        user.setAccount(account);
        // 加密处理
        password = CodeUtils.md5(password);
        user.setPassword(password);
        user.setNickname(nickname);
        return this.addUser(user);
    }

    private void verifyAccKey(String word) throws ServiceException {
        if (word.indexOf(KEY_ACC) > -1)
            throw new ServiceException(UserError.ACC_113.getErrCode(), "账号中不能包含关键字");
    }

    private void verifyNicKey(String word) throws ServiceException {
        if (word.indexOf(KEY_NIC) > -1)
            throw new ServiceException(UserError.ACC_114.getErrCode(), "昵称中不能包含关键字");
    }

    private User addUser(User user) {
        user.setUserId(userDao.selectSequence());
        userDao.insert(user);
        return user;
    }

    @Override
    public User checkLogin(String account, String password, String sysVcode, String vcode)
            throws ServiceException {
        if (!vcode.equalsIgnoreCase(sysVcode)) {
            throw new ServiceException(UserError.ACC_116.getErrCode(), "验证码错误");
        }
        
        password = CodeUtils.md5(password);
        String pwd = userDao.queryPwdByAccount(account);
        if (StringUtils.isEmptyOrNull(pwd) || !password.equals(pwd)) {
            throw new ServiceException(UserError.ACC_109.getErrCode(), "用户账号或者密码错误");
        }
        return userDao.queryByAccount(account);
    }

    @Override
    public boolean verifyAccountRep(String account) {
        int n = userDao.countByAccount(account);
        if (n != 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean verifyNickRep(String nickname) {
        int n = userDao.countByNick(nickname);
        if (n != 0) {
            return true;
        }
        return false;
    }

    @Override
    public User getUserById(Long userId) {
        return userDao.selectOneById(userId);
    }

    @Override
    public User getUserPrivacyById(long userId) {
        return userDao.queryPrivacyById(userId);
    }

    public User getUserPrivacyByAccount(String account) {
        return userDao.queryPrivacyByAccount(account);
    }

    @Override
    public void editUserPassword(long userId, String oldPsw, String newPsw)
            throws ServiceException {
        // 验证密码格式
        if (StringUtils.isEmptyOrNull(newPsw) || !newPsw.matches("^.{6,20}$")) {
            throw new ServiceException(UserError.ACC_107.getErrCode(), "密码格式错误");
        }

        // 密码MD5加密
        oldPsw = CodeUtils.md5(oldPsw);
        User user = this.getUserById(userId);
        if (!user.getPassword().equals(oldPsw)) {
            throw new ServiceException(UserError.ACC_103.getErrCode(), "用户原始密码错误");
        }

        newPsw = CodeUtils.md5(newPsw);
        userDao.updatePassword(userId, newPsw);
    }

    @Override
    public void editUserPhoto(long userId, String avatarServer, String avatarPath) {
        userDao.updatePhoto(userId, avatarServer, avatarPath);
		//songmUserService.editUserInfo(String.valueOf(userId),
		//		JsonUtils.getInstance().toJson(getUserById(userId)));
    }

    @Override
    public void editUserBasic(long userId, String nickname, String userName,
            Integer gender, Integer birthYear, Integer birthMonth,
            Integer birthDay, String summary) throws ServiceException {
        // 数据不能为空
        if (userId <= 0 || nickname.trim().equals("")) {
            throw new IllegalArgumentException();
        }
        // 验证昵称格式
        if (StringUtils.isEmptyOrNull(nickname) || !nickname.matches("^.{1,12}$")) {
            throw new ServiceException(UserError.ACC_106.getErrCode(), "昵称格式错误");
        }
        // 验证生日格式
        if (birthYear != null && birthMonth != null && birthDay != null) {
        	Calendar calendar = Calendar.getInstance();
            try {
                calendar.set(birthYear, birthMonth - 1, birthDay);
            } catch (Exception e) {
                throw new ServiceException(UserError.ACC_104.getErrCode(), "生日格式错误", e);
            }
        }
        // 验证昵称
        User user = this.getUserById(userId);
        if (!user.getNickname().equals(nickname)) {
            // 验证昵称中的关键字
            verifyNicKey(nickname);
            if (this.verifyNickRep(nickname)) {
                throw new ServiceException(UserError.ACC_102.getErrCode(), "昵称已经被使用");
            }
        }

        userDao.update(userId, nickname, userName, gender, birthYear,
                birthMonth, birthDay, summary);
        user.setNickname(nickname);
		user.setRealName(userName);
		user.setGender(gender);
		user.setBirthYear(birthYear);
		user.setBirthMonth(birthMonth);
		user.setBirthDay(birthDay);
		user.setSummary(summary);
		//songmUserService.editUserInfo(String.valueOf(userId), JsonUtils.getInstance().toJson(user));
    }

    @Override
    public void editNickname(long userId, String nickname) throws ServiceException {
        // 数据不能为空
        if (userId <= 0 || nickname.trim().equals("")) {
            throw new IllegalArgumentException();
        }
        
        // 验证昵称格式
        if (StringUtils.isEmptyOrNull(nickname) || !nickname.matches("^.{1,12}$")) {
            throw new ServiceException(UserError.ACC_106.getErrCode(), "昵称格式错误");
        }
        
        // 验证昵称
        User user = this.getUserById(userId);
        if (!user.getNickname().equals(nickname)) {
            // 验证昵称中的关键字
            verifyNicKey(nickname);
            if (this.verifyNickRep(nickname)) {
                throw new ServiceException(UserError.ACC_102.getErrCode(), "昵称已经被使用");
            }
        }

        userDao.update(userId, nickname, null, null, null, null, null, null);
        user.setNickname(nickname);
        //songmUserService.editUserInfo(String.valueOf(userId), JsonUtils.getInstance().toJson(user));
    }

    @Override
    public void editRealName(long userId, String realName) {
        if (userId <= 0 || realName.trim().equals("")) {
            throw new IllegalArgumentException();
        }

        userDao.update(userId, null, realName, null, null, null, null, null);
        //User user = this.getUserById(userId);
        //songmUserService.editUserInfo(String.valueOf(userId), JsonUtils.getInstance().toJson(user));
    }

    @Override
    public void editUserGender(long userId, Integer gender) {
        if (userId <= 0) {
            throw new IllegalArgumentException();
        }
        
        userDao.update(userId, null, null, gender, null, null, null, null);
        //User user = this.getUserById(userId);
        //songmUserService.editUserInfo(String.valueOf(userId), JsonUtils.getInstance().toJson(user));
    }

    @Override
    public void editUserBirthday(long userId, int birthYear, int birthMonth,
            int birthDay) throws ServiceException {
        // 数据不能为空
        if (userId <= 0) {
            throw new IllegalArgumentException();
        }
        
        // 验证生日格式
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.set(birthYear, birthMonth - 1, birthDay);
        } catch (Exception e) {
            throw new ServiceException(UserError.ACC_104.getErrCode(), "生日格式错误", e);
        }

        userDao.update(userId, null, null, null, birthYear, birthMonth, birthDay, null);
        //User user = this.getUserById(userId);
        //songmUserService.editUserInfo(String.valueOf(userId), JsonUtils.getInstance().toJson(user));
    }

    @Override
    public void editSummary(long userId, String summary) {
        if (userId <= 0) {
            throw new IllegalArgumentException();
        }

        userDao.update(userId, null, null, null, null, null, null, summary);
        //User user = this.getUserById(userId);
        //songmUserService.editUserInfo(String.valueOf(userId), JsonUtils.getInstance().toJson(user));
    }

	@Override
	public void editUserAccount(long userId, String account, String password) throws ServiceException {
		User user = this.getUserById(userId);
		if (StringUtils.isEmptyOrNull(user.getAccount())) {
			throw new ServiceException(UserError.ACC_AEXIST.getErrCode(), "用户账号已经存在");
		}
		
		// 验证密码格式
        if (StringUtils.isEmptyOrNull(password) || !password.matches("^.{6,20}$")) {
            throw new ServiceException(UserError.ACC_107.getErrCode(), "密码格式错误");
        }

        userDao.updateAccount(userId, account, CodeUtils.md5(password));
        //songmUserService.editUserInfo(String.valueOf(userId), JsonUtils.getInstance().toJson(user));
	}

}
