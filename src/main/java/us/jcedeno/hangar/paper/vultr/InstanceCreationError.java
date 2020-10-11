package us.jcedeno.hangar.paper.vultr;

import lombok.Data;

@Data
public class InstanceCreationError implements InstanceResult{
    String error;
    int status;
}
