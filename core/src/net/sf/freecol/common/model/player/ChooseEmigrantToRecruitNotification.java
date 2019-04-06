package net.sf.freecol.common.model.player;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class ChooseEmigrantToRecruitNotification implements Notification {

	public static class Xml extends XmlNodeParser<ChooseEmigrantToRecruitNotification> {

		@Override
		public void startElement(XmlNodeAttributes attr) {
			nodeObject = new ChooseEmigrantToRecruitNotification();
		}

		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "chooseEmigrantToRecruitNotification";
		}
		
	}
	
}
