package net.sf.freecol.common.model.ai.missions;

class TestingMission extends AbstractMission {
    private String name;

    public TestingMission(String name) {
    	super(name);
        this.name = name;
    }

    @Override
    public String toString() {
    	return "TestMission [name=" + name + "]";
    }

    @Override
    public void blockUnits(UnitMissionsMapping unitMissionsMapping) {
    }

    @Override
    public void unblockUnits(UnitMissionsMapping unitMissionsMapping) {
    }
}
