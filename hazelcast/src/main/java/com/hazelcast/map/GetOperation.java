/*
 * Copyright (c) 2008-2012, Hazel Bilisim Ltd. All Rights Reserved.
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

package com.hazelcast.map;

import com.hazelcast.impl.DefaultRecord;
import com.hazelcast.impl.Record;
import com.hazelcast.nio.Data;
import com.hazelcast.spi.impl.AbstractNamedKeyBasedOperation;

import static com.hazelcast.nio.IOUtil.toData;
import static com.hazelcast.nio.IOUtil.toObject;

public class GetOperation extends AbstractNamedKeyBasedOperation {

    public GetOperation(String name, Data dataKey) {
        super(name, dataKey);
    }

    public GetOperation() {
    }

    public void run() {
        MapService mapService = (MapService) getService();
        MapPartition mapPartition = mapService.getMapPartition(getPartitionId(), name);
        Record record = mapPartition.records.get(dataKey);
        Data result = null;
        if (record == null) {
            if (mapPartition.loader != null) {
                Object key = toObject(dataKey);
                Object value = mapPartition.loader.load(key);
                if (value != null) {
                    result = toData(value);
                    record = new DefaultRecord(getPartitionId(), dataKey, result, -1, -1, mapService.nextId());
                    mapPartition.records.put(dataKey, record);
                }
            }
        } else {
            result = record.getValueData();
        }
        getResponseHandler().sendResponse(result);
    }

    @Override
    public String toString() {
        return "GetOperation{" +
               '}';
    }
}