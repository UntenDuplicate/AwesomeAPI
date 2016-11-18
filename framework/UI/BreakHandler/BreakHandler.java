package com.BreakHandler;

import com.runemate.game.api.client.ClientUI;
import com.runemate.game.api.hybrid.util.StopWatch;

/**
 * Created by Matthew on 11/15/2016.
 */
public class BreakHandler {

    public static boolean isBreaking(String[] SbreakTimes, String[] Sdurations, StopWatch stopWatch){
        boolean breaking = false;
        int breakTimes[] = new int[SbreakTimes.length];
        int durations[] = new int[Sdurations.length];

        for(int i = 0; i < breakTimes.length; i++){
            breakTimes[i] = convertToMilli(SbreakTimes[i]);
            durations[i] = convertToMilli(Sdurations[i]);
        }
        for (int i = 0; i < breakTimes.length; i++) {
            if (breakTimes[i] <= 0 || durations[i] <= 0) {
                break;
            } else {
                if (breakTimes[i] <= (stopWatch.getRuntime()) && (breakTimes[i] + durations[i]) >= stopWatch.getRuntime()) {
                    breaking = true;
                    break;
                }
            }
        }
        return breaking;
    }

    public static boolean checkValid(String time){
        if(!time.matches("[0-9]+:+[0-9]+:+[0-9]+")){
            ClientUI.sendTrayNotification("Time is not in correct format, Ex (00:00:00)");
            return false;
        }
        return true;
    }

    public static String getEnd(String start, String duration){
        return revertToString(convertToMilli(start) + convertToMilli(duration));
    }

    public static String getStart(String duration, String end){
        return revertToString(convertToMilli(end) - convertToMilli(duration));
    }

    public static String getDuration(String start, String end){
        return revertToString(convertToMilli(end) - convertToMilli(start));
    }

    public static int convertToMilli(String runtime){
        int numOfColons = 0;
        int converted = 0;
        for (int l = 0; l < runtime.length(); l++){
            if(runtime.charAt(l) == ':'){
                numOfColons++;
            }
        }
        if(numOfColons != 2){
            ClientUI.sendTrayNotification("Time is not in correct format, Ex (00:00:00)");
        }
        else if(!checkValid(runtime)){
            ClientUI.sendTrayNotification("Time is not in correct format, Ex (00:00:00)");
        }
        else{
            converted = Integer.parseInt(runtime.substring(0, runtime.indexOf(':'))) * 3600000
                    + Integer.parseInt(runtime.substring(runtime.indexOf(':') + 1, runtime.lastIndexOf(':'))) * 60000
                    + Integer.parseInt(runtime.substring(runtime.lastIndexOf(':') + 1, runtime.length())) * 1000;
        }
        return converted;
    }

    public static String revertToString(int breakTime) {
        String breakTime1 = "";
        int hours;
        int minutes;
        int seconds;


        if ((hours = (breakTime) / 3600000) < 10)
            breakTime1 = breakTime1 + "0" + hours + ":";
        else {
            breakTime1 = breakTime1 + hours + ":";
        }


        if ((minutes = (((breakTime) % 3600000) / 60000)) < 10)
            breakTime1 = breakTime1 + "0" + minutes + ":";
        else {
            breakTime1 = breakTime1 + minutes + ":";
        }


        if ((seconds = ((breakTime) % 3600000 % 60000) / 1000) < 10) {
            breakTime1 = breakTime1 + "0" + seconds;
        } else {
            breakTime1 = breakTime1 + seconds;
        }

        return breakTime1;
    }
}
