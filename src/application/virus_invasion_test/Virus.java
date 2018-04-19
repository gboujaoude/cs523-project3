package application.virus_invasion_test;

import engine.Actor;
import engine.Circle2D;

import java.util.HashSet;
import java.util.Random;

public class Virus extends Circle2D {
    private boolean _invaded = false;

    public Virus(double locationX, double locationY) {
        super(locationX, locationY, 5, 5, 1);
        Random rng = new Random();
        setSpeedXY(rng.nextDouble() * 100, rng.nextDouble() * 100);
        //addToWorld();
    }

    // Makes this virus a copy of another virus
    public Virus(Virus other) {
        super(other.getLocationX(), other.getLocationY(), other.getWidth(), other.getHeight(), other.getDepth());
        Random rng = new Random();
        setSpeedXY(rng.nextDouble() * 100, rng.nextDouble() * 100);
        //addToWorld();
    }

    @Override
    public void onActorOverlapped(Actor self, HashSet<Actor> collidedWith) {
        if (_invaded) return;
        for (Actor actor : collidedWith) {
            if (actor instanceof SittingDuckCell) {
                SittingDuckCell cell = (SittingDuckCell)actor;
                if (cell.infected()) continue;
                cell.infect(this);
                _invaded = true;
                break;
            }
        }
    }
}
