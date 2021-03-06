package cn.appinfo.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.popup.QMUIListPopup;
import com.qmuiteam.qmui.widget.popup.QMUIPopup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.appinfo.R;
import cn.appinfo.entity.AppInfo;
import cn.appinfo.entity.UserInfo;
import cn.appinfo.tools.Constants;
import cn.appinfo.tools.OkHttpUtil;
import cn.appinfo.tools.ResultUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DetailActivity extends Activity {
    @BindView(R.id.topbar)
    QMUITopBar mTopBar;
    @BindView(R.id.app_detail)
    ListView listView;
    @BindView(R.id.app_info_icon)
    ImageView iconView;

    @BindView(R.id.pass)
    Button passButton;
    @BindView(R.id.no_pass)
    Button unPassButton;
    @BindView(R.id.dev_button_Layout)
    LinearLayout devButtonLayout;

    @BindView(R.id.shelves_button)
    Button shelvesButton;
    @BindView(R.id.updateAppInfo_button)
    Button updateButton;
    @BindView(R.id.deleteAppInfo_button)
    Button deleteButton;
    @BindView(R.id.audit_button_Layout)
    RelativeLayout auditButtonLayout;
    private AppInfo appInfo;
    private int loginType;
    private UserInfo userInfo;
    private Context context=this;
    private Integer appInfoId;
    private SimpleDateFormat simpleDateFormat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appinfo_detail);
        ButterKnife.bind(this);
        QMUIStatusBarHelper.translucent(this);// 沉浸式状态栏
        Intent intent=getIntent();
        appInfo= (AppInfo) intent.getSerializableExtra("appInfo");
        loginType=intent.getIntExtra("loginType",Constants.BACKEND_USER_TYPE);
        userInfo= (UserInfo) intent.getSerializableExtra("userInfo");
        if(loginType==Constants.BACKEND_USER_TYPE){
            showAuditInfo(appInfo);
        }else{
            showDevAppInfo(appInfo);
        }
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        appInfoId=appInfo.getId();
        loadAppInfo();

        initTopBar();
        mTopBar.setTitle(appInfo.getSoftwareName());


    }
    private void loadAppInfo(){
        List<String> list=new ArrayList<>();
        list.add("软件名称："+appInfo.getSoftwareName());
        list.add("APK名称："+appInfo.getApkName());
        list.add("支持Rom："+appInfo.getSupportROM());
        list.add("界面语言："+appInfo.getInterfaceLanguage());
        list.add("软件大小："+appInfo.getSoftwareSize()+"MB");
        list.add("下载次数："+appInfo.getDownloads()+"次");
        list.add("所属平台："+appInfo.getFlatformName());
        list.add("所属分类："+appInfo.getCategoryLevel1Name()+"--"+appInfo.getCategoryLevel2Name()+"--"+appInfo.getCategoryLevel3Name());
        list.add("软件状态："+appInfo.getStatusName());
        list.add("软件简介："+appInfo.getAppInfo());
        list.add("创建时间："+simpleDateFormat.format(appInfo.getCreationDate().getTime()));
        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,list);
        listView.setAdapter(arrayAdapter);
        loadIcon();
    }
    private void showAuditInfo(AppInfo appInfo){
        devButtonLayout.setVisibility(View.GONE);
        if (appInfo.getVersionId()==null || appInfo.getVersionId()==0){
            passButton.setClickable(false);
            passButton.setText("  该软件无新版本，无需审核  ");
            passButton.getLayoutParams().width= ViewGroup.LayoutParams.WRAP_CONTENT;
            unPassButton.setVisibility(View.GONE);
        }else {
            switch (appInfo.getStatus()) {
                case Constants.PASS_STATUS:
                    passButton.setClickable(false);
                    passButton.setText("已审核通过");
                    unPassButton.setVisibility(View.GONE);
                    break;
                case Constants.UN_PASS_STATUS:
                    unPassButton.setClickable(false);
                    unPassButton.setText("已审核不通过");
                    passButton.setVisibility(View.GONE);
                    break;
                case Constants.SOLD_UP_STATUS:
                    passButton.setClickable(false);
                    passButton.setText("已上架");
                    unPassButton.setVisibility(View.GONE);
                    break;
                case Constants.SOLD_DOWN_STATUS:
                    passButton.setClickable(false);
                    passButton.setText("已下架");
                    unPassButton.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }
    }
    private void showDevAppInfo(AppInfo appInfo) {
        auditButtonLayout.setVisibility(View.GONE);
        switch (appInfo.getStatus()) {
            case Constants.WAITING_AUDIT:
            case Constants.UN_PASS_STATUS:
                shelvesButton.setVisibility(View.GONE);
                break;
            case Constants.PASS_STATUS:
            case Constants.SOLD_DOWN_STATUS:
                shelvesButton.setText("上架");
                break;
            case Constants.SOLD_UP_STATUS:
                shelvesButton.setText("下架");
                break;
            default:
                break;
        }
    }
    private void loadIcon() {
        final String url = Constants.SERVER_IP + ":" + Constants.SERVER_PORT + appInfo.getLogoPicPath();
        final File dir = context.getFilesDir();
        File file = new File(dir, appInfo.getId() + ".png");
        if (file.exists()) {
            //本地加载
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
            if(bitmap!=null){
                iconView.setImageBitmap(bitmap);
            }else{
                iconView.setImageResource(R.mipmap.no_picture);
            }
        } else {
            //网络加载
            if (appInfo.getLogoPicPath() != null) {

                Message message=new Message();
                OkHttpUtil.sendOkHttpRequest(url, null, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i("loadIconUrl", "load---" + url + "----Fail!!");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        byte[] data = response.body().bytes();
                        //保存文件
                        OutputStream os = new FileOutputStream(new File(dir, appInfo.getId() + ".png"));
                        os.write(data);
                        os.flush();
                        os.close();
                        //创建BitMap
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        message.what=Constants.LOAD_IMAGE_SUCCESS;
                        message.obj=bitmap;
                        handler.sendMessage(message);
                    }

                });
            }
        }
    }
    private void initTopBar(){
        mTopBar.addLeftBackImageButton().setOnClickListener(v -> finish());
        if(loginType==Constants.DEV_USER_TYPE) {
            mTopBar.addRightImageButton(R.mipmap.icon_topbar_overflow, R.id.topbar_right_change_button)
                .setOnClickListener(v -> {
                    QMUIListPopup qmuiListPopup;
                    List<String> data = new ArrayList<>();
                    data.add("新增版本");
                    if(appInfo.getVersionId()!=null){
                        data.add("修改版本");
                    }
                    ArrayAdapter adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, data);//为listview创建适配器
                    qmuiListPopup = new QMUIListPopup(context, QMUIPopup.DIRECTION_NONE, adapter);//创建QMUIListPopup实例 第二个参数：显示位置，第三个位置绑定adapter
                    //创建一个listview第一个参数：设置宽度 2设置高度，3设置adapter的监听
                    qmuiListPopup.create(QMUIDisplayHelper.dp2px(context, 250), QMUIDisplayHelper.dp2px(context, 200), (adapterView, view22, i1, l) -> {
                        if(i1==0){//新增版本
                            Intent intent=new Intent(context,AppVersionActivity.class);
                            intent.putExtra("appId",appInfo.getId());
                            intent.putExtra("softwareName",appInfo.getSoftwareName());
                            intent.putExtra("userInfo",userInfo);
                            startActivity(intent);
                        }else if(i1==1){//修改版本
                            Intent intent=new Intent(context,AppVersionActivity.class);
                            intent.putExtra("appId",appInfo.getId());
                            intent.putExtra("versionId",appInfo.getVersionId());
                            intent.putExtra("softwareName",appInfo.getSoftwareName());
                            intent.putExtra("userInfo",userInfo);
                            intent.putExtra("isUpdate","true");
                            startActivity(intent);
                        }
                        qmuiListPopup.dismiss();
                    });
                    qmuiListPopup.setAnimStyle(QMUIPopup.ANIM_GROW_FROM_CENTER);//设置显示样式
                    qmuiListPopup.show(v);//设置在哪个控件上显示QMUIpopup
                });
        }
    }

    /**
     * 审核通过
     */
    @OnClick(R.id.pass)
    public void auditPass(){
        new QMUIDialog.MessageDialogBuilder(this)
                .setMessage("确定通过审核?")
                .addAction("取消", (dialog, index) -> dialog.dismiss())
                .addAction("确定", (dialog, index) -> {
                    audit(Constants.PASS_STATUS);
                    dialog.dismiss();
                }).show();

    }
    /**
     * 审核不通过
     */
    @OnClick(R.id.no_pass)
    public void auditNoPass(){
        new QMUIDialog.MessageDialogBuilder(this)
            .setMessage("确定不通过审核?")
            .addAction("取消", (dialog, index) -> dialog.dismiss())
            .addAction("确定", (dialog, index) -> {
                audit(Constants.UN_PASS_STATUS);
                dialog.dismiss();
            }).show();
    }
    public void audit(int status){
        RequestBody requestBody = new FormBody.Builder()
            .add("appInfoId", appInfoId+"")
            .add("status",status+"")
            .build();
        OkHttpUtil.sendOkHttpRequest(Constants.AUDIT_APP_URL, requestBody, new Callback() {
            Message message = new Message();

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                message.what = Constants.FAIL;
                message.obj = "网络异常,请检查网络连接.";
                handler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                boolean result = Boolean.parseBoolean(response.body().string());
                if(result){
                    message.what = Constants.AUDIT_SUCCESS;
                    message.obj = "审核成功！";
                    finish();
                }else{
                    message.what = Constants.FAIL;
                    message.obj = "审核失败！";
                }
                handler.sendMessage(message);

            }
        });
    }

    /**
     * 上下架操作
     */
    @OnClick(R.id.shelves_button)
    public void soldUpAndDown(){
        String operation=shelvesButton.getText().toString();
        new QMUIDialog.MessageDialogBuilder(this)
            .setMessage("确定"+operation+"？")
            .addAction("取消", (dialog, index) -> dialog.dismiss())
            .addAction("确定", (dialog, index) -> {
                String url=null;
                if(operation.equals("上架")){
                    url=Constants.SOLD_UP_APP_URL;
                }else if(operation.equals("下架")){
                    url=Constants.SOLD_DOWN_APP_URL;
                }
                RequestBody requestBody = new FormBody.Builder()
                        .add("id", appInfoId+"")
                        .build();
                OkHttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
                    Message message = new Message();

                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        message.what = Constants.FAIL;
                        message.obj = "网络异常,请检查网络连接.";
                        handler.sendMessage(message);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String json = response.body().string();
                        Gson gson=new Gson();
                            ResultUtil resultUtil = gson.fromJson(json, ResultUtil.class);
                            Message msg=new Message();
                            if (resultUtil.isResult()){
                                msg.what=Constants.SOLD_UP_OR_DOWN_APP_SUCCESS;
                                message.obj = operation+"操作成功！";
                                finish();
                            }else {
                                msg.what=Constants.FAIL;
                                message.obj = operation+"操作失败！";
                            }
                            handler.sendMessage(message);

                        }
                    });
                    dialog.dismiss();
                }).show();

    }

    @OnClick(R.id.updateAppInfo_button)
    public void updateApp(){
        Intent intent=new Intent(context, AddAppInfoActivity.class);
        intent.putExtra("appInfo",appInfo);
        intent.putExtra("devId",appInfo.getDevId());
        startActivityForResult(intent,Constants.UPDATE_APPINFO_CODE);
    }
    /**
     * 删除app信息
     */
    @OnClick(R.id.deleteAppInfo_button)
    public void deleteApp(){
        new QMUIDialog.MessageDialogBuilder(this)
                .setMessage("添加一次很辛苦的，确定删除APP信息？")
                .addAction("取消", (dialog, index) -> dialog.dismiss())
                .addAction("确定", (dialog, index) -> {
                    RequestBody requestBody = new FormBody.Builder()
                            .add("id", appInfoId+"")
                            .build();
                    OkHttpUtil.sendOkHttpRequest(Constants.DELETE_APP_URL, requestBody, new Callback() {
                        Message message = new Message();

                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                            message.what = Constants.FAIL;
                            message.obj = "网络异常,请检查网络连接.";
                            handler.sendMessage(message);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String json = response.body().string();
                            Gson gson=new Gson();
                            ResultUtil resultUtil = gson.fromJson(json, ResultUtil.class);
                            Message msg=new Message();
                            if (resultUtil.isResult()){
                                msg.what=Constants.DELETE_APP_SUCCESS;
                                message.obj = "删除成功！";
                                finish();
                            }else {
                                msg.what=Constants.FAIL;
                                message.obj = "删除失败！";
                            }
                            handler.sendMessage(message);
                        }
                    });
                    dialog.dismiss();
                }).show();
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case Constants.LOAD_APPINFO_DETAIL_SUCCESS:
                    loadAppInfo();
                break;
                case Constants.LOAD_IMAGE_SUCCESS:
                    iconView.setImageBitmap((Bitmap) msg.obj);
                    break;
                case Constants.AUDIT_SUCCESS:
                case Constants.SOLD_UP_OR_DOWN_APP_SUCCESS:
                case Constants.DELETE_APP_SUCCESS:
                    setResult(Constants.LOAD_APPINFO_DETAIL_CODE);
                default:
                    Toast.makeText(context,msg.obj.toString(),Toast.LENGTH_LONG).show();
                break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case Constants.UPDATE_APPINFO_CODE:
                Message message = new Message();
                RequestBody requestBody = new FormBody.Builder()
                        .add("id", appInfo.getId()+"").build();
                OkHttpUtil.sendOkHttpRequest(Constants.GET_APP_DETAIL_URL, requestBody, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        message.what = Constants.FAIL;
                        message.obj = "网络超时,请查看网络......";
                        handler.sendMessage(message);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        message.what = Constants.LOAD_APPINFO_DETAIL_SUCCESS;
                        String json = response.body().string();
                        //把json转换成Java对象
                        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
                        appInfo = gson.fromJson(json, AppInfo.class);
                        handler.sendMessage(message);
                    }
                });
                break;
        }
    }
}
