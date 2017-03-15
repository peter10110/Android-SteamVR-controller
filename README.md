# Android-SteamVR-controller 
This application can be used as a controlelr for my custom SteamVR driver, which can be found here: https://github.com/peter10110/driver_leap
 
# What is this?
It's a small application which can send UDP messages (TCP is not implemented yet) to the given address, with orientation and button data. The application is capable of emulating all the buttons and orientation (not position!) of a HTC Vive controller.

# System requirements
- An Android-based phone with gyroscope, accelerometer and magnetometer.
- Android 4.1 or newer
I've tested and made the application on a Nexus 5, with Android 7.1.1.

# Notes
This app is REALLY just a demo. It can function as a controller, but it's ugly and not very comfortable.

# How to get it?
Compile it from source, or just install the APK in the "Compiled" folder.

# How to use?
 The application is quite straightforward. You can access a side menu, by swiping your finger from the left side, where you can navigate between the tabs.
### WiFi mode
 On the opening page (WiFi mode), set up the target IP and port, and send interval of the packets (better for the bettery, worse for the smooth controller movement). 
 Other buttons:
- TCP / UDP: You could select between UDP and TCP communication, if I had implemented it. But i didn't, so now it's UDP only (which is perfect for this application)
- Set zero point: sets the current position of the device as the "zero".
- Left hand / Right hand: which hand's controller should be emulated by the app.
- The other buttons are the soft equivalent's of the buttons on the Vive controller.
- If you are done with the settings, just click on the "OFF" button to start sending the data.
### Controller
 This tab is what you need, after you started the data sending. You will find here the 3 buttons (menu, system, grip), and the touchpad of the Vive controller. For the trigger and touchpad press, you have to use the volume buttons. It's maybe not very usable on some phones, depending the position of these buttons.
 On the bottom there's also a "Reset orientation" button, to set the controller position to zero (for the initial comfortable pose, and for fixing the gyro drift).


# Used components
Big thaks for Alexander Pacha, for his work wit the "Sensor fusion Demo" project (https://bitbucket.org/apacha/sensor-fusion-demo).
I've used his algorhythm in the app to get a stable and reliable orientation of the phone. The basic cube visualisation of the orientation is also from his application.
