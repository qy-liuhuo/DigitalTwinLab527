package org.mobinets.dtlab.common.enums;

import com.baomidou.mybatisplus.annotation.IEnum;

public enum UserStatus implements IEnum<Integer> {
    INACTIVE,
    ACTIVE,
    DELETED;

    @Override
    public Integer getValue() {
        return ordinal();
    }
}
