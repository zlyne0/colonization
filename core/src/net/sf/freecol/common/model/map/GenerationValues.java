package net.sf.freecol.common.model.map;

import net.sf.freecol.common.model.Identifiable;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class GenerationValues implements Identifiable {

	private int humidityMin;
	private int humidityMax;
	private int temperatureMin;
	private int temperatureMax;
	private int altitudeMin;
	private int altitudeMax;
	
	@Override
	public String getId() {
		throw new IllegalStateException("GenerationValues has no id");
	}

	public boolean match(int temperature, int humidity) {
		return humidityMin <= humidity && humidity <= humidityMax && temperatureMin <= temperature && temperature <= temperatureMax;
	}
	
	public static class Xml extends XmlNodeParser {

		@Override
		public void startElement(XmlNodeAttributes attr) {
			GenerationValues g = new GenerationValues();
			g.humidityMin = attr.getIntAttribute("humidityMin", 0);
			g.humidityMax = attr.getIntAttribute("humidityMax", 0);
			g.temperatureMin = attr.getIntAttribute("temperatureMin", 0);
			g.temperatureMax = attr.getIntAttribute("temperatureMax", 0);
			g.altitudeMin = attr.getIntAttribute("altitudeMin", 0);
			g.altitudeMax = attr.getIntAttribute("altitudeMax", 0);
			nodeObject = g;
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
