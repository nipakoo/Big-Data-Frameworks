University of Helsinki

Carat Context Factor Dataset


This is a README for Carat context factors dataset collected from 149,788 mobile devices of 2535 different Android models during 2013 and beginning of 2014. The data and attributes are presented in our article E. Peltonen, E. Lagerspetz, P. Nurmi, and S. Tarkoma: "Energy Modeling of System Settings: A Crowdsourced Approach", Percom'15, which we recommend to read first in order to understand this dataset.

USAGE

This dataset can be used only for research purposes as presented in the license provided with the
dataset. Please, read the license carefully. We also require that any publications using this dataset cite our article. 

FORMAT

There are 11,209,125 data items, each in a row of the text file. Attributes are separated by
semicolons. The format of each row is:

energyRate;batteryHealth;batteryTemperature;batteryVoltage;cpuUsage;distanceTraveled;mobileDataActivity;mobileDataStatus;mobileNetworkType;networkType;roamingEnabled;screenBrightness;wifiLinkSpeed;wifiSignalStrength

for example,

0.0016;Good;30.0;3.954;0.4860335195530726;0.0;none;connected;unknown;mobile;0;-1.0;-1;-200

There the energy rate means battery drain given as percent per second, and it is presented in our
article. All the other attributes are obtained using the Android API. Any private information, such
as user identifiers, device models, or operating systems, have been removed from this dataset,
and it is forbidden to try to reconstruct them.

PREPROCESSING

The dataset is NOT preprocessed save for removal of empty lines. Battery charging events are not
present in the dataset. No outlier detection has been done, and the data can contain default values
from the Android API and values that signify the attribute is unavailable. Some of the attributes can be given as integer instead of float from some devices.

For our results in our article, we have used attribute value ranges to consider normal behavior only. These are:
CPU load from 0 to 1 (0% to 100%)
Distance traveled: higher than or exactly zero (binary classification)
Screen brightness: from 0 to 255, and -1 as an automatic setting
Battery voltage: from 0 to 5
Wi-fi signal strength: from -100 to 0, exclusive
Battery temperature: higher than or exactly zero

For categorical attributes, such as battery health, we have used all the values present in the dataset (Good, Overheat etc). All the possible values can be found from the descriptions of the Android API.

CONTACT

If have any questions or interest in the Carat project, please contact us carat@cs.helsinki.fi

