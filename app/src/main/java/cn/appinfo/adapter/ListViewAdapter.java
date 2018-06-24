package cn.appinfo.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.appinfo.R;
import cn.appinfo.entity.AppInfo;
import cn.appinfo.entity.UserInfo;
import cn.appinfo.service.JumpActivityService;
import cn.appinfo.tools.Constants;

public class ListViewAdapter extends BaseAdapter {
    private List<AppInfo> appInfoList;
    private LayoutInflater inflater;
    private UserInfo userInfo;
    private int loginType;
    private JumpActivityService jumpActivityService;

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public void setJumpActivityService(JumpActivityService jumpActivityService) {
        this.jumpActivityService = jumpActivityService;
    }

    ListViewAdapter(Context context){
        inflater= LayoutInflater.from(context);
    }
    public void setAppInfoList(List<AppInfo> appInfoList) {
        this.appInfoList = appInfoList;
    }

    public void setLoginType(int loginType) {
        this.loginType = loginType;
    }

    @Override
    public int getCount() {
        return appInfoList==null?0: appInfoList.size();
    }

    @Override
    public Object getItem(int i) {
        return appInfoList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(Constants.BACKEND_USER_TYPE==loginType){
            view=showManagerAppList(i,view);
        }else if(Constants.DEV_USER_TYPE==loginType){
            view=showDeveloperAppList(i,view);
        }
        return view;
    }
    @SuppressLint({"SetTextI18n", "InflateParams"})
    private View showManagerAppList(int i, View view) {
        ViewHolder viewHolder;
        if(view==null){
            viewHolder=new ViewHolder();
            view=inflater.inflate(R.layout.items_check,null);
            viewHolder.iconView=view.findViewById(R.id.soft_image);
            viewHolder.softNameView=view.findViewById(R.id.soft_name);
            viewHolder.versionView=view.findViewById(R.id.soft_version);
            viewHolder.statusView=view.findViewById(R.id.soft_status);
            viewHolder.buttonCheck=view.findViewById(R.id.button_check);
            view.setTag(viewHolder);
        }else{
            viewHolder= (ViewHolder) view.getTag();
        }
        AppInfo appInfo=appInfoList.get(i);
        if(appInfo.getBitmap()!=null){
            viewHolder.iconView.setImageBitmap(appInfo.getBitmap());
        }else{
            viewHolder.iconView.setImageResource(R.mipmap.no_picture);
        }
        viewHolder.softNameView.setText(appInfo.getSoftwareName());
        viewHolder.versionView.setText("最新版本号："+(appInfo.getVersionNo()==null?"无":appInfo.getVersionNo()));
        viewHolder.statusView.setText("状态:"+appInfo.getStatusName());
        viewHolder.buttonCheck.setText(appInfo.getStatusName());
        viewHolder.buttonCheck.setOnClickListener((view1) -> jumpActivityService.jump(appInfo,Constants.AUDIT));
        view.setOnClickListener((view2)-> jumpActivityService.jump(appInfo,Constants.AUDIT));

        return view;
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    private View showDeveloperAppList(int i, View view) {
        ViewHolder viewHolder;
        if(view==null){
            viewHolder=new ViewHolder();
            view=inflater.inflate(R.layout.items_app,null);
            viewHolder.iconView=view.findViewById(R.id.soft_image);
            viewHolder.softNameView=view.findViewById(R.id.soft_name);
            viewHolder.versionView=view.findViewById(R.id.soft_version);
            viewHolder.statusView=view.findViewById(R.id.soft_status);
            viewHolder.buttonCheck=view.findViewById(R.id.button_check);
            view.setTag(viewHolder);
        }else{
            viewHolder= (ViewHolder) view.getTag();
        }
        AppInfo appInfo=appInfoList.get(i);
        if(appInfo.getBitmap()!=null){
            viewHolder.iconView.setImageBitmap(appInfo.getBitmap());
        }else{
            viewHolder.iconView.setImageResource(R.mipmap.no_picture);
        }
        viewHolder.softNameView.setText(appInfo.getSoftwareName());
        viewHolder.versionView.setText("最新版本号："+(appInfo.getVersionNo()==null?"无":appInfo.getVersionNo()));
        viewHolder.statusView.setText("状态:"+appInfo.getStatusName());
        viewHolder.buttonCheck.setOnClickListener((view1) -> jumpActivityService.jump(appInfo,Constants.DETAIL));
        view.setOnClickListener((view2)-> jumpActivityService.jump(appInfo,Constants.DETAIL));
        return view;
    }
    static class ViewHolder{
        ImageView iconView;
        TextView softNameView;
        TextView versionView;
        TextView statusView;
        Button buttonCheck;
    }
}
