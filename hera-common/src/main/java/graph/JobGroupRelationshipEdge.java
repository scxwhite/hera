package graph;

import lombok.Builder;
import lombok.Data;
import org.jgrapht.graph.DefaultEdge;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午12:30 2018/6/21
 * @desc
 */

@Builder
@Data
public class JobGroupRelationshipEdge<T> extends DefaultEdge {

    private T in;

    private T out;

    private T label;
}
