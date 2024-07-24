package dev.sashacorp.statemachine.machine.kubernetes;

public record V1Pod(String namespace, String name, String status) {}
