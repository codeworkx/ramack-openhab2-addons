/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.km200.internal.handler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.km200.internal.KM200Device;
import org.openhab.binding.km200.internal.KM200ServiceObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The KM200SwitchProgramService representing a switch program service with its all capabilities
 *
 * @author Markus Eckhardt - Initial contribution
 */
public class KM200SwitchProgramServiceHandler {
    private final Logger logger = LoggerFactory.getLogger(KM200SwitchProgramServiceHandler.class);

    private int maxNbOfSwitchPoints = 8;
    private int maxNbOfSwitchPointsPerDay = 8;
    private int switchPointTimeRaster = 10;
    private String setpointProperty;
    private String positiveSwitch;
    private String negativeSwitch;

    protected final Integer MIN_TIME = 0;
    protected final Integer MAX_TIME = 1430;
    protected final String TYPE_MONDAY = "Mo";
    protected final String TYPE_TUESDAY = "Tu";
    protected final String TYPE_WEDNESDAY = "We";
    protected final String TYPE_THURSDAY = "Th";
    protected final String TYPE_FRIDAY = "Fr";
    protected final String TYPE_SATURDAY = "Sa";
    protected final String TYPE_SUNDAY = "Su";

    private String activeDay = TYPE_MONDAY;
    private Integer activeCycle = 1;

    /* Night- and daylist for all weekdays */
    public Map<String, HashMap<String, ArrayList<Integer>>> switchMap;

    /* List with all days */
    private List<String> days;
    public static List<StateOption> daysList;
    /* List with setpoints */
    ArrayList<String> setpoints;

    public KM200SwitchProgramServiceHandler() {
        switchMap = new HashMap<String, HashMap<String, ArrayList<Integer>>>();
        days = new ArrayList<String>();
        days.add(TYPE_MONDAY);
        days.add(TYPE_TUESDAY);
        days.add(TYPE_WEDNESDAY);
        days.add(TYPE_THURSDAY);
        days.add(TYPE_FRIDAY);
        days.add(TYPE_SATURDAY);
        days.add(TYPE_SUNDAY);
        daysList = new ArrayList<StateOption>();
        daysList.add(new StateOption(TYPE_MONDAY, "Monday"));
        daysList.add(new StateOption(TYPE_TUESDAY, "Tuesday"));
        daysList.add(new StateOption(TYPE_WEDNESDAY, "Wednesday"));
        daysList.add(new StateOption(TYPE_THURSDAY, "Thursday"));
        daysList.add(new StateOption(TYPE_FRIDAY, "Friday"));
        daysList.add(new StateOption(TYPE_SATURDAY, "Saturday"));
        daysList.add(new StateOption(TYPE_SUNDAY, "Sunday"));
        setpoints = new ArrayList<String>();
    }

    /**
     * This function inits the week list
     */
    void initWeeklist(String setpoint) {
        HashMap<String, ArrayList<Integer>> weekMap = switchMap.get(setpoint);
        if (weekMap == null) {
            weekMap = new HashMap<String, ArrayList<Integer>>();
            for (String day : days) {
                weekMap.put(day, new ArrayList<Integer>());
            }
            switchMap.put(setpoint, weekMap);
        }
    }

    /**
     * This function adds a switch to the switchmap
     */
    void addSwitch(String day, String setpoint, int time) {
        logger.debug("Adding day: {} setpoint: {} time: {}", day, setpoint, time);
        if (!days.contains(day)) {
            logger.error("This type of weekday is not supported, get day: {}", day);
            throw new IllegalArgumentException("This type of weekday is not supported, get day: " + day);
        }
        if (!setpoints.contains(setpoint)) {
            if (setpoints.size() == 2 && "on".compareTo(setpoint) == 0) {
                if ("high".compareTo(setpoints.get(0)) == 0 && "off".compareTo(setpoints.get(1)) == 0) {
                    if ("on".compareTo(positiveSwitch) == 0 && "off".compareTo(negativeSwitch) == 0) {
                        logger.info(
                                "!!! Wrong configuration on device. 'on' instead of 'high' in switch program. It seems that's a firmware problem-> ignoring it !!!");
                    } else {
                        throw new IllegalArgumentException(
                                "This type of setpoint is not supported, get setpoint: " + setpoint);
                    }
                }
            }
        }
        HashMap<String, ArrayList<Integer>> weekMap = switchMap.get(setpoint);
        if (weekMap == null) {
            initWeeklist(setpoint);
            weekMap = switchMap.get(setpoint);
        }
        ArrayList<Integer> dayList = weekMap.get(day);
        dayList.add(time);
        Collections.sort(dayList);
    }

