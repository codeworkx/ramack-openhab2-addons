/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.io.hueemulation.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.storage.Storage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openhab.io.hueemulation.internal.dto.HueDataStore;
import org.openhab.io.hueemulation.internal.dto.HueDevice;
import org.openhab.io.hueemulation.internal.dto.HueStatePlug;

import com.google.gson.Gson;

/**
 * Tests for {@link LightItems}.
 *
 * @author David Graeff - Initial contribution
 */
public class LightItemsTest {
    private Gson gson;
    private HueDataStore ds;
    private LightItems lightItems;

    @Mock
    private ItemRegistry itemRegistry;

    @Mock
    Storage<Integer> storage;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(itemRegistry.getItems()).thenReturn(Collections.emptyList());
        gson = new Gson();
        ds = new HueDataStore();
        lightItems = spy(new LightItems(ds));
        lightItems.setItemRegistry(itemRegistry);
        lightItems.setFilterTags(Collections.singleton("Switchable"), Collections.singleton("ColorLighting"),
                Collections.singleton("Lighting"));
        verify(itemRegistry).getItems();
    }

    @Test
    public void loadStorage() throws IOException {
        Map<String, Integer> itemUIDtoHueID = new TreeMap<>();
        itemUIDtoHueID.put("switch1", 12);
        when(storage.getKeys()).thenReturn(itemUIDtoHueID.keySet());
        when(storage.get(eq("switch1"))).thenReturn(itemUIDtoHueID.get("switch1"));
        lightItems.loadMappingFromFile(storage);
    }

    @Test
    public void addSwitchableByCategory() throws IOException {
        SwitchItem item = new SwitchItem("switch1");
        item.setCategory("Light");
        lightItems.added(item);
        HueDevice device = ds.lights.get(lightItems.itemUIDtoHueID.get("switch1"));
        assertThat(device.item, is(item));
        assertThat(device.state, is(instanceOf(HueStatePlug.class)));

    }

    @Test
    public void addSwitchableByTag() throws IOException {
        SwitchItem item = new SwitchItem("switch1");
        item.addTag("Switchable");
        lightItems.added(item);
        HueDevice device = ds.lights.get(lightItems.itemUIDtoHueID.get("switch1"));
        assertThat(device.item, is(item));
        assertThat(device.state, is(instanceOf(HueStatePlug.class)));
    }

    @Test
    public void addGroupSwitchableByTag() throws IOException {
        GroupItem item = new GroupItem("group1", new SwitchItem("switch1"));
        item.addTag("Switchable");
        lightItems.added(item);
        HueDevice device = ds.lights.get(lightItems.itemUIDtoHueID.get("group1"));
        assertThat(device.item, is(item));
        assertThat(device.state, is(instanceOf(HueStatePlug.class)));
    }

    @Test
    public void updateSwitchable() throws IOException {
        SwitchItem item = new SwitchItem("switch1");
        item.setLabel("labelOld");
        item.addTag("Switchable");
        lightItems.added(item);
        Integer hueID = lightItems.itemUIDtoHueID.get("switch1");
        HueDevice device = ds.lights.get(hueID);
        assertThat(device.item, is(item));
        assertThat(device.state, is(instanceOf(HueStatePlug.class)));
        assertThat(device.name, is("labelOld"));

        SwitchItem newitem = new SwitchItem("switch1");
        newitem.setLabel("labelNew");
        newitem.addTag("Switchable");
        lightItems.updated(item, newitem);
        device = ds.lights.get(hueID);
        assertThat(device.item, is(newitem));
        assertThat(device.state, is(instanceOf(HueStatePlug.class)));
        assertThat(device.name, is("labelNew"));

        // Update with an item that has no tags anymore -> should be removed
        SwitchItem newitemWithoutTag = new SwitchItem("switch1");
        newitemWithoutTag.setLabel("labelNew2");
        lightItems.updated(newitem, newitemWithoutTag);

        device = ds.lights.get(hueID);
        assertThat(device, nullValue());
        assertThat(lightItems.itemUIDtoHueID.get("switch1"), nullValue());
    }
}
