package com.SkillTrackerPane;

import com.BreakHandler.BreakHandler;
import com.runemate.game.api.hybrid.local.Skill;
import com.runemate.game.api.hybrid.util.StopWatch;
import com.runemate.game.api.script.framework.AbstractBot;
import com.runemate.game.api.script.framework.core.LoopingThread;
import com.runemate.game.api.script.framework.listeners.events.SkillEvent;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;

/**
 * Created by Matthew on 11/16/2016.
 */
public class SkillTrackerPane {

    private static int level;
    private static double progress;
    public static List<ProgressIndicatorBar> skillBars = new ArrayList<>();
    private static TitledPane pane;
    public static List<Label> skillLabels = new ArrayList<>();
    private static List<Label> expHourLabels = new ArrayList<>();
    private static VBox AN_SkillTracker;
    private static List<Label> timeLables = new ArrayList<>();
    private static int expLeft;
    private static int tempExp;

    public static void createLabelUpdater(AbstractBot bot){
        StopWatch watch = new StopWatch();
        watch.start();
        LoopingThread loopingThread = new LoopingThread(() -> {
            for(int i = 0; i < skillBars.size(); i++){
                int finalI = i;
                Platform.runLater(() -> expHourLabels.get(finalI).setText(Math.round(Integer.parseInt(skillLabels.get(finalI).getText())/((double)watch.getRuntime()/3600000)) + ""));
                Platform.runLater(() -> {
                    try {
                        expLeft = bot.getPlatform().invokeAndWait(() -> Skill.valueOf(skillBars.get(finalI).getSkill().toUpperCase()).getExperienceToNextLevel());
                        if(expLeft > 0)
                            tempExp = expLeft;
                        timeLables.get(finalI).setText(BreakHandler.revertToString((int)(3600000 * (((double)(tempExp)/Integer.parseInt(expHourLabels.get(finalI).getText()))))));
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        }, 500);
        loopingThread.start();
    }

    public static void addSkillBar(AbstractBot bot, SkillEvent event, int i){
        try {
            level = bot.getPlatform().invokeAndWait(() -> event.getSkill().getBaseLevel());
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

    public static void setAN_SkillTracker(VBox AN_SkillTracker) {
        SkillTrackerPane.AN_SkillTracker = AN_SkillTracker;
    }

    public static class ProgressIndicatorBar extends StackPane {
        final private int level;
        final private String skillName;

        final private ProgressBar bar  = new ProgressBar();
        final private Text text = new Text();

        public ProgressIndicatorBar(final double progress, final String skillName, final int level, final int levelsGained) {
            this.level = level;
            this.skillName = skillName;


            text.setFont(Font.font("arial", FontWeight.EXTRA_BOLD, 16));
            //Setting the Stroke
            text.setStrokeWidth(.5);

            text.setFill(Color.GHOSTWHITE);

            // Setting the stroke color
            text.setStroke(Color.BLACK);

            String css = "-fx-accent rgb(" + ((double)1-progress) * 255 + ", " + progress * 255 + ", 0)";

            bar.styleProperty().set(css);

            /*if(progress < .1){
                bar.styleProperty().set("-fx-accent: darkred");
            }
            else if(progress < .2){
                bar.styleProperty().set("-fx-accent: red");
            }
            else if(progress < .3){
                bar.styleProperty().set("-fx-accent: crimson");
            }
            else if(progress < .4){
                bar.styleProperty().set("-fx-accent: darkorange");
            }
            else if(progress < .5){
                bar.styleProperty().set("-fx-accent: gold");
            }
            else if(progress < .6){
                bar.styleProperty().set("-fx-accent: yellow");
            }
            else if(progress < .7){
                bar.styleProperty().set("-fx-accent: yellowgreen");
            }
            else if(progress < .8){
                bar.styleProperty().set("-fx-accent: greenyellow");
            }
            else if(progress < .9){
                bar.styleProperty().set("-fx-accent: limegreen");
            }
            else if(progress < 1){
                bar.styleProperty().set("-fx-accent: green");
            }
            */
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
                    /*
                    if (progress < .1) {
                        bar.styleProperty().set("-fx-accent: darkred");
                    } else if (progress < .2) {
                        bar.styleProperty().set("-fx-accent: red");
                    } else if (progress < .3) {
                        bar.styleProperty().set("-fx-accent: crimson");
                    } else if (progress < .4) {
                        bar.styleProperty().set("-fx-accent: darkorange");
                    } else if (progress < .5) {
                        bar.styleProperty().set("-fx-accent: gold");
                    } else if (progress < .6) {
                        bar.styleProperty().set("-fx-accent: yellow");
                    } else if (progress < .7) {
                        bar.styleProperty().set("-fx-accent: yellowgreen");
                    } else if (progress < .8) {
                        bar.styleProperty().set("-fx-accent: greenyellow");
                    } else if (progress < .9) {
                        bar.styleProperty().set("-fx-accent: limegreen");
                    } else if (progress < 1) {
                        bar.styleProperty().set("-fx-accent: green");
                    }
                    */
                }
            }
        }
    }

    public static void addSkillPane(int i) throws ExecutionException, InterruptedException, IOException {
       pane = new TitledPane(null, null);

        pane.setMinSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);

        ProgressIndicatorBar myBar = skillBars.get(i);

        System.out.println("Added " + myBar.getSkill());

        pane.setGraphic(myBar);

        Label label1 = new Label(myBar.getSkill() + " Exp Gained: ");

        Label label2 = new Label(myBar.getSkill() + "Exp/Hour: ");

        Label label3 = new Label("Time to Next: ");

        Label labelT = new Label("0");

        Label labels = new Label("0");

        Label label = new Label("0");

        skillLabels.add(label);

        expHourLabels.add(labels);

        timeLables.add(labelT);

        Separator separator = new Separator(Orientation.VERTICAL);

        Separator separator1 = new Separator(Orientation.VERTICAL);

        HBox hbox = new HBox(label1, label, separator, label2, labels, separator1, label3, labelT);

        hbox.setAlignment(Pos.CENTER);

        hbox.setSpacing(10);

        pane.setContent(hbox);

        AN_SkillTracker.getChildren().add(pane);

        Separator separator2 = new Separator(Orientation.HORIZONTAL);

        AN_SkillTracker.getChildren().add(separator2);

        System.out.println("added");

        myBar.prefWidthProperty().bind(pane.widthProperty().subtract(46));
    }
}
