package com.AwesomeAPI.framework.UI.BreakHandler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runemate.game.api.hybrid.Environment;
import com.runemate.game.api.hybrid.util.StopWatch;
import com.runemate.game.api.hybrid.util.calculations.Random;
import com.runemate.game.api.script.framework.AbstractBot;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
     * @param Sdurations  A String array of durations.
     * @param stopWatch   A StopWatch from the main bot to use to check times.
     * @return Boolean that is true when a break contains the current StopWatch time, false if none do.
     */
    public static boolean isBreaking(String[] SbreakTimes, String[] Sdurations, StopWatch stopWatch) {
        boolean breaking = false;
        int breakTimes[] = new int[SbreakTimes.length];
        int durations[] = new int[Sdurations.length];

        for (int i = 0; i < breakTimes.length; i++) {
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
    public static boolean checkValid(String time) {
        if (!time.matches("^\\d{2}:\\d{2}:\\d{2}$")) {
            System.out.println("Sent");
            return false;
        }
        return true;
    }

    /**
     * Used to calculate the end time as a time String from a start and duration String.
     *
     * @param start    A String time format for the start time.
     * @param duration A String time format for the duration of the break.
     * @return A String that is the end time for the break.
     */
    public static String getEnd(String start, String duration) {
        return revertToString(convertToMilli(start) + convertToMilli(duration));
    }

    /**
     * Used to calculate the start time as a time String from a duration and end String.
     *
     * @param duration A String time format for the duration of the break.
     * @param end      A String time format for the end time of the break.
     * @return A String that is the Start time of the break.
     */
    public static String getStart(String duration, String end) {
        return revertToString(convertToMilli(end) - convertToMilli(duration));
    }

    /**
     * Used to get the duration of a break from the start and end time Strings.
     *
     * @param start A String time format for the start time of the break.
     * @param end   A String time format for the end of the break.
     * @return A String that is the duration of the break.
     */
    public static String getDuration(String start, String end) {
        return revertToString(convertToMilli(end) - convertToMilli(start));
    }

    /**
     * Used to convert a String time to an int in milliseconds.
     * Calls the checkValid() method to check if the input time String is a valid time String.
     *
     * @param runtime A String time to be converted to milliseconds.
     * @return An int that is the number of whole milliseconds the runtime represents.
     */
    public static int convertToMilli(String runtime) {
        int numOfColons = 0;
        int converted = 0;
        for (int l = 0; l < runtime.length(); l++) {
            if (runtime.charAt(l) == ':') {
                numOfColons++;
            }
        }
        if (numOfColons != 2) {
        } else if (!checkValid(runtime)) {
        } else {
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
     * @param LV_Start    A ListView for the start times.
     * @param LV_Duration A ListView for the Durations.
     * @param LV_End      A ListView for the ends.
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

    /*
    Here is how to add this properly to your UI controller

    Objects to delare as named in your UI.fxml
    |
    v
    @FXML
    private TableView<BreakHandler.BreakTracker> TV_BreakHandler;
    @FXML
    private TableColumn<BreakHandler.BreakTracker, String> TC_Start, TC_Duration, TC_End;

    If you are going to use the save/load options as well, add these also:
    |
    v
    @FXML
    private Button BN_Load, BN_Generate, BN_Clear, BN_Save, BN_Delete;

    @FXML
    private ComboBox<String> CB_Profile;

    @FXML
    private TextField TF_ProfName;

    This is not @FXML
    private ObservableList<BreakHandler.BreakTracker> breakTracker = FXCollections.observableArrayList();

    In Initialize() put these:
        |
        v
        TC_Start.setCellFactory(TextFieldTableCell.forTableColumn());
        TC_Duration.setCellFactory(TextFieldTableCell.forTableColumn());
        TC_End.setCellFactory(TextFieldTableCell.forTableColumn());
        TV_BreakHandler.setItems(breakTracker);

        TC_Start.setEditable(true);
        TC_Duration.setEditable(true);
        TC_End.setEditable(true);

        TC_Start.setOnEditCommit(BreakHandler.tableFillInTimes(breakTracker));
        TC_Duration.setOnEditCommit(BreakHandler.tableFillInTimes(breakTracker));
        TC_End.setOnEditCommit(BreakHandler.tableFillInTimes(breakTracker));

        TV_BreakHandler.getItems().addAll(new BreakHandler.BreakTracker("", "", ""), new BreakHandler.BreakTracker("", "", ""));

        TC_Start.setCellValueFactory(param -> param.getValue().startTimeProperty());
        TC_Duration.setCellValueFactory(param -> param.getValue().durationProperty());
        TC_End.setCellValueFactory(param -> param.getValue().endProperty());

        If you have all the buttons also, use these in Initialize():
        |
        v
        CB_Profile.getItems().addAll(BreakHandler.loadProfs(bot));
        BN_Generate.setOnAction(BreakHandler.generateBreaks(breakTracker));
        BN_Clear.setOnAction(BreakHandler.clearBreaks(breakTracker));
        BN_Load.setOnAction(BreakHandler.loadBreaks(breakTracker, CB_Profile, bot));
        BN_Save.setOnAction(BreakHandler.saveBreaks(breakTracker, TF_ProfName, CB_Profile, bot));
        BN_Delete.setOnAction(BreakHandler.deleteProf(CB_Profile, bot));
        */

    public static class BreakTracker {

        private final StringProperty startTime;
        private final StringProperty duration;
        private final StringProperty endTime;

        public BreakTracker(String startTime, String duration, String endTime) {
            this.startTime = new SimpleStringProperty(startTime);
            this.endTime = new SimpleStringProperty(endTime);
            this.duration = new SimpleStringProperty(duration);
        }

        public String getStartTime() {
            return startTime.get();
        }

        public String getDuration() {
            return duration.get();
        }

        public String getEndTime() {
            return endTime.get();
        }

        public void setStartTime(String time) {
            startTime.set(time);
        }

        public void setDuration(String time) {
            duration.set(time);
        }

        public void setEndTime(String time) {
            endTime.set(time);
        }

        public StringProperty startTimeProperty() {
            return startTime;
        }

        public StringProperty durationProperty() {
            return duration;
        }

        public StringProperty endProperty() {
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
            if (BreakHandler.checkValid(time)) {
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

    public static EventHandler<ActionEvent> generateBreaks(List<BreakTracker> breakTracker) {
        return event -> {
            breakTracker.clear();
            for (int i = 0; i < 15; i++) {
                int duration = 0;
                int startTime = 0;
                if (i > 0)
                    breakTracker.add(new BreakTracker(revertToString((startTime = convertToMilli(breakTracker.get(i - 1).getEndTime()) + 6 * Random.nextInt(900000, 3600001))),
                            revertToString((duration = 6 * Random.nextInt(450000, 3600001))), revertToString(startTime + duration)));
                else
                    breakTracker.add(new BreakTracker(revertToString((startTime = 6 * Random.nextInt(900000, 3600001))),
                            revertToString((duration = 6 * Random.nextInt(450000, 3600001))), revertToString(startTime + duration)));
            }
        };
    }

    public static EventHandler<ActionEvent> clearBreaks(List<BreakTracker> breakTracker) {
        return event -> {
            breakTracker.clear();
            breakTracker.add(new BreakTracker("", "", ""));
        };
    }

    public static EventHandler<ActionEvent> saveBreaks(List<BreakTracker> breakTracker, TextField profTF, ComboBox<String> profsBox, AbstractBot bot) {
        return event -> {
            String path = "";
            String prof = profTF.getText();
            if(prof != null) {
                profsBox.getItems().add(prof);
                JsonObject jBreaks = new JsonObject();
                JsonArray listOfStarts = new JsonArray();
                JsonArray listOfDurations = new JsonArray();
                JsonArray listOfEnds = new JsonArray();
                for (int i = 0; i < breakTracker.size(); i++) {
                    listOfStarts.add(breakTracker.get(i).getStartTime());
                }
                for (int i = 0; i < breakTracker.size(); i++) {
                    listOfDurations.add(breakTracker.get(i).getDuration());
                }
                for (int i = 0; i < breakTracker.size(); i++) {
                    listOfEnds.add(breakTracker.get(i).getEndTime());
                }

                jBreaks.add("Start", listOfStarts);
                jBreaks.add("Duration", listOfDurations);
                jBreaks.add("End", listOfEnds);

                System.out.println(jBreaks);

                try {

                    try {
                        path = bot.getPlatform().invokeAndWait(() -> Environment.getStorageDirectory().getPath());
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    // Writing to a file
                    System.out.println(prof);
                    File file = new File(path + "\\" + prof + ".json");
                    file.createNewFile();
                    FileWriter fileWriter = new FileWriter(file);
                    System.out.println("Writing JSON object to file");
                    System.out.println("-----------------------");

                    fileWriter.write(jBreaks.toString());
                    fileWriter.flush();
                    fileWriter.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public static EventHandler<ActionEvent> loadBreaks(List<BreakTracker> breakTracker, ComboBox<String> profName, AbstractBot bot) {
        return event -> {
            breakTracker.clear();
            String path = "";
            String prof = profName.getSelectionModel().getSelectedItem();

            try {
                path = bot.getPlatform().invokeAndWait(() -> Environment.getStorageDirectory().getPath());

                JsonParser parser = new JsonParser();

                Object obj = parser.parse(new FileReader(path + "\\" + prof + ".json"));

                JsonObject jsonObject = (JsonObject) obj;

                System.out.println("Starts are :");
                JsonArray listOfStarts = (JsonArray) jsonObject.get("Start");
                Iterator<JsonElement> iterator = listOfStarts.iterator();
                while (iterator.hasNext()) {
                    System.out.println(iterator.next());
                }
                System.out.println("Durations are :");
                JsonArray listOfDurations = (JsonArray) jsonObject.get("Duration");
                Iterator<JsonElement> iteratorD = listOfDurations.iterator();
                while (iteratorD.hasNext()) {
                    System.out.println(iteratorD.next());
                }
                System.out.println("Ends are :");
                JsonArray listOfEnds = (JsonArray) jsonObject.get("End");
                Iterator<JsonElement> iteratorE = listOfEnds.iterator();
                while (iteratorE.hasNext()) {
                    System.out.println(iteratorE.next());
                }

                for(int i = 0; i < listOfStarts.size(); i++){
                    breakTracker.add(new BreakTracker(listOfStarts.get(i).getAsString(), listOfDurations.get(i).getAsString(), listOfEnds.get(i).getAsString()));
                }
            } catch (IOException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }


        };
    }

    public static List<String> loadProfs(AbstractBot bot) {
        List<String> results = new ArrayList<>();
        String path = null;
        try {
            path = bot.getPlatform().invokeAndWait(() -> Environment.getStorageDirectory().getPath());
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }


        File[] files = new File(path).listFiles((dir, name) -> name.endsWith(".json"));

        for (File file : files) {
            if (file.isFile()) {
                results.add(file.getName().replace(".json", ""));
            }
        }

        return results;
    }

    public static EventHandler<ActionEvent> deleteProf(ComboBox<String> cb_profile, AbstractBot bot) {
        return event -> {
            String path = "";
            try {
                path = bot.getPlatform().invokeAndWait(() -> Environment.getStorageDirectory().getPath());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            String prof = cb_profile.getSelectionModel().getSelectedItem();
            int index = cb_profile.getSelectionModel().getSelectedIndex();
            if(prof != null) {
                File file = new File(path + "\\" + prof + ".json");
                if(file.delete()){
                    System.out.println("Deleted Profile: " + prof);
                    cb_profile.getItems().remove(index);
                }
            }
        };
    }
}
