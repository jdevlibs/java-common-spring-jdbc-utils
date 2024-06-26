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

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author supot.jdev
 * @version 1.0
 */
@Data
public class Paging<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<T> items;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private boolean first;
    private Criteria criteria;

    public void calculateTotalPage() {
        if (totalElements == 0 || criteria == null) {
            first = true;
            totalPages = 0;
            return;
        }

        if (criteria.getSize() != null && criteria.getPage() != null) {
            totalPages = (int) Math.ceil((double) this.totalElements / (double) criteria.getSize());
            first = (criteria.getPage() == 1);
            last = (criteria.getPage() >= totalPages);
        }
    }

    public void addItem(T item) {
        if (item == null) {
            return;
        }
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
    }
}
