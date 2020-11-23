package us.jcedeno.hangar.paper.tranciever.guis.creator.objects;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "of")
public class TransferRequest {
    String player_name;
    String ip;
    String game_id;
}
