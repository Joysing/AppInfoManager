package cn.appinfo.entity;

import java.io.Serializable;

/**
 * 存放登录的用户类
 * Created by gumuyun on 2018/6/13.
 */
public class UserInfo<T> implements Serializable{
    private int userType;//用户类型
    private T user;//用户对象,可以是BackendUser\DevUser

    public UserInfo() {
    }

    public UserInfo(int userType, T user) {
        this.userType = userType;
        this.user = user;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public T getUser() {
        return user;
    }

    public void setUser(T user) {
        this.user = user;
    }
}
