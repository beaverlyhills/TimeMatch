package com.dkhromov.timematch;

import java.util.Collection;
import java.util.LinkedHashMap;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.content.res.XmlResourceParser;

public class TimeZoneCatalog {

	public static class Item {
		String id;
		String displayName;
		
		@Override
		public String toString() {
			return displayName;
		}
	}
	
	LinkedHashMap<String, Item> items = new LinkedHashMap<String, Item>();
	
	public TimeZoneCatalog(Activity context) {
		
		XmlResourceParser parser = context.getResources().getXml(R.xml.timezones);
		
		try {
			Item current = null;
			int event = parser.getEventType();
			while (event != XmlResourceParser.END_DOCUMENT) {
				if ( event == XmlResourceParser.START_TAG ) {
					if ( parser.getName().equalsIgnoreCase("timezone") ) {
						current = new Item();
						current.id = parser.getAttributeValue(null, "id");
					}
				} else if ( event == XmlResourceParser.END_TAG ) {
					items.put(current.id, current);
					current = null;
				} else if ( event == XmlPullParser.TEXT ) {
					if ( current != null ) {
						current.displayName = parser.getText();
					}
				}
				event = parser.next();
			}
		} catch (Exception e) {}
		
	}

	public Collection<Item> getList() {
		return items.values();
	}

	public Item get(String id) {
		return items.get(id);
	}
}
