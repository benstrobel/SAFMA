# Readme

## What is this project?

As part of my bachelors thesis I developed a framework which allows the collection of aggregations of sensitive data from mobile devices while maintaining data privacy towards the data collecting entity and all other participants. The abstract of the thesis was published in the [Congress for German Medical Science 2022](https://www.egms.de/static/en/meetings/gmds2022/22gmds010.shtml). Maintaining data privacy is possible by calculating pairwise perturbations, where the perturbations of two partners cancel each other out. Then the perturbations get added to each partners payload and sent to the server. Since the perturbation pairs cancel each other out when combined, the resulting aggregation is correct. The framework additionally handles dropout of up to half of the participants at any time during the protocol.

## Setup

### Server Setup

See /server/healthaggregation/readme.md

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
