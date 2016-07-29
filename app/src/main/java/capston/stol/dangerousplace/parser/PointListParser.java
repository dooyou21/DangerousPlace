package capston.stol.dangerousplace.parser;

import com.nhn.android.maps.maplib.NGeoPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import capston.stol.dangerousplace.bean.Point;

/**
 * Created by sjlee on 2016-07-22.
 * 원래는 x, y인데 서버랑 퉁신하면서 데이터 순서가 꼬여서....
 *
 * 원래대로라면
 * 좌표 받아올때
 * longitude = xcoord, latitude = ycoord여야 하지만 순서가 바뀌어있다.
 */
public class PointListParser {

    public ArrayList<Point> mResult;
    public String mString;

    public PointListParser(String pString) {
        mString = pString;
    }

    public ArrayList<Point> parse() {
        mResult = new ArrayList<>();

        JSONObject base = null;
        try {
            base = new JSONObject(mString);

            JSONArray List = base.getJSONArray("PointList");

            for(int i=0;i<List.length();i++){
                Point point = new Point();

                JSONObject jpoint = List.getJSONObject(i);

                point.setPointTitle(jpoint.getString("pointTitle"));
                point.setPoint(new NGeoPoint(jpoint.getDouble("ycoord"), jpoint.getDouble("xcoord")));
                point.setIdx(jpoint.getInt("idx"));

                mResult.add(point);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mResult;

    }


}
