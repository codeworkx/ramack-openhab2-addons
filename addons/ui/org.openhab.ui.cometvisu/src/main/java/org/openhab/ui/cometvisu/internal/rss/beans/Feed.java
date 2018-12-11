/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.cometvisu.internal.rss.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Feed} is used by the CometVisu rss-plugin
 * 
 * @author Tobias Bräutigam
 */
public class Feed {
    public String feedUrl;
    public String title;
    public String link;
    public String author;
    public String description;
    public String type;
    public List<Entry> entries = new ArrayList<Entry>();
}
