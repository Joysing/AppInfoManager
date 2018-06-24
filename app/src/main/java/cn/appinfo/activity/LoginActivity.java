package cn.appinfo.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.appinfo.R;
import cn.appinfo.entity.BackendUser;
import cn.appinfo.entity.DevUser;
import cn.appinfo.entity.UserInfo;
import cn.appinfo.tools.Constants;
import cn.appinfo.tools.LoginUtil;
import cn.appinfo.tools.OkHttpUtil;
import cn.appinfo.tools.QMUITipDialogUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;


public class LoginActivity extends Activity {
    @BindView( R.id.rolesSpinner)
    Spinner rolesSpinner;
    @BindView( R.id.buttonLogin)
    Button buttonLogin;
    @BindView( R.id.editText_userName)
    EditText editText_userName;
    @BindView( R.id.editText_password)
    EditText editText_password;
    @BindView( R.id.checkBox_rememberMe)
    CheckBox checkBox_rememberMe;

    final Context context=this;
    int loginType;// 登录类型（1管理员，2开发者）
    QMUITipDialog tipDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //判断是否有保存已登录的用户信息,如果有则跳转
        checkLoginUser();
        super.onCreate(savedInstanceState);
        setContentView( R.layout.login);
        QMUIStatusBarHelper.translucent(this);// 沉浸式状态栏
        ButterKnife.bind(this);
        initLogin();
        rolesSpinner.setSelection(1);
    }

    public void checkLoginUser(){
        //获得已登录的用户
        UserInfo userInfo = LoginUtil.getLoginUserInfo(this);
        if (userInfo!=null){
            //跳转
            jumpActivity(userInfo);
            finish();//结束当前Activity的生命 周期
        }
    }
    /**
     * 界面跳转,跟据用户类型跳转相对应的Activity
     */
    private void jumpActivity(UserInfo userInfo) {
        Intent intent =null;
        switch (userInfo.getUserType()){
            case Constants.BACKEND_USER_TYPE:
                //跳管理员后台
                intent=new Intent(this,ManagerMainActivity.class);
                break;
            case Constants.DEV_USER_TYPE:
                //跳开发者平台
                intent=new Intent(this,DeveloperMainActivity.class);
                break;
        }
        intent.putExtra("userInfo",userInfo);
        startActivity(intent);//界面跳转
    }

    private void initLogin() {
        UserInfo userInfo=LoginUtil.getLoginUserInfo(context);
        if(userInfo!=null){
            if(Constants.BACKEND_USER_TYPE==userInfo.getUserType()){
                BackendUser user= (BackendUser) userInfo.getUser();
                editText_userName.setText(user.getUserCode());
            }else if(Constants.DEV_USER_TYPE==userInfo.getUserType()){
                DevUser user= (DevUser) userInfo.getUser();
                editText_userName.setText(user.getDevCode());
            }
        }
        List<String> rolesList = new ArrayList<>();
        rolesList.add("我是开发者");
        rolesList.add("我是管理员");
        ArrayAdapter<String> adapter=new ArrayAdapter<>(//创建适配器(下拉框的数据源是来自适配器)
        this,
                android.R.layout.simple_spinner_item,
                rolesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);//为适配器添加样式
        rolesSpinner.setAdapter(adapter);
        rolesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // parent： 为控件Spinner view：显示文字的TextView position：下拉选项的位置从0开始
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position==0){
                    loginType=Constants.DEV_USER_TYPE;
                } else if(position==1){
                    loginType=Constants.BACKEND_USER_TYPE;
                }
            }
            //没有选中时的处理
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @SuppressLint("HandlerLeak")
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Object result = msg.obj;
            switch (msg.what){
                case Constants.LOGIN_FAIL:
                    Toast.makeText(context, result.toString(), Toast.LENGTH_LONG).show();
                    break;
                case Constants.LOGIN_SUCCESS:
                    Toast.makeText(context, "登录成功", Toast.LENGTH_LONG).show();
                    UserInfo userInfo= (UserInfo) result;
                    //界面跳转,跟据UserInfo的userType来决定跳转界面
                    jumpActivity(userInfo);
                    finish();
                    break;
                default:
                    break;
            }
        }
    };

    @OnClick( R.id.buttonLogin)
    protected void onClickButtonLogin() {
        String username = editText_userName.getText().toString();
        String password = editText_password.getText().toString();
        if ("".equals(username.trim()) || "".equals(password.trim())) {
            Toast.makeText(this, "账号或密码不能为空", Toast.LENGTH_LONG).show();
            return;
        }

        tipDialog = QMUITipDialogUtil.showTipDialog(Constants.ICON_TYPE_LOADING, context, "登录中...");
        tipDialog.show();//显示创建的tipDialog

        Message message = new Message();
        RequestBody requestBody = new FormBody.Builder()
                .add("userCode", username)
                .add("userPassword", password).build();
        String loginUrl="";
        if (loginType==Constants.BACKEND_USER_TYPE) {//管理员登录
            loginUrl = Constants.BACKEND_LOGIN_URL;
        } else if (loginType==Constants.DEV_USER_TYPE) {//开发者登录
            loginUrl = Constants.DEV_LOGIN_URL;
        }
        OkHttpUtil.sendOkHttpRequest(loginUrl, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                message.what = Constants.LOGIN_FAIL;
                message.obj = "网络超时,请查看网络......";
                handler.sendMessage(message);
                tipDialog.dismiss();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                tipDialog.dismiss();
                message.what = Constants.LOGIN_SUCCESS;
                String json = response.body().string();
                //把json转换成Java对象
                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
                switch (loginType){
                    case Constants.BACKEND_USER_TYPE:
                        try{
                            BackendUser backendUser = gson.fromJson(json, BackendUser.class);
                            //判断用户是否封装成功
                            if (backendUser==null){
                                message.what = Constants.LOGIN_FAIL;
                                message.obj = "用户名或密码不正确!";
                                break;
                            }
                            UserInfo<BackendUser> backendUserUserInfo= new UserInfo<>(Constants.BACKEND_USER_TYPE, backendUser);
                            message.obj = backendUserUserInfo;
                            //保存登录用户信息
                            if (checkBox_rememberMe.isChecked()) {
                                LoginUtil.putLoginUserInfo(context,backendUserUserInfo);
                            }
                        }catch (Exception ex){
                            ex.printStackTrace();
                            message.what = Constants.LOGIN_FAIL;
                            message.obj = "请求出服器出错，请联系管理员!";
                        }

                        break;
                    case Constants.DEV_USER_TYPE:
                        try{
                            DevUser devUser = gson.fromJson(json, DevUser.class);
                            if (devUser==null){
                                message.what = Constants.LOGIN_FAIL;
                                message.obj = "用户名或密码不正确!";
                                break;
                            }
                            UserInfo<DevUser> devUserUserInfo=new UserInfo<>(Constants.DEV_USER_TYPE,devUser);
                            message.obj = devUserUserInfo;
                            //保存登录用户信息
                            if (checkBox_rememberMe.isChecked()) {
                                LoginUtil.putLoginUserInfo(context,devUserUserInfo);
                            }
                        }catch (Exception ex){
                            ex.printStackTrace();
                            message.what = Constants.LOGIN_FAIL;
                            message.obj = "请求出服器出错,请联系管理员!";
                        }
                        break;
                }
                handler.sendMessage(message);
            }
        });
    }
}
