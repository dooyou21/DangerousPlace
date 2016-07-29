package capston.stol.dangerousplace.util;

/**
 * Created by 이수정 on 2016-03-22.
 */
public class TimeForUser {

    int hour;
    String[] arr;

    public TimeForUser(String time){
        arr = new String[3];
        arr = time.split(":");
        hour = Integer.parseInt(arr[0]);

        //if hour -12 작으면 오전 크면 오후 해서 오전오후 스트링으로 붙혀서 오전 -시 -분 -초 를 스트링으로 반환

    }

    public String getTime(){
        if(hour-12 <0)
            arr[0] = "오전 "+hour;
        else {
            if(hour == 12) arr[0] = "오후 " + hour;
            else arr[0] = "오후 " + (hour - 12);
        }

        String timeChange = arr[0]+"시 "+arr[1]+"분 "+arr[2]+"초";
        return timeChange;
    }
}
