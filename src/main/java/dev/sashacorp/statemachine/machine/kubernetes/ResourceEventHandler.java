package dev.sashacorp.statemachine.machine.kubernetes;

public interface ResourceEventHandler {
  void onAdd(V1Pod pod);

  void onUpdate(V1Pod oldPod, V1Pod newPod);

  void onDelete(V1Pod pod, boolean deletedFinalStateUnknown);
}
