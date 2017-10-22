package net.sf.freecol.common.model.ai.missions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import net.sf.freecol.common.model.ai.missions.AbstractMission;

class TestingAbstractMission extends AbstractMission {
    private String name;

    public TestingAbstractMission(String name) {
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

public class AbstractMissionTest {

	class TestMission extends TestingAbstractMission {
		public TestMission(String name) {
			super(name);
		}
	}
	class MissionA extends TestingAbstractMission {
		public MissionA(String name) {
			super(name);
		}
	}
	class MissionB extends TestingAbstractMission {
		public MissionB(String name) {
			super(name);
		}
	}

	TestMission a = new TestMission("A");
	TestMission b = new TestMission("B");
	TestMission c = new TestMission("C");
	TestMission d = new TestMission("D");
	TestMission e = new TestMission("E");
	TestMission f = new TestMission("F");
	TestMission g = new TestMission("G");
	
	
	@Before
	public void setup() {
		/*
     G
     |
A -> B -> C -> D
     |    + -> E
     |
     F   
        
        
		 */
		
		a.addDependMission(b);
		b.addDependMission(g);
		b.addDependMission(f);
		b.addDependMission(c);
		c.addDependMission(d);
		c.addDependMission(e);
	}
	
    @Test
    public void canGetLeafsMissionToExecute() throws Exception {
        // given
        
        // when
        LinkedList<AbstractMission> leafMissionToExecute = a.getLeafMissionToExecute();

        // then
        assertThat(leafMissionToExecute).contains(g, f, d, e);
        assertThat(leafMissionToExecute).doesNotContain(a, b, c);
    }

    @Test
	public void canDetermineThatMissionHasChild() throws Exception {
		
		// given
    	MissionB e2 = new MissionB("E2");
    	c.addDependMission(e2);
    	
		// when
    	boolean hasDependMissionsType = a.hasDependMissionsType(MissionB.class);
    	
		// then
    	assertThat(hasDependMissionsType).isTrue();
	}
}
