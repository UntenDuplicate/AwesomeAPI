package com.AwesomeAPI.framework.UI.BreakHandler;

import com.runemate.game.api.client.ClientUI;
import com.runemate.game.api.hybrid.util.StopWatch;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;

import java.util.List;

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
        System.out.println(breaking);
        return breaking;
    }

    /**
     * Used to check user input times for a break handler.
     *
     * @param time A use input String for a runtime in format 00:00:00.
     * @return True if the format contains the ":"s and appropriate numbers with no other characters.
     */
    public static boolean checkValid(String time){
        if(!time.matches("^\\d{2}:\\d{2}:\\d{2}$")){
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

    /**
     * An EventHandler that will fill in the different ListViews you have for your break handler.
     * I wish i knew how to use TableView :( .
     *
     * @param LV_Start A ListView for the start times.
     * @param LV_Duration A ListView for the Durations.
     * @param LV_End A ListView for the ends.
     * @return An EventHandler that will execute the event whenever someone edits the ListView this is added to.
     */
    public static EventHandler<ListView.EditEvent<String>> listViewFillInTimes(ListView<String> LV_Start, ListView<String> LV_Duration, ListView<String> LV_End) {
        return event -> {
            event.getSource().getItems().set(event.getIndex(), event.getNewValue());
            System.out.println("Edited :D");
            String time;
            int row;
            String time1;
            if (event.getSource().getId().equals("LV_Start")) {
                System.out.println("TC_Start :D");
                row = event.getIndex();
                if (!(time = LV_Duration.getItems().get(row)).isEmpty()) {
                    time1 = event.getSource().getItems().get(row);
                    System.out.println("Time: " + time + " row: " + row + " Time1: " + time1);
                    LV_End.getItems().set(row, BreakHandler.getEnd(time, time1));
                } else if (!(time = LV_End.getItems().get(row)).isEmpty()) {
                    time1 = event.getSource().getItems().get(row);
                    LV_Duration.getItems().set(row, BreakHandler.getDuration(time, time1));
                }
            } else if (event.getSource().getId().equals("LV_Duration")) {
                row = event.getIndex();
                if (!(time = LV_Start.getItems().get(row)).isEmpty()) {
                    time1 = event.getSource().getItems().get(row);
                    LV_End.getItems().set(row, BreakHandler.getEnd(time, time1));
                } else if (!(time = LV_End.getItems().get(row)).isEmpty()) {
                    time1 = event.getSource().getItems().get(row);
                    LV_Start.getItems().set(row, BreakHandler.getStart(time1, time));
                }
            } else if (event.getSource().getId().equals("LV_End")) {
                row = event.getIndex();
                if (!(time = LV_Duration.getItems().get(row)).isEmpty()) {
                    time1 = event.getSource().getItems().get(row);
                    LV_Start.getItems().set(row, BreakHandler.getStart(time, time1));
                } else if (!(time = LV_Start.getItems().get(row)).isEmpty()) {
                    time1 = event.getSource().getItems().get(row);
                    LV_Duration.getItems().set(row, BreakHandler.getDuration(time, time1));
                }
            }
        };
    }


    /**
     * Created by Matthew on 11/18/2016.
     */
    public static class BreakTracker {

        private final StringProperty startTime;
        private final StringProperty duration;
        private final StringProperty endTime;

        public BreakTracker(String startTime, String duration, String endTime){
            this.startTime = new SimpleStringProperty(startTime);
            this.endTime = new SimpleStringProperty(endTime);
            this.duration = new SimpleStringProperty(duration);
        }

        public String getStartTime(){
            return startTime.get();
        }

        public String getDuration(){
            return duration.get();
        }

        public String getEndTime(){
            return endTime.get();
        }

        public void setStartTime(String time){
            startTime.set(time);
        }

        public void setDuration(String time){
            duration.set(time);
        }

        public void setEndTime(String time){
            endTime.set(time);
        }

        public StringProperty startTimeProperty() {
            return startTime;
        }

        public StringProperty durationProperty(){
            return duration;
        }

        public StringProperty endProperty(){
            return endTime;
        }

    }

    public static EventHandler<TableColumn.CellEditEvent<BreakTracker, String>> tableFillInTimes(List<BreakTracker> breakTracker) {
        return event -> {
            String time;
            int row;
            int col;
            String time1;
            time = event.getNewValue();
            if(BreakHandler.checkValid(time)) {
                row = event.getTablePosition().getRow();
                System.out.println(row);
                col = event.getTablePosition().getColumn();
                if (breakTracker.size() - 1 <= row) {
                    breakTracker.add(new BreakTracker("", "", ""));
                }
                if (col == 0) { //Start time
                    breakTracker.get(row).setStartTime(event.getNewValue());
                    if (!(time1 = breakTracker.get(row).endProperty().get()).isEmpty()) {
                        breakTracker.get(row).setDuration(BreakHandler.getDuration(time, time1));
                    } else if (!(time1 = breakTracker.get(row).durationProperty().get()).isEmpty()) {
                        breakTracker.get(row).setEndTime(BreakHandler.getEnd(time, time1));
                    }
                } else if (col == 1) { //Duration
                    breakTracker.get(row).setDuration(event.getNewValue());
                    if (!(time1 = breakTracker.get(row).endProperty().get()).isEmpty()) {
                        breakTracker.get(row).setStartTime(BreakHandler.getStart(time, time1));
                    } else if (!(time1 = breakTracker.get(row).startTimeProperty().get()).isEmpty()) {
                        breakTracker.get(row).setEndTime(BreakHandler.getEnd(time1, time));
                    }
                } else {
                    breakTracker.get(row).setEndTime(event.getNewValue());
                    if (!(time1 = breakTracker.get(row).startTimeProperty().get()).isEmpty()) {
                        breakTracker.get(row).setDuration(BreakHandler.getDuration(time1, time));
                    } else if (!(time1 = breakTracker.get(row).durationProperty().get()).isEmpty()) {
                        breakTracker.get(row).setStartTime(BreakHandler.getStart(time1, time));
                    }
                }
            }
        };
    }
}
