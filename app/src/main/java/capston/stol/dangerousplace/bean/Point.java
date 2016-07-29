package capston.stol.dangerousplace.bean;

import com.nhn.android.maps.maplib.NGeoPoint;

/**
 * Created by sjlee on 2016-07-22.
 * 알림받을 위치 표시할 때 쓰이는 Point
 */
public class Point {

    private int Idx;//point index! not user index
    private String PointTitle;
    private NGeoPoint Point = new NGeoPoint();

    public int getIdx() {
        return Idx;
    }

    public void setIdx(int idx) {
        Idx = idx;
    }

    public String getPointTitle() {
        return PointTitle;
    }

    public void setPointTitle(String pointTitle) {
        PointTitle = pointTitle;
    }

    public NGeoPoint getPoint() {
        return Point;
    }

    public void setPoint(NGeoPoint nGeoPoint) {
        Point.set(nGeoPoint.getLongitude(), nGeoPoint.getLatitude());
    }
}
