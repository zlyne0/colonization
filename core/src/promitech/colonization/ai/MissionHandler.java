package promitech.colonization.ai;

public interface MissionHandler<T extends AbstractMission> {
    void handle(T mission);
}
