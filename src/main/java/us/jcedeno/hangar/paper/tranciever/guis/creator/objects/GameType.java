package us.jcedeno.hangar.paper.tranciever.guis.creator.objects;

public enum GameType {
    UHC("UHC", TerrainGeneration.VANILLA), RUN("UHC-RUN", TerrainGeneration.RUN),
    MEETUP("UHC-Meetup", TerrainGeneration.VANILLA);

    String name;
    TerrainGeneration defaulTerrainGeneration;

    GameType(String name, TerrainGeneration defaulTerrainGeneration) {
        this.name = name;
        this.defaulTerrainGeneration = defaulTerrainGeneration;
    }

    public TerrainGeneration getDefaulTerrainGeneration() {
        return defaulTerrainGeneration;
    }

    public GameCreator getDefaulGameCreator(){
        return GameCreator.of(this, this.getDefaulTerrainGeneration());
    }

    @Override
    public String toString() {
        return name;
    }
}
