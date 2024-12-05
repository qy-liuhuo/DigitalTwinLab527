package org.mobinets.dtlab.common.utils;

import org.mobinets.dtlab.common.pojo.Coordinates;

public class CoordinateConverter {
    private static final double camera_x = 5640;
    private static final double camera_y = 1050;
    private static final double camera_z = 2150;
    private static final double camera_angle = 30;

    private static final double room_x = 12230;

    private static final double room_y = 8100;

    private static final double room_z = 3200;

    public static Coordinates convert(Coordinates coordinates) {
        Coordinates newCoordinates = new Coordinates();
        newCoordinates.setZ(0);
        newCoordinates.setX(camera_x + coordinates.getX());
        newCoordinates.setY(coordinates.getZ()/Math.cos(camera_angle) + camera_y);
        return newCoordinates;
    }
}
