package net.sf.freecol.common.model.ai;

public interface MapTileDebugInfo {

	void str(int x, int y, String str);	

	void strIfNull(int x, int y, String str);

    void appendStr(int x, int y, String str);
}
