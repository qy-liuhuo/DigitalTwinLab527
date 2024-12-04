package org.mobinets.dtlab.common.enums;

import com.baomidou.mybatisplus.annotation.IEnum;

public enum UserRole implements IEnum<Integer> {
    VISITOR,
    USER,
    ADMIN;

    @Override
    public Integer getValue() {
        return ordinal();
    }
}