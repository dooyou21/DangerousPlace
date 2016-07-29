package capston.stol.dangerousplace.parser;

import com.nhn.android.maps.maplib.NGeoPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import capston.stol.dangerousplace.bean.Path;

/**
 * Created by sjlee on 2016-07-03.
 */
public class PathListParser {

    public ArrayList<Path> mResult;
    public String mString;

    public PathListParser(String pString) {
        mString = pString;
    }

    public ArrayList<Path> parse() {

        mResult = new ArrayList<>();

        JSONObject base = null;
        try {
            base = new JSONObject(mString);
            JSONArray List = base.getJSONArray("List");

            Path path = null;
            String point[];
            String coord[];
            ArrayList<NGeoPoint> points;

            for(int i=0;i<List.length();i++){

                path = new Path();

                JSONObject jpath = List.getJSONObject(i);
                path.setPathIdx(Integer.parseInt(jpath.getString("idx")));

                point = jpath.getString("route").split(" ");

                points = new ArrayList<>();

                for(int j=1;j<point.length;j++){
                    coord = point[j].split(",");
                    points.add(new NGeoPoint(Double.parseDouble(coord[1]), Double.parseDouble(coord[0])));

                }
                path.setPoints(points);

//                Log.i(i+"th point: ", path.getPoints().toString());
                mResult.add(path);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mResult;
    }
}
