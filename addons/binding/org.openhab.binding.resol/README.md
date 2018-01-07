# Resol Binding

Resol Binding connects to Solar and System Controllers of RESOL - Elektronische Regelungen GmbH, also including branded versions from Viessmann, SOLEX, COSMO, SOLTEX, DeDietrich and many more.

## Supported Things

VBusLAN-Bridge, DataLogger DL2 and DL3 as interface between LAN and Resol VBus
Many Resol Controllers and Modules like WMZ heat meters, HKM Heating circuit extensions etc.

## Discovery

Discovery is supported for VBus-LAN adapters , but it should also work for data loggers providing the same IP interface as the VBusLAN-Adapter. 

## Binding Configuration

_If your binding requires or supports general configuration settings, please create a folder ```cfg``` and place the configuration file ```<bindingId>.cfg``` inside it. In this section, you should link to this file and provide some information about the options. The file could e.g. look like:_

```
# Configuration for the Philips Hue Binding
#
# Default secret key for the pairing of the Philips Hue Bridge.
# It has to be between 10-40 (alphanumeric) characters 
# This may be changed by the user for security reasons.
secret=EclipseSmartHome
```

_Note that it is planned to generate some part of this based on the information that is available within ```ESH-INF/binding``` of your binding._

_If your binding does not offer any generic configurations, you can remove this section completely._

## Thing Configuration

Password for the VBusLAN needs to be configured!
_Describe what is needed to manually configure a thing, either through the (Paper) UI or via a thing-file. This should be mainly about its mandatory and optional configuration parameters. A short example entry for a thing file can help!_

_Note that it is planned to generate some part of this based on the XML files within ```ESH-INF/thing``` of your binding._

## Channels

Channels are dynamically created dependent on the devices connected to the VBus. So far only reading is supported.

## Full Example

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._

## TODO
see tickets
- cleanup unit, unitfamily and type handling for channels
- check timezones of date and time fields
- stop interpretation / close TCP connection to VBus adapter on dispose of the BridgeHandler
  * check if on thing removal (dispose) the BridgeHandler does not try to update it
- check whether updates shall only be called on data change
- cleanup this README

- check whether there is a useful way to support/utilize events

- test with multiple VBus bridges
- remove adapterSerial from Bridge configuration parameters?

- Add NOTICE file https://www.eclipse.org/projects/handbook/#legaldoc
