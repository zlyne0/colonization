package net.sf.freecol.common.model.map;

import java.io.IOException;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class GenerationValues {

	private int humidityMin;
	private int humidityMax;
	private int temperatureMin;
	private int temperatureMax;
	private int altitudeMin;
	private int altitudeMax;
	
	public boolean match(int temperature, int humidity) {
		return humidityMin <= humidity && humidity <= humidityMax && temperatureMin <= temperature && temperature <= temperatureMax;
	}
	
	public static class Xml extends XmlNodeParser<GenerationValues> {

		private static final String ALTITUDE_MAX = "altitudeMax";
		private static final String ALTITUDE_MIN = "altitudeMin";
		private static final String TEMPERATURE_MAX = "temperatureMax";
		private static final String TEMPERATURE_MIN = "temperatureMin";
		private static final String HUMIDITY_MAX = "humidityMax";
		private static final String HUMIDITY_MIN = "humidityMin";

		@Override
		public void startElement(XmlNodeAttributes attr) {
			GenerationValues g = new GenerationValues();
			g.humidityMin = attr.getIntAttribute(HUMIDITY_MIN, 0);
			g.humidityMax = attr.getIntAttribute(HUMIDITY_MAX, 0);
			g.temperatureMin = attr.getIntAttribute(TEMPERATURE_MIN, 0);
			g.temperatureMax = attr.getIntAttribute(TEMPERATURE_MAX, 0);
			g.altitudeMin = attr.getIntAttribute(ALTITUDE_MIN, 0);
			g.altitudeMax = attr.getIntAttribute(ALTITUDE_MAX, 0);
			nodeObject = g;
		}

		@Override
		public void startWriteAttr(GenerationValues g, XmlNodeAttributesWriter attr) throws IOException {
			attr.set(HUMIDITY_MIN, g.humidityMin);
			attr.set(HUMIDITY_MAX, g.humidityMax);
			attr.set(TEMPERATURE_MIN, g.temperatureMin);
			attr.set(TEMPERATURE_MAX, g.temperatureMax);
			attr.set(ALTITUDE_MIN, g.altitudeMin);
			attr.set(ALTITUDE_MAX, g.altitudeMax);
		}
		
		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "gen";
		}
		
	}
}
