package capston.stol.dangerousplace.parser;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import capston.stol.dangerousplace.bean.WarningInfo;

/**
 * Created by sbpark1 on 2016-01-08.
 */
public class WarningInfoDetailParser {

    public String dString;

    private WarningInfo wd;

    public WarningInfoDetailParser(String pString) {
        dString = pString;
    }

    public WarningInfo parse() {

        try {

            Log.d("totalstring", dString + "");
            JSONObject json = new JSONObject(dString);

            wd = new WarningInfo();

            wd.setUsage(json.getBoolean("usage"));//                wd.setIdx(jobg.getInt("idx"));
            wd.setUsrIdx(json.getInt("idx_users"));
            wd.setContent(json.getString("contents"));
            wd.setXCoord(json.getDouble("xcoord"));
            wd.setTitle(json.getString("title"));
            wd.setYCoord(json.getDouble("ycoord"));
            wd.setWCount(json.getInt("w_count"));
            wd.setDatetime(json.getString("date"));
            wd.setRegistDateTime(json.getString("regdate"));
            wd.setUsage(json.getBoolean("usage"));
            wd.setCheckWCount(json.getBoolean("checkUser"));
            wd.setIdx(json.getInt("idx"));
            wd.setCategory(json.getInt("category"));


        } catch (JSONException e) {
            Log.e("WarningInfoDetailParser", "Parse Error");
        }

        return wd;
    }
}
