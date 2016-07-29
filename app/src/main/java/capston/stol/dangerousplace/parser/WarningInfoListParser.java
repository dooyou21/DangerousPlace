package capston.stol.dangerousplace.parser;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import capston.stol.dangerousplace.bean.WarningInfo;
import capston.stol.dangerousplace.bean.WarningInfoList;

/**
 * Created by 박세빈 on 2016-04-25.
 *
 * Add comment by leesj on 2016-07-23
 *
 * 원래는 x, y인데 서버랑 데이터 순서가 꼬여서....
 *
 * 원래대로라면
 * 좌표 받아올때
 * longitude = xcoord, latitude = ycoord여야 하지만 순서가 바뀌어있다
 * 때문에 XCoord에 lat값이. YCoord에 lng값이 들어간다.
 *
 * 언젠가는 다갈아엎어버리리라....
 */
public class WarningInfoListParser {
    public WarningInfoList mResult;
    public String mString;

    private ArrayList<WarningInfo> wilist;

    public WarningInfoListParser(String pString) {
        mString = pString;
    }

    public WarningInfoList parse() {

        try {
            //Total and List
            JSONObject base = new JSONObject(mString);
//            Log.d("totalstring", mString+"");

            JSONArray List = base.getJSONArray("List");
//            Log.d("listLength", List.length()+"");

            wilist = new ArrayList<WarningInfo>();

            WarningInfo winfo;

            for (int i = 0; i < List.length(); i++) {//WInfo갯수만큼
                winfo = new WarningInfo();
                JSONObject Marker = List.getJSONObject(i);

//                Log.d("json", Marker.getDouble("xcoord") + ", " + Marker.getDouble("ycoord") +", " + Marker.getString("title"));

                winfo.setXCoord(Marker.getDouble("xcoord"));
                winfo.setTitle(Marker.getString("title"));
                winfo.setIdx(Marker.getInt("idx"));
                winfo.setYCoord(Marker.getDouble("ycoord"));
                winfo.setWCount(Marker.getInt("w_count"));
                winfo.setCategory(Marker.getInt("category"));
                winfo.setDatetime(Marker.getString("dateTime"));



//                Log.d("winfo", winfo.getXCoord() + ", " + winfo.getYCoord() + ", " + winfo.getTitle());

                wilist.add(winfo);
            }
        } catch (JSONException e) {
            Log.e("WarningInfoParser", "Parse Error");
        }

        mResult = new WarningInfoList();
        mResult.setWarningInfoArrayList(wilist);
        mResult.setTotal(wilist.size());

        return mResult;
    }
}