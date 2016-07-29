package capston.stol.dangerousplace.bean;

/**
 * Created by 이수정 on 2015-12-29.
 *
 * Add commments by leesj on 2016-07-23
 * xcoord, ycoord -> NGeoPoint로 바꾸는게 나을 듯 싶다...? 아닌가?
 */
public class WarningInfo {

    private int Idx;
    private int UsrIdx;
    private int WCount;
    private int Category;
    private double XCoord;
    private double YCoord;
    private String Title;
    private String Content;
    private String Datetime;
    private String RegistDateTime;
    private boolean Usage;
    private boolean CheckWCount;

    public boolean isUsage() {
        return Usage;
    }

    public boolean isCheckWCount() {
        return CheckWCount;
    }

    public void setCheckWCount(boolean checkWCount) {
        CheckWCount = checkWCount;
    }

    public boolean getUsage() { return Usage; }

    public void setUsage(boolean usage) { Usage = usage; }

    public String getRegistDateTime() { return RegistDateTime; }

    public void setRegistDateTime(String registDateTime) { RegistDateTime = registDateTime; }

    public String getDatetime() {
        return Datetime;
    }

    public void setDatetime(String datetime) {
        Datetime = datetime;
    }

    public int getUsrIdx() {
        return UsrIdx;
    }

    public void setUsrIdx(int usrIdx) {
        UsrIdx = usrIdx;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String context) {
        Content = context;
    }


    public int getIdx() { return Idx; }
    public int getWCount() { return WCount; }
    public double getXCoord() { return XCoord; }
    public double getYCoord() { return YCoord; }
    public String getTitle() { return Title; }
    public int getCategory() { return Category;}

    public void setIdx(int idx) { Idx = idx; }
    public void setWCount(int wcount) { WCount = wcount; }
    public void setXCoord(double xcoord) { XCoord = xcoord; }
    public void setYCoord(double ycoord) { YCoord = ycoord; }
    public void setTitle(String title) { Title = title; }
    public void setCategory(int category) { Category = category; }
}
