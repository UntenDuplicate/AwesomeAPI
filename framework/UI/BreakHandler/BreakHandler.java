package com.AwesomeAPI.framework.UI.BreakHandler;

import com.runemate.game.api.client.ClientUI;
import com.runemate.game.api.hybrid.util.StopWatch;

/**
 * Created by Matthew on 11/15/2016.
 *
 * Feel free to add more methods or edit existing ones.
 *
 * A class for converting time from milliseconds to seconds as well as determining
 * if a break handler is supposed to be breaking.
 * There is also a method to check if the input strings for break handlers are valid times.
 *
 */
public class BreakHandler {

    /**
     * Used to determine if a bot is breaking or not.
     *
     * @param SbreakTimes A String array of startTimes.
     * @param Sdurations A String array of durations.
     * @param stopWatch A StopWatch from the main bot to use to check times.
     * @return Boolean that is true when a break contains the current StopWatch time, false if none do.
     */
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

    /**
     * Used to check user input times for a break handler.
     *
     * @param time A use input String for a runtime in format 00:00:00.
     * @return True if the format contains the ":"s and appropriate numbers with no other characters.
     */
    public static boolean checkValid(String time){
        if(!time.matches("[0-9]+:+[0-9]+:+[0-9]+")){
            ClientUI.sendTrayNotification("Time is not in correct format, Ex (00:00:00)");
            return false;
        }
        return true;
    }

    /**
     * Used to calculate the end time as a time String from a start and duration String.
     *
     * @param start A String time format for the start time.
     * @param duration A String time format for the duration of the break.
     * @return A String that is the end time for the break.
     */
    public static String getEnd(String start, String duration){
        return revertToString(convertToMilli(start) + convertToMilli(duration));
    }

    /**
     * Used to calculate the start time as a time String from a duration and end String.
     *
     * @param duration A String time format for the duration of the break.
     * @param end A String time format for the end time of the break.
     * @return A String that is the Start time of the break.
     */
    public static String getStart(String duration, String end){
        return revertToString(convertToMilli(end) - convertToMilli(duration));
    }

    /**
     * Used to get the duration of a break from the start and end time Strings.
     *
     * @param start A String time format for the start time of the break.
     * @param end A String time format for the end of the break.
     * @return A String that is the duration of the break.
     */
    public static String getDuration(String start, String end){
        return revertToString(convertToMilli(end) - convertToMilli(start));
    }

    /**
     * Used to convert a String time to an int in milliseconds.
     * Calls the checkValid() method to check if the input time String is a valid time String.
     *
     * @param runtime A String time to be converted to milliseconds.
     * @return An int that is the number of whole milliseconds the runtime represents.
     */
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

    /**
     * Takes a time in milliseconds as an int and converts it back to a time String.
     * The max time unit is Hours. This does not go to days, be warned.
     *
     * @param breakTime An int of the milliseconds for a time.
     * @return The time String from the milliseconds.
     */
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
