package org.mobinets.dtlab.common.pojo;

import lombok.Data;

@Data
public class Target{
    private int pos_id;
    private double time;
    private Coordinates coordinates;
}
