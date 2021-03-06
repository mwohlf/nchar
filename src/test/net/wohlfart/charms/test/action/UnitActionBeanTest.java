package net.wohlfart.charms.test.action;

import java.math.BigInteger;

import net.wohlfart.changerequest.ChangeRequestUnitHome;
import net.wohlfart.refdata.entities.ChangeRequestUnit;

import org.jboss.seam.Component;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class UnitActionBeanTest extends AbstractTranslateableActionBase {

    @BeforeMethod
    @Override
    public void begin() {
        super.begin();
        // setup the component under test
        abstractEntityHome = (ChangeRequestUnitHome) Component.getInstance("changeRequestUnitHome");
        Assert.assertNotNull(abstractEntityHome, "abstractEntityHome is null");
    }

    @Test
    public void testSetEntityIdAction() {
        String outcome;

        // ---------------- testing missing required field --------------------

        outcome = abstractEntityHome.setEntityId(null);
        Assert.assertEquals(outcome, "valid", "error setting roleid");

        outcome = abstractEntityHome.persist();
        Assert.assertEquals(outcome, "invalid", "outcome is: " + outcome + " should be invalid because of missing name");

        hibernateSession.clear(); // reset session
        abstractEntityHome.clearInstance();

        // ---------------- testing a random invalid id
        // -------------------------

        outcome = abstractEntityHome.setEntityId("blabla");
        Assert.assertEquals(outcome, "invalid", "error setting roleid to an invalid value is possible");

        outcome = abstractEntityHome.persist();
        Assert.assertEquals(outcome, "invalid", "error setting roleid to an invalid value is possible");

        hibernateSession.clear();
        abstractEntityHome.clearInstance();

        // ---------------- testing random unused id --------------------------

        // id too high wont find anything
        outcome = abstractEntityHome.setEntityId("3000");
        Assert.assertEquals(outcome, "invalid", "error setting userid to an invalid value is possible");

        hibernateSession.clear();
        abstractEntityHome.clearInstance();

        // ---------------- testing id < 0 ------------------------------------

        // negative ids won't find anything
        outcome = abstractEntityHome.setEntityId("-3");
        Assert.assertEquals(outcome, "invalid", "error setting userid to an invalid value is possible");

        hibernateSession.clear();
        abstractEntityHome.clearInstance();

        // ---------------- some long parsing behaviour checks ----------------

        // and now for something completely different:
        Long l;
        try {
            l = new Long("-0");
            assert l == 0;
        } catch (final NumberFormatException e) {
            assert false : "number format exception";
        }

        try {
            l = new Long("+0");
            assert l == 0;
            assert false : "no number format exception";
        } catch (final NumberFormatException e) {
            // expected exception
        }

        // ---------------- testing id == -0 ----------------------------------

        outcome = abstractEntityHome.setEntityId("-0");
        Assert.assertEquals(outcome, "invalid", "error setting userid to an invalid value is possible");

        hibernateSession.clear();
        abstractEntityHome.clearInstance();

        // ---------------- testing id == +0 ----------------------------------

        outcome = abstractEntityHome.setEntityId("+0");
        Assert.assertEquals(outcome, "invalid", "error setting userid to an invalid value is possible");

        hibernateSession.clear();
        abstractEntityHome.clearInstance();

        // ---------------- testing id > Long.MAX_VALUE -----------------------

        BigInteger bigNumber = BigInteger.valueOf(Long.MAX_VALUE);
        BigInteger evenBigger = bigNumber.multiply(BigInteger.valueOf(500L));

        outcome = abstractEntityHome.setEntityId(evenBigger.toString());
        Assert.assertEquals(outcome, "invalid", "setting entity id to Long.MAX_VALUE + x is possible");

        hibernateSession.clear();
        abstractEntityHome.clearInstance();

        // ---------------- testing id > Long.MAX_VALUE and returning
        // entity-----------------------

        bigNumber = BigInteger.valueOf(Long.MAX_VALUE);
        evenBigger = bigNumber.multiply(BigInteger.valueOf(500L));

        outcome = abstractEntityHome.setEntityId(evenBigger.toString());
        Assert.assertEquals(outcome, "invalid", "setting entity id to Long.MAX_VALUE + x is possible");

        // calling the factory method after we got "invalid" for the id
        // should give us a new unsaved entity
        final Object entity1 = abstractEntityHome.getInstance();
        Assert.assertNotNull(entity1, "entity must not be null");
        Assert.assertNull(abstractEntityHome.getId(), "id for new home object should be null but is " + abstractEntityHome.getId());

        hibernateSession.clear();
        abstractEntityHome.clearInstance();

        // ---------------- testing id > Long.MAX_VALUE and returning
        // entity-----------------------

        // this is a call from pages.xml to create an entity
        outcome = abstractEntityHome.setEntityId("");
        Assert.assertEquals(outcome, "valid", "setting entity id to Long.MAX_VALUE + x is possible");

        final Object entity2 = abstractEntityHome.getInstance();
        Assert.assertNotNull(entity2, "entity must not be null");
        Assert.assertNull(abstractEntityHome.getId(), "id for new home object should be null but is " + abstractEntityHome.getId());

        outcome = abstractEntityHome.setEntityId(null);
        Assert.assertEquals(outcome, "valid", "setting entity id to Long.MAX_VALUE + x is possible");

        final Object entity3 = abstractEntityHome.getInstance();
        Assert.assertNotNull(entity3, "entity must not be null");
        Assert.assertNull(abstractEntityHome.getId(), "id for new home object should be null but is " + abstractEntityHome.getId());

        hibernateSession.clear();
        abstractEntityHome.clearInstance();

    }

    @Test
    public void testCreateAction() {
        String outcome;
        // convert to specific class
        final ChangeRequestUnitHome home = (ChangeRequestUnitHome) abstractEntityHome;

        outcome = home.setEntityId(null);
        Assert.assertEquals(outcome, "valid", "error setting entityId to null for creatig a new entity");
        // calling the factory method
        final ChangeRequestUnit entity = home.getInstance();

        // simulate some user entries:
        entity.setDefaultName("unit1");
        entity.setEnabled(true);
        outcome = home.persist();
        Assert.assertEquals(outcome, "persisted", "error setting entityId to null for creatig a new entity");

        final Long id = home.getInstance().getId();
        Assert.assertNotNull(id, "persisted error has null id");

        hibernateSession.clear();
        abstractEntityHome.clearInstance();
        
        // delete the entity
        outcome = home.setEntityId(id + "");
        Assert.assertEquals(outcome, "valid", "error setting entityId to null for finding an entity");
        outcome = home.remove();
        Assert.assertEquals(outcome, "removed", "error removing an entity");

    }

    @Test
    public void testNameCollisionAction() {
        String outcome;
        // convert to specific class
        final ChangeRequestUnitHome home = (ChangeRequestUnitHome) abstractEntityHome;

        outcome = home.setEntityId(null);
        Assert.assertEquals(outcome, "valid", "error setting entityId to null for creatig a new entity");
        final ChangeRequestUnit entity1 = home.getInstance();
        entity1.setDefaultName("entity1");
        outcome = home.persist();
        Assert.assertEquals(outcome, "persisted");

        // simulate a name collision
        outcome = home.setEntityId(null);
        Assert.assertEquals(outcome, "valid", "error setting entityId to null for creatig a new entity");
        ChangeRequestUnit entity2 = home.getInstance();
        entity2.setDefaultName("entity1");
        outcome = home.persist();
        Assert.assertEquals(outcome, "invalid");

        // persist with a better name
        entity2.setDefaultName("entity2");
        outcome = home.persist();
        Assert.assertEquals(outcome, "persisted");

        // simulate collision on rename
        outcome = home.setEntityId(entity2.getId().toString());
        Assert.assertEquals(outcome, "valid", "error setting entityId to null for creatig a new entity");
        entity2 = home.getInstance();
        Assert.assertEquals("entity2", entity2.getDefaultName());
        entity2.setDefaultName("entity1");
        outcome = home.persist();
        Assert.assertEquals(outcome, "invalid");

        // persist with a better name
        entity2.setDefaultName("entity3");
        outcome = home.persist();
        Assert.assertEquals(outcome, "persisted");

        // remove the entities:
        outcome = home.setEntityId(entity1.getId().toString());
        Assert.assertEquals(outcome, "valid", "error setting entityId to lookup an entity");
        outcome = home.remove();
        Assert.assertEquals(outcome, "removed", "error removing an entity");      
        outcome = home.setEntityId(entity2.getId().toString());
        Assert.assertEquals(outcome, "valid", "error setting entityId to lookup an entity");
        outcome = home.remove();
        Assert.assertEquals(outcome, "removed", "error removing an entity");

    }

}
