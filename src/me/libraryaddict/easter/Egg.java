package me.libraryaddict.easter;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Item;

public class Egg {
    private ArrayList<String> finders = new ArrayList<String>();
    private Item item;
    private Location loc;

    public Egg(Location eggLoc) {
        loc = eggLoc.clone().add(0.5, 0.1, 0.5);
    }

    public Egg(Location eggLoc, ArrayList<String> finders) {
        this.loc = eggLoc.clone().add(0.5, 0.1, 0.5);
        this.finders = finders;
    }

    public void addFound(String name) {
        finders.add(name);
    }

    public ArrayList<String> getFinders() {
        return finders;
    }

    public Item getItem() {
        return item;
    }

    public Location getLocation() {
        return loc;
    }

    public boolean hasFound(String name) {
        return finders.contains(name);
    }

    public void setItem(Item item) {
        this.item = item;
    }
}
