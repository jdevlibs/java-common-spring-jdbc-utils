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
package io.github.jdevlibs.spring.jdbc.criteria;

import io.github.jdevlibs.spring.jdbc.enums.ProcedureTypes;
import io.github.jdevlibs.spring.jdbc.enums.SqlTypes;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Supot Saelao
 * @version 1.0
 */
@Data
public class ProcedureParam implements Serializable {

    private Object value;
    private ProcedureTypes type;
    private SqlTypes sqlType;

    public ProcedureParam(Object value) {
        this.value = value;
        this.type = ProcedureTypes.IN;
        this.sqlType = SqlTypes.VARCHAR;
    }

    public ProcedureParam(Object value, SqlTypes sqlType) {
        this.value = value;
        this.type = ProcedureTypes.IN;
        this.sqlType = sqlType;
    }

    public ProcedureParam(Object value, ProcedureTypes type) {
        this.value = value;
        this.type = type;
    }

    public ProcedureParam(Object value, ProcedureTypes type, SqlTypes sqlType) {
        this.value = value;
        this.type = type;
        this.sqlType = sqlType;
    }
}
