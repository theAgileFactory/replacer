# Replacer
The replacer is a tool which is used in BizDock "packaging process".

By default the builds that are created by the deployable components are "neutral" to any environment.
You can find in most of the components an "empty.properties" file which contains, so to speak, some sample properties.
Yet in real-life environments you may have a CI, ST, PRE-PROD, PROD.
At deployment, we "replace" the configuration files templates (which may contains variables like ${a.property.name}) inside the package resulting from the component build.

In the deployable components, in the META-INF, you'll find a `com.agifac.deploy.replacer.resources.properties` file.
This file is listing the "configuration" files to be replaced at deployment.

So for instance building the component "maf-dbmdl" requires:
  * to build it with maven (`mvn clean install`)
  * to prepare a properties file (using the default `empty.properties` which is in https://github.com/theAgileFactory/maf-dbmdl/tree/master/src/main/properties)
  
