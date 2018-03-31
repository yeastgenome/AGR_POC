package org.alliancegenome.indexer.entity.relationship;

import org.alliancegenome.indexer.entity.Neo4jEntity;
import org.alliancegenome.indexer.entity.node.Gene;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@RelationshipEntity(type="ORTHOLOGOUS")
public class Orthologous extends Neo4jEntity {

    @StartNode
    private Gene gene1;
    @EndNode
    private Gene gene2;
    
    private String primaryKey;
    private boolean isBestRevScore;
    private boolean isBestScore;
    private String confidence;
    
}
