package capston.stol.dangerousplace.util;

/**
 * Created by 이수정 on 2015-12-30.
 */
public class Constant {
    public static final class KEY {
//        public static final String NMAPAPIKEY = "c52bd0ce704151a593aedfe77d32946d";
        public static final String NMAPCLIENTID = "qriFxY4YpvOj15zMVM30";
    }

    public static final class URL {
        //BASE
        public static final String Base = "http://114.70.235.39:9999/DangerousPlace/";
        //DETAIL
        public static final String GoogleSignIn = "user/regist?";
        public static final String WarningInfoView = "warning/view?";
        public static final String WarningInfoRegist = "warning/regist?";
        public static final String WarningInfoDetail = "warning/detail?";
        public static final String WarningInfoDelete = "warning/delete?";
        public static final String WarningInfoUpdate = "warning/modify?";
        public static final String SuSangHaeUpdate = "susang/update?";
        public static final String WarningInfoList = "warning/list?";

        //PATH
        public static final String SavePath = "user/registroute?";
        public static final String GetPath = "user/selectroute?";
        public static final String DetelePath = "user/deleteroute?";

        //EMERGENCY
        public static final String EmergencyView = "emg/view?";
        public static final String EmergencyUpdate = "emg/update?";
        public static final String EmergencyRequestData = "emg/request?";

        //for PUSH Alert
        public static final String SetPushOn = "push_on?";
        public static final String SetPushOff = "push_off?";

        //MYPAGE - MyWaringInfo
        public static final String MyWarningInfo = "mypage/mywinfo?";

        //MyPoint
        public static final String GetPointList = "user/viewpoint?";
        public static final String InsertPoint = "user/registpoint?";
        public static final String DeletePoint = "user/deletepoint?";

        //UserDelete
        public static final String UserDropOut = "user/delete?";

    }

    public static final class PARAMETER {

        //login
        public static final String Email = "email";

        //WarningInfoInsert
        public static final String Lng = "xcoord";
        public static final String Lat = "ycoord";
        public static final String Content = "contents";
        public static final String Title = "title";
        public static final String Datetime = "date";
        public static final String UserIdx = "idx_users";
        public static final String Category = "category";

        //WarningInfoDetail
        public static final String InfoIdx = "info_idx";
        public static final String MyIdx = "my_idx";

        //WarningCountAddMinus
        public static final String WInfoIdx = "w_info_idx";
        public static final String WMyIdx = "w_my_idx";

        //Path save&get
        public static final String Path = "route";
        public static final String PathIdx = "idx";

        //EmergencyData
        public static final String EmgNo = "emg";
        public static final String EmgName = "emg_name";
        public static final String EmgPhone = "emg_num";
        public static final String EmgContent = "emg_content";

        //MyPoint
        public static final String PointIdx = "point_idx";
        public static final String PointTitle = "point_title";

        //instance id
        public static final String InstanceId = "instance_id";


    }

}
