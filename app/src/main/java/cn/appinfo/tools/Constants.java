package cn.appinfo.tools;

/**
 * 常量类
 * 
 * @Author Joysing
 *
 */
public class Constants {
	/**
	 * 后台管理员用户类
	 */
	public final static int BACKEND_USER_TYPE = 1;
	/**
	 * 开发者用户类
	 */
	public final static int DEV_USER_TYPE = 2;
	/**
	 * 错误消息
	 */
	public final static String SYS_MESSAGE = "message";
	/**
	 * 页面大小
	 */
	public final static int PAGESIZE = 5;
	/**
	 * APK信息不完整！
	 */
	public final static String FILEUPLOAD_ERROR_1 = " * APK信息不完整！";
	/**
	 * 上传失败！
	 */
	public final static String FILEUPLOAD_ERROR_2 = " * 上传失败！";
	/**
	 * 上传文件格式不正确！
	 */
	public final static String FILEUPLOAD_ERROR_3 = " * 上传文件格式不正确！";
	/**
	 * 上传文件过大！
	 */
	public final static String FILEUPLOAD_ERROR_4 = " * 上传文件过大！";
	/**
	 * 用户类型
	 */
	public final static String USER_TYPE = "USER_TYPE";
	/**
	 * APP状态
	 */
	public final static String APP_STATUS = "APP_STATUS";
	/**
	 * 所属平台
	 */
	public final static String APP_FLATFORM = "APP_FLATFORM";
	/**
	 * 发布状态
	 */
	public final static String PUBLISH_STATUS = "PUBLISH_STATUS";
	/**
	 * 不显示任何icon
	 */
	public static final int ICON_TYPE_NOTHING = 0;
	/**
	 * 显示 Loading 图标
	 */
	public static final int ICON_TYPE_LOADING = 1;
	/**
	 * 显示成功图标
	 */
	public static final int ICON_TYPE_SUCCESS = 2;
	/**
	 * 显示失败图标
	 / */
	public static final int ICON_TYPE_FAIL = 3;
	/**
	 * 显示信息图标
	 */
	public static final int ICON_TYPE_INFO = 4;

	public static final int CONNECTION_TIMEOUT=10;//发送OkHttp请求时的超时时间 S
	public static final int READ_TIMEOUT=10;//读取OkHttp响应数据时长

	public static final String SERVER_IP="http://joysing.cc";
	public static final String SERVER_PORT="8081";
	public static final String SERVER_APP_NAME="appsys";
	public static final String SERVER_BASE_PATH=SERVER_IP+":"+SERVER_PORT+"/"+SERVER_APP_NAME;

	public static final String BACKEND_LOGIN_URL=SERVER_BASE_PATH+"/android/user/doLogin";
	public static final String DEV_LOGIN_URL=SERVER_BASE_PATH+"/android/dev/doLogin";
	public static final String GET_BACKEND_APPS_URL=SERVER_BASE_PATH+"/android/backend/appInfo/appList";
	public static final String GET_DEV_APPS_URL=SERVER_BASE_PATH+"/android/dev/appInfo/appList";
	public static final String GET_APP_DETAIL_URL=SERVER_BASE_PATH+"/android/dev/appInfo/appInfoView";
    public static final String AUDIT_APP_URL=SERVER_BASE_PATH+"/android/backend/appInfo/doCheck";
    public static final String SOLD_UP_APP_URL=SERVER_BASE_PATH+"/android/dev/appInfo/soldUp.json";
    public static final String SOLD_DOWN_APP_URL=SERVER_BASE_PATH+"/android/dev/appInfo/soldDown.json";
    public static final String DELETE_APP_URL=SERVER_BASE_PATH+"/android/dev/appInfo/delapp.json";
	public static final String GET_FLATFROM_URL=SERVER_BASE_PATH+"/android/dev/appInfo/flatform.json";
    public static final String GET_CATEGORY_URL=SERVER_BASE_PATH+"/android/dev/appInfo/category.json";
    public static final String ADD_APPINFO_URL=SERVER_BASE_PATH+"/android/dev/appInfo/doAdd";
    public static final String UPDATE_APPINFO_URL=SERVER_BASE_PATH+"/android/dev/appInfo/doUpdate";
	public static final String GET_APP_VERSIONS_URL=SERVER_BASE_PATH+"/android/dev/appInfo/appVersion";
    public static final String ADD_APP_VERSION_URL=SERVER_BASE_PATH+"/android/dev/appInfo/doVersion";
    public static final String GET_APP_VERSION_URL=SERVER_BASE_PATH+"/android/dev/appInfo/getVersion";
    public static final String UPDATE_APP_VERSION_URL=SERVER_BASE_PATH+"/android/dev/appInfo/doUpdateVersion";

    public static final int SUCCESS=1;
    public static final int FAIL=0;

	public static final int LOGIN_SUCCESS=1;
	public static final int LOGIN_FAIL=0;

	public static final int LOAD_APP_LIST_SUCCESS=1;
	public static final int LOAD_APP_LIST_FAIL=0;

	public static final int DETAIL=1;
	public static final int AUDIT=2;

    public static final int WAITING_AUDIT=1;//待审核
    public static final int PASS_STATUS=2;//审核通过
    public static final int UN_PASS_STATUS=3;//审核不通过
    public static final int SOLD_UP_STATUS=4;//已上架
    public static final int SOLD_DOWN_STATUS=5;//已下架

    public static final int LOAD_CATEGORY_ONE_LIST_SUCCESS=1;
	public static final int LOAD_CATEGORY_TWO_LIST_SUCCESS=2;
	public static final int LOAD_CATEGORY_THREE_LIST_SUCCESS=3;
	public static final int LOAD_FLATFORM_LIST_SUCCESS=4;
	public static final int ADD_APPINFO_SUCCESS=5;
	public static final int UPDATE_APPINFO_SUCCESS=6;

    public static final int AUDIT_SUCCESS=1;
    public static final int LOAD_APPINFO_DETAIL_SUCCESS=2;
    public static final int SOLD_UP_OR_DOWN_APP_SUCCESS=3;
    public static final int DELETE_APP_SUCCESS=4;
    public static final int LOAD_IMAGE_SUCCESS=5;

	public static final int LOAD_APP_VERSIONS_SUCCESS=1;
    public static final int ADD_APP_VERSION_SUCCESS=2;
    public static final int UPDATE_APP_VERSION_SUCCESS=3;
    public static final int LOAD_APP_NEW_VERSION_SUCCESS=4;

    public static final int UPDATE_APPINFO_CODE=1;
    public static final int LOAD_APPINFO_DETAIL_CODE=2;
}
