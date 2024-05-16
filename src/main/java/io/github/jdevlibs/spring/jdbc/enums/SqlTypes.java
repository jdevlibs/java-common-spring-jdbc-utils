/*  ---------------------------------------------------------------------------
 *  * Copyright 2020-2021 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      https://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  ---------------------------------------------------------------------------
 */
package io.github.jdevlibs.spring.jdbc.enums;

import java.sql.Types;

/**
 * @author Supot Saelao
 * @version 1.0
 */
public enum SqlTypes {
    VARCHAR(Types.VARCHAR),
    CHAR(Types.CHAR),
    NUMERIC(Types.NUMERIC),
    DECIMAL(Types.DECIMAL),
    INTEGER(Types.INTEGER),
    DATE(Types.DATE),
    TIMESTAMP(Types.TIMESTAMP),
    BLOB(Types.BLOB),
    CLOB(Types.CLOB),
    NULL(Types.NULL),
    OTHER(Types.OTHER),
    CURSOR(-10);

    private final int value;

    SqlTypes(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}