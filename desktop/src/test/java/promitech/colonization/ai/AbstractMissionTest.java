package promitech.colonization.ai;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedList;

import org.junit.Test;

import net.sf.freecol.common.model.ai.AbstractMission;
import net.sf.freecol.common.model.ai.UnitMissionsMapping;

public class AbstractMissionTest {

    class TestMission extends AbstractMission {
        private String name;

        public TestMission(String name) {
        	super(name);
            this.name = name;
        }

        @Override
        public String toString() {
            return "TestMission [name=" + name + "]";
        }

		@Override
		public void blockUnits(UnitMissionsMapping unitMissionsMapping) {
		}

		@Override
		public void unblockUnits(UnitMissionsMapping unitMissionsMapping) {
		}
    }

    @Test
    public void canGetLeafsMissionToExecute() throws Exception {
        // given
/*
     G
     |
A -> B -> C -> D
     |    + -> E
     |
     F   
        
        
*/
        
        TestMission a = new TestMission("A");
        TestMission b = new TestMission("B");
        TestMission c = new TestMission("C");
        TestMission d = new TestMission("D");
        TestMission e = new TestMission("E");
        TestMission f = new TestMission("F");
        TestMission g = new TestMission("G");
        
        a.addDependMission(b);
        b.addDependMission(g);
        b.addDependMission(f);
        b.addDependMission(c);
        c.addDependMission(d);
        c.addDependMission(e);
        
        // when
        LinkedList<AbstractMission> leafMissionToExecute = a.getLeafMissionToExecute();

        // then
        assertThat(leafMissionToExecute).contains(g, f, d, e);
        assertThat(leafMissionToExecute).doesNotContain(a, b, c);
    }

}
