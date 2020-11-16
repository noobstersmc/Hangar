package us.jcedeno.hangar.paper.condor;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor(staticName = "of")
@Data
public class CondorGame {
    String host;
    String displayname;
    String game_type;
    Map<Object, Object> instance_type;
    Map<Object, Object> extra_data;
}
