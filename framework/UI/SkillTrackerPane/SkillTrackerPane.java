package com.AwesomeAPI.framework.UI.SkillTrackerPane;

import com.AwesomeAPI.framework.UI.BreakHandler.BreakHandler;
import com.runemate.game.api.hybrid.Environment;
import com.runemate.game.api.hybrid.GameEvents;
import com.runemate.game.api.hybrid.RuneScape;
import com.runemate.game.api.hybrid.local.Skill;
import com.runemate.game.api.hybrid.util.StopWatch;
import com.runemate.game.api.script.Execution;
import com.runemate.game.api.script.framework.AbstractBot;
import com.runemate.game.api.script.framework.core.BotPlatform;
import com.runemate.game.api.script.framework.core.LoopingThread;
import com.runemate.game.api.script.framework.listeners.events.SkillEvent;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;

/**
 * Created by Matthew on 11/16/2016.
 * A class that contains methods that create TitledPanes
 * which contain ProgressBars and Labels as their Header
 * and can now be removed/added by users.
 */
public class SkillTrackerPane {

    private int level;
    private double progress;
    public List<ProgressIndicatorBar> skillBars = new ArrayList<>();
    private TitledPane pane;
    public List<Label> skillLabels = new ArrayList<>();
    private List<Label> expHourLabels = new ArrayList<>();
    private List<Label> timeLables = new ArrayList<>();
    private int expLeft;
    private int tempExp;
    private VBox vBox = new VBox();
    private List<TextField> goals = new ArrayList<>();
    public AbstractBot bot;
    private boolean changing;
    private BotPlatform platform;
    private StopWatch watch = new StopWatch();
    private String skillName = "";
    private List<Button> removeButtons = new ArrayList<>();
    private boolean updating = false;
    private LoopingThread loopingThread;

    /**
     * The constructor for the pane that creates a SkillTrackerPane object for a bot
     * @param bot -> The bot being run
     */
    public SkillTrackerPane(AbstractBot bot){
        this.bot = bot;
    }

