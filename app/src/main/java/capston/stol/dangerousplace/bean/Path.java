package capston.stol.dangerousplace.bean;

import com.nhn.android.maps.maplib.NGeoPoint;

import java.util.ArrayList;

/**
 * Created by sjlee on 2016-07-03.
 */
public class Path {

    private int PathIdx;
    private ArrayList<NGeoPoint> Points = new ArrayList<>();

    public int getPathIdx() {
        return PathIdx;
    }
    public void setPathIdx(int i){
        PathIdx = i;
    }

    public ArrayList<NGeoPoint> getPoints() {
        return Points;
    }

    public void setPoints(ArrayList<NGeoPoint> points) {
        Points = points;
    }
}
