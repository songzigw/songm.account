package songm.account.dao;

import songm.account.entity.PageInfo;
import songm.account.entity.User;

/**
 * 用户数据持久层
 * 
 * @author 张松
 *
 */
public interface UserDao {
    /**
     * 获取主键
     * 
     * @return
     */
    public long getId();

    /**
     * 插入用户数据
     * 
     * @param user
     * @return
     */
    public int insert(User user);

    /**
     * 根据ID查询用户数据
     * 
     * @param userId
     * @return
     */
    public User queryById(Long userId);

    /**
     * 根据账号查询用户数据
     * 
     * @param account
     * @return
     */
    public User queryByAccount(String account);

    /**
     * 根据ID获取用户隐私
     * @param userId
     * @return
     */
    public User queryPrivacyById(long userId);

    /**
     * 根据账号获取用户隐私
     * @param account
     * @return
     */
    public User queryPrivacyByAccount(String account);

    /**
     * 根据账号，计算拥有该账号用户个数。
     * 
     * @param account
     * @return
     */
    public int countByAccount(String account);

    /**
     * 根据昵称，计算使用该昵称用户个数。
     * 
     * @param nick
     * @return
     */
    public int countByNick(String nick);

    /**
     * 查询
     * 
     * @param keyword
     * @param currPage
     * @param pageSize
     * @return
     */
    public PageInfo<User> queryListByKeyword(String keyword, int currPage,
            int pageSize);

    /**
     * 修改密码
     * 
     * @param userId
     * @param newPsw
     * @return
     */
    public int updatePsw(Long userId, String newPsw);

    /**
     * 修改头像
     * 
     * @param userId
     * @param avatar
     */
    public int updatePhoto(Long userId, String avatar);

    /**
     * 修改用户信息
     * 
     * @param userId
     * @param nick
     * @param realName
     * @param gender
     * @param birthdayYear
     * @param birthdayMonth
     * @param birthdayDay
     * @param summary
     */
    public int update(Long userId, String nick, String realName,
            Integer gender, int birthdayYear, int birthdayMonth, int birthdayDay,
            String summary);

    /**
     * 修改用户rongToken
     * 
     * @param userId
     * @param rongToken
     * @return
     */
    public int updateRongToken(long userId, String rongToken);

    /**
     * 修改账号
     * 
     * @param userId
     * @param account
     * @return
     */
    public int updateAccount(Long userId, String account);

    /**
     * 根据账号获取密码
     * @param account
     * @return
     */
    public String queryPwdByAccount(String account);
}
