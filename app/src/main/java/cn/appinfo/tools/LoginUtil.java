package cn.appinfo.tools;

import android.content.Context;
import android.content.SharedPreferences;

import cn.appinfo.entity.BackendUser;
import cn.appinfo.entity.DevUser;
import cn.appinfo.entity.UserInfo;

/**
 * 用于保存登录的用户
 * Created by gumuyun on 2018/6/13.
 */
public class LoginUtil {

    public static void removeLoginUserInfo(Context context,UserInfo userInfo){
        SharedPreferences sharedPreferences = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.remove("userType");
        switch (userInfo.getUserType()){
            case Constants.BACKEND_USER_TYPE:
                editor.remove("userId");
                editor.remove("userCode");
                editor.remove("userName");
                break;
            case Constants.DEV_USER_TYPE:
                editor.remove("userId");
                editor.remove("email");
                editor.remove("userName");
                break;
        }
        editor.apply();//提交
    }


    /**
     * 获得已登录的用户信息
     * @param context
     * @return
     */
    public static UserInfo getLoginUserInfo(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        int userType = sharedPreferences.getInt("userType", 0);

        switch (userType){
            case Constants.BACKEND_USER_TYPE:
                BackendUser backendUser=new BackendUser();
                int backendUserId = sharedPreferences.getInt("userId", 0);
                String userCode = sharedPreferences.getString("userCode", null);
                String userName = sharedPreferences.getString("userName", null);
                backendUser.setId(backendUserId);
                backendUser.setUserCode(userCode);
                backendUser.setUserName(userName);
                UserInfo<BackendUser> backendUserUserInfo=new UserInfo<>(userType,backendUser);
                return backendUserUserInfo;

            case Constants.DEV_USER_TYPE:
                DevUser devUser=new DevUser();
                int userId = sharedPreferences.getInt("userId", 0);
                String email = sharedPreferences.getString("email", null);
                String devName = sharedPreferences.getString("userName", null);
                devUser.setId(userId);
                devUser.setDevName(devName);
                devUser.setDevEmail(email);
                UserInfo<DevUser> devUserUserInfo=new UserInfo<>(userType,devUser);
                return devUserUserInfo;
        }
        return null;
    }

    /**
     * 保存用户登录信息
     * @param context
     * @param userInfo
     */
    public static void putLoginUserInfo(Context context,UserInfo userInfo){
        //保存数据对象
        SharedPreferences sharedPreferences = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        //写数据
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("userType",userInfo.getUserType());//保存用户的类型
        //判断是什么类型的用户
        switch (userInfo.getUserType()){
            case Constants.BACKEND_USER_TYPE:
                BackendUser backendUser= (BackendUser) userInfo.getUser();
                editor.putInt("userId",backendUser.getId());
                editor.putString("userCode",backendUser.getUserCode());
                editor.putString("userName",backendUser.getUserName());
                break;
            case Constants.DEV_USER_TYPE:
                DevUser devUser= (DevUser) userInfo.getUser();
                editor.putInt("userId",devUser.getId());
                editor.putString("userName",devUser.getDevName());
                editor.putString("email",devUser.getDevEmail());
                break;
        }
        editor.apply();
        //editor.commit();
    }


}
