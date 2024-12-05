package org.mobinets.dtlab.common.pojo;

import org.mobinets.dtlab.common.utils.CoordinateConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.io.IOException;
import java.util.List;

@Data
public class Targets {
    private List<Target> targets;

    public static void main(String[] args) throws IOException {
        String test = "{\"targets\": [{\"pos_id\": 1, \"time\": 1733373403.5378683, \"coordinates\": {\"x\": -2195.33544921875, \"y\": -866.8582153320312, \"z\": 4401.435546875}}, {\"pos_id\": 3, \"time\": 1733373403.5378683, \"coordinates\": {\"x\": -218.37648010253906, \"y\": -405.8705749511719, \"z\": 2313.453857421875}}, {\"pos_id\": 4, \"time\": 1733373403.5378683, \"coordinates\": {\"x\": -1518.86181640625, \"y\": -104.92803192138672, \"z\": 3217.211181640625}}]}";
        ObjectMapper mapper = new ObjectMapper();
        Targets targets = mapper.readValue(test, Targets.class);
        for(Target target: targets.getTargets()) {
            target.setCoordinates(CoordinateConverter.convert(target.getCoordinates()));
        }
        System.out.println(targets);
    }
}


