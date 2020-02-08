package jmri;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the NamedBeanUsageReport Class
 * @author Dave Sand Copyright (C) 2020
 */
public class NamedBeanUsageReportTest {

    @Test
    public void testReport() {
        Block usingBean = InstanceManager.getDefault(BlockManager.class).provideBlock("BlockName");
        Sensor usedBean = InstanceManager.getDefault(SensorManager.class).provideSensor("IT102");
        NamedBeanUsageReport report = new NamedBeanUsageReport(usingBean, usedBean, "Test_Bean_Report");
        Assert.assertNotNull("exists", report);
        Assert.assertEquals("Test_Bean_Report", report.usageKey);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
