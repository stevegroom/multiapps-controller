package com.sap.cloud.lm.sl.cf.process.steps;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.sap.cloud.lm.sl.cf.core.helpers.SystemParameters;
import com.sap.cloud.lm.sl.cf.core.model.SupportedParameters;
import com.sap.cloud.lm.sl.cf.process.Constants;
import com.sap.cloud.lm.sl.mta.model.DeploymentDescriptor;
import com.sap.cloud.lm.sl.mta.model.Module;

public class CollectBlueGreenSystemParametersStepTest extends CollectSystemParametersStepBaseTest {

    @Before
    public void setNoConfirm() {
        context.setVariable(Constants.PARAM_NO_CONFIRM, false);
    }

    @Test
    public void testIdleGeneralParameters() {
        prepareDescriptor("system-parameters/mtad.yaml");
        prepareClient();

        step.execute(context);

        DeploymentDescriptor descriptor = StepsUtil.getDeploymentDescriptorWithSystemParameters(context);
        Map<String, Object> generalParameters = descriptor.getParameters();
        assertEquals(DEFAULT_DOMAIN, generalParameters.get(SupportedParameters.DEFAULT_IDLE_DOMAIN));
        assertEquals(DEFAULT_DOMAIN, generalParameters.get(SupportedParameters.DEFAULT_DOMAIN));
    }

    @Test
    public void testIdleHostBasedModuleParameters() {
        prepareDescriptor("system-parameters/mtad.yaml");
        prepareClient();

        step.execute(context);

        DeploymentDescriptor descriptor = StepsUtil.getDeploymentDescriptorWithSystemParameters(context);
        List<Module> modules = descriptor.getModules();
        assertEquals(2, modules.size());
        for (Module module : modules) {
            validateIdleHostBasedModuleParameters(module);
        }
    }

    private void validateIdleHostBasedModuleParameters(Module module) {
        Map<String, Object> parameters = module.getParameters();
        String expectedIdleHost = String.format("%s-%s-%s-idle", ORG, SPACE, module.getName());
        assertEquals(DEFAULT_DOMAIN, parameters.get(SupportedParameters.IDLE_DOMAIN));
        assertEquals(expectedIdleHost, parameters.get(SupportedParameters.DEFAULT_IDLE_HOST));
        assertEquals(expectedIdleHost, parameters.get(SupportedParameters.IDLE_HOST));
        assertEquals(expectedIdleHost, parameters.get(SupportedParameters.DEFAULT_HOST));
        assertEquals(expectedIdleHost, parameters.get(SupportedParameters.HOST));
        assertEquals(SystemParameters.DEFAULT_HOST_BASED_IDLE_URI, parameters.get(SupportedParameters.DEFAULT_IDLE_URI));
        assertEquals(SystemParameters.DEFAULT_HOST_BASED_IDLE_URI, parameters.get(SupportedParameters.DEFAULT_URI));
        assertEquals(SystemParameters.DEFAULT_IDLE_URL, parameters.get(SupportedParameters.DEFAULT_IDLE_URL));
        assertEquals(SystemParameters.DEFAULT_IDLE_URL, parameters.get(SupportedParameters.DEFAULT_URL));
    }

    @Override
    protected CollectBlueGreenSystemParametersStep createStep() {
        return new CollectBlueGreenSystemParametersStep();
    }

}
