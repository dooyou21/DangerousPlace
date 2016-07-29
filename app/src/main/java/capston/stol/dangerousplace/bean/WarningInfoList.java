package capston.stol.dangerousplace.bean;

import java.util.ArrayList;

/**
 * Created by 이수정 on 2016-01-04.
 */
public class WarningInfoList {
    private int total;
    private ArrayList<WarningInfo> warningInfoArrayList = null;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public ArrayList<WarningInfo> getWarningInfoArrayList() {
        return warningInfoArrayList;
    }

    public void setWarningInfoArrayList(ArrayList<WarningInfo> warningInfoArrayList) {
        this.warningInfoArrayList = warningInfoArrayList;
    }
}