    /**
     * This function removes all switches from the switchmap
     *
     */
    void removeAllSwitches() {
        if (switchMap != null) {
            switchMap.clear();
        }
    }

    public void setMaxNbOfSwitchPoints(Integer nbr) {
        if (nbr != null) {
            maxNbOfSwitchPoints = nbr;
        }
    }

    public void setMaxNbOfSwitchPointsPerDay(Integer nbr) {
        if (nbr != null) {
            maxNbOfSwitchPointsPerDay = nbr;
        }
    }

    public void setSwitchPointTimeRaster(Integer raster) {
        if (raster != null) {
            switchPointTimeRaster = raster;
        }
    }

    public void setSetpointProperty(String property) {
        setpointProperty = property;
    }

    /**
     * This function sets the day
     */
    public void setActiveDay(String day) {
        if (!days.contains(day)) {
            logger.error("This type of weekday is not supported, get day: {}", day);
            throw new IllegalArgumentException("This type of weekday is not supported, get day: " + day);
        }
        activeDay = day;
    }

    /**
     * This function sets the cycle
     */
    public void setActiveCycle(Integer cycle) {
        if (cycle > this.getMaxNbOfSwitchPoints() / 2 || cycle > this.getMaxNbOfSwitchPointsPerDay() / 2 || cycle < 1) {
            logger.error("The value of cycle is not valid, get cycle: {}", cycle);
            throw new IllegalArgumentException("The value of cycle is not valid, get cycle: " + cycle.toString());
        }
        /* limit the cycle to the next one after last (for creating a new one) */
        if (cycle > (getNbrCycles() + 1) || getNbrCycles() == 0) {
            activeCycle = getNbrCycles() + 1;
        } else {
            activeCycle = cycle;
        }
    }

    /**
     * This function sets the positive switch to the selected day and cycle
     */
    public void setActivePositiveSwitch(Integer time) {
        Integer actTime;
        if (time < MIN_TIME) {
            actTime = MIN_TIME;
        } else if (time > MAX_TIME) {
            actTime = MAX_TIME;
        } else {
            actTime = time;
        }
        synchronized (switchMap) {
            HashMap<String, ArrayList<Integer>> week = switchMap.get(getPositiveSwitch());
            if (week != null) {
                ArrayList<Integer> daysList = week.get(getActiveDay());
                if (daysList != null) {
                    Integer actC = getActiveCycle();
                    Integer nbrC = getNbrCycles();
                    Integer nSwitch = null;
                    boolean newS = false;
                    if (nbrC < actC) {
                        /* new Switch */
                        newS = true;
                    }
                    if (switchMap.get(getNegativeSwitch()).get(getActiveDay()).size() < actC) {
                        nSwitch = 0;
                    } else {
                        nSwitch = switchMap.get(getNegativeSwitch()).get(getActiveDay()).get(actC - 1);
                    }
                    /* The positiv switch cannot be higher then the negative */
                    if (actTime > (nSwitch - getSwitchPointTimeRaster()) && nSwitch > 0) {
                        actTime = nSwitch;
                        if (nSwitch < MAX_TIME) {
                            actTime -= getSwitchPointTimeRaster();
                        }
                    }
                    /* Check whether the time would overlap with the previous one */
                    if (actC > 1) {
                        Integer nPrevSwitch = switchMap.get(getNegativeSwitch()).get(getActiveDay()).get(actC - 2);
                        /* The positiv switch cannot be lower then the previous negative */
                        if (actTime < (nPrevSwitch + getSwitchPointTimeRaster())) {
                            actTime = nPrevSwitch + getSwitchPointTimeRaster();
                        }
                    }
                    if (newS) {
                        daysList.add(actTime);
                    } else {
                        daysList.set(actC - 1, actTime);
                    }
                    checkRemovement();
                }
            }
        }

    }

