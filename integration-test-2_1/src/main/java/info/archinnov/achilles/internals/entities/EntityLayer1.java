/*
 * Copyright (C) 2012-2018 DuyHai DOAN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.archinnov.achilles.internals.entities;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Frozen;
import info.archinnov.achilles.annotations.PartitionKey;
import info.archinnov.achilles.annotations.Table;

@Table(table="layer")
public class EntityLayer1 {
    @PartitionKey
    @Column
    private String layer;

    @Frozen
    @Column
    private Layer2 layer2;

    public EntityLayer1() {
    }

    public EntityLayer1(String layer, Layer2 layer2) {
        this.layer = layer;
        this.layer2 = layer2;
    }

    public String getLayer() {
        return layer;
    }

    public void setLayer(String layer) {
        this.layer = layer;
    }

    public Layer2 getLayer2() {
        return layer2;
    }

    public void setLayer2(Layer2 layer2) {
        this.layer2 = layer2;
    }
}