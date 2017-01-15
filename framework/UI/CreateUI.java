package com.AwesomeAPI.framework.UI;

import com.AwesomeAPI.framework.UI.BreakHandler.BreakHandler;
import com.AwesomeAPI.framework.UI.ItemTrackerPane.ItemTrackerPane;
import com.AwesomeAPI.framework.UI.SkillTrackerPane.SkillTrackerPane;
import com.runemate.game.api.hybrid.Environment;
import com.runemate.game.api.hybrid.GameEvents;
import com.runemate.game.api.hybrid.RuneScape;
import com.runemate.game.api.hybrid.util.Resources;
import com.runemate.game.api.hybrid.util.StopWatch;
import com.runemate.game.api.script.Execution;
import com.runemate.game.api.script.framework.AbstractBot;
import com.runemate.game.api.script.framework.core.LoopingThread;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by Matthew on 12/27/2016.
 */
public class CreateUI extends VBox {
    private BreakHandler breakHandler;
    private ItemTrackerPane itemTrackerPane;
    private SkillTrackerPane skillTrackerPane;
    private Future<InputStream> stream;

    @FXML
    private Label LL_Version;

    @FXML
    private TitledPane TP_SkillTracker;

    @FXML
    private Label LL_BotName;

    @FXML
    private Label LL_Runtime;

    @FXML
    private TextField TF_StopTime;

    @FXML
    private TitledPane TP_ItemTracker;

    @FXML
    private TitledPane TP_BreakHandler;

    @FXML
    private HBox HB_Setup;

    @FXML
    public ListView<String> LV_CurrentTask;

    @FXML
    private TitledPane TP_Currently;

    public CreateUI(AbstractBot bot) {

        FXMLLoader loader = new FXMLLoader();
        loader.setController(this);
        loader.setRoot(this);

        stream = bot.getPlatform().invokeLater(() -> Resources.getAsStream("com/AwesomeAPI/framework/UI/UI.fxml"));

        try {
            loader.load(stream.get());
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        breakHandler = new BreakHandler();
        breakHandler.createBreakHandler(TP_BreakHandler, bot);
        itemTrackerPane = new ItemTrackerPane();
        itemTrackerPane.createTableView(TP_ItemTracker);
        skillTrackerPane = new SkillTrackerPane(bot);
        skillTrackerPane.createSkillTracker(TP_SkillTracker);

        StopWatch watch = new StopWatch();
        watch.start();
        Platform.runLater(() -> {
                    LoopingThread runTime = new LoopingThread(() -> setRuntime(watch.getRuntimeAsString()), 5000);
                    runTime.start();
                });

        setBotName(bot.getMetaData().getName());
        setVersion(bot.getMetaData().getVersion());
    }

    public BreakHandler getBreakHandler() {
        return breakHandler;
    }

    public ItemTrackerPane getItemTracker() {
        return itemTrackerPane;
    }

    public SkillTrackerPane getSkillTracker() {
        return skillTrackerPane;
    }

    public Label getVersion() {
        return LL_Version;
    }

    public void setVersion(String version) {
        LL_Version.setText("V" + version);
    }

    public Label getBotName() {
        return LL_BotName;
    }

    public void setBotName(String botName) {
        LL_BotName.setText(botName);
    }

    public Label getRuntime() {
        return LL_Runtime;
    }

    public void setRuntime(String runtime) {
        LL_Runtime.setText(runtime);
    }

    public TextField getStopTime() {
        return TF_StopTime;
    }

    public void setStopTime(String stopTime) {
        TF_StopTime.setText(stopTime);
    }

    public boolean validateStopTime(String stopTime) {
        return stopTime.matches("^\\d{2}:\\d{2}:\\d{2}$");
    }

    public HBox getSetupHB() {
        return HB_Setup;
    }

    public void stopBot() {
        GameEvents.OSRS.LOGIN_HANDLER.disable();
        GameEvents.RS3.LOGIN_HANDLER.disable();
        GameEvents.OSRS.LOBBY_HANDLER.disable();
        GameEvents.RS3.LOBBY_HANDLER.disable();
        while (Environment.getBot().isRunning() && RuneScape.isLoggedIn() && RuneScape.logout()) {
            Execution.delayUntil(() -> !RuneScape.isLoggedIn(), 10000);
        }
        Environment.getBot().stop();
    }

    public boolean checkStopBot(StopWatch watch){
        String userTime;
        if(!(userTime = TF_StopTime.getText()).equals("00:00:00") && BreakHandler.checkValid(userTime) && BreakHandler.convertToMilli(userTime) > 0){
            if(watch.getRuntime() >= BreakHandler.convertToMilli(userTime)){
                return true;
            }
        }
        return false;
    }

}