    /**
     * This function sets the negative switch to the selected day and cycle
     */
    public void setActiveNegativeSwitch(Integer time) {
        Integer actTime;
        if (time < MIN_TIME) {
            actTime = MIN_TIME;
        } else if (time > MAX_TIME) {
            actTime = MAX_TIME;
        } else {
            actTime = time;
        }
        synchronized (switchMap) {
            HashMap<String, ArrayList<Integer>> week = switchMap.get(getNegativeSwitch());
            if (week != null) {
                ArrayList<Integer> daysList = week.get(getActiveDay());
                if (daysList != null) {
                    Integer nbrC = getNbrCycles();
                    Integer actC = getActiveCycle();
                    Integer pSwitch = null;
                    boolean newS = false;
                    if (nbrC < actC) {
                        /* new Switch */
                        newS = true;
                    }
                    /* Check whether the positive switch is existing too */
                    if (switchMap.get(getPositiveSwitch()).get(getActiveDay()).size() < actC) {
                        /* No -> new Switch */
                        pSwitch = 0;
                    } else {
                        pSwitch = switchMap.get(getPositiveSwitch()).get(getActiveDay()).get(actC - 1);
                    }
                    /* The negative switch cannot be lower then the positive */
                    if (actTime < (pSwitch + getSwitchPointTimeRaster())) {
                        actTime = pSwitch + getSwitchPointTimeRaster();
                    }
                    /* Check whether the time would overlap with the next one */
                    if (nbrC > actC) {
                        Integer pNextSwitch = switchMap.get(getPositiveSwitch()).get(getActiveDay()).get(actC);
                        /* The negative switch cannot be higher then the next positive switch */
                        if (actTime > (pNextSwitch - getSwitchPointTimeRaster()) && pNextSwitch > 0) {
                            actTime = pNextSwitch - getSwitchPointTimeRaster();
                        }
                    }
                    if (newS) {
                        daysList.add(actTime);
                    } else {
                        daysList.set(actC - 1, actTime);
                    }
                    checkRemovement();
                }
            }
        }
    }

    /**
     * This function checks whether the actual cycle have to be removed (Both times set to MAX_TIME)
     */
    void checkRemovement() {
        if (getActiveNegativeSwitch().equals(MAX_TIME) && getActivePositiveSwitch().equals(MAX_TIME)
                && getNbrCycles() > 0) {
            switchMap.get(getNegativeSwitch()).get(getActiveDay()).remove(getActiveCycle() - 1);
            switchMap.get(getPositiveSwitch()).get(getActiveDay()).remove(getActiveCycle() - 1);
        }
    }

    /**
     * This function determines the positive and negative switch point names
     */
    public boolean determineSwitchNames(KM200Device device) {
        if (setpointProperty != null) {
            logger.debug("Determine switch names..");
            KM200ServiceObject setpObject = device.getServiceObject(setpointProperty);
            if (setpObject.serviceTreeMap.keySet().isEmpty()) {
                return false;
            }
            for (String key : setpObject.serviceTreeMap.keySet()) {
                logger.debug("Key: {}", key);
                setpoints.add(key);
            }
        }
        return true;
    }

