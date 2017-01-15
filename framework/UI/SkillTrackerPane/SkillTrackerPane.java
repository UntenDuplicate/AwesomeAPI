package com.AwesomeAPI.framework.UI.SkillTrackerPane;

import com.AwesomeAPI.framework.UI.BreakHandler.BreakHandler;
import com.runemate.game.api.hybrid.Environment;
import com.runemate.game.api.hybrid.GameEvents;
import com.runemate.game.api.hybrid.RuneScape;
import com.runemate.game.api.hybrid.local.Skill;
import com.runemate.game.api.hybrid.util.StopWatch;
import com.runemate.game.api.script.Execution;
import com.runemate.game.api.script.framework.AbstractBot;
import com.runemate.game.api.script.framework.core.LoopingThread;
import com.runemate.game.api.script.framework.listeners.events.SkillEvent;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
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

    public SkillTrackerPane(AbstractBot bot){
        this.bot = bot;
    }


    public void createLabelUpdater(){
        StopWatch watch = new StopWatch();
        watch.start();
        LoopingThread loopingThread = new LoopingThread(() -> {
            checkGoals();
            for(int i = 0; i < skillBars.size(); i++){
                int finalI = i;
                Platform.runLater(() -> expHourLabels.get(finalI).setText(Math.round(Integer.parseInt(skillLabels.get(finalI).getText())/((double)watch.getRuntime()/3600000)) + ""));
                Platform.runLater(() -> {
                    try {
                        if(bot.getPlatform() != null && bot.getPlatform().invokeAndWait(() -> RuneScape.isLoggedIn())) {
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
        }, 500);
        loopingThread.start();
    }

    public void updateLevels(SkillEvent event) {
        if (skillBars.isEmpty()) {
            addSkillBar(event, 0);
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
                addSkillBar(event, i);
            }
        }
    }

    public void addSkillBar(SkillEvent event, int i){
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

    public void createSkillTracker(TitledPane AN_SkillTracker) {
        AN_SkillTracker.setContent(vBox);
        createLabelUpdater();
    }

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

    public void addSkillPane(int i) throws ExecutionException, InterruptedException, IOException {
        pane = new TitledPane(null, null);

        pane.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);

        ProgressIndicatorBar myBar = skillBars.get(i);

        System.out.println("Added " + myBar.getSkill());

        pane.setGraphic(myBar);

        Label label1 = new Label(myBar.getSkill() + " Exp Gained: ");

        Label label2 = new Label(myBar.getSkill() + "Exp/Hour: ");

        Label label3 = new Label("Time to Next: ");

        Label labelT = new Label("0");

        Label labels = new Label("0");

        Label label = new Label("0");

        TextField goal = new TextField("0");

        goal.setPrefWidth(40);

        goals.add(goal);

        goal.setOnAction(event -> {
            StopWatch watch = new StopWatch();
            watch.start();
            while(watch.getRuntime(TimeUnit.SECONDS) < 20){
                changing = true;
            }
            changing = false;
        });

        skillLabels.add(label);

        expHourLabels.add(labels);

        timeLables.add(labelT);

        Separator separator = new Separator(Orientation.VERTICAL);

        Separator separator1 = new Separator(Orientation.VERTICAL);

        HBox hbox = new HBox(label1, label, separator, label2, labels, separator1, label3, labelT, new Separator(Orientation.VERTICAL), new Label("Goal: "), goal);

        hbox.setAlignment(Pos.CENTER);

        hbox.setSpacing(10);

        pane.setContent(hbox);

        vBox.getChildren().add(pane);

        Separator separator2 = new Separator(Orientation.HORIZONTAL);

        vBox.getChildren().add(separator2);

        System.out.println("added");

        myBar.prefWidthProperty().bind(pane.widthProperty().subtract(55));
    }

    public void checkGoals(){
        if(!changing) {
            for (int i = 0; i < goals.size(); i++) {
                int tempGoal;
                if ((tempGoal = Integer.parseInt(goals.get(i).getText())) > 0) {
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
