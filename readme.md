# Readme

## Setup

### Server Setup

TODO, See /server/healthaggregation/readme.md for now
TODO copy .env.example to .env and replace dummy values

#### Firebase Configuration

1. Create a Firebase project in the Firebase console (https://console.firebase.google.com/)
2. Navigate to the cloud messaging tab in the project settings
3. Copy the serverkey and paste it into the FCM_API_KEY variable in server .env file


### Android Setup

#### Firebase Configuration

To be able to run the android app you need to configure the firebase project you'd like to use via a google-services.json file. 
Follow these steps to aquire the google-services.json config file for your firebase project.

1. Open firebase cloud console and select the project you created for the server setup
2. Add your app to the project. The package name of the app contained in this project is "com.strobel.healthaggregation"
3. Download the "google-services.json" config file for your project and save it to /client-android/app/ (**Warning**: This file contains your unique but non-secret firebase project id)


#### (Optional) Setup OAuth for Google Fit

Follow the tutorial on https://developers.google.com/fit/android/get-api-key to set up OAuth for your android/google cloud project to be able to access data of google fit

To be able to publish your application you might have to apply to get it checked.
For debugging purposes you may add up to 100 google accounts as test accounts to the project via the APIs & Services section of your google cloud project.


#### (Optional) Add Endpoints to the data resolver

The data resolver takes the requested URL and possible arguements, accesses the corresponding data on the device and returns them to the framework.
If you'd like to use other datasources than the already implemented sources you may add additional endpoints to the data resolver by following these steps:

1. Implement a class extending the DataSource class that abstracts the access to your data source
2. Call the registerDataSource method of the URLResolver instance on app startup
