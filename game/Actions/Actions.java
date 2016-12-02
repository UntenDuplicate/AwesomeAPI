package com.AwesomeAPI.game.Actions;

import com.runemate.game.api.hybrid.entities.Player;
import com.runemate.game.api.hybrid.location.Coordinate;
import com.runemate.game.api.hybrid.location.navigation.Path;
import com.runemate.game.api.hybrid.location.navigation.basic.BresenhamPath;
import com.runemate.game.api.hybrid.location.navigation.cognizant.RegionPath;

import java.util.List;
import java.util.function.Predicate;

/**
 * Created by Matthew on 11/17/2016.
 */
public class Actions {

    private static Predicate<Coordinate> notReachable = coordinate -> !coordinate.isReachable();

    public static boolean isAnimating(Player me){
        return me != null && me.getAnimationId() != -1;
    }

    public static boolean walkToSpot(Coordinate spot){
        Path path = null;
        if(spot != null) {
            path = RegionPath.buildTo(spot);
        }
        if(path != null)
        return path.step();
        else
            return false;
    }

    public static boolean walkToSpotB(Coordinate spot){
        Path path = null;
        if(spot != null) {
            path = BresenhamPath.buildTo(spot);
        }
        if(path != null)
            return path.step();
        else
            return false;
    }


    public static Coordinate getReachable(List<Coordinate> surroundingCoordinates) {
        surroundingCoordinates.removeIf(notReachable);
        if(!surroundingCoordinates.isEmpty())
        return surroundingCoordinates.get(0);
        else{
            return surroundingCoordinates.get(0);
        }
    }
}
