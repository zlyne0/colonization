package net.sf.freecol.common.model.ai.missions.workerrequest;

/**

@startuml

actor "debugConsole" as debugConsole
usecase "GUIGameController.endTurn" as endTurn

package "ai" {
    usecase "EuropeanMissionPlaner" as europeanMissionPlaner
    usecase "MissionExecutor" as missionExecutor

    endTurn --> europeanMissionPlaner : 1. invoke
    endTurn --> missionExecutor : 2. invoke
}

package "ColonyWorkerRequest" {
    usecase "ColonyWorkerRequestPlaner" as planer
    usecase "ColonyWorkerMission" as mission
    usecase "ColonyWorkerMissionHandler" as handler

    usecase "ColonyPlaceGenerator" as ColonyPlaceGenerator

    planer --> mission : create
    planer -> ColonyPlaceGenerator : use
    handler --> mission : handle

    europeanMissionPlaner --> planer : execute
    missionExecutor --> handler : execute

    debugConsole ---> planer : generateWorkerReqScore\n (print on map)
}

@enduml
 */
class Doc {
}