    /**
     * This function updates objects the switching points
     */
    public void updateSwitches(JsonObject nodeRoot, KM200Device device) {
        synchronized (switchMap) {
            /* Update the list of switching points */
            removeAllSwitches();
            JsonArray sPoints = nodeRoot.get("switchPoints").getAsJsonArray();
            logger.debug("sPoints: {}", nodeRoot);
            if (positiveSwitch == null || negativeSwitch == null) {
                /* First start. Determine the positive and negative switching points */
                if (sPoints.size() > 0) {
                    for (int i = 0; i < sPoints.size(); i++) {
                        JsonObject subJSON = sPoints.get(i).getAsJsonObject();
                        String setpoint = subJSON.get("setpoint").getAsString();
                        if (positiveSwitch == null || negativeSwitch == null) {
                            positiveSwitch = setpoint;
                            negativeSwitch = setpoint;
                        } else {
                            negativeSwitch = setpoint;
                        }
                        if (!positiveSwitch.equals(negativeSwitch)) {
                            break;
                        }
                    }
                } else {
                    if (setpointProperty != null) {
                        BigDecimal firstVal = null;
                        KM200ServiceObject setpObject = device.getServiceObject(setpointProperty);
                        logger.debug("No switch points set. Use alternative way. {}", nodeRoot);
                        for (String key : setpoints) {
                            if (positiveSwitch == null || negativeSwitch == null) {
                                positiveSwitch = key;
                                negativeSwitch = key;
                                firstVal = (BigDecimal) setpObject.serviceTreeMap.get(key).getValue();
                            } else {
                                BigDecimal nextVal = (BigDecimal) setpObject.serviceTreeMap.get(key).getValue();
                                if (nextVal.compareTo(firstVal) > 0) {
                                    positiveSwitch = key;
                                } else {
                                    negativeSwitch = key;
                                }
                            }
                            if (!positiveSwitch.equalsIgnoreCase(negativeSwitch)) {
                                break;
                            }
                        }
                    }
                }
            }
            logger.debug("Positive switch: {}", positiveSwitch);
            logger.debug("Negative switch: {}", negativeSwitch);
            HashMap<String, ArrayList<Integer>> weekMap = null;
            weekMap = switchMap.get(positiveSwitch);
            if (weekMap == null) {
                initWeeklist(positiveSwitch);
            }
            weekMap = switchMap.get(negativeSwitch);
            if (weekMap == null) {
                initWeeklist(negativeSwitch);
            }
            for (int i = 0; i < sPoints.size(); i++) {
                JsonObject subJSON = sPoints.get(i).getAsJsonObject();
                String day = subJSON.get("dayOfWeek").getAsString();
                String setpoint = subJSON.get("setpoint").getAsString();
                Integer time = subJSON.get("time").getAsInt();
                addSwitch(day, setpoint, time);
            }
        }
    }

    /**
     * This function updates objects JSONData on the actual set switch points.
     */
    public JsonObject getUpdatedJSONData(KM200ServiceObject parObject) {
        synchronized (switchMap) {
            boolean prepareNewOnly = false;
            JsonArray sPoints = new JsonArray();
            for (String day : days) {
                if (switchMap.get(getPositiveSwitch()).containsKey(day)
                        && switchMap.get(getNegativeSwitch()).containsKey(day)) {
                    Integer j;
                    Integer minDays = Math.min(switchMap.get(getPositiveSwitch()).get(day).size(),
                            switchMap.get(getNegativeSwitch()).get(day).size());
                    for (j = 0; j < minDays; j++) {
                        JsonObject tmpObj = new JsonObject();
                        tmpObj.addProperty("dayOfWeek", day);
                        tmpObj.addProperty("setpoint", getPositiveSwitch());
                        tmpObj.addProperty("time", switchMap.get(getPositiveSwitch()).get(day).get(j));
                        sPoints.add(tmpObj);
                        tmpObj = new JsonObject();
                        tmpObj.addProperty("dayOfWeek", day);
                        tmpObj.addProperty("setpoint", getNegativeSwitch());
                        tmpObj.addProperty("time", switchMap.get(getNegativeSwitch()).get(day).get(j));
                        sPoints.add(tmpObj);
                    }

                    /* Check whether one object for a new cycle is already created */
                    if (switchMap.get(getPositiveSwitch()).get(day).size() > minDays) {
                        JsonObject tmpObj = new JsonObject();
                        tmpObj.addProperty("dayOfWeek", day);
                        tmpObj.addProperty("setpoint", getPositiveSwitch());
                        tmpObj.addProperty("time", switchMap.get(getPositiveSwitch()).get(day).get(j));
                        sPoints.add(tmpObj);
                        prepareNewOnly = true;
                    } else if (switchMap.get(getNegativeSwitch()).get(day).size() > minDays) {
                        JsonObject tmpObj = new JsonObject();
                        tmpObj.addProperty("dayOfWeek", day);
                        tmpObj.addProperty("setpoint", getNegativeSwitch());
                        tmpObj.addProperty("time", switchMap.get(getNegativeSwitch()).get(day).get(j));
                        sPoints.add(tmpObj);
                        prepareNewOnly = true;
                    }
                }
            }
            logger.debug("New switching points: {}", sPoints);
            JsonObject switchRoot = parObject.getJSONData();
            switchRoot.remove("switchPoints");
            switchRoot.add("switchPoints", sPoints);
            parObject.setJSONData(switchRoot);
            /* Preparation for are new cycle, don't sent it to the device */
            if (prepareNewOnly) {
                return null;
            } else {
                return switchRoot;
            }
        }
    }

