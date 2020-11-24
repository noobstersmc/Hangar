package us.jcedeno.hangar.paper.tranciever.guis.creator.objects;

public enum TerrainGeneration {
    VANILLA("UHC"), RUN("UHC-Run");

    String name;

    TerrainGeneration(String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
