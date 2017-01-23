package com.AwesomeAPI.framework.UI;

import com.AwesomeAPI.framework.UI.BreakHandler.BreakHandler;
import com.AwesomeAPI.framework.UI.CurrentTaskList.CurrentTaskList;
import com.AwesomeAPI.framework.UI.ItemTrackerPane.ItemTrackerPane;
import com.AwesomeAPI.framework.UI.SkillTrackerPane.SkillTrackerPane;
import com.runemate.game.api.client.ClientUI;
import com.runemate.game.api.hybrid.Environment;
import com.runemate.game.api.hybrid.GameEvents;
import com.runemate.game.api.hybrid.RuneScape;
import com.runemate.game.api.hybrid.entities.Player;
import com.runemate.game.api.hybrid.region.Players;
import com.runemate.game.api.hybrid.util.Resources;
import com.runemate.game.api.hybrid.util.StopWatch;
import com.runemate.game.api.script.Execution;
import com.runemate.game.api.script.framework.AbstractBot;
import com.runemate.game.api.script.framework.core.LoopingThread;
import com.runemate.game.api.script.framework.listeners.ChatboxListener;
import com.runemate.game.api.script.framework.listeners.InventoryListener;
import com.runemate.game.api.script.framework.listeners.SkillListener;
import com.runemate.game.api.script.framework.listeners.events.ItemEvent;
import com.runemate.game.api.script.framework.listeners.events.MessageEvent;
import com.runemate.game.api.script.framework.listeners.events.SkillEvent;
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
import java.util.concurrent.TimeUnit;

/**
 * Created by Matthew on 12/27/2016.
 * This Class is used to Create the Whole UI from my API with almost no extra stuff needed.
 */
public class CreateUI extends VBox implements SkillListener, InventoryListener, ChatboxListener{
    private final AbstractBot bot;
    public CurrentTaskList currentTaskList;
    private BreakHandler breakHandler;
    private ItemTrackerPane itemTrackerPane;
    private SkillTrackerPane skillTrackerPane;
    private Future<InputStream> stream;
    private StopWatch bankTimer = new StopWatch();
    private Player player;

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
    private ListView<String> LV_CurrentTask;

    @FXML
    private TitledPane TP_Currently;
    private String tempMess;

    /**
     * Creates all the UI parts and now Handles the skill Listeners and inventory
     * @param bot -> The bot
     */
    public CreateUI(AbstractBot bot) {
        this.bot = bot;

        try {
            bot.getPlatform().invokeAndWait(() -> GameEvents.InactivityShutdownFailsafe.setInactivityTimeout(120000));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

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
        currentTaskList = new CurrentTaskList();
        LoopingThread thread = new LoopingThread(() -> {
            LV_CurrentTask.setItems(currentTaskList.getList());

            if (!currentTaskList.getList().isEmpty()) {
                Platform.runLater(() -> TP_Currently.setText("Currently: " + currentTaskList.getList().get(0)));
            }
        }, 500);
        thread.start();

        setBotName(bot.getMetaData().getName());
        setVersion(bot.getMetaData().getVersion());

        bot.getEventDispatcher().addListener(this);
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
        Platform.runLater(() -> LL_Runtime.setText(runtime));
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

    public void startBankTimer(){
        bankTimer.start();
    }

    public void resetBankTimer(){
        bankTimer.reset();
    }

    public void stopBankTimer(){
        bankTimer.stop();
    }

    public long getBankTimerRuntime(){
        return bankTimer.getRuntime();
    }

    public void stopBot() {
        GameEvents.Universal.LOGIN_HANDLER.disable();
        GameEvents.Universal.LOBBY_HANDLER.disable();
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

    @Override
    public void onExperienceGained(SkillEvent event) {
        skillTrackerPane.updateLevels(event);
    }

    @Override
    public void onItemAdded(ItemEvent event){
        if(bankTimer.getRuntime(TimeUnit.MILLISECONDS) > 1000)
            itemTrackerPane.refreshItems(event);
    }

    @Override
    public void onItemRemoved(ItemEvent event){
        if(bankTimer.getRuntime(TimeUnit.MILLISECONDS) > 1000)
            itemTrackerPane.refreshItems(event);
    }


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String name;
        try {
            if((player = bot.getPlatform().invokeAndWait(() -> Players.getLocal())) != null){
                if((name = player.getName()) != null) {
                    if ((tempMess = messageEvent.getMessage()) != null && tempMess.contains(name)) {
                        ClientUI.sendTrayNotification(messageEvent.getSender() + " Mentioned you.");
                    }
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
