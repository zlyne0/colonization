package net.sf.freecol.common.model.ai.missions;

class MissionB extends AbstractMission {
    private String name;

    public MissionB(String name) {
        super(name);
        this.name = name;
    }

    @Override
    public String toString() {
        return "BMission [name=" + name + "]";
    }

    @Override
    public void blockUnits(UnitMissionsMapping unitMissionsMapping) {

    }

    @Override
    public void unblockUnits(UnitMissionsMapping unitMissionsMapping) {

    }
}