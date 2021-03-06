package com.sap.cloud.lm.sl.cf.core.cf.v3;

import com.sap.cloud.lm.sl.mta.model.DeploymentDescriptor;
import com.sap.cloud.lm.sl.mta.model.Resource;

public class ServicesCloudModelBuilder extends com.sap.cloud.lm.sl.cf.core.cf.v2.ServicesCloudModelBuilder {

    public ServicesCloudModelBuilder(DeploymentDescriptor deploymentDescriptor) {
        super(deploymentDescriptor);
    }

    @Override
    protected boolean isOptional(Resource resource) {
        return resource.isOptional();
    }

}
