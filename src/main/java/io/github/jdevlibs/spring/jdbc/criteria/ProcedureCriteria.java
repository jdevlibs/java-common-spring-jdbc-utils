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
import java.util.ArrayList;
import java.util.List;

/**
 * @author supot.jdev
 * @version 1.0
 */
@Data
public class ProcedureCriteria implements Serializable {

    private String name;
    private List<ProcedureParam> params;

    public void addParam(ProcedureParam value) {
        getParams().add(value);
    }

    public void addParam(Object value) {
        addParam(value, ProcedureTypes.IN, SqlTypes.OTHER);
    }

    public void addParam(Object value, SqlTypes sqlType) {
        addParam(value, ProcedureTypes.IN, sqlType);
    }

    public void addParam(Object value, ProcedureTypes type) {
        addParam(value, type, SqlTypes.OTHER);
    }

    public void addParam(Object value, ProcedureTypes type, SqlTypes sqlType) {
        if (params == null) {
            params = new ArrayList<>();
        }
        getParams().add(new ProcedureParam(value, type, sqlType));
    }

    public void resetParams() {
        params = new ArrayList<>();
    }
}
