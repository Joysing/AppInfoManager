
package cn.appinfo.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.qmuiteam.qmui.widget.QMUITopBar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.appinfo.R;
import cn.appinfo.entity.AppCategory;
import cn.appinfo.entity.AppInfo;
import cn.appinfo.entity.FlatForm;
import cn.appinfo.tools.Constants;
import cn.appinfo.tools.FileUtil;
import cn.appinfo.tools.OkHttpUtil;
import cn.appinfo.tools.ResultUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddAppInfoActivity extends AppCompatActivity {
    Context context=this;
    @BindView(R.id.topbar)
    QMUITopBar qmuiTopBar;
    @BindView(R.id.flatform_spinner)
    Spinner flatformSpinner;
    @BindView(R.id.categoryOne)
    Spinner categoryOneSpinner;
    @BindView(R.id.categoryTow)
    Spinner categoryTowSpinner;
    @BindView(R.id.categoryThree)
    Spinner categoryThreeSpinner;
    @BindView(R.id.softName_et)
    EditText softNameEditText;
    @BindView(R.id.apkName_et)
    EditText apkNameEditText;
    @BindView(R.id.supportROM_et)
    EditText supportRomEditText;
    @BindView(R.id.interfaceLanguage_et)
    EditText interfaceLanguageEditText;
    @BindView(R.id.softwareSize_et)
    EditText softwareSizeEditText;
    @BindView(R.id.downloads_et)
    EditText downloadsEditText;
    @BindView(R.id.appInfomation_et)
    EditText appInfomationEditText;
    @BindView(R.id.imageButton)
    ImageButton imageButton;
    @BindView(R.id.addAppInfo_button)
    Button addAppInfoButton;

    private List<FlatForm> flatFormList=null;
    private List<AppCategory> categoryList;
    private Map<String,String> flatFormMap=new HashMap<>();
    private Map<String,String> categoryMap=new HashMap<>();

    private AppInfo appInfo;
    private Integer devId;
    private String flatFormId;
    private String categoryOneId;
    private String categoryTwoId;
    private String categoryThreeId;
    private String logoPicPath;
    private String logoPicFileName;
    private static final int FILE_SELECT_CODE = 1;
    String TAG="appinfo";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_app_info);
        ButterKnife.bind(this);
        qmuiTopBar.addLeftBackImageButton().setOnClickListener(v -> finish());
        qmuiTopBar.setTitle("添加App信息");
        Intent intent=getIntent();
        devId=intent.getIntExtra("devId",0);
        appInfo= (AppInfo) intent.getSerializableExtra("appInfo");
        getFlatform();
        getCategories("",1);

        if(appInfo!=null){
            updateAppInfo();
        }
    }

    /**
     * 新增APP基础信息
     */
    @OnClick(R.id.addAppInfo_button)
    public void addAppInfo(){
//        String softwareName="安卓测试添加";
//        String apkName="cc.joysing.test";
//        String supportROM="Android4.0+";
//        String interfaceLanguage="中文";
//        String softwareSize="0.48";
//        String downloads="1000";
//        String appInfomation="简介";
//        String logoPicPath="/storage/emulated/0/DCIM/sontancomputer.jpg";
        String softwareName=softNameEditText.getText().toString();
        String apkName=apkNameEditText.getText().toString();
        String supportROM=supportRomEditText.getText().toString();
        String interfaceLanguage=interfaceLanguageEditText.getText().toString();
        String softwareSize=softwareSizeEditText.getText().toString();
        String downloads=downloadsEditText.getText().toString();
        String appInfomation=appInfomationEditText.getText().toString();
        if(softwareName.equals("")){
            Toast.makeText(context,"软件名称不能为空",Toast.LENGTH_SHORT).show();
            return;
        }else if(apkName.equals("")){
            Toast.makeText(context,"apk文件名不能为空",Toast.LENGTH_SHORT).show();
            return;
        }else if(supportROM.equals("")){
            Toast.makeText(context,"支持的Rom不能为空",Toast.LENGTH_SHORT).show();
            return;
        }else if(interfaceLanguage.equals("")){
            Toast.makeText(context,"界面语言不能为空",Toast.LENGTH_SHORT).show();
            return;
        }else if(softwareSize.equals("")){
            Toast.makeText(context,"软件大小不能为空",Toast.LENGTH_SHORT).show();
            return;
        }else if(downloads.equals("")){
            Toast.makeText(context,"下载次数不能为空",Toast.LENGTH_SHORT).show();
            return;
        }else if(appInfomation.equals("")){
            Toast.makeText(context,"软件简介不能为空",Toast.LENGTH_SHORT).show();
            return;
        }else if(logoPicPath.equals("")){
            Toast.makeText(context,"请选择软件图标",Toast.LENGTH_SHORT).show();
            return;
        }
        Message message = new Message();
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), new File(logoPicPath));
        RequestBody multipartBody=new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("softwareName", softwareName)
                .addFormDataPart("apkName", apkName)
                .addFormDataPart("supportROM", supportROM)
                .addFormDataPart("interfaceLanguage", interfaceLanguage)
                .addFormDataPart("softwareSize", softwareSize)
                .addFormDataPart("downloads", downloads)
                .addFormDataPart("appInfomation", appInfomation)
                .addFormDataPart("flatFormId",flatFormId)
                .addFormDataPart("categoryOneId",categoryOneId)
                .addFormDataPart("categoryTwoId",categoryTwoId)
                .addFormDataPart("categoryThreeId",categoryThreeId)
                .addFormDataPart("devId",devId+"")
                .addFormDataPart("_logoPicPath",logoPicFileName,fileBody)
                .build();
        OkHttpUtil.sendOkHttpRequest(Constants.ADD_APPINFO_URL, multipartBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                message.what = Constants.FAIL;
                message.obj = "网络超时,请查看网络......";
                handler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                Log.i(TAG, "onResponse: "+json);
                //把json转换成Java对象
                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
                ResultUtil resultUtil = gson.fromJson(json, ResultUtil.class);
                Message msg=new Message();
                if (resultUtil.isResult()){
                    msg.what=Constants.ADD_APPINFO_SUCCESS;
                    message.obj = "添加App成功！";
                    finish();
                }else {
                    msg.what=Constants.FAIL;
                    message.obj = "添加App失败！"+resultUtil.getMessage();
                }
                handler.sendMessage(message);
            }
        });
    }

    /**
     * 修改APP基础信息
     */
    public void updateAppInfo(){
        qmuiTopBar.setTitle("修改 ["+appInfo.getSoftwareName()+"]");
        softNameEditText.setText(appInfo.getSoftwareName());
        apkNameEditText.setText(appInfo.getApkName());
        supportRomEditText.setText(appInfo.getSupportROM());
        interfaceLanguageEditText.setText(appInfo.getInterfaceLanguage());
        softwareSizeEditText.setText(appInfo.getSoftwareSize()+"");
        downloadsEditText.setText(appInfo.getDownloads()+"");
        appInfomationEditText.setText(appInfo.getAppInfo());

        //本地加载图片
        File file = new File(getFilesDir(), appInfo.getId() + ".png");
        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
        if(bitmap!=null){
            imageButton.setImageBitmap(bitmap);
        }else{
            imageButton.setImageResource(R.mipmap.no_picture);
        }
        addAppInfoButton.setText("提交修改");
        addAppInfoButton.setOnClickListener(view -> {
            String softwareName=softNameEditText.getText().toString();
            String apkName=apkNameEditText.getText().toString();
            String supportROM=supportRomEditText.getText().toString();
            String interfaceLanguage=interfaceLanguageEditText.getText().toString();
            String softwareSize=softwareSizeEditText.getText().toString();
            String downloads=downloadsEditText.getText().toString();
            String appInfomation=appInfomationEditText.getText().toString();

            if(softwareName.equals("")){
                Toast.makeText(context,"软件名称不能为空",Toast.LENGTH_SHORT).show();
                return;
            }else if(apkName.equals("")){
                Toast.makeText(context,"apk文件名不能为空",Toast.LENGTH_SHORT).show();
                return;
            }else if(supportROM.equals("")){
                Toast.makeText(context,"支持的Rom不能为空",Toast.LENGTH_SHORT).show();
                return;
            }else if(interfaceLanguage.equals("")){
                Toast.makeText(context,"界面语言不能为空",Toast.LENGTH_SHORT).show();
                return;
            }else if(softwareSize.equals("")){
                Toast.makeText(context,"软件大小不能为空",Toast.LENGTH_SHORT).show();
                return;
            }else if(downloads.equals("")){
                Toast.makeText(context,"下载次数不能为空",Toast.LENGTH_SHORT).show();
                return;
            }else if(appInfomation.equals("")){
                Toast.makeText(context,"软件简介不能为空",Toast.LENGTH_SHORT).show();
                return;
            }
            RequestBody fileBody=new FormBody.Builder().build();
            if(!"".equals(logoPicPath)){
                File cacheImage = new File(context.getFilesDir(), appInfo.getId() + ".png");
                cacheImage.delete();
                fileBody = RequestBody.create(MediaType.parse("image/*"), new File(logoPicPath));
            }
            Message message = new Message();
            RequestBody multipartBody=new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("appInfoId", appInfo.getId()+"")
                    .addFormDataPart("softwareName", softwareName)
                    .addFormDataPart("apkName", apkName)
                    .addFormDataPart("supportROM", supportROM)
                    .addFormDataPart("interfaceLanguage", interfaceLanguage)
                    .addFormDataPart("softwareSize", softwareSize)
                    .addFormDataPart("downloads", downloads)
                    .addFormDataPart("appInfomation", appInfomation)
                    .addFormDataPart("flatFormId",flatFormId)
                    .addFormDataPart("categoryOneId",categoryOneId)
                    .addFormDataPart("categoryTwoId",categoryTwoId)
                    .addFormDataPart("categoryThreeId",categoryThreeId)
                    .addFormDataPart("devId",devId+"")
                    .addFormDataPart("_logoPicPath",logoPicFileName,fileBody)
                    .build();
            OkHttpUtil.sendOkHttpRequest(Constants.UPDATE_APPINFO_URL, multipartBody, new Callback() {
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
                        msg.what=Constants.ADD_APPINFO_SUCCESS;
                        message.obj = "App基础信息修改成功！";
                        finish();
                    }else {
                        msg.what=Constants.FAIL;
                        message.obj = "App基础信息修改失败！"+resultUtil.getMessage();
                    }
                    handler.sendMessage(message);
                }
            });
        });

    }

    /**
     * 获取所属平台列表
     */
    public void getFlatform(){
        Message message = new Message();
        OkHttpUtil.sendOkHttpRequest(Constants.GET_FLATFROM_URL, null, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                message.what = Constants.FAIL;
                message.obj = "网络超时,请查看网络......";
                handler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                message.what = Constants.SUCCESS;
                String json = response.body().string();
                //把json转换成Java对象
                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
                flatFormList = gson.fromJson(json,new TypeToken<List<FlatForm>>() {}.getType());
                if (flatFormList==null){
                    message.what = Constants.FAIL;
                    message.obj = "获取所属平台列表失败。";
                }else {
                    message.what = Constants.LOAD_FLATFORM_LIST_SUCCESS;
                    message.obj = flatFormList;
                }
                handler.sendMessage(message);
            }
        });
    }

    /**
     * 获取分类列表
     * @param parentId 上级分类id
     * @param level 需要获取第几级分类
     */
    public void getCategories(String parentId,int level){
        Message message = new Message();
        RequestBody requestBody = new FormBody.Builder()
                .add("parentId", parentId).build();
        OkHttpUtil.sendOkHttpRequest(Constants.GET_CATEGORY_URL, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                message.what = Constants.FAIL;
                message.obj = "网络超时,请查看网络......";
                handler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                message.what = Constants.SUCCESS;
                String json = response.body().string();
                //把json转换成Java对象
                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
                categoryList = gson.fromJson(json,new TypeToken<List<AppCategory>>() {}.getType());
                if (categoryList==null){
                    message.what = Constants.FAIL;
                    message.obj = "获取分类列表失败。";
                }else {
                    switch (level){
                        case 1:
                            message.what = Constants.LOAD_CATEGORY_ONE_LIST_SUCCESS;
                            break;
                        case 2:
                            message.what = Constants.LOAD_CATEGORY_TWO_LIST_SUCCESS;
                            break;
                        case 3:
                            message.what = Constants.LOAD_CATEGORY_THREE_LIST_SUCCESS;
                            break;
                        default:break;
                    }
                    message.obj = categoryList;
                }
                handler.sendMessage(message);
            }
        });
    }

    /**
     * 从本地选择APP图标
     */
    @OnClick(R.id.imageButton)
    public void selectPicture(){
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
        intent.setType("image/*");
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
                logoPicPath = uri.getPath();
            }else{
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                    logoPicPath = FileUtil.getPath(context, uri);
                } else {//4.4以下下系统调用方法
                    logoPicPath = FileUtil.getRealPathFromURI(context,uri);
                }
            }
            logoPicFileName=new File(logoPicPath).getName();
            //本地加载
            Bitmap bitmap = BitmapFactory.decodeFile(logoPicPath);
            if(bitmap!=null){
                imageButton.setImageBitmap(bitmap);
            }
            Toast.makeText(context,"已选择图片："+logoPicFileName,Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("HandlerLeak")
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Object result = msg.obj;
            List<String> stringList = new ArrayList<>();
            switch (msg.what){
                case Constants.FAIL:
                    Toast.makeText(context, result.toString(), Toast.LENGTH_LONG).show();
                    break;
                case Constants.LOAD_FLATFORM_LIST_SUCCESS:
                    for(FlatForm flatForm:flatFormList){
                        stringList.add(flatForm.getValueName());
                        flatFormMap.put(flatForm.getValueId(),flatForm.getValueName());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, stringList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);//为适配器添加样式
                    flatformSpinner.setAdapter(adapter);
                    flatformSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        // parent： 为控件Spinner view：显示文字的TextView position：下拉选项的位置从0开始
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            flatFormId=getKey(flatFormMap,flatformSpinner.getSelectedItem().toString());
                        }
                        //没有选中时的处理
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });

                    break;
                case Constants.LOAD_CATEGORY_ONE_LIST_SUCCESS:
                    for(AppCategory appCategory:categoryList){
                        stringList.add(appCategory.getCategoryName());
                        categoryMap.put(appCategory.getId().toString(),appCategory.getCategoryName());
                    }
                    adapter =new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, stringList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);//为适配器添加样式
                    categoryOneSpinner.setAdapter(adapter);
                    categoryOneSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        // parent： 为控件Spinner view：显示文字的TextView position：下拉选项的位置从0开始
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            categoryOneId=getKey(categoryMap,categoryOneSpinner.getSelectedItem().toString());
                            getCategories(categoryOneId,2);
                        }
                        //没有选中时的处理
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                    break;
                case Constants.LOAD_CATEGORY_TWO_LIST_SUCCESS:
                    for(AppCategory appCategory:categoryList){
                        stringList.add(appCategory.getCategoryName());
                        categoryMap.put(appCategory.getId().toString(),appCategory.getCategoryName());
                    }
                    adapter =new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, stringList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);//为适配器添加样式
                    categoryTowSpinner.setAdapter(adapter);
                    categoryTowSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        // parent： 为控件Spinner view：显示文字的TextView position：下拉选项的位置从0开始
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            categoryTwoId=getKey(categoryMap,categoryTowSpinner.getSelectedItem().toString());
                            getCategories(categoryTwoId,3);
                        }
                        //没有选中时的处理
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                    break;
                case Constants.LOAD_CATEGORY_THREE_LIST_SUCCESS:
                    for(AppCategory appCategory:categoryList){
                        stringList.add(appCategory.getCategoryName());
                        categoryMap.put(appCategory.getId().toString(),appCategory.getCategoryName());
                    }
                    adapter =new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, stringList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);//为适配器添加样式
                    categoryThreeSpinner.setAdapter(adapter);
                    categoryThreeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        // parent： 为控件Spinner view：显示文字的TextView position：下拉选项的位置从0开始
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            categoryThreeId=getKey(categoryMap,categoryThreeSpinner.getSelectedItem().toString());
                        }
                        //没有选中时的处理
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                    break;
                case Constants.ADD_APPINFO_SUCCESS:
                case Constants.UPDATE_APPINFO_SUCCESS:
                    Toast.makeText(context, result.toString(), Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };
    /**
     * 根据map的value获取map的key
     */
    private static String getKey(Map<String,String> map,String value){
        String key="";
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if(value.equals(entry.getValue())){
                key=entry.getKey();
            }
        }
        return key;
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
