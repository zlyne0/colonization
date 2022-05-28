package net.sf.freecol.common.model.ai.missions;

/**

@startuml
header: example of mission hierarchy

[PioneerMission] as pm
[ColonyWorkerMission] as cwm
[RequestGoodsMission] as gr


[TransportUnitMission] as tm
pm --> gr : can create
gr --> tm

cwm ---> tm

footer: example of mission hierarchy
@enduml
 */
class Doc {
}
