package cn.appinfo.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIPackageHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.popup.QMUIListPopup;
import com.qmuiteam.qmui.widget.popup.QMUIPopup;
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUICenterGravityRefreshOffsetCalculator;
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIPullRefreshLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.appinfo.R;
import cn.appinfo.activity.AddAppInfoActivity;
import cn.appinfo.activity.DetailActivity;
import cn.appinfo.activity.LoginActivity;
import cn.appinfo.entity.AppInfo;
import cn.appinfo.entity.BackendUser;
import cn.appinfo.entity.DevUser;
import cn.appinfo.entity.UserInfo;
import cn.appinfo.service.JumpActivityService;
import cn.appinfo.tools.Constants;
import cn.appinfo.tools.LoginUtil;
import cn.appinfo.tools.OkHttpUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ViewPagerAdapter extends PagerAdapter implements JumpActivityService{
    private Context context; //声明Context成员变量
    private View viewPage;
    private ListView listViewCheck;
    private EditText searchEditText;
    private int loginType;
    private UserInfo userInfo;
    private List<AppInfo> appInfoList;
    private ListViewAdapter listViewAdapter;
    private QMUIPullRefreshLayout qmuiPullRefreshLayout;
    public static String searchSoftwareName="";

    private Comparator<AppInfo> sortByNameComparator= (appInfo, t1) -> appInfo.getSoftwareName().compareTo(t1.getSoftwareName());
    private Comparator<AppInfo> sortBySizeComparator= (appInfo, t1) -> appInfo.getSoftwareSize().compareTo(t1.getSoftwareSize());
    private Comparator<AppInfo> sortByTimeComparator= (appInfo, t1) -> {
        if(appInfo.getCreationDate().getTime()>t1.getCreationDate().getTime()){
            return -1;
        }else if(appInfo.getCreationDate().getTime()<t1.getCreationDate().getTime()){
            return 1;
        }
        return 0;
    };
    private Comparator<AppInfo> defaultComparator=sortByTimeComparator;
    /**
     * 构造方法
     */
    public ViewPagerAdapter(Context context,UserInfo userInfo) {
        this.context = context;
        this.userInfo=userInfo;
    }

    /**
     * viewpager显示的页数
     */
    @Override
    public int getCount() {
        return 2;
    }

    /**
     * 比较View与object是否相等
     */
    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    /**
     * 初始化条目的内容
     * 条目的内容可以是单个控件，也可以是整个布局
     */
    @SuppressLint({"InflateParams", "SetTextI18n"})
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        switch (position) {
            case 0:
                loginType=userInfo.getUserType();
                viewPage=LayoutInflater.from(context).inflate(R.layout.viewpager_applist, null);
                searchEditText=viewPage.findViewById(R.id.searchEditText);
                searchEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
                    if (i == EditorInfo.IME_ACTION_SEARCH || i == EditorInfo.IME_ACTION_UNSPECIFIED) {
                        searchSoftwareName=textView.getText().toString().trim();
                        getAppList();
                        return true;
                    }
                    return false;
                });
                QMUITopBar mTopBar = viewPage.findViewById(R.id.topbar);
                mTopBar.setTitle("App列表");
                mTopBar.addRightImageButton(R.mipmap.search_btn_no, R.id.topbar_right_search_button).setOnClickListener((View view) -> {
                    if(searchEditText.getVisibility()==View.INVISIBLE){
                        searchEditText.setVisibility(View.VISIBLE);
                        searchEditText.setFocusable(true);
                        searchEditText.setFocusableInTouchMode(true);
                        searchEditText.requestFocus();
                        InputMethodManager inputManager = (InputMethodManager)searchEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.showSoftInput(searchEditText, 0);
                        mTopBar.setTitle("");
                        mTopBar.addLeftBackImageButton().setOnClickListener(view1 -> {
                            mTopBar.setTitle("App列表");
                            mTopBar.removeAllLeftViews();
                            searchEditText.setVisibility(View.INVISIBLE);
                            searchSoftwareName="";
                            getAppList();
                        });
                    }
                });
                if(loginType==Constants.DEV_USER_TYPE){
                    mTopBar.addRightImageButton(R.mipmap.topbar_right_add_button, R.id.topbar_right_add_button)
                    .setOnClickListener(v->{
                        Intent intent=new Intent(context, AddAppInfoActivity.class);
                        intent.putExtra("devId", ((DevUser)userInfo.getUser()).getId());
                        context.startActivity(intent);

                    });
                }
                mTopBar.addRightImageButton(R.mipmap.topbar_right_change_button, R.id.topbar_right_change_button)
                        .setOnClickListener(v -> {
                            QMUIListPopup qmuiListPopup;
                            String[] listItems = new String[]{
                                    "按名称排序",
                                    "按时间排序",
                                    "按大小排序",
                            };
                            List<String> data = new ArrayList<>();
                            Collections.addAll(data, listItems);
                            ArrayAdapter adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, data);//为listview创建适配器
                            qmuiListPopup = new QMUIListPopup(context, QMUIPopup.DIRECTION_NONE, adapter);//创建QMUIListPopup实例 第二个参数：显示位置，第三个位置绑定adapter
                            //创建一个listview第一个参数：设置宽度 2设置高度，3设置adapter的监听
                            qmuiListPopup.create(QMUIDisplayHelper.dp2px(context, 250), QMUIDisplayHelper.dp2px(context, 200), (adapterView, view22, i1, l) -> {
                                switch (i1){
                                    case 0://按名称排序
                                        defaultComparator=sortByNameComparator;
                                        break;
                                    case 1://按时间排序
                                        defaultComparator=sortByTimeComparator;
                                        break;
                                    case 2://按大小排序
                                        defaultComparator=sortBySizeComparator;
                                        break;
                                    default:
                                        break;
                                }
                                sort(defaultComparator);
                                qmuiListPopup.dismiss();

                            });
                            qmuiListPopup.setAnimStyle(QMUIPopup.ANIM_GROW_FROM_CENTER);//设置显示样式
                            qmuiListPopup.show(v);//设置在哪个控件上显示QMUIpopup
                        });
                listViewAdapter=new ListViewAdapter(context);
                listViewAdapter.setJumpActivityService(this);
                listViewCheck=viewPage.findViewById(R.id.listview_applist);
                qmuiPullRefreshLayout=viewPage.findViewById(R.id.pull_to_refresh);
                qmuiPullRefreshLayout.setRefreshOffsetCalculator(new QMUICenterGravityRefreshOffsetCalculator());//处于下拉区域中间效果
                qmuiPullRefreshLayout.setOnPullListener(new QMUIPullRefreshLayout.OnPullListener() {
                    @Override
                    public void onMoveTarget(int offset) {

                    }

                    @Override
                    public void onMoveRefreshView(int offset) {

                    }

                    @Override
                    public void onRefresh() {
                        getAppList();
                    }
                });
                getAppList();
                break;
            case 1:
                String usernameShow=loginType==Constants.BACKEND_USER_TYPE
                        ?((BackendUser)userInfo.getUser()).getUserName()
                        :((DevUser)userInfo.getUser()).getDevName();
                viewPage=LayoutInflater.from(context).inflate(R.layout.viewpager_mine, null);
                LayoutInflater.from(context).inflate(R.layout.main, null).setBackgroundResource(R.color.qmui_config_color_white);
                ((TextView)viewPage.findViewById(R.id.textView_usernameShow)).setText("欢迎您："+ usernameShow);
                ((TextView)viewPage.findViewById(R.id.app_version)).setText("软件版本：v"+QMUIPackageHelper.getAppVersion(context));
                Button buttonLogout=viewPage.findViewById(R.id.buttonLogout);
                buttonLogout.setOnClickListener((view)-> new QMUIDialog.MessageDialogBuilder(context)
                        .setMessage("退出当前账号?")
                        .addAction("取消", (dialog, index) -> dialog.dismiss())
                        .addAction("确定", (dialog, index) -> {
                            LoginUtil.removeLoginUserInfo(context, userInfo);
                            Intent intent = new Intent(context, LoginActivity.class);
                            context.startActivity(intent);
                            ((Activity) context).finish();
                            dialog.dismiss();
                        }).show());
                break;
            default:
                break;
        }
        container.addView(viewPage); //把生产好的条目，增加到ViewPager
        return viewPage;
    }

    /**
     * 处理器
     */
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.LOAD_APP_LIST_SUCCESS:
                    qmuiPullRefreshLayout.finishRefresh();
                    for (int i = 0; i < appInfoList.size(); i++) {
                        //加载图片
                        loadIcon(i);
                    }
                    sort(defaultComparator);
                    listViewAdapter.setLoginType(loginType);
                    listViewAdapter.setUserInfo(userInfo);
                    listViewCheck.setAdapter(listViewAdapter);
                    break;

                case Constants.LOAD_APP_LIST_FAIL:
                    //失败
                    Toast.makeText(context,msg.obj.toString(),Toast.LENGTH_LONG).show();
                    break;
            }

        }
    };
    /**
     * 获取App列表
     */
    private void getAppList(){
        String url=null;
        RequestBody requestBody=null;
        if(loginType==Constants.BACKEND_USER_TYPE){
            url=Constants.GET_BACKEND_APPS_URL;
            requestBody = new FormBody.Builder()
                    .add("querySoftwareName",searchSoftwareName)
                    .build();
        }else if(loginType==Constants.DEV_USER_TYPE){
            url=Constants.GET_DEV_APPS_URL;
            requestBody = new FormBody.Builder()
                    .add("devId", ((DevUser)userInfo.getUser()).getId()+"")
                    .add("querySoftwareName",searchSoftwareName)
                    .build();
        }
        OkHttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            Message message = new Message();

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                message.what = Constants.LOAD_APP_LIST_FAIL;
                message.obj = "网络异常,请检查网络连接.";
                handler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                //1.创建Gson对象
                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
                //2.使用fromJson()方法转换成集合对象
                appInfoList = gson.fromJson(json, new TypeToken<List<AppInfo>>() {}.getType());
                if (appInfoList==null){
                    message.what = Constants.LOAD_APP_LIST_FAIL;
                    message.obj = "获取列表失败，请联系管理员";
                }else {
                    message.what = Constants.LOAD_APP_LIST_SUCCESS;
                }
                handler.sendMessage(message);//发送消息
            }
        });
    }
    private void loadIcon(final int index) {
        final AppInfo appInfo = appInfoList.get(index);
        final String url = Constants.SERVER_IP + ":" + Constants.SERVER_PORT + appInfo.getLogoPicPath();
        final File dir = context.getFilesDir();
        File file = new File(dir, appInfo.getId() + ".png");
        if (file.exists()) {
            //本地加载
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
            appInfoList.get(index).setBitmap(bitmap);
        } else {
            //网络加载
            if(appInfo.getLogoPicPath()!=null) {
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
                        appInfoList.get(index).setBitmap(bitmap);//
                    }

                });
            }
        }


    }

    /**
     * 排序
     */
    private void sort(Comparator<AppInfo> comparator){
        Collections.sort(appInfoList,comparator);
        listViewAdapter.setAppInfoList(appInfoList);
        //通知adapter刷新数
        listViewAdapter.notifyDataSetChanged();
    }
    /**
     * 删除条目的内容
     */
    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        //super.destroyItem(container, position, object); //注释掉,否则，报错
        container.removeView((View) object); //删除ViewPager条目
    }


    @Override
    public void jump(AppInfo appInfo, int operation) {
        Intent intent=new Intent(context, DetailActivity.class);
        intent.putExtra("appInfo",appInfo);
        intent.putExtra("operation",operation);
        intent.putExtra("loginType",loginType);
        intent.putExtra("userInfo",userInfo);
        ((Activity)context).startActivityForResult(intent,Constants.LOAD_APPINFO_DETAIL_CODE);
    }
}