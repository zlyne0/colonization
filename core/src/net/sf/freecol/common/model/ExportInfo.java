package net.sf.freecol.common.model;

import java.io.IOException;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class ExportInfo extends ObjectWithId {

	private static final boolean DEFAULT_EXPORT_FLAG = false;
	private static final int DEFAULT_LEVEL = 50;
	
	private int exportLevel = DEFAULT_LEVEL; 
	private boolean export = DEFAULT_EXPORT_FLAG;
	
	public ExportInfo(String goodsTypeId) {
		super(goodsTypeId);
	}

	public boolean isNotDefaultSettings() {
		return exportLevel != DEFAULT_LEVEL || export != DEFAULT_EXPORT_FLAG;
	}
	
	public int getExportLevel() {
		return exportLevel;
	}

	public boolean isExport() {
		return export;
	}

	public void setExportLevel(int exportLevel) {
		this.exportLevel = exportLevel;
	}

	public void setExport(boolean export) {
		this.export = export;
	}

	public String toString() {
		return getId() + " export: " + export + ", exportLevel: " + exportLevel;
	}
	
	public static class Xml extends XmlNodeParser<ExportInfo> {
		private static final String ATTR_EXPORT_LEVEL = "exportLevel";
		private static final String ATTR_EXPORT_FLAG = "export";

		@Override
		public void startElement(XmlNodeAttributes attr) {
			nodeObject = new ExportInfo(attr.getId());
			nodeObject.export = attr.getBooleanAttribute(ATTR_EXPORT_FLAG, DEFAULT_EXPORT_FLAG);
			nodeObject.exportLevel = attr.getIntAttribute(ATTR_EXPORT_LEVEL, DEFAULT_LEVEL);
		}

		@Override
		public void startWriteAttr(ExportInfo info, XmlNodeAttributesWriter attr) throws IOException {
			attr.setId(info);
			attr.set(ATTR_EXPORT_FLAG, info.export, DEFAULT_EXPORT_FLAG);
			attr.set(ATTR_EXPORT_LEVEL, info.exportLevel, DEFAULT_LEVEL);
		}
		
		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "exportInfo";
		}
	}
	
}
