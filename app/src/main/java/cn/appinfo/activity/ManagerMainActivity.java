package cn.appinfo.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.widget.QMUITabSegment;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.appinfo.R;
import cn.appinfo.adapter.ViewPagerAdapter;
import cn.appinfo.entity.UserInfo;
import cn.appinfo.tools.Constants;


public class ManagerMainActivity extends Activity {
    @BindView( R.id.tabSegment)
    QMUITabSegment mTabSegment;
    @BindView( R.id.contentViewPager)
    ViewPager mContentViewPager;
    private UserInfo userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.main);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        userInfo = (UserInfo) intent.getSerializableExtra("userInfo");
        ViewGroup.LayoutParams params = mContentViewPager.getLayoutParams();
        params.height =QMUIDisplayHelper.getScreenHeight(this)
                -QMUIDisplayHelper.getStatusBarHeight(this)
                -QMUIDisplayHelper.getActionBarHeight(this)
                -mTabSegment.getHeight();
        mContentViewPager.setLayoutParams(params);
        initTabSegment();
    }

    private void initTabSegment() {
        int normalColor = QMUIResHelper.getAttrColor(this,  R.attr.qmui_config_color_gray_6);
        int selectColor = QMUIResHelper.getAttrColor(this,  R.attr.qmui_config_color_blue);
        mTabSegment.setDefaultNormalColor(normalColor);    //设置tab正常下的颜色
        mTabSegment.setDefaultSelectedColor(selectColor);    //设置tab选中下的颜色
        QMUITabSegment.Tab checkTab = new QMUITabSegment.Tab(
                ContextCompat.getDrawable(this,  R.mipmap.view_app_list_no),//未选中图片
                ContextCompat.getDrawable(this,  R.mipmap.view_app_list_pr),//选中图片
                "审核", true
        );
        QMUITabSegment.Tab mineTab = new QMUITabSegment.Tab(
                ContextCompat.getDrawable(this,  R.mipmap.view_user_no),//未选中图片
                ContextCompat.getDrawable(this,  R.mipmap.view_user_pr),//选中图片
                "我的", true
        );
        mContentViewPager.setAdapter(new ViewPagerAdapter(this, userInfo));
        mTabSegment.addTab(checkTab).addTab(mineTab);
        int space = QMUIDisplayHelper.dp2px(this, 16);
        mTabSegment.setHasIndicator(true);
        mTabSegment.setMode(QMUITabSegment.MODE_FIXED);  //MODE_SCROLLABLE 自适应宽度+滚动   MODE_FIXED  均分
        mTabSegment.setItemSpaceInScrollMode(space);
        mTabSegment.setupWithViewPager(mContentViewPager, false);
        mTabSegment.setPadding(space, 0, space, 0);

    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case Constants.LOAD_APPINFO_DETAIL_CODE:
                mContentViewPager.setAdapter(new ViewPagerAdapter(this, userInfo));
                break;
        }
    }
}
