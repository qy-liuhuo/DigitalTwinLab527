package org.mobinets.dtlab.common.dao;

import lombok.Data;

import java.util.Date;

@Data
public class Ticket {
    private int id;
    private int userId;
    private String ticket;
    private int status;
    private Date expired;
}
