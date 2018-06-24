package cn.appinfo.tools;

import android.content.Context;

import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

public class QMUITipDialogUtil {
    /**
     * 显示提示信息
     * @param tipIconType 提示图标类型
     * @param context 上下文
     * @param info 提示的信息内容
     */
    public static QMUITipDialog showTipDialog(int tipIconType , Context context, String info){
        QMUITipDialog tipDialog;

        switch (tipIconType) {
            case 0:
                tipDialog = new QMUITipDialog.Builder(context)//创建tipdialog实例为其设置显示样式
                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_NOTHING)//设置有加载框模式的dialog
                        .setTipWord(info)
                        .create();
                break;
            case 1:
                tipDialog = new QMUITipDialog.Builder(context)
                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                        .setTipWord(info)
                        .create();
                break;
            case 2:
                tipDialog = new QMUITipDialog.Builder(context)
                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                        .setTipWord(info)
                        .create();
                break;
            case 3:
                tipDialog = new QMUITipDialog.Builder(context)
                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                        .setTipWord(info)
                        .create();
                break;
            case 4:
                tipDialog = new QMUITipDialog.Builder(context)
                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_INFO)
                        .setTipWord(info)
                        .create();
                break;
            default:
                tipDialog = new QMUITipDialog.Builder(context)
                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                        .setTipWord(info)
                        .create();
        }
        return tipDialog;
    }
}
