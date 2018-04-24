package application.layered_immune_macrophage;

public class CytokineData {
    public double locationX = 0.0; // x location of attack
    public double locationY = 0.0; // y location of attack

    public CytokineData() { }

    public CytokineData(CytokineData other) {
        locationX = other.locationX;
        locationY = other.locationY;
    }
}
