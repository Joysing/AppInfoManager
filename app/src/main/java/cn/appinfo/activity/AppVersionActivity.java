package cn.appinfo.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.appinfo.R;
import cn.appinfo.entity.AppVersion;
import cn.appinfo.entity.DevUser;
import cn.appinfo.entity.UserInfo;
import cn.appinfo.tools.Constants;
import cn.appinfo.tools.FileUtil;
import cn.appinfo.tools.OkHttpUtil;
import cn.appinfo.tools.Progress.ProgressHelper;
import cn.appinfo.tools.Progress.ProgressUIListener;
import cn.appinfo.tools.ResultUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AppVersionActivity extends AppCompatActivity {
    private static final String TAG = "AppVersionActivity";
    Context context=this;
    @BindView(R.id.topbar)
    QMUITopBar qmuiTopBar;
    @BindView(R.id.historyVersion)
    ListView listView;
    @BindView(R.id.versionNo_et)
    EditText versionNoEditText;
    @BindView(R.id.versionInfo_et)
    EditText versionInfoEditText;
    @BindView(R.id.addVersion_button)
    Button updateVersionButton;
    @BindView(R.id.apk_filename)
    TextView apkFileNameTextView;
    @BindView(R.id.apkfileTag)
    TextView apkFileTag;
    @BindView(R.id.imageButton)
    ImageButton imageButton;
    @BindView(R.id.uploadInfo)
    TextView uploadInfo;
    @BindView(R.id.progressBar)
    android.widget.ProgressBar progressBar;

    private UserInfo userInfo;
    private int appId;
    private int userId;
    private int versionId;
    private String apkFilePath="";
    private String apkFileName="";
    private BigDecimal versionSize;

    AppVersion appVersion;
    private static final int FILE_SELECT_CODE = 1;
    List<AppVersion> appVersionList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_version);
        ButterKnife.bind(this);
        Intent intent=getIntent();
        qmuiTopBar.addLeftBackImageButton().setOnClickListener(v -> finish());
        qmuiTopBar.setTitle("新增版本信息【"+intent.getStringExtra("softwareName")+"】");
        userInfo= (UserInfo) intent.getSerializableExtra("userInfo");
        userId=((DevUser)userInfo.getUser()).getId();
        appId=intent.getIntExtra("appId",0);
        versionId=intent.getIntExtra("versionId",0);
        getHistoryVersion();

        if(intent.getStringExtra("isUpdate")!=null){
            qmuiTopBar.setTitle("修改版本信息【"+intent.getStringExtra("softwareName")+"】");
            getNewVersion();
            updateVersion();
        }
    }

    /**
     * 获取历史版本
     */
    private void getHistoryVersion(){
        Message message = new Message();
        RequestBody requestBody = new FormBody.Builder()
                .add("id", appId+"")
                .build();
        OkHttpUtil.sendOkHttpRequest(Constants.GET_APP_VERSIONS_URL, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                message.what = Constants.FAIL;
                message.obj = "网络超时,请查看网络......";
                handler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                //把json转换成Java对象
                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
                Log.i("00",json);
                appVersionList = gson.fromJson(json, new TypeToken<List<AppVersion>>() {}.getType());
                message.what=Constants.LOAD_APP_VERSIONS_SUCCESS;
                handler.sendMessage(message);
            }
        });
    }
    /**
     * 获取最新版本
     */
    private void getNewVersion(){
        Message message = new Message();
        RequestBody requestBody = new FormBody.Builder()
                .add("appInfoId", appId+"")
                .add("versionId", versionId+"")
                .build();
        OkHttpUtil.sendOkHttpRequest(Constants.GET_APP_VERSION_URL, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                message.what = Constants.FAIL;
                message.obj = "网络超时,请查看网络......";
                handler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                //把json转换成Java对象
                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
                Log.i("00",json);
                appVersion = gson.fromJson(json, AppVersion.class);
                message.what=Constants.LOAD_APP_NEW_VERSION_SUCCESS;
                handler.sendMessage(message);
            }
        });
    }

    /**
     * 新增版本信息
     */
    @OnClick(R.id.addVersion_button)
    protected void addVersion(){
        Message message = new Message();
        String versionNo=versionNoEditText.getText().toString();
        String versionInfo=versionInfoEditText.getText().toString();
        if("".equals(versionNo)){
            Toast.makeText(context,"版本号不能为空",Toast.LENGTH_SHORT).show();
            return;
        }else if("".equals(versionInfo)){
            Toast.makeText(context,"版本简介不能为空",Toast.LENGTH_SHORT).show();
            return;
        }else if("".equals(apkFilePath)){
            Toast.makeText(context,"请选择apk文件",Toast.LENGTH_SHORT).show();
            return;
        }
        RequestBody fileBody = RequestBody.create(MediaType.parse("application/vnd.android.package-archive"), new File(apkFilePath));
        RequestBody multipartBody=new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("versionNo", versionNo)
                .addFormDataPart("versionInfo", versionInfo)
                .addFormDataPart("apkFile",apkFileName,fileBody)
                .addFormDataPart("appId", appId+"")
                .addFormDataPart("versionSize", versionSize+"")
                .addFormDataPart("createdBy", userId+"")
                .build();
        RequestBody requestBody = ProgressHelper.withProgress(multipartBody, new ProgressUIListener() {
            QMUITipDialog qmuiTipDialog;
            @Override
            public void onUIProgressStart(long totalBytes) {
                super.onUIProgressStart(totalBytes);
                Log.e(TAG, "onUIProgressStart:" + totalBytes);
                progressBar.setVisibility(View.VISIBLE);
                uploadInfo.setVisibility(View.VISIBLE);
                updateVersionButton.setClickable(false);
            }

            @Override
            public void onUIProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
                uploadInfo.setText("正在上传Apk文件:"+ (int)(percent * 100) + " %");
                progressBar.setProgress( (int)(percent * 100));
            }

            @Override
            public void onUIProgressFinish() {
                super.onUIProgressFinish();
                Log.e(TAG, "onUIProgressFinish:");
                qmuiTipDialog.dismiss();
            }
        });
        OkHttpUtil.sendOkHttpRequest(Constants.ADD_APP_VERSION_URL, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                message.what = Constants.FAIL;
                message.obj = "网络超时,请查看网络......";
                handler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                //把json转换成Java对象
                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
                ResultUtil resultUtil = gson.fromJson(json, ResultUtil.class);
                Message msg=new Message();
                if (resultUtil.isResult()){
                    msg.what=Constants.ADD_APP_VERSION_SUCCESS;
                    message.obj = "App版本添加成功！";
                    finish();
                }else {
                    msg.what=Constants.FAIL;
                    message.obj = "App版本添加失败！"+resultUtil.getMessage();
                }
                handler.sendMessage(message);
            }
        });
    }

    /**
     * 修改版本信息
     */
    private void updateVersion(){
        imageButton.setVisibility(View.INVISIBLE);
        apkFileNameTextView.setVisibility(View.INVISIBLE);
        apkFileTag.setVisibility(View.INVISIBLE);
        updateVersionButton.setText("提交更改");
        updateVersionButton.setOnClickListener(view -> {
            Message message = new Message();
            String versionNo=versionNoEditText.getText().toString();
            String versionInfo=versionInfoEditText.getText().toString();
            if("".equals(versionNo)){
                Toast.makeText(context,"版本号不能为空",Toast.LENGTH_SHORT).show();
                return;
            }else if("".equals(versionInfo)){
                Toast.makeText(context,"版本简介不能为空",Toast.LENGTH_SHORT).show();
                return;
            }
            RequestBody multipartBody=new MultipartBody.Builder()
                    .addFormDataPart("id", appVersion.getId()+"")
                    .addFormDataPart("appId", appId+"")
                    .addFormDataPart("modifydBy", userId+"")
                    .addFormDataPart("versionNo", versionNo)
                    .addFormDataPart("versionInfo", versionInfo)
                    .build();
            OkHttpUtil.sendOkHttpRequest(Constants.UPDATE_APP_VERSION_URL, multipartBody, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    message.what = Constants.FAIL;
                    message.obj = "网络超时,请查看网络......";
                    handler.sendMessage(message);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String json = response.body().string();
                    //把json转换成Java对象
                    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
                    ResultUtil resultUtil = gson.fromJson(json, ResultUtil.class);
                    Message msg=new Message();
                    if (resultUtil.isResult()){
                        msg.what=Constants.ADD_APP_VERSION_SUCCESS;
                        message.obj = "App版本信息修改成功！";
                        finish();
                    }else {
                        msg.what=Constants.FAIL;
                        message.obj = "App版本信息修改失败！";
                    }
                    handler.sendMessage(message);
                }
            });
        });
    }

    /**
     * 选择apk文件
     */
    @OnClick(R.id.imageButton)
    protected void choiceApkFile(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //如果App的权限申请曾经被用户拒绝过，就需要在这里跟用户做出解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this,"please give me the permission",Toast.LENGTH_SHORT).show();
            } else {
                //进行权限请求
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_SOME_FEATURES_PERMISSIONS);
            }
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.android.package-archive");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "选择文件"), FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "亲，木有文件管理器啊-_-!!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if ("file".equalsIgnoreCase(uri.getScheme())){//使用第三方应用打开
                apkFilePath = uri.getPath();
            }else{
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                    apkFilePath = FileUtil.getPath(context, uri);
                } else {//4.4以下下系统调用方法
                    apkFilePath = FileUtil.getRealPathFromURI(context,uri);
                }
            }
            if(apkFilePath!=null){
                try {
                    versionSize= BigDecimal.valueOf(getFileSize(new File(apkFilePath))/(1024.0*1024));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                apkFileName=new File(apkFilePath).getName();
                apkFileNameTextView.setText(apkFileName);
                Toast.makeText(context,"已选择apk文件："+apkFileName,Toast.LENGTH_SHORT).show();
            }
        }
    }
    @SuppressLint("HandlerLeak")
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Object result = msg.obj;
            switch (msg.what){
                case Constants.FAIL:
                    Toast.makeText(context, result.toString(), Toast.LENGTH_LONG).show();
                    break;
                case Constants.LOAD_APP_VERSIONS_SUCCESS:
                    List<String> list=new ArrayList<>();
                    for(AppVersion appVersion:appVersionList){
                        list.add("版本号："+appVersion.getVersionNo() +"\n"
                                +"版本大小："+appVersion.getVersionSize() +"MB\n"
                                +"版本简介："+appVersion.getVersionInfo()+"\n"
                                +"上传时间："+appVersion.getCreationDate());
                    }
                    ArrayAdapter<String> arrayAdapter=new ArrayAdapter<>(context,android.R.layout.simple_list_item_1,list);
                    listView.setAdapter(arrayAdapter);
                    break;
                case Constants.LOAD_APP_NEW_VERSION_SUCCESS:
                    versionNoEditText.setText(appVersion.getVersionNo());
                    versionInfoEditText.setText(appVersion.getVersionInfo());
                    apkFileNameTextView.setText(appVersion.getApkFileName());
                    break;
                case Constants.ADD_APP_VERSION_SUCCESS:
                    Toast.makeText(context, result.toString(), Toast.LENGTH_LONG).show();
                    break;
                case Constants.UPDATE_APP_VERSION_SUCCESS:
                    Toast.makeText(context, result.toString(), Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 获取指定文件大小(单位：字节)
     *
     * @param file 文件
     * @return 返回字节
     */
    public static long getFileSize(File file) throws Exception {
        if (file == null) {
            return 0;
        }
        long size = 0;
        if (file.exists()) {
            FileInputStream fis;
            fis = new FileInputStream(file);
            size = fis.available();
        }
        return size;
    }
    final public static int  REQUEST_CODE_SOME_FEATURES_PERMISSIONS=0;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_SOME_FEATURES_PERMISSIONS: {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.e("TTT","Permissions --> " + "Permission Granted: " + permissions[i]);
                    } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        Log.e("TTT","Permissions --> " + "Permission Denied: " + permissions[i]);
                    }
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }
}