    int getMaxNbOfSwitchPoints() {
        return maxNbOfSwitchPoints;
    }

    int getMaxNbOfSwitchPointsPerDay() {
        return maxNbOfSwitchPointsPerDay;
    }

    public int getSwitchPointTimeRaster() {
        return switchPointTimeRaster;
    }

    public String getSetpointProperty() {
        return setpointProperty;
    }

    public String getPositiveSwitch() {
        return positiveSwitch;
    }

    public String getNegativeSwitch() {
        return negativeSwitch;
    }

    /**
     * This function returns the number of cycles
     */
    public Integer getNbrCycles() {
        synchronized (switchMap) {
            HashMap<String, ArrayList<Integer>> weekP = switchMap.get(getPositiveSwitch());
            HashMap<String, ArrayList<Integer>> weekN = switchMap.get(getNegativeSwitch());
            if (weekP != null && weekN != null) {
                if (weekP.isEmpty() && weekN.isEmpty()) {
                    return 0;
                }
                ArrayList<Integer> daysListP = weekP.get(getActiveDay());
                ArrayList<Integer> daysListN = weekN.get(getActiveDay());
                return Math.min(daysListP.size(), daysListN.size());
            }
        }
        return null;
    }

    /**
     * This function returns the selected day
     */
    public String getActiveDay() {
        return activeDay;
    }

    /**
     * This function returns the selected cycle
     */
    public Integer getActiveCycle() {
        return activeCycle;
    }

    /**
     * This function returns the positive switch to the selected day and cycle
     */
    public Integer getActivePositiveSwitch() {
        synchronized (switchMap) {
            HashMap<String, ArrayList<Integer>> week = switchMap.get(getPositiveSwitch());
            if (week != null) {
                ArrayList<Integer> daysList = week.get(getActiveDay());
                if (daysList.size() > 0) {
                    Integer cycl = getActiveCycle();
                    if (cycl <= daysList.size()) {
                        return (daysList.get(getActiveCycle() - 1));
                    }
                }
            }
        }
        return 0;
    }

    /**
     * This function returns the negative switch to the selected day and cycle
     */
    public Integer getActiveNegativeSwitch() {
        synchronized (switchMap) {
            HashMap<String, ArrayList<Integer>> week = switchMap.get(getNegativeSwitch());
            if (week != null) {
                ArrayList<Integer> daysList = week.get(getActiveDay());
                if (daysList.size() > 0) {
                    Integer cycl = getActiveCycle();
                    if (cycl <= daysList.size()) {
                        return (daysList.get(getActiveCycle() - 1));
                    }
                }
            }
        }
        return 0;
    }
}
