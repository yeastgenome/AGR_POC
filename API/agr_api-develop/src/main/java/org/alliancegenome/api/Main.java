package org.alliancegenome.api;

import org.wildfly.swarm.Swarm;

public class Main {
    public static void main(String[] args) throws Exception {
        new Swarm().start().deploy();
    }
}
