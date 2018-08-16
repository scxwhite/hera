package graph;

import lombok.Data;

@Data
public class JobRelation {

    private String id;

    private String name;

    private String dependencies;

    private String pid;

    private String pname;


}