    /**
     * Pretty self explanatory, a label updater Thread that loops every 500ms.
     */
    public void createLabelUpdater(){
        StopWatch watch = new StopWatch();
        watch.start();
        loopingThread = new LoopingThread(() -> {
            checkGoals();
            updating = true;
            for (int i = 0; i < skillBars.size(); i++) {
                int finalI = i;
                Platform.runLater(() -> expHourLabels.get(finalI).setText(Math.round(Integer.parseInt(skillLabels.get(finalI).getText()) / ((double) watch.getRuntime() / 3600000)) + ""));
                Platform.runLater(() -> {
                    try {
                        if (bot != null &&
                                (platform = bot.getPlatform()) != null &&
                                platform.invokeAndWait(() -> RuneScape.isLoggedIn())) {
                            try {
                                expLeft = bot.getPlatform().invokeAndWait(() -> Skill.valueOf(skillBars.get(finalI).getSkill().toUpperCase()).getExperienceToNextLevel());
                                if (expLeft > 0)
                                    tempExp = expLeft;
                                timeLables.get(finalI).setText(BreakHandler.revertToString((int) (3600000 * (((double) (tempExp) / Integer.parseInt(expHourLabels.get(finalI).getText()))))));
                            } catch (ExecutionException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
            updating = false;
        }, 500);
        loopingThread.start();
    }

    /**
     * Calls a method that either adds or updates a SkillTracker TitledPane.
     * @param event -> The skill which changed
     */
    public void updateLevels(SkillEvent event) {
        if (skillBars.isEmpty()) {
            addSkillBar(event);
        }

        for (int i = 0; i < skillBars.size(); i++) {
            if (skillBars.get(i).getSkill().equals(event.getSkill().toString())) {
                System.out.println("Index: " + i + " Skill: " + event.getSkill());
                try {
                    level = bot.getPlatform().invokeAndWait(() -> event.getSkill().getBaseLevel());
                    progress = bot.getPlatform().invokeAndWait(() -> (double) (100 - event.getSkill().getExperienceToNextLevelAsPercent()) / 100.0);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                int finalI = i;
                Platform.runLater(() -> skillBars.get(finalI).syncProgress(progress, level, (level - skillBars.get(finalI).getLevel())));
                Platform.runLater(() -> skillLabels.get(finalI).setText(Integer.parseInt(skillLabels.get(finalI).getText()) + event.getChange() + ""));
                break;
            } else if (i == skillBars.size() - 1) {
                addSkillBar(event);
            }
        }
    }

    /**
     * Adds a SkillBar (ProgressBar and Label) to a list of ProgressIndicator bars
     * @param event -> The SkillEvent
     */
    public void addSkillBar(SkillEvent event){
        try {
            level =
                    bot.getPlatform().invokeAndWait(
                            () -> event.getSkill().
                                    getBaseLevel());
            progress = bot.getPlatform().invokeAndWait(() -> (double)(100 - event.getSkill().getExperienceToNextLevelAsPercent())/100.0);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        skillBars.add(new ProgressIndicatorBar(progress, event.getSkill().toString(), level, 0));
        Platform.runLater(() -> {
            try {
                addSkillPane(skillBars.size() - 1);
            } catch (ExecutionException | InterruptedException | IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Fills the SkillTracker TitledPane with a VBox that includes a Spinner and Add button
     * @param AN_SkillTracker -> The skilltracker TitledPane sorry the name should have been TP_SkillTracker
     */
    public void createSkillTracker(TitledPane AN_SkillTracker) {
        AN_SkillTracker.setContent(vBox);
        ObservableList<String> allSkills = FXCollections.observableArrayList("AGILITY", "ATTACK", "CONSTITUTION", "CONSTRUCTION", "COOKING", "CRAFTING",
                "DEFENSE", "DIVINATION", "DUNGEONEERING", "FARMING", "FIREMAKING", "FISHING", "FLETCHING", "HERBLORE", "HUNTER", "INVENTION", "MAGIC",
                "MINING", "PRAYER", "RANGED", "RUNECRAFTING", "SLAYER", "SMITHING", "STRENGTH", "SUMMONING", "THIEVING", "WOODCUTTING");
        Spinner<String> spinner = new Spinner<>(allSkills);
        Button createSkillTracker = new Button("Add");
        EventHandler<ActionEvent> checkSkillSelected = event -> {
            if((skillName = spinner.getValue()) != null){
                SkillEvent skillEvent = new SkillEvent(Skill.valueOf(skillName), SkillEvent.Type.EXPERIENCE_GAINED, 0, 0);
                addSkillBar(skillEvent);
            }
        };
        createSkillTracker.setOnAction(checkSkillSelected);
        HBox hbox = new HBox(spinner, new Separator(Orientation.VERTICAL), createSkillTracker);
        hbox.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(hbox, new Separator(Orientation.HORIZONTAL));
        createLabelUpdater();
    }

    /**
     * A ProgressIndicator Object that creates a ProgressBar and Label TitledPane
     */
    public class ProgressIndicatorBar extends StackPane {
        final private int level;
        final private String skillName;

        final private ProgressBar bar  = new ProgressBar();
        final private Text text = new Text();

        public ProgressIndicatorBar(final double progress, final String skillName, final int level, final int levelsGained) {
            this.level = level;
            this.skillName = skillName;


            text.setFont(Font.font("comic sans", FontWeight.EXTRA_BOLD, 16));

            String css = "-fx-accent rgb(" + ((double)1 - progress) * 255 + ", " + progress * 255 + ", 0)";

            bar.styleProperty().set(css);

            bar.setMaxWidth(Double.MAX_VALUE); // allows the progress bar to expand to fill available horizontal space.

            bar.setMinHeight(27);

            getChildren().setAll(bar, text);

            syncProgress(progress, level, levelsGained);


        }

        public String getSkill(){
            return skillName;
        }

        public int getLevel(){return level;}

        // synchronizes the progress indicated with the progress and levels passed in.
        public void syncProgress(double progress, int level, int levelsGained) {
            if (skillName == this.skillName) {
                if (progress == 0) {
                    text.setText(skillName);
                    bar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
                } else {
                    text.setText(skillName + " | Level: " + level + " | Levels Gained: " + levelsGained);
                    bar.setProgress(progress);
                    String css = "-fx-accent: rgb(" + ((double)1-progress) * 255 + ", " + progress * 255 + ", 0)";
                    bar.styleProperty().set(css);
                }
            }
        }
    }

    /**
     * Adds a TitledPane to the Vbox using the int 'i' as the index for the skill(obtained from a list)
     * @param i -> Index of the skill in the lists
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws IOException
     */
        public void addSkillPane(int i) throws ExecutionException, InterruptedException, IOException {
        pane = new TitledPane(null, null);

        pane.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);

        ProgressIndicatorBar myBar = skillBars.get(i);

        System.out.println("Added " + myBar.getSkill());

        pane.setGraphic(myBar);

        Label label1 = new Label("Exp Gained: ");

        Label label2 = new Label("Exp/Hour: ");

        Label label3 = new Label("Time to Next: ");

        Label labelT = new Label("0");

        Label labels = new Label("0");

        Label label = new Label("0");

        TextField goal = new TextField("0");

        goal.setPrefWidth(40);

        goals.add(goal);

        goal.textProperty().addListener((observable, oldValue, newValue) -> {
            watch.reset();
            System.out.println("textfield changed from " + oldValue + " to " + newValue);
            watch.start();
        });

        skillLabels.add(label);

        expHourLabels.add(labels);

        timeLables.add(labelT);

        Separator separator = new Separator(Orientation.VERTICAL);

        Separator separator1 = new Separator(Orientation.VERTICAL);

        Button removeButton = new Button("X");

        removeButton.setOnAction(deleteSkillTracker());

        removeButtons.add(removeButton);

        HBox hbox = new HBox(label1, label, separator, label2, labels, separator1, label3, labelT, new Separator(Orientation.VERTICAL), new Label("Goal: "), goal, new Separator(Orientation.VERTICAL), removeButton);

        hbox.setAlignment(Pos.CENTER);

        hbox.setSpacing(10);

        pane.setContent(hbox);

        vBox.getChildren().addAll(pane, new Separator(Orientation.HORIZONTAL));

        System.out.println("added");

        myBar.prefWidthProperty().bind(pane.widthProperty().subtract(55));
    }

    /**
     * Deletes the SkillTracker which had it's delete button pressed
     * @return Does the removing.
     */
        private EventHandler<ActionEvent> deleteSkillTracker() {
        return event -> {
            for(int i = 0; i < removeButtons.size(); i++){
                if(removeButtons.get(i).equals(event.getSource())){

                    while(updating){

                    }
                    ObservableList<Node> children;
                    Node temp = (children = vBox.getChildren()).get(i * 2 + 2);
                    children.remove(temp);
                    children.remove(children.get(i * 2 + 1));

                    skillLabels.remove(i);
                    expHourLabels.remove(i);
                    timeLables.remove(i);
                    removeButtons.remove(i);
                    goals.remove(i);
                    skillBars.remove(i);
                    System.out.println("Removed SkillListener");
                }
            }
        };
    }

    /**
     * Checks the list of goals to see if any have been reached and logs out/stops bot if so.
     */
    public void checkGoals(){
        if(watch.getRuntime(TimeUnit.SECONDS) > 20) {
            for (int i = 0; i < goals.size(); i++) {
                int tempGoal;
                String text;
                if ((text = goals.get(i).getText()).matches("^[1-9]\\d*$") && (tempGoal = Integer.parseInt(text)) > 0) {
                    int finalI = i;
                    try {
                        bot.getPlatform().invokeAndWait(() -> {
                            if (Skill.valueOf(skillBars.get(finalI).getSkill().toUpperCase()).getCurrentLevel() >= tempGoal) {
                                GameEvents.OSRS.LOGIN_HANDLER.disable();
                                GameEvents.RS3.LOGIN_HANDLER.disable();
                                GameEvents.OSRS.LOBBY_HANDLER.disable();
                                GameEvents.RS3.LOBBY_HANDLER.disable();
                                while (Environment.getBot().isRunning() && RuneScape.isLoggedIn()) {
                                    if (RuneScape.logout()) {
                                        Execution.delayUntil(() -> !RuneScape.isLoggedIn(), 10000);
                                    }
                                }
                                loopingThread.interrupt();
                                Environment.getBot().stop();
                            }
                        });
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
