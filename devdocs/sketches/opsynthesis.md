# Op Synthesis

This is a sketch of API boundaries and types for the NB driver API revamp.

```puml
participant "Workload\nModel" as workload
participant "Activity\nType" as adapter
participant "Op\nType" as optype
participant "Op\nSource" as opsource
participant "Standard\nActivity" as activity
participant "Standard\nAction" as action
participant "Native\nDriver" as native
participant "Target\nSystem" as target

activity -> workload: getOpTemplate
activity <- workload: <OpTemplate>

activity -> workload: getOp
workload -> activity

activity -> adapter

activity -> adapter: getOpSource
activate adapter
activity <- adapter: <OpSource>
deactivate adapter

activity -> opsource: getOp

native -> target: execute operation
activate target


native <- target:
deactivate target

```
