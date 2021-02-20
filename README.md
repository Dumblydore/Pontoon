# Pontoon: Unofficial Floatplane Android App

[![Pontoon Beta Workflow](https://github.com/Dumblydore/Pontoon/actions/workflows/beta-workflow.yml/badge.svg)](https://github.com/Dumblydore/Pontoon/actions/workflows/beta-workflow.yml) [![](https://img.shields.io/badge/license-GPL--3.0-blue.svg)](LICENSE)

<img src="assets/screenshots/player.png" width="200"> <img src="assets/screenshots/subscriptions.png" width="200"> <img src="assets/screenshots/subscriptions_pip.png" width="200">

<br/>
<a href='https://play.google.com/store/apps/details?id=me.mauricee.pontoon&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width="200px"/></a>
<br/>

An unnoffical Floatplane Club app. Still very much a work in progress. Looking for testers!

## Contributing
Feel free to make a Pull Request and I'll look at it when I get a chance
### Getting Started
#### Building
   There's nothing special you have to do to run the project (after commit [394624e](https://github.com/Dumblydore/Pontoon/commit/394624e7f67d53b66a12cdfe79a314774be55080)). 
   
   In Android Studio just sync and run. 
   
   With gradle just run `./gradlew assembleDebug`

#### Build number
   add `buildNumber=${NUMBER}`. This is '0' by default

#### Enabling Analytics
   This app uses Crashlytics and Firebase Analytics. By default these will not be enabled when
   building an APK. to enable both:
   - Follow [this guide](https://docs.fabric.io/android/fabric/overview.html) to obtain fabric.properties file and place in the `/app` directory.
   - Follow [this guide](https://firebase.google.com/docs/android/setup) to obtain an google-services.json file and place in the root of the project.
   - add `enableAnalytics=true` as a gradle task property.

#### Extras
   - Add `pontoon.username` for the app to autofill that field when logging in. 
   - Add `pontoon.password` for the app to autofill that field when logging in. 
