/*
 * Copyright 2015 BlackBerry Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blackberry.bdp.common.versioned;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HashedList<K, V> {

	private final List<V> list = new ArrayList<>();
	private final Map<K, V> map = new HashMap<>();
	
	public void add(K key, V value) {
		list.add(value);
		map.put(key, value);
	}

	public V get(int index) {
		return list.get(index);
	}

	public V get(K key) {
		return map.get(key);
	}

}
