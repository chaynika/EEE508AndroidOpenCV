# EEE508AndroidOpenCV

Description of the app:
1. App implements usage of edge detection in Image and Video processing
2. App finds out the coordinates and color of a blob when clicked in any area in the camera frame
3. When the app is opened, it provides a list of two things to the user, among which any one can be implemented by the user
4. One option is to perform straight line detection in a camera video frame while the other is to perform edge detection while
also retrieving the true colors of the camera frame
5. Apart from that, anywhere if it is clicked, it provides color and coordinates of that point

New features implemented and its application:
We have implemented two features in the application giving the user a platform to implement different applications of Image processing. The first feature is to perform straight line detection in a live video frame. Second feature is to perform edge detection. Both the features of the app are extremely important as a part of feature extraction for various other applications. Line detection has applications like finding lens distortion in pictures and which can help rectify pictures. Line detection is very important for the robustness and flexibility of Computer Vision systems. Edge detection is a very important application of Image Processing and is used for image segmentation and data extraction in areas such as image processing, computer vision etc. It has vast number of applications in feature detection and feature extraction where it does step detection wherever there are any discontinuities. In this app, however, the original color of the camera frame is maintained so that the color can also be correctly recorded for a point, otherwise direct implementation of Canny edge detection turns the video black and white. The app is very basic. At times, just like any other Android app we all use, it tends to go through improper shutdown or the app crashes. However, most of the times, it works fine. More functionalities of Image Processing can be implemented in the app as a future task for the project. Also, these basic features implemented by the app can be exploited heavily and also made sturdier. The edge detection feature works well. The line detection feature does detect al straight lines however and need some deeper study and knowledge about the subject. 

How to run the app:
1. Download the zip file and unzip it and extract the code.
2. Open the project using Android Studio.
3. The app only uses libraries which have been shown in the tutorial pdf shared by the TA so no additional packages need to be installed
4. Run the application in the Android Emulator using the 'Run' button in the app.
5. Once the app opens, there will be two buttons which are for the user to pick from. One of them implements the Line detection feature while the other implements the Edge detection feature. Both of them however retain the functionality of finding color and coordinates of the point we click on.
6. We can select any one of the two buttons to implement the functionality.


