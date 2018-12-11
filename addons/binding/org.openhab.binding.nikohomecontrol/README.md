# Niko Home Control Binding

The Niko Home Control binding integrates with a [Niko Home Control](http://www.nikohomecontrol.be/) system through a Niko Home Control IP-interface.

The binding has been tested with a Niko Home Control IP-interface (550-00508). This IP-interface provides access on the LAN.
The binding does not require a Niko Home Control Gateway (550-00580), but does work with it in the LAN. It will not make a remote connection.
It has also been confirmed to work with the Niko Home Control Connected Controller (550-00003).
The binding does not work for Niko Home Control II.

The binding exposes all actions from the Niko Home Control System that can be triggered from the smartphone/tablet interface, as defined in the Niko Home Control programming software.

Supported action types are switches, dimmers and rollershutters or blinds.
Niko Home Control alarm and notice messages are retrieved and made available in the binding.

## Supported Things

The Niko Home Control Controller is represented as a bridge in the binding.
Connected to a bridge, the Niko Home Control Binding supports on/off actions (e.g. for lights or groups of lights), dimmers, rollershutters or blinds and thermostats.

## Binding Configuration

The bridge representing the Niko Home Control IP-interface needs to be added first in the things file or through Paper UI.
A bridge can be auto-discovered or created manually.
No bridge configuration is required when using auto-discovery. An auto-discovered bridge will have an IP-address parameter automatically filled with the current IP-address of the IP-interface. This IP-address for the discovered bridge will automatically update when the IP-address of the IP-interface changes.

The IP-address and port can be set when manually creating the bridge.

If the IP-address is set on a manually created bridge, no attempt will be made to discover the correct IP-address. You are responsible to force a fixed IP address on the Niko Home Control IP-interface through settings in your DHCP server.

The port is set to 8000 by default and should match the port used by the Niko Home Control IP-interface.

An optional refresh interval will be used to restart the bridge at regular intervals (every 300 minutes by default).
Restarting the bridge at regular times improves the connection stability and avoids loss of connection. It can be turned off completely.

## Discovery

A discovery scan will first discover the Niko Home Control IP-interface in the network as a bridge.
Default parameters will be used.
Note that this may fail to find the correct Niko Home Control IP-interface when there are multiple IP-interfaces in the network, or when traffic to port 10000 on the openHAB server is blocked.

When the Niko Home Control bridge is added as a thing, from the discovery inbox or manually, system information will be read from the Niko Home Control Controller and will be put in the bridge properties, visible through Paper UI.

Subsequently, all defined actions that can be triggered from a smartphone/tablet in the Niko Home Control system will be discovered and put in the inbox.
It is possible to trigger a manual scan for things on the Niko Home Control bridge.

If the Niko Home Control system has locations configured, these will be copied to thing locations and grouped as such in PaperUI.
Locations can subsequently be changed through the thing location parameter in PaperUI.

## Thing Configuration

Besides using PaperUI to manually configure things or adding automatically discovered things through PaperUI, you can add thing definitions in the things file.

The Thing configuration for the **bridge** uses the following syntax:

```
Bridge nikohomecontrol:bridge:<bridgeId> [ addr="<IP-address of IP-interface>", port=<listening port>,
                                           refresh=<Refresh interval> ]
```

`bridgeId` can have any value.

`addr` is the fixed Niko Home Control IP-interface address and is required.
`port` will be the port used to connect and is 8000 by default.
`refresh` is the interval to restart the communication in minutes (300 by default), if 0 or omitted the connection will not restart at regular intervals.

The Thing configuration for **Niko Home Control actions** has the following syntax:

```
Thing nikohomecontrol:<thing type>:<bridgeId>:<thingId> "Label" @ "Location"
                        [ actionId=<Niko Home Control action ID>,
                          step=<dimmer increase/decrease step value> ]
```

or nested in the bridge configuration:

```
<thing type> <thingId> "Label" @ "Location" [ actionId=<Niko Home Control action ID>,
                         step=<dimmer increase/decrease step value> ]
```

The following action thing types are valid for the configuration:

```
onOff, dimmer, blind
```

`thingId` can have any value, but will be set to the same value as the actionId parameter if discovery is used.

`"Label"` is an optional label for the thing.

`@ "Location"` is optional, and represents the location of the Thing. Auto-discovery would have assigned a value automatically.

The `actionId` parameter is the unique ip Interface Object ID (`ipInterfaceObjectId`) as automatically assigned in the Niko Home Control Controller when programming the Niko Home Control system using the Niko Home Control programming software.
It is not directly visible in the Niko Home Control programming or user software, but will be detected and automatically set by openHAB discovery.
For textual configuration, you can manually retrieve it from the content of the .nhcp configuration file created by the programming software.
Open the file with an unzip tool to read it's content.

The `step` parameter is only available for dimmers.
It sets a step value for dimmer increase/decrease actions. The parameter is optional and set to 10 by default.

The Thing configuration for **Niko Home Control thermostats** has the following syntax:

```
Thing nikohomecontrol:thermostat:<bridgeId>:<thingId> "Label" @ "Location"
                        [ thermostatId=<Niko Home Control thermostat ID> ]
```

or nested in the bridge configuration:

```
thermostat <thingId> "Label" @ "Location" [ thermostatId=<Niko Home Control thermostat ID> ]
```

`thingId` can have any value, but will be set to the same value as the thermostatId parameter if discovery is used.

`"Label"` is an optional label for the Thing.

`@ "Location"` is optional, and represents the location of the thing. Auto-discovery would have assigned a value automatically.

The `thermostatId` parameter is the unique ip Interface Object ID as automatically assigned in the Niko Home Control Controller when programming the Niko Home Control system using the Niko Home Control programming software.
It is not directly visible in the Niko Home Control programming or user software, but will be detected and automatically set by openHAB discovery.
For textual configuration, it can be retrieved from the .nhcp configuration file.

The `overruleTime` parameter is used to set the standard overrule duration when you set a new setpoint without providing an overrule duration. The default value is 60 minutes.

## Channels

For thing type `onOff` the supported channel is `switch`.
OnOff command types are supported.

For thing type `dimmer` the supported channel is `brightness`.
OnOff, IncreaseDecrease and Percent command types are supported.
Note that sending an ON command will switch the dimmer to the value stored when last turning the dimmer off, or 100% depending on the configuration in the Niko Home Control Controller.
This can be changed with the Niko Home Control programming software.

For thing type `blind` the supported channel is `rollershutter`. UpDown, StopMove and Percent command types are supported.

For thing type `thermostat` the supported channels are `measured`, `mode`, `setpoint` and `overruletime`.
`measured` gives the current temperature in QuantityType<Temperature>, allowing for different temperature units. This channel is read only.
`mode` can be set and shows the current thermostat mode. Allowed values are 0 (day), 1 (night), 2 (eco), 3 (off), 4 (coll), 5 (prog 1), 6 (prog 2), 7 (prog 3). If mode is set, the `setpoint` temperature will return to its standard value from the mode.
`setpoint` can be set and shows the current thermostat setpoint value in QuantityType<Temperature>. When updating `setpoint`, it will overrule the temperature setpoint defined by the thermostat mode for `overruletime` duration.
`overruletime` is used to set the total duration to apply the setpoint temperature set in the setpoint channel before the thermostat returns to the setting in its mode.

The bridge has two trigger channels `alarm` and `notice`.
It can be used as a trigger to rules. The event message is the alarm or notice text coming from Niko Home Control.

## Limitations

The binding has been tested with a Niko Home Control IP-interface (550-00508) and the Niko Home Control Connected Controller (550-00003).

The binding has been developed for and tested with Niko Home Control I. It does not work with Niko Home Control II, or with Niko Home Control I installations upgraded to Niko Home Control II.

The action events implemented are limited to onOff, dimmer and rollershutter or blinds.
Other actions have not been implemented.
It is not possible to tilt the slats of venetian blinds.

Beyond action and thermostat events, the Niko Home Control communication also supports electricity usage data.
This has not been implemented.

## Example

.things:

```
Bridge nikohomecontrol:bridge:nhc1 [ addr="192.168.0.70", port=8000, refresh=300 ] {
    onOff 1 "LivingRoom" @ "Downstairs" [ actionId=1 ]
    dimmer 2 "TVRoom" [ actionId=2, step=5 ]
    blind 3 [ actionId=3 ]
    thermostat 4 [ thermostatId=0 ]
}

Bridge nikohomecontrol:bridge:nhc2 [ addr="192.168.0.110" ] {
    onOff 11 @ "Upstairs"[ actionId=11 ]
    dimmer 12 [ actionId=12, step=5 ]
    blind 13 [ actionId=13 ]
    thermostat 14 [ thermostatId=10 ]
}
```

.items:

```
Switch LivingRoom       {channel="nikohomecontrol:onOff:nhc1:1:switch"}           # Switch for onOff type action
Dimmer TVRoom           {channel="nikohomecontrol:dimmer:nhc1:2:brightness"}      # Changing brightness dimmer type action
Rollershutter Kitchen   {channel="nikohomecontrol:blind:nhc1:3:rollershutter"}    # Controlling rollershutter or blind type action
Number:Temperature CurTemperature   "[%.1f °F]"  {channel="nikohomecontrol:thermostat:nhc1:4:measured"}   # Getting measured temperature from thermostat in °F, read only
Number ThermostatMode   {channel="nikohomecontrol:thermostat:nhc1:4:mode"}        # Get and set thermostat mode
Number:Temperature SetTemperature   "[%.1f °C]"  {channel="nikohomecontrol:thermostat:nhc1:4:setpoint"}   # Get and set target temperature in °C
Number OverruleDuration {channel="nikohomecontrol:thermostat:nhc1:4:overruletime} # Get and set the overrule time
```

.sitemap:

```
Switch item=LivingRoom
Slider item=TVRoom
Switch item=TVRoom          # allows switching dimmer item off or on (with controller defined behavior)
Rollershutter item=Kitchen
Text item=CurTemperature
Selection item=ThermostatMode mappings="[0="day", 1="night", 2="eco", 3="off", 4="cool", 5="prog 1", 6="prog 2", 7="prog 3"]
Setpoint item=SetTemperature minValue=0 maxValue=30
Slider item=OverruleDuration minValue=0 maxValue=120
```

Example trigger rule:

```
rule "example trigger rule"
when
    Channel 'nikohomecontrol:bridge:nhc1:alarm' triggered or
    Channel 'nikohomecontrol:bridge:nhc1:notice' triggered
then
    var message = receivedEvent.getEvent()
    logInfo("nhcTriggerExample", "Message: {}", message)
    ...
end
```
